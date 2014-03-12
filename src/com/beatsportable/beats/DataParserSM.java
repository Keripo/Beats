package com.beatsportable.beats;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import com.beatsportable.beats.DataNote.NoteType;
import com.beatsportable.beats.DataNotesData.Difficulty;
import com.beatsportable.beats.DataNotesData.NotesType;

/*
 * See http://www.stepmania.com/wiki/The_.SM_file_format
 * 
 * #TITLE:...; - The "main title" of the song.
 * #SUBTITLE:...; - This text will appear underneath the main title of the song on the Select Music screen. e.g. "~Dirty Mix~" or "(remix)". 
 * #ARTIST:...; - The artist of the song. 
 * #TITLETRANSLIT:...; - Transliteration of song's main title. 
 * #SUBTITLETRANSLIT:...; - Transliteration of song's subtitle. 
 * #ARTISTTRANSLIT:...; - Transliteration of the artist's name. 
 * #CREDIT:...; - Give yourself some credit here for creating a wonderful song. 
 * #BANNER:...; - The file name of the banner image. e.g. "b4u-banner.png". This image must reside in the song folder. 
 * #BACKGROUND:...; - The file name of the background image. e.g. "b4u-bg.png". This image must reside in the song folder. 
 * #CDTITLE:...; - The file name of the spinning CD logo. e.g. "b4u-cdtitle.png". This image must reside in the song folder. 
 * #MUSIC:...; - The file name of the music file. e.g. "b4u.mp3". This image must reside in the song folder. 
 * #OFFSET:...; - The time in seconds at which beat 0 occurs in the music. This is specified as a floating point value. e.g. "2.34". 
 * #SAMPLESTART:...; - The time in seconds to start the music sample that plays on the Select Music screen. This is specified as a floating point value. e.g. "32.34". 
 * #SAMPLELENGTH:...; - The time in seconds let the sample music play after starting. This is specified as a floating point value. e.g. "16.00". Note that in the last 1 second of playing the music will fade out. 
 * #SELECTABLE:...; - If "NO", the song can not be selected manually and can only be played as part of a course. If "ROULETTE", the song can can also be selected via roulette. The default value is "YES". 
 * #BPMS:...; - A value of the format "beat=bpm". Indicates that at 'beat', the speed of the arrows will change to "bpm". Both of these values are specified as (positive) floating point values. You must specifiy a BPM value for beat 0. Multiple BPMs can be given by separating them with commas. e.g. "0=160,120=80". 
 * #STOPS:...; - A value of the format "beat=sec". Indicates that at 'beat', the motion of the arrows should stop for "sec" seconds. Both of these values are specified as floating point values. Multiple stops can be given by separating them with commas. e.g. "60=2.23,80=1.12". 
 * #BGCHANGE:...; - A value of the format "beat=bg name". Indicates that at 'beat', the background should begin playing a new background named 'bg name'. 'beat' is a fractional value value and 'bg name' is a string. Different bg changes are separated by commas. e.g. "60=falling,80=flower". When StepMania looks for a backgound, it searches in this order:
     1. Looks for a movie with file name = "bg name" in the song folder. You must include the file extension in "bg name". e.g. "60=falling.avi,80=flower.mpg".
     2. Looks for a BGAnimation folder with the name "bg name" in the song folder.
     3. Looks for a movie with file name "bg name" in the RandomMovies folder. You must include the file extension in "bg name". e.g. "60=falling.avi,80=flower.mpg".
     4. Looks for a BGAnimation with file name "bg name" in the BGAnimations folder.
     5. Looks for a Visualization with the file name "bg name" in the Visualizations folder. For example, if you have a song B4U and special B4U-specific BGAnimations called "robot" and "electric". First, move the robot and electric BGAnimation folders into the B4U song folder (e.g. "Songs\4th Mix\B4U\robot" and "Songs\4th Mix\B4U\electric"). Then, using the editor, insert a new background change at each point in the song where you to switch to a new BGAnimation. 
 * #NOTES...; - The main part of the file, that describes the steps. 
 * 
 */

public class DataParserSM {
	
