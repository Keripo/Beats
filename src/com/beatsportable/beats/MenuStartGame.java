package com.beatsportable.beats;

import java.io.File;

import android.app.*;
import android.content.*;
import android.os.*;

public class MenuStartGame implements Runnable {
	private Activity a;
	private String title = "";
	
	private int defaultDifficulty;
	private int availableDifficulty;
	private String smFilePath;
	private ProgressDialog loadingDialog;
	
	public static DataParser dp;
	
	public MenuStartGame(Activity a, String title) {
		this.a = a;
		this.title = title;
		
		defaultDifficulty = 0;
		availableDifficulty = 0;
		smFilePath = null;
		loadingDialog = null;
	}
	
	// Titlebar
	public void setTitle() {
		try {
			if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.cancel();
			a.setTitle(title);
		} catch (IllegalArgumentException e) {
			ToolsTracker.error("MenuStartGame.setTitle", e, smFilePath);
			if (Tools.getBooleanSetting(R.string.debugLogCat, R.string.debugLogCatDefault)) {
				Tools.toast(Tools.getString(R.string.Tools_window_error));
			}
		}
	}
	
	public void showLoadingDialog() {
		String smFileName = smFilePath;
		if (smFilePath.contains("/")) {
			smFileName = smFilePath.substring(smFilePath.lastIndexOf('/') + 1);
		}
		a.setTitle(
				Tools.getString(R.string.MenuStartGame_loading_title) + 
				smFileName
				);
		loadingDialog = ProgressDialog.show(
				a, null,
				Tools.getString(R.string.MenuStartGame_loading_progress_bar) + 
				smFileName,
				true
				);
	}
	

	// Thread
	private String errorMessage = "";
	private void showFailParseMsg() {
		Tools.error(
				Tools.getString(R.string.MenuStartGame_fail_parse) + 
				smFilePath +
				Tools.getString(R.string.Tools_error_msg) + 
				errorMessage,
				Tools.cancel_action
				);
	}
	
	private void startGameActivity() {
		setTitle();
		Intent i = new Intent();
		i.setClass(a, GUIGame.class);
		a.startActivity(i);
	}
	
	private void checkOGG() {
		String musicFilePath = dp.df.getMusic().getPath();
		if (Tools.isOGGFile(musicFilePath) &&
			!Tools.getBooleanSetting(R.string.ignoreOGGWarning, R.string.ignoreOGGWarningDefault) && 
			Integer.valueOf(Tools.getSetting(R.string.manualOGGOffset, R.string.manualOGGOffsetDefault)) == 0
			) {
			// OGG warning
			DialogInterface.OnClickListener start_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					checkOSU();
				}
			};
			
