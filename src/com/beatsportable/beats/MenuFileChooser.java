package com.beatsportable.beats;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.*;

public class MenuFileChooser extends ListActivity {
	private MenuFileArrayAdapter adapter;
	private File cwd;
	private String selectedFilePath;
	private boolean useShortDirNames;
	
	// Show files
	private String shortDirName(String s) {
		String stripped = s;
		// "[name] song"
		if (s.charAt(0) == '[' && s.indexOf(']') != -1 && s.indexOf(']') < s.length() - 1) {
			stripped = s.substring(s.indexOf(']') + 1).trim();
		// "(name) song"
		} else if (s.charAt(0) == '(' && s.indexOf(')') != -1 && s.indexOf(')') < s.length() - 1) {
			stripped = s.substring(s.indexOf(')') + 1).trim();
		// "#### song"
		} else if (Character.isDigit(s.charAt(0))) {
			int i = 0;
			while (i < s.length() && s.charAt(i) != ' ' && !Character.isLetter(s.charAt(i))) {
				i++;
			}
			if (i < (s.length() - 1)) {
				stripped = s.substring(i).trim();
			}
		}
		if (stripped.length() > 0) {
			return stripped;
		} else {
			return s;
		}
	}
	
	private void ls(File dir) {
		if (dir == null) return;
		if (!Tools.isMediaMounted()) {
			Tools.toast(
					Tools.getString(R.string.MenuFilechooser_list_error) +
					dir.getPath() +
					Tools.getString(R.string.Tools_usb_error)
					);
			return;
		}
		if (!dir.canRead()) {
			Tools.toast(
					Tools.getString(R.string.MenuFilechooser_list_error) +
					dir.getPath() +
					Tools.getString(R.string.Tools_permissions_error)
					);
			return;
		}
		
		setTitle(dir.getAbsolutePath());
		// Get lists
		File[] l = dir.listFiles();
		ArrayList<MenuFileItem> dl = new ArrayList<MenuFileItem>();
		ArrayList<MenuFileItem> fl = new ArrayList<MenuFileItem>(); 
		
		// Populate list
		for (File f : l) {
			String s = f.getName();
			if (!s.startsWith(".")) {
				if (f.isDirectory()) {
					if (useShortDirNames) s = shortDirName(s);
					dl.add(new MenuFileItem(s + "/", f.getAbsolutePath(), true, f));
				} else if (Tools.isStepfile(s) || Tools.isLink(s) || Tools.isStepfilePack(s) || Tools.isText(s)) {
					fl.add(new MenuFileItem(s, f.getAbsolutePath(), false, f));
				}
			}
		}
		Collections.sort(dl);
		Collections.sort(fl);
		dl.addAll(fl); // Add file list to end of directories list
		
		// Add "Parent directory" item
		if(!dir.getName().equalsIgnoreCase("") && !dir.getName().equalsIgnoreCase("sdcard")) {
			MenuFileItem up_dir;
			up_dir = 
				new MenuFileItem(Tools.getString(R.string.MenuFilechooser_up_dir), dir.getParent(), true, null);
			dl.add(0, up_dir);
		}
		
		// Display
		adapter = new MenuFileArrayAdapter(this, R.layout.choose, dl);
		setListAdapter(adapter);
	}
	
	public void refresh() {
		ls(cwd);
	}
	
	// Setup
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.registerForContextMenu(this.getListView());
		Tools.setContext(this);
		
		adapter = null;
		cwd = null;
		selectedFilePath = null;
		
		// Get last dir	
		String prefLastDir = Tools.getSetting(R.string.lastDir, R.string.lastDirDefault);
		useShortDirNames = Tools.getBooleanSetting(R.string.useShortDirNames, R.string.useShortDirNamesDefault);
		