	private static void parseBPM(DataFile df, String buffer) throws DataParserException {
		Scanner vsc = new Scanner(buffer);
		vsc.useDelimiter(",");
		while (vsc.hasNext()) {
			String pair = vsc.next().trim();
			try {
				if (pair.indexOf('=') < 0) {
					throw new Exception("No '=' found");
				} else {
					float beat = Float.parseFloat(pair.substring(0, pair.indexOf('=')));
					float value = Float.parseFloat(pair.substring(pair.indexOf('=') + 1));
					df.addBPM(beat, value);
				}
			} catch (Exception e) { // Also catch NumberFormatExceptions
				vsc.close();
				throw new DataParserException(
						e.getClass().getSimpleName(),
						"Improperly formatted #BPMS pair \"" + pair + "\": " +
						e.getMessage(), e
						);
			}
		}
		vsc.close();
	}
	
	private static void parseStop(DataFile df, String buffer) throws DataParserException {
		Scanner vsc = new Scanner(buffer);
		vsc.useDelimiter(",");
		while (vsc.hasNext()) {
			String pair = vsc.next().trim();
			try {
				if (pair.indexOf('=') < 0) {
					throw new Exception("No '=' found");
				} else {
					float beat = Float.parseFloat(pair.substring(0, pair.indexOf('=')));
					float value = Float.parseFloat(pair.substring(pair.indexOf('=') + 1));
					df.addStop(beat, value);
				}
			} catch (Exception e) { // Also catch NumberFormatExceptions
				vsc.close();
				throw new DataParserException(
						e.getClass().getSimpleName(),
						"Improperly formatted #STOPS pair \"" + pair + "\": " +
						e.getMessage(), e
						);
			}
		}
		vsc.close();
	}
	
	@SuppressWarnings("unused")
	private static void parseBGChange(DataFile df, String buffer) throws DataParserException {
		Scanner vsc = new Scanner(buffer);
		vsc.useDelimiter(",");
		while (vsc.hasNext()) {
			String pair = vsc.next().trim();
			try {
				if (pair.indexOf('=') < 0) {
					throw new Exception("No '=' found");
				} else {
					float beat = Float.parseFloat(pair.substring(0, pair.indexOf('=')));
					String value = pair.substring(pair.indexOf('=') + 1);
					df.addBGChange(beat, value);
				}
			} catch (Exception e) { // Also catch NumberFormatExceptions
				vsc.close();
				throw new DataParserException(
						e.getClass().getSimpleName(),
						"Improperly formatted #BGCHANGES pair \"" + pair + "\": " +
						e.getMessage(), e
						);
			}
		}
		vsc.close();
	}
	
	/*
	 * See http://www.stepmania.com/wiki/The_.SM_file_format
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
     */
	private static NoteType parseNoteType(char c, boolean holds) {
		switch (c) {
			case '0': return NoteType.NO_NOTE;
			case '1': return NoteType.TAP_NOTE;
			case '2': 
				if (holds) {
					return NoteType.HOLD_START;
				} else {
					return NoteType.TAP_NOTE;
				}
			case '3':
				if (holds) {
					return NoteType.HOLD_END;
				} else {
					return NoteType.NO_NOTE;
				}
			case '4': return NoteType.ROLL;
			case 'M': return NoteType.MINE;
			case 'L': return NoteType.LIFT;
			default: return NoteType.NO_NOTE;
		}
	}
	
	private static int parseFraction(int lineIndex, int lineCount) throws DataParserException {
		int fraction = lineIndex * 192 / lineCount;
		if (fraction % (192/4) == 0) {
			return 4;
		} else if (fraction % (192/8) == 0) {
			return 8;
		} else if (fraction % (192/12) == 0) {
			return 12;
		} else if (fraction % (192/16) == 0) {
			return 16;
		} else if (fraction % (192/24) == 0) {
			return 24;
		} else if (fraction % (192/32) == 0) {
			return 32;
		} else if (fraction % (192/48) == 0) {
			return 48;
		} else if (fraction % (192/64) == 0) {
			return 64;
		} else if (fraction % (192/192) == 0) {
			return 192;
		} else {
			throw new DataParserException (
					"Unable to determine fraction type with lineIndex " +
					lineIndex +
					" and lineCount " +
					lineCount
					);
		}
	}
	
