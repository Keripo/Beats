package com.beatsportable.beats;

public class DataParserTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String path = "18260 Masayoshi Minoshima feat nomico - Bad Apple!!/Masayoshi Minoshima feat. nomico - Bad Apple!! (ouranhshc) [lepidon! - Taiko Oni].osu";
			DataParser dp = new DataParser(path);
			// Info
			System.out.println(dp.df.getArtist());
			System.out.println(dp.df.getBPMRange(dp.notesDataIndex));
			System.out.println(dp.df.getFilename());
			System.out.println(dp.df.getNotesDataDifficulties());
			System.out.println(dp.df.getOffset());
			System.out.println(dp.df.getPath());
			System.out.println(dp.df.getTitle());
			System.out.println(dp.df.getMusic());
			System.out.println(dp.df.notesData.size());
			dp.setNotesDataIndex(0);
			// Notes Data info		
			DataNotesData nd = dp.getNotesData();
			System.out.println(nd.getDescription());
			System.out.println(nd.getDifficultyMeter());
			System.out.println(nd.getDifficulty());
			System.out.println(nd.getNotesType());
			// NotesData
			//DataNotesData nd = dp.getNotesData();
			//System.out.println(nd.getNotesData());
			
			///*
			dp.loadNotes(true, true, false, true);
			/*
			for (DataNote n : dp.df.notesData.get(0).notes) { 
				System.out.println(n);
			}
			*/
			//*/
			
			/*
			while (dp.hasNext()) {
				System.out.println(dp.next());
			}
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
