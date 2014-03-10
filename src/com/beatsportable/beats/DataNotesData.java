package com.beatsportable.beats;
import java.util.ArrayList;

/*
 * See http://www.stepmania.com/wiki/The_.SM_file_format
 * 
 * Format:
 * #NOTES:
 * <NotesType>:
 * <Description>:
 * <DifficultyClass>:
 * <DifficultyMeter>:
 * <RadarValues>:
 * <NoteData>
 * 
 * NotesType - Must be one of the currently supported types in StepMania:
      o dance-single
      o dance-double
      o dance-couple
      o dance-solo
      o pump-single
      o pump-double
      o pump-couple
      o ez2-single
      o ez2-double
      o ez2-real
      o para-single 
 * Description - This will be displayed on the gameplay screen. This can be any text, but is most commonly: "Beginner", "Basic", "Another", "Trick", "Standard", "SSR", "Maniac", "Heavy", "Challenge", or "SManiac", for traditional reasons. 
 * DifficultyClass - This value must be "beginner", "easy", "medium", "hard", or "challenge". These values correspond to the five levels of difficulty on the Select Difficulty screen. 
 * DifficultyMeter - The difficulty of these notes as a bar rating. The value must be an integer between 1 and traditionally 10, though many keyboard files go above ten. 
 * RadarValues - Five floating point numbers separated by commas that determine the "voltage", "stream", "chaos", "freeze", and "air" values for the set of steps, as is displayed in the "groove radar" in the default theme, for example. 
 * NoteData - This value requires a longer explanation: 
 *
 * Each note is represented by a character:
 * 0 = no note here
 * 1 = a regular "tap note"
 * 2 = beginning of a "hold note"
 * 3 = end of a "hold note"
 * 4 = beginning of a roll (3.9+, 3.95+, 4.0)
 * M = Mine
 * L = Lift (3.9+ and 4.0)
 * a-z,A-z = tap notes reserved for game types that have sounds associated with notes 
 *
 * Notes that are hit at the same time are grouped into rows. For example, if the NotesType is "dance-single", the row "1001" would specify that both the Left and Right panels should be hit at the same time.
 *
 * The number of notes per row (also called the number of 'columns') depends on the "NotesType".
 * dance-single = 4 notes/row (Left,Down,Up,Right)
 * dance-double = 8 notes/row
 * dance-couple = 8 notes/row
 * dance-solo = 6 notes/row
 * pump-single = 5 notes/row
 * pump-double = 10 notes/row
 * pump-couple = 10 notes/row
 * ez2-single = 5 notes/row
 * ez2-double = 10 notes/row
 * ez2-real = 7 notes/row
 * para-single = 5 notes/row 
 * Note rows are grouped into measures. The number of note rows you specify in a measure will determine the time value of each note. For example, if there are 4 note rows in a measure, each note will be treated as a quarter note. If there are 8 notes rows in a measure, each note will be treated as a eighth note. If there are 12 notes rows in a measure, each note will be treated as a triplet (1/12th) note. Measures are separated by a comma.
 *  
 */

public class DataNotesData implements Comparable<DataNotesData> {
	// Info
	public enum NotesType {
		DANCE_UNKNOWN	("unknown", 99),
		DANCE_SINGLE	("dance-single", 4),
		DANCE_DOUBLE	("dance-double", 8),
		DANCE_COUPLE	("dance-couple", 8),
		DANCE_SOLO		("dance-solo", 6),
		PUMP_SINGLE		("pump-single", 5),
		PUMP_DOUBLE		("pump-double", 10),
		PUMP_COUPLE		("pump-couple", 10),
		EZ2_SINGLE		("ez2-single", 5),
		EZ2_DOUBLE		("ez2-double", 10),
		EZ2_REAL		("ez2-real", 7),
		PARA_SINGLE		("para-single", 5);
		private String name;
		private int notesCount;
		NotesType(String name, int notesCount) {
			this.name = name;
			this.notesCount = notesCount;
		}
		public int getNotesCount() { return notesCount; }
		public String toString() { return name; }
	}
	
	public enum Difficulty {
		BEGINNER	(R.string.Difficulty_beginner),
		EASY		(R.string.Difficulty_easy),
		MEDIUM		(R.string.Difficulty_medium),
		HARD		(R.string.Difficulty_hard),
		CHALLENGE	(R.string.Difficulty_challenge),
		EDIT		(R.string.Difficulty_edit),
		UNKNOWN		(R.string.Difficulty_unknown);
		private int name;
		Difficulty(int name) {
			this.name = name;
		}
		public String toString() {
			return Tools.getString(name);
		}
	}
	
	private NotesType notestype = NotesType.DANCE_UNKNOWN;
	private String description = "";
	private Difficulty difficulty = Difficulty.BEGINNER;
	private int difficultyMeter = 0;
	// at least 5 values "voltage", "stream", "chaos", "freeze", and "air"
	private ArrayList<Float> radarValues = new ArrayList<Float>(5);
	
	// These are references only for .osu files
	public ArrayList<Float> bpmBeat;
	public ArrayList<Float> bpmValue;
	
	private String notesData = "";
	public ArrayList<DataNote> notes = new ArrayList<DataNote>();
	
	public void setNotesType(NotesType notestype) { this.notestype = notestype; }
	public NotesType getNotesType() { return notestype; }
	
	public void setDescription(String description) { this.description = description; }
	public String getDescription() { return description; }
	
	public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
	public Difficulty getDifficulty() { return difficulty; }
	
	public void setDifficultyMeter(int difficultyMeter) { this.difficultyMeter = difficultyMeter; }
	public int getDifficultyMeter() { return difficultyMeter; }
	
	public void addRadarValue(float f) {
		radarValues.add(f);
	}
	public float getRasdarValue(int i) throws IndexOutOfBoundsException {
		return radarValues.get(i);
	}
	
	public void setNotesData(String notesData) { this.notesData = notesData; }
	public String getNotesData() { return notesData; }
	
	public void addNote(DataNote n) {
		notes.add(n);
	}
	//public int getNotesCount() {
		//return notes.size();
	//}
	//public DataNote getNote(int i) throws DataParserException {
		//if (i >= notes.size()) {
			//throw new DataParserException(
					//"IndexOutOfBoundsException",
					//i, notes.size(), "getNote");
		//}
		//return notes.get(i);
	//}
	
	public int compareTo(DataNotesData another) {
		return this.getDifficultyMeter() - another.getDifficultyMeter();
	}
	
}
