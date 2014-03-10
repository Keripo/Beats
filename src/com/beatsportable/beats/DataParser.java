package com.beatsportable.beats;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;

public class DataParser implements Iterator<DataNote>  {
	
	public DataFile df;
	public int notesDataIndex;
	private int notesIndex;
	
	public static boolean osuData;
	
	public DataParser(String filename)
	throws DataParserException, FileNotFoundException {
		if (!Tools.isStepfile(filename)) {
			throw new DataParserException(
					"Tools.getString(R.string.DataParser_unsupported)");
		}
		df = new DataFile(filename);
		notesDataIndex = 0;
		notesIndex = 0;
		
		if (Tools.isSMFile(filename)) {
			DataParserSM.parse(df);
			osuData = false;
		} else if (Tools.isDWIFile(filename)) {
			DataParserDWI.parse(df);
			osuData = false;
		} else if (Tools.isOSUFile(filename)) {
			// Load other .osu files in directory
			File dir = new File(df.getPath());
			for (File f : dir.listFiles()) {
				String filename2 = f.getAbsolutePath();
				if (Tools.isOSUFile(filename2)) {
					DataParserOSU.parse(df, filename2);
				}
			}
			// Load the original last
			DataParserOSU.parse(df, filename);
			osuData = !Tools.randomizeBeatmap;
		}
	}
	
	//public DataFile getDataFile() {
		//return df;
	//}
	
	public DataNotesData getNotesData() {
		return df.notesData.get(notesDataIndex);
	}
	
	public void setNotesDataIndex(int i) {
		notesDataIndex = i;
	}
	
	public void loadNotes(boolean jumps, boolean holds, boolean osu, boolean randomize)
	throws DataParserException {
		if (Tools.isSMFile(df.getFilename())) {
			DataParserSM.parseNotesData(
					df, df.notesData.get(notesDataIndex),
					jumps, holds, osu, randomize
					);
		} else if (Tools.isDWIFile(df.getFilename())) {
			DataParserDWI.parseNotesData(
					df, df.notesData.get(notesDataIndex),
					jumps, holds, osu, randomize
					);
		} else if (Tools.isOSUFile(df.getFilename())) {
			DataParserOSU.parseNotesData(
					df, df.notesData.get(notesDataIndex),
					jumps, holds, osu, Tools.randomizeBeatmap
					);
		} else {
			throw new DataParserException(
					"Tools.getString(R.string.DataParser_unsupported)");
		}
		Collections.sort(df.notesData.get(notesDataIndex).notes);
		
	}
	
	public boolean hasNext() {
		try {
			return (notesIndex < df.notesData.get(notesDataIndex).notes.size());
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	public DataNote peek() {
		if (hasNext()) {
			try {
				return df.notesData.get(notesDataIndex).notes.get(notesIndex);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public DataNote next() {
		DataNote n = peek();
		if (n != null) {
			notesIndex++;
		}
		return n;
	}
	
	public void remove() {
		throw new UnsupportedOperationException("ZE METHOD, ZIT DOES NOSSING!");
	}

}
