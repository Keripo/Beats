package com.beatsportable.beats;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class DataFile {
	
	// Stepfile
	private String filename = "";
	private String path = "";
	
	// Strings
	private String title = "";
	private String subtitle = "";
	private String artist = "";
	private String titletranslit = "";
	private String subtitletranslit = "";
	private String artisttranslit = "";
	private String credit = "";
	
	// Images
	private File banner = null;
	private File background = null;
	private File cdtitle = null;
	
	// Music
	private File music = null;
	private float offset = 0f;
	private float samplestart = 0f;
	private float samplelength = 0f;
	
	// Misc
	private boolean selectable = true;
	// Genre, etc.?
	
	// Arrays
	private ArrayList<Float> bpmBeat = new ArrayList<Float>();
	private ArrayList<Float> bpmValue = new ArrayList<Float>();
	private ArrayList<Float> stopsBeat = new ArrayList<Float>();
	private ArrayList<Float> stopsValue = new ArrayList<Float>();
	private ArrayList<Float> bgchangeBeat = new ArrayList<Float>();
	private ArrayList<File> bgchangeFile = new ArrayList<File>();

	// Notes Data
	public ArrayList<DataNotesData> notesData = new ArrayList<DataNotesData>();
	
	// md5 Hash
	public String md5hash;
	
	// File-finding
	private String imageFileExtensions[] = {
		".jpg", ".JPG", ".png", ".PNG", ".bmp", ".BMP"
	};
	private String musicFileExtensions[] = {
		// File existence check, prefers MP3s over OGGs
		// See http://code.google.com/p/android/issues/detail?id=11590
		".mp3", ".MP3",
		".ogg.mp3", ".ogg.MP3", ".OGG.mp3", ".OGG.MP3",
		".ogg", ".OGG",
		".wav", ".WAV",
		".mp3.mp3", ".mp3.MP3", ".MP3.MP3" // in case of laziness in renaming
	};
	private String bgFileSuffixe[] = {
		"", "bg", " bg", "-bg"
	};
	private String bnFileSuffixe[] = {
		"", "bn", " bn", "-bn"
	};
	private String noFileSuffixe[] = {
		""
	};
	
	private File getFile(String s, String[] exts, String[] suffixe) {
		File f;
		f = new File(s);
		if (f.exists()) {
			return f; 
		} else {
			String b; // Basename
			b = Tools.getBasename(s);
			for (int i = 0; i < suffixe.length; i++) {
				for (int j = 0; j < exts.length; j++) {
					f = new File(path + b + suffixe[i] + exts[j]);
					if (f.exists()) {
						return f;
					}
				}
			}
			// Try with filename
			b = Tools.getBasename(filename);
			for (int i = 0; i < suffixe.length; i++) {
				for (int j = 0; j < exts.length; j++) {
					f = new File(path + b + suffixe[i] + exts[j]);
					if (f.exists()) {
						return f;
					}
				}
			}
			return null;
		}
	}
	
	// Constructor
	public DataFile(String filename) {
		this.filename = filename;
		if (filename.contains("/")) {
			path = filename.substring(0, filename.lastIndexOf('/') + 1);
		} else {
			path = "/";
		}
		this.md5hash = Tools.getMD5Checksum(filename);
	}
	
	// Stepfile
	public String getFilename() { return filename; }
	public String getPath() { return path; }
	
	// Strings
	public void setTitle(String title) { this.title = title; }
	public String getTitle() { return title; }
	
	public void setSubTitle(String subtitle) { this.subtitle = subtitle; }
	public String getSubTitle() { return subtitle; }
	
	public void setArtist(String artist) { this.artist = artist; }
	public String getArtist() { return artist; }
	
	public void setTitleTranslit(String titletranslit) { this.titletranslit = titletranslit; }
	public String getTitleTranslit() { return titletranslit; }
	
	public void setSubTitleTranslit(String subtitletranslit) { this.subtitletranslit = subtitletranslit; }
	public String getSubTitleTranslit() { return subtitletranslit; }
	
	public void setArtistTranslit(String artisttranslit) { this.artisttranslit = artisttranslit; }
	public String getArtistTranslit() { return artisttranslit; }
	
	public void setCredit(String credit) { this.credit = credit; }
	public String getCredit() { return credit; }
	
	// Images
	public void setBanner(String filename) {
		banner = getFile(filename, imageFileExtensions, bnFileSuffixe);
	}
	public File getBanner() { return banner; }
	
	public void setBackground(String filename) {
		background = getFile(filename, imageFileExtensions, bgFileSuffixe);
	}
	public void setBackgroundBackup() {
		// Just use any large image file in the folder
		if (background == null) {
			try {
				File[] files = new File(path).listFiles();
				for (File f : files) {
					// Look for big files first over 75kbs
					if (f.length() > 75000) {
						String name = f.getAbsolutePath();
						for (String suffix : imageFileExtensions) {
							if (name.endsWith(suffix)) {
								background = f;
								return;
							}
						}
					}
				}
				// Still none? Check rest, and use first image found
				for (File f : files) {
					String name = f.getAbsolutePath();
					for (String suffix : imageFileExtensions) {
						if (name.endsWith(suffix)) {
							background = f;
							return;
						}
					}
				}
			} catch (Exception e) {} // Whatever
		}
	}
	public File getBackground() { return background; }
	
	public void setCDTitle(String filename) {
		cdtitle = getFile(filename, imageFileExtensions, noFileSuffixe);
	}
	public File getCDTitle() { return cdtitle; }
	
	// Music
	public void setMusic(String filename) {
		music = getFile(filename, musicFileExtensions, noFileSuffixe);
	}
	public void setMusicBackup() {
		// Just use any music file in the folder
		if (music == null) {
			try {
				File[] files = new File(path).listFiles();
				for (File f : files) {
					String name = f.getAbsolutePath();
					for (String suffix : musicFileExtensions) {
						if (name.endsWith(suffix)) {
							music = f;
							return;
						}
					}
				}
			} catch (Exception e) {} // Whatever
		}
	}
	public File getMusic() { return music; }
	
	public void setOffset(float offset) { this.offset = offset; }
	public float getOffset() { return offset; }
	
	public void setSampleStart(float samplestart) { this.samplestart = samplestart; }
	public float getSampleStart() { return samplestart; }
	
	public void setSampleLength(float samplelength) { this.samplelength = samplelength; }
	public float getSampleLength() { return samplelength; }
	
	// Misc
	public void setSelectable(boolean selectable) { this.selectable = selectable; }
	public boolean getSelectable() { return selectable; }
	
	// Arrays - assume all beats are added sequentially
	public void clearBPMs(DataNotesData nd) { // For .osu parsing
		nd.bpmBeat = bpmBeat;
		nd.bpmValue = bpmValue;
		bpmBeat = new ArrayList<Float>();
		bpmValue = new ArrayList<Float>();
	}
	public float getBPM(DataNotesData nd, float beat) {
		int index = 0;
		for (int i = 0; i < nd.bpmBeat.size(); i++) {
			if (nd.bpmBeat.get(i) <= beat) {
				index = i;
			} else {
				break;
			}
		}
		return nd.bpmValue.get(index);
	}
	public void addBPM(float beat, float value) {
		bpmBeat.add(beat);
		bpmValue.add(value);
	}
	public float getBPM(float beat) {
		int index = 0;
		for (int i = 0; i < bpmBeat.size(); i++) {
			if (bpmBeat.get(i) <= beat) {
				index = i;
			} else {
				break;
			}
		}
		return bpmValue.get(index);
	}
	public String getBPMRange(int notesDataIndex) {
		if (bpmValue.size() == 0) { // Will happen with .osu parsing
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			ArrayList<Float> osuBPMValue;
			if (notesData.size() > 0 && (osuBPMValue = notesData.get(notesDataIndex).bpmValue) != null) {
				if (osuBPMValue.size() > 1) {
					for (int i = 0; i < osuBPMValue.size(); i++) { // Just look at the first one
						float value = (60f * 1000f) / osuBPMValue.get(i);
						if (value < min) min = value;
						if (value > max) max = value;
					}
					return min + "-" + max;
				} else {
					return ((60f * 1000f) / osuBPMValue.get(0)) + "";
				}
			} else {
				return "ERROR";
			}
		} else if (bpmValue.size() > 1) {
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for (int i = 0; i < bpmValue.size(); i++) {
				float value = bpmValue.get(i);
				if (value < min) min = value;
				if (value > max) max = value;
			}
			return min + "-" + max;
		} else {
			return bpmValue.get(0).toString();
		}
	}
	
	public void addStop(float beat, float value) {
		stopsBeat.add(beat);
		stopsValue.add(value);
	}
	public float getStop(float beat) {
		for (int i = 0; i < stopsBeat.size(); i++) {
			if (Math.abs(stopsBeat.get(i) - beat) < 0.49/192) {
				return stopsValue.get(i);
			}
		}
		return 0f;
	}
	
	public Queue<Float> getStopsBeat() {
		return new LinkedList<Float>(stopsBeat);
	}
	public Queue<Float> getStopsValue() {
		return new LinkedList<Float>(stopsValue);
	}
	
	public void addBGChange(float beat, String filename) throws DataParserException {
		File f = getFile(filename, imageFileExtensions, bgFileSuffixe);
		if (f == null) {
			// Unessential
			//throw new DataParserException("Unable to find changed background image file \"" + filename + "\"."); 
		}
		bgchangeBeat.add(beat);
		bgchangeFile.add(f);
	}
	public File getBGChange(float beat) {
		int index = 0;
		for (int i = 0; i < bgchangeBeat.size(); i++) {
			if (bgchangeBeat.get(i) <= beat) {
				index = i;
			} else {
				break;
			}
		}
		return bgchangeFile.get(index);
	}
	
	// Notes Data
	public void addNotesData(DataNotesData nd) {
		notesData.add(nd);
		Collections.sort(notesData);
	}
	//public int getNotesDataCount() {
		//return notesData.size();
	//}
	//public DataNotesData getNotesData(int notesDataIndex) {
		//return notesData.get(notesDataIndex);
	//}
	public String getNotesDataDifficulties() {
		String difficulties = "";
		for (int i = 0; i < notesData.size(); i++) {
			if (!difficulties.equals("")) {
				difficulties += ", ";
			}
			DataNotesData nd = notesData.get(i);
			difficulties += nd.getDifficulty().toString();
			difficulties += " [" + nd.getDifficultyMeter() + "]"; 
		}
		return difficulties;
	}
	/*
	public int getNotesCount(int notesDataIndex) throws DataParserException {
		try {
			int count = notesData.get(notesDataIndex).notes.size();
			return count;
		} catch (IndexOutOfBoundsException e) {
			throw new DataParserException(
					"IndexOutOfBoundsException",
					notesDataIndex, notesData.size(), "getNotesCount"
					);
		}
	}
	*/
	/*
	public DataNote getNote(int notesDataIndex, int noteIndex) throws DataParserException {
		try {
			DataNote n = notesData.get(notesDataIndex).notes.get(noteIndex);
			return n;
		} catch (IndexOutOfBoundsException e) {
			throw new DataParserException(
					"IndexOutOfBoundsException",
					notesDataIndex, notesData.size(), "getNote for " + noteIndex
					);
		}
	}
	*/
	
}