	public static boolean isSupportedNoteType(NoteType nt) {
		// TODO - Support mines and actual holds later
		return (nt.equals(NoteType.TAP_NOTE) || nt.equals(NoteType.HOLD_START) || 
				nt.equals(NoteType.HOLD_END));
	}
	
	// Confusing hold logic but pretty much its to ensure that holds end when jump is on
	private static LinkedList<Integer> activeHolds;
	private static int osu_num, osu_fraction;
	private static Randomizer rand;
	
	private static void addNotes(
			DataNotesData nd, String line,
			boolean holds, boolean jumps, boolean osu, boolean randomize,
			int lineIndex, int lineCount, float beat, float time, float timeIncrease, float offset)
	throws DataParserException {
		boolean noteAdded = false;
		for (int i = 0; i < line.length(); i++) {
			// No holds if randomize - logic and keeping track is too confusing (I tried and failed)
			NoteType nt = parseNoteType(line.charAt(i), (holds && !randomize));
			if (isSupportedNoteType(nt)) {
				// Series of checks whether or not the note should be added, confusinggg~
				boolean addNote = false;
				if (jumps) addNote = true;
				if (osu) addNote = false;
				if (!noteAdded) addNote = true;
				if (!jumps && !activeHolds.isEmpty()) addNote = false;
				if (nt.equals(NoteType.HOLD_END) && activeHolds.contains(i)) addNote = true;
				if (addNote) {
					int pitch;
					int fraction;
					int noteTime = (int)(time + timeIncrease - offset);
					float[] coords;
					if (osu) {
						coords = rand.nextCoords(lineIndex, lineCount);
						pitch = osu_num;
						fraction = osu_fraction;
					} else { // holds pitch logic put in else since osu! Mod doesn't use pitches
						coords = new float[4];
						pitch = i;
						fraction = parseFraction(lineIndex, lineCount);
						if (nt.equals(NoteType.HOLD_START)) {
							activeHolds.add(i);
						} else if (nt.equals(NoteType.HOLD_END) && activeHolds.contains(i)) {
							activeHolds.remove(activeHolds.indexOf(i));
						} else if (randomize) {
							pitch = rand.nextPitch(jumps);
						}
					}
					DataNote n = new DataNote(
							nt,
							fraction,
							pitch,
							noteTime,
							beat,
							coords,
							osu_num
							);
					nd.addNote(n);
					osu_num++;
					noteAdded = true;
				}
			}
		}
	}
	
	/*
	 * See http://www.stepmania.com/wiki/The_.SM_file_format
	 * 
	 * Note rows are grouped into measures. The number of note rows you specify in a measure
	 * will determine the time value of each note. For example,
	 * If there are 4 notes rows in a measure, each note will be treated as a quarter note.
	 * If there are 8 notes rows in a measure, each note will be treated as a eighth note.
	 * If there are 12 notes rows in a measure, each note will be treated as a triplet (1/12th) note.
	 * Measures are separated by a comma. 
	 */
	public static void parseNotesData(DataFile df, DataNotesData nd,
			boolean jumps, boolean holds, boolean osu, boolean randomize)
	throws DataParserException {
		Scanner nsc = new Scanner(nd.getNotesData());
		nsc.useDelimiter(",");
		
		float beat = 0f;
		float time = 0;
		String line = "";
		String measure = "";

		Queue<Float> stopsBeat = df.getStopsBeat();
		Queue<Float> stopsValue = df.getStopsValue();
		activeHolds = new LinkedList<Integer>();
		rand = new Randomizer(df.md5hash.hashCode());
		try {
			// Measure
			osu_fraction = 0; // ++ -> 1
			while (nsc.hasNext()) {
				measure = nsc.next().trim();
				osu_num = 1;
				osu_fraction++;
				if (osu_fraction > GUINoteImage.OSU_FRACTION_MAX) osu_fraction = 1;
				rand.setupNextMeasure();
				
				// Get measure count
				Scanner msc = new Scanner(measure);
				int lineCount = 0;
				while (msc.hasNextLine()) {
					if (msc.nextLine().trim().charAt(0) != '/') { // not comment
						lineCount++;
					}
				}
				msc.close();
				
				// Assume that stepfile makers are nice and separate lines within a measure by page breaks
				int lineIndex = 0;
				float timeIncrease = 0;
				float offset = df.getOffset();
				msc = new Scanner(measure);
				// Line
				while (msc.hasNextLine()) {
					line = msc.nextLine().trim();
					if (line.charAt(0) == '/') { // comment
						continue;
					}
					
					if (line.length() != nd.getNotesType().getNotesCount()) {
						msc.close();
						throw new DataParserException(
								"line length " +
								line.length() +
								" does not match note type " +
								nd.getNotesType().toString()
								);
					}					
					// Note
					rand.setupNextLine();
					addNotes(
							nd,
							line, holds, jumps, osu, randomize,
							lineIndex, lineCount,
							beat, time, timeIncrease, offset
							);
					
					// TIME_PER_MEASURE = 60s * 1000ms/s * 4 beats/measure
					timeIncrease += (60f * 1000f * 4f) / ((float)lineCount * df.getBPM(beat));

					if (!stopsBeat.isEmpty() && beat >= stopsBeat.peek()) {
						stopsBeat.poll();
						timeIncrease += stopsValue.poll() * 1000;
					}
					lineIndex++;
					beat += 4.0 / (float)lineCount;
				}
				msc.close();
				time += timeIncrease;
			}
		} catch (Exception e) {
			throw new DataParserException(
					e.getClass().getSimpleName(),
					e.getMessage() +
					" for line " +
					line, e
					);
		} finally {
			nsc.close();
		}
	}
	