		if (prefLastDir.equals("")) {
			prefLastDir = Tools.getSongsDir();
		}
		if (prefLastDir != null &&
			prefLastDir.length() > 0 &&
			(cwd = new File(prefLastDir)) != null &&
			cwd.exists() &&
			cwd.getParentFile() != null &&
			!cwd.getPath().equals(Tools.getSongsDir())
			) {
			cwd = cwd.getParentFile();
		} else {
			String[] browseLocationOrder = {
					prefLastDir,
					Tools.getSongsDir(),
					Tools.getBeatsDir(),
					Environment.getExternalStorageDirectory().getPath(),
					"/sdcard",
					"/" //dangerous
			};
			for (String path: browseLocationOrder) {
				if (path != null) {
					cwd = new File(path);
					if (cwd.canRead() && cwd.isDirectory()) {
						break;
					}
				}
			}
		}
		ToolsTracker.data("Opened file browser", "cwd", cwd.getAbsolutePath());
		refresh();
	}
	
	private String parseURL(File url) {
		Scanner sc = null;
		try {
			sc = new Scanner(url);
			String buffer = "";
			while (sc.hasNextLine()) {
				buffer = sc.nextLine();
				if (buffer.contains("URL=")) {
					sc.close();
					return (buffer.substring(buffer.indexOf("URL=") + 4)).trim();
				}
			}
		} catch (Exception e) {
			ToolsTracker.error("MenuFileChooser.parseURL", e, url.getAbsolutePath());
		}
		if (sc != null) sc.close();
		return null;
	}
	
	private void selectStepfile(String path) {
		// Save preferences
		String smFilePath = path;
		Tools.putSetting(R.string.smFilePath, smFilePath);
		Tools.putSetting(R.string.lastDir, cwd.getPath());
		
		String smFileName;
		if (smFilePath.lastIndexOf('/') != -1) {
			smFileName = smFilePath.substring(smFilePath.lastIndexOf('/') + 1);
		} else {
			smFileName = smFilePath;
		}
		
		if (!Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
			Tools.toast(
					Tools.getString(R.string.MenuFilechooser_selected_stepfile) +
					smFileName +
					Tools.getString(R.string.MenuFilechooser_start_info)
					);
		}
		setResult(RESULT_OK);
		finish();
	}

	private void displayTextFile(MenuFileItem i) {
		/*using html, because otherwise the text size is too big. I'm not sure why;
		the New User Notes box is plaintext and its text size is fine. */
		//TODO make this into an activity?
		try {
			StringBuilder msg = new StringBuilder();
			msg.append("<small>"); //<font size=\"2\"> doesn't work
			BufferedReader r = new BufferedReader(new FileReader(i.getFile()));
			while (true) {
				String s = r.readLine();
				if (s == null) break;
				msg.append(s);
				msg.append("<br/>");
			}
			r.close();
			msg.append("</small>");
			Tools.note(i.getName(), R.drawable.icon_small, Html.fromHtml(msg.toString()),
					Tools.getString(R.string.Button_close), null, null, null, -1);
		} catch (Exception e) {
			ToolsTracker.error("MenuFileChooser.displayTextFile", e, i.getPath());
			Tools.warning(
					Tools.getString(R.string.MenuFilechooser_file_open_error) +
					i.getName() +
					Tools.getString(R.string.Tools_error_msg) +
					e.getMessage(),
					Tools.cancel_action,
					-1
					);
			
		}
	}
	
	private void onFileClick(MenuFileItem i) {
		selectedFilePath = i.getPath();
		// Directory
		if (i.isDirectory()) {
			File f = new File(i.getPath());
			if (f.canRead()) {
				cwd = f;
				String path;
				if (Tools.getBooleanSetting(R.string.stepfileFolderCheck, R.string.stepfileFolderCheckDefault)) {
					path = Tools.checkStepfileDir(f);
				} else {
					path = null;
				}
				if (path == null) {
					refresh();
				} else {
					selectStepfile(path);
				}
			} else {
				Tools.toast(
						Tools.getString(R.string.MenuFilechooser_list_error) +
						i.getPath() +
						Tools.getString(R.string.Tools_permissions_error)
						);
			}
			return;
		// URL
		} else if (Tools.isLink(selectedFilePath)) {
			String link = parseURL(i.getFile());
			Tools.toast("Opening link:\n" + link);
			if (link == null || link.length() < 2) {
				Tools.toast(
						Tools.getString(R.string.MenuFilechooser_url_error)
						);
			} else {
				Intent webBrowser = new Intent(Intent.ACTION_VIEW);
				webBrowser.setData(Uri.parse(link));
				startActivity(webBrowser);
			}
		// Stepfile
		} else if (Tools.isStepfile(selectedFilePath)) {
			selectStepfile(selectedFilePath);
		// Stepfile pack?
		} else if (Tools.isStepfilePack(selectedFilePath)) {
			new ToolsUnzipper(this, selectedFilePath, false).unzip();
		//Text file?
		} else if (Tools.isText(selectedFilePath)) {
			displayTextFile(i);
		} else {
			Tools.toast(
					Tools.getString(R.string.MenuFilechooser_file_extension_error)
					);
		}
		refresh();
	}
	
	// File deletion
	private void deleteFile(File f) throws SecurityException {
		if (f.isDirectory()) {
			for (File nf : f.listFiles()) {
				deleteFile(nf);
			}
			if (!f.delete()) {
				throw new SecurityException(f.getPath());
			}
		} else {
			if(!f.delete()) {
				throw new SecurityException(f.getPath());
			}
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		MenuFileItem i = adapter.getItem(info.position);
		if (i.getFile() == null) {
			return; // ignore the "..[up one directory]"
		} else { 
			menu.setHeaderIcon(R.drawable.icon_small);
			menu.setHeaderTitle(Tools.getString(R.string.MenuFilechooser_file_options));
			menu.add(0, v.getId(), 0, " " + Tools.getString(R.string.MenuFilechooser_file_open));  
			menu.add(0, v.getId(), 1, " " + Tools.getString(R.string.MenuFilechooser_file_delete));
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		MenuFileItem i = adapter.getItem(info.position);
		if (item.getOrder() == 0) { // Open
			onFileClick(i);
		} else { //item.getOrder() == 1 // Delete 
			selectedFilePath = i.getPath();
			
			DialogInterface.OnClickListener delete_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					try {
						deleteFile(new File(selectedFilePath));
					} catch (Exception e) {
						ToolsTracker.error("MenuFileChooser.deleteFile", e, selectedFilePath);
						Tools.error(
								Tools.getString(R.string.MenuFilechooser_file_delete_error) +
								selectedFilePath +
								Tools.getString(R.string.Tools_error_msg) +
								e.getMessage(),
								Tools.cancel_action);
					}
					refresh();
					dialog.cancel();
				}
			};
			
			Tools.alert(
					Tools.getString(R.string.MenuFilechooser_file_delete),
					R.drawable.icon_del,
					Tools.getString(R.string.MenuFilechooser_file_delete_confirm) +
					i.getName(),
					Tools.getString(R.string.Button_yes),
					delete_action,
					Tools.getString(R.string.Button_no),
					Tools.cancel_action,
					-1
					);
		}
		return true;
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		MenuFileItem i = adapter.getItem(position);
		onFileClick(i);
	}
	
	public void onWindowFocusChanged (boolean hasFocus) {
		if (hasFocus) {
			Tools.setContext(this);
			refresh();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