			DialogInterface.OnClickListener cancel_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					setTitle();
				}
			};
			
			Tools.warning(
					Tools.getString(R.string.MenuStartGame_ogg_warning),
					start_action,
					cancel_action,
					R.string.ignoreOGGWarning
					);
			ToolsTracker.info("OGG song loaded");
		} else {
			checkOSU();
		}
	}
	
	private void checkOSU() {
		String smFilePath = dp.df.getFilename();
		if (Tools.isOSUFile(smFilePath) &&
			!Tools.getBooleanSetting(R.string.ignoreOSUWarning, R.string.ignoreOSUWarningDefault)
			) {
			// OGG warning
			DialogInterface.OnClickListener start_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					startGameActivity();
				}
			};
			
			Tools.warning(
					Tools.getString(R.string.MenuStartGame_osu_warning),
					start_action,
					R.string.ignoreOSUWarning
					);
			ToolsTracker.info("OSU song loaded");
		} else {
			startGameActivity();
		}
	}
	
	private void checkDifficulty() {
		// Different difficulty warning
		if (availableDifficulty != defaultDifficulty &&
			!Tools.getBooleanSetting(R.string.ignoreDifficultyWarning, R.string.ignoreDifficultyWarningDefault)
			) {
			DialogInterface.OnClickListener start_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					checkOGG();
				}
			};
			
			DialogInterface.OnClickListener cancel_action = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					setTitle();
				}
			};
			
			Tools.warning(
					Tools.getString(R.string.MenuStartGame_difficulty_selected) + 
					DataNotesData.Difficulty.values()[defaultDifficulty].toString() +
					Tools.getString(R.string.MenuStartGame_difficulty_closest) + 
					DataNotesData.Difficulty.values()[availableDifficulty].toString() +
					Tools.getString(R.string.MenuStartGame_difficulty_continue), 
					start_action,
					cancel_action,
					R.string.ignoreDifficultyWarning
					);
		} else {
			checkOGG();
		}
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					checkDifficulty();
					break;
				default: //case -1:
					showFailParseMsg();
					setTitle();
					break;
			}
		}
	};
	
	public void run() {
		try {
			// Parse stepfile
			dp = new DataParser(smFilePath);
			defaultDifficulty = Integer.valueOf(
					Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault));
			for (int i = dp.df.notesData.size(); i > 0; i--) {
				availableDifficulty = dp.df.notesData.get(i-1).getDifficulty().ordinal();
				if (availableDifficulty <= defaultDifficulty) {
					dp.setNotesDataIndex(i-1);
					break;
				}
			}
			
			// Load notes
			boolean jumps = Tools.getBooleanSetting(R.string.jumps, R.string.jumpsDefault);
			boolean holds = Tools.getBooleanSetting(R.string.holds, R.string.holdsDefault);
			boolean osu = (Tools.gameMode == Tools.OSU_MOD);
			boolean randomize = Integer.parseInt(
					Tools.getSetting(R.string.randomize, R.string.randomizeDefault)) != Randomizer.OFF;
			dp.loadNotes(jumps, holds, osu, randomize);
			
			// Sanity checks
			if (!dp.hasNext()) {
				throw new Exception(Tools.getString(R.string.MenuStartGame_notes_error));
			}
			String musicFilePath = dp.df.getMusic().getPath();
			if (dp.df.getMusic() == null ||
				dp.df.getMusic().getPath() == null ||
				dp.df.getMusic().getPath().length() == 0 ||
				!dp.df.getMusic().exists() ||
				!dp.df.getMusic().canRead()
				) {
				throw new Exception(Tools.getString(R.string.MenuStartGame_music_error));
			}
			// Ensure it's a valid/supported song format
			try {
				new MusicService(musicFilePath);
			} catch (Exception e) {
				throw new Exception(Tools.getString(R.string.MenuStartGame_music_error));
			}
			handler.sendEmptyMessage(0); // Done parsing
		} catch (Exception e) {
			ToolsTracker.error("MenuStartGame.run", e, smFilePath);
			errorMessage = e.getMessage();
			handler.sendEmptyMessage(-1); // Fail
		}
	}
	
	// Start Game Checks
	private void startGame() {
		// Set the screen dimensions
		// Have to be called outside of an onCreate for some reason...
		Tools.setTopbarHeight();
		Tools.setScreenDimensions();
		// Run thread
		showLoadingDialog();
		new Thread(this).start();
	}
	
	public void startGameCheck() {
		if (!Tools.isMediaMounted()) {
			return;
		}
		
		smFilePath = Tools.getSetting(R.string.smFilePath, R.string.smFilePathDefault);
		
		if (smFilePath.length() < 2) {
			Tools.warning(
					Tools.getString(R.string.MenuStartGame_select_music),
					Tools.cancel_action,
					-1);
		} else if (!(new File(smFilePath).exists())) {
			Tools.error(
					Tools.getString(R.string.MenuStartGame_missing_sm) +
					smFilePath + 
					Tools.getString(R.string.MenuStartGame_choose_new),
					Tools.cancel_action
					);
		} else {
			startGame();
		}
	}
}