	private static void parseNotes(DataFile df, String buffer)
	throws DataParserException {
		// Expected format:
		// #NOTES:
		// <NotesType>:
		// <Description>:
		// <DifficultyClass>:
		// <DifficultyMeter>:
		// <RadarValues>:
		// <NoteData>;
		
		Scanner ndsc = new Scanner(buffer);
		ndsc.useDelimiter(":");
		String nbuffer = "";
		DataNotesData nd = new DataNotesData();
		
		try {			
			// Notes Type
			nbuffer = ndsc.next().trim();
			for (NotesType nt : NotesType.values()) {
				if (nbuffer.equals(nt.toString())) {
					nd.setNotesType(nt);
					break;
				}
			}
			// Because only 4-keys (dance-single) is currently supported
			// TODO - add a note reduction system maybe that can reduce more than 4-keys to 4-keys? 
			if (!nd.getNotesType().equals(NotesType.DANCE_SINGLE)) {
				ndsc.close();
				return;
			}
			
			// Description
			nbuffer = ndsc.next().trim();
			nd.setDescription(nbuffer);
			
			// Difficulty
			nbuffer = ndsc.next().trim();
			if (nbuffer.equalsIgnoreCase("beginner")) {
				nd.setDifficulty(Difficulty.BEGINNER);
			} else if (nbuffer.equalsIgnoreCase("easy")) {
				nd.setDifficulty(Difficulty.EASY);
			} else if (nbuffer.equalsIgnoreCase("medium")) {
				nd.setDifficulty(Difficulty.MEDIUM);
			} else if (nbuffer.equalsIgnoreCase("hard")) {
				nd.setDifficulty(Difficulty.HARD);
			} else if (nbuffer.equalsIgnoreCase("challenge")) {
				nd.setDifficulty(Difficulty.CHALLENGE);
			} else if (nbuffer.equalsIgnoreCase("edit")) {
				nd.setDifficulty(Difficulty.EDIT);
			} else {
				nd.setDifficulty(Difficulty.UNKNOWN);
			}
			
			// Difficulty Meter
			nbuffer = ndsc.next().trim();
			if (nbuffer.length() > 0) {
				nd.setDifficultyMeter(Integer.parseInt(nbuffer));
			}
			
			// Radar Values
			nbuffer = ndsc.next().trim();
			Scanner rsc = new Scanner(nbuffer);
			rsc.useDelimiter(",");
			int radarValueCount = 0;
			while (rsc.hasNext()) {
				try {
					nd.addRadarValue(Float.valueOf(rsc.next().trim()));
					radarValueCount++;
				} catch (Exception e) {
					rsc.close();
					if (radarValueCount < 5) {
						throw new DataParserException(
								e.getClass().getSimpleName(),
								e.getMessage(), e
								);
					}
				}
			}
			
			// Notes Data
			nbuffer = ndsc.next().trim();
			nd.setNotesData(nbuffer);
			
			df.addNotesData(nd);
			ndsc.close();
			
		} catch (Exception e) {
			ndsc.close();
			throw new DataParserException(
					e.getClass().getSimpleName(),
					"Improperly formatted #NOTES data: " +
					e.getMessage(), e
					);
		}
	}
	
	private static String stripSM(String buffer) throws DataParserException {
		if (!buffer.contains(":")) {
			throw new DataParserException("Info tag missing ':' char: " + buffer);
		} else {
			return buffer.substring(buffer.indexOf(":") + 1).trim();
		}
	}
	
	public static void parse(DataFile df) throws DataParserException, FileNotFoundException {
		// Setup
		File f = new File(df.getFilename());
		Scanner sc = new Scanner(f, "UTF-8"); // For all us otaku out there!
		sc.useDelimiter(";");
		String buffer = "";
		try {
			while (sc.hasNext()) {
				buffer = sc.next().trim();
				if (buffer.contains("#")) { // Info tag
					// Ignore comments and the byte order mark (xEF BB BF)
					if (buffer.charAt(0) != '#') {
						buffer = buffer.substring(buffer.indexOf('#'));
					}
					
					// Start filling in the info...
					if (buffer.contains("#TITLE:")) {
						df.setTitle(stripSM(buffer));
					} else if (buffer.contains("#SUBTITLE:")) {
						df.setSubTitle(stripSM(buffer));
					} else if (buffer.contains("#ARTIST:")) {
						df.setArtist(stripSM(buffer));
					} else if (buffer.contains("#TITLETRANSLIT:")) {
						df.setTitleTranslit(stripSM(buffer));
					} else if (buffer.contains("#SUBTITLETRANSLIT:")) {
						df.setSubTitleTranslit(stripSM(buffer));
					} else if (buffer.contains("#ARTISTTRANSLIT:")) {
						df.setArtistTranslit(stripSM(buffer));
					} else if (buffer.contains("#CREDIT:")) {
						df.setCredit(stripSM(buffer));
					} else if (buffer.contains("#BANNER:")) {
						df.setBanner(stripSM(buffer));
					} else if (buffer.contains("#BACKGROUND:")) {
						df.setBackground(stripSM(buffer));
					} else if (buffer.contains("#CDTITLE:")) {
						// Unimplemented
						//df.setCDTitle(stripSM(buffer));
					} else if (buffer.contains("#MUSIC:")) {
						df.setMusic(stripSM(buffer));
					} else if (buffer.contains("#OFFSET:")) {
						df.setOffset(Float.parseFloat(stripSM(buffer)) * 1000f);
					} else if (buffer.contains("#SAMPLESTART:")) {
						// Unimplemented
						//df.setSampleStart(Float.parseFloat(stripSM(buffer)));
					} else if (buffer.contains("#SAMPLELENGTH:")) {
						// Unimplemented
						//df.setSampleLength(Float.parseFloat(stripSM(buffer)));
					} else if (buffer.contains("#SELECTABLE:")) {
						// Unimplemented
						//df.setSelectable(stripSM(buffer).equalsIgnoreCase("YES"));
					} else if (buffer.contains("#BPMS:")) {
						parseBPM(df, stripSM(buffer));
					} else if (buffer.contains("#STOPS:")) {
						parseStop(df, stripSM(buffer));
					} else if (buffer.contains("#BGCHANGES:")) {
						// Unimplemented
						//parseBGChange(df, stripSM(buffer));
					} else if (buffer.contains("#NOTES:")) {
						parseNotes(df, stripSM(buffer));
					} else if (buffer.contains("#LYRICSPATH:")) {
						// Unsupported
					} else if (buffer.contains("#GENRE:")) {
						// Unsupported
					} else {
						// Unsupported tag outside of SM 3.9's specification?
					}
				} else {
					//Ignore, probably a comment
				}
				
				// Because some lazy people don't fully add all tags
				df.setMusicBackup();
				df.setBackgroundBackup();
			}
		} catch (Exception e) {
			sc.close();
			throw new DataParserException(e.getMessage(), e);
		}
		sc.close();
	}
}
