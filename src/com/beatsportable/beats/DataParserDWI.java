package com.beatsportable.beats;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import com.beatsportable.beats.DataNote.NoteType;
import com.beatsportable.beats.DataNotesData.Difficulty;
import com.beatsportable.beats.DataNotesData.NotesType;

/*
 * See http://dwi.ddruk.com/readme.php#4
 * 
 * Steps:
 * Step-patterns are defined in the same way as .MSD files - use the numeric keypad as a reference for most patterns:
 * 7=U+L  8=U  9=U+R
 * 4=L         6=R
 * 1=D+L  2=D  3=D+R
 * (U+D = A and L+R = B)
 * 
 * A '0' indicates no step. Each character defaults to one 1/8 of a beat.
 * Surround a series of characters with the following brackets to change the rate at which the steps come:
 * (...)  = 1/16 steps
 * [...]  = 1/24 steps
 * {...}  = 1/64 steps
 * `...'  = 1/192 steps
 * 
 * Holds:
 * In the DWI file format a hold arrow is signified with the ! symbol.
 * The string 8!8 will begin an 'up' hold arrow, and the arrow will be released the next time the program encounters
 * an 'up' arrow: by itself or combined with another arrow (7, 8, 9, A, etc.) The characters 7!4 would show both 'up'
 * and 'left' arrows but only the left arrow would be held. The format could best be described as "show!hold".
 *
 * Tags:
 * These tags should be in every DWI file:
 * #TITLE:...;  	title of the song.
 * #ARTIST:...;  	artist of the song.
 * #GAP:...;  	number of milliseconds that pass before the program starts counting beats. Used to sync the steps to the music.
 * #BPM:...;  	BPM of the music
 * #DISPLAYTITLE:...;   	provides an alternate version of the song name that can also include special characters.
 * #DISPLAYARTIST:...;  	provides an alternate version of the artist name that can also include special characters.
 * #DISPLAYBPM:...; 	tells DWI to display the BPM on the song select screen in a user-defined way.  Options can be:
 * *    - BPM cycles randomly
 * a    - BPM stays set at 'a' value (no cycling)
 * a..b - BPM cycles between 'a' and 'b' values
 * #FILE:...;   	path to the music file to play (eg. /music/mysongs/abc.mp3 )
 * (NB: if the file is not found, a .wav or .mp3 file in the same folder as the DWI file is used)
 * #MD5:...;   	an MD5 string for the music file. Helps ensure that same music file is used on all systems.
 * #FREEZE:...;   	a value of the format "BBB=sss". Indicates that at 'beat' "BBB", the motion of the arrows should
 * stop for "sss" milliseconds. Turn on beat-display in the System menu to help determine what values to use. Multiple
 * freezes can be given by separating them with commas.
 * #CHANGEBPM:...;   	a value of the format "BBB=nnn". Indicates that at 'beat' "BBB", the speed of the arrows will
 * change to reflect a new BPM of "nnn". Multiple BPM changes can be given by separating them with commas.
 * #STATUS:...;   	can be "NEW" or "NORMAL". Changes the display of songs on the song-select screen.
 * #GENRE:...;   	a genre to assign to the song if "sort by Genre" is selected in the System Options. Multiple Genres
 * can be given by separating them with commas.
 * #CDTITLE:...;   	points to a small graphic file (64x40) that will display in the song selection screen in the bottom
 * right of the background, showing which CD the song is from. The colour of the pixel in the upper-left will be made transparent.
 * #SAMPLESTART:...;   	the time in the music file that the preview music should start at the song-select screen.
 * Can be given in Milliseconds (eg. 5230), Seconds (eg. 5.23), or minutes (eg. 0:05.23). Prefix the number with a "+" to factor in the GAP value.
 * #SAMPLELENGTH:...;   	how long to play the preview music for at the song-select screen. Can be in milliseconds, seconds, or minutes.
 * #RANDSEED:x;   	provide a number that will influence what AVIs DWI picks and their order. Will be the same animation
 * each time if AVI filenames and count doesn't change (default is random each time).
 * #RANDSTART:x;   	tells DWI what beat to start the animations on. Default is 32.
 * #RANDFOLDER:...;   	tells DWI to look in another folder when choosing AVIs, allowing 'themed' folders.
 * #RANDLIST:...;   	a list of comma-separated filenames to use in the folder.
 * 
 * Each pattern of steps for different modes have the same basic format:
 * 
 * #SINGLE:BASIC:X:...;
 *  ^      ^     ^ ^
 *  |      |     | + step patterns.  In doubles, the left pad's steps are given first, 
 *  |      |     |   then the right pad's, separated by a colon (:).
 *  |      |     |
 *  |      |     + difficulty rating.  Should be 1 or higher.
 *  |      |
 *  |      + Difficulty.  Can be one of "BASIC", "ANOTHER", "MANIAC", or "SMANIAC"
 *  |
 *  + Style.  Can be one of "SINGLE", "DOUBLE", "COUPLE", or "SOLO".  "COUPLE" is 
 *    Battle-mode steps.
 * 
 * Comments can be used by using "//". Everything after this on the same line in the file will be ignored.
 * 
 */

public class DataParserDWI {
	
	private static void parseBPM(DataFile df, String buffer) throws DataParserException {
		Scanner vsc = new Scanner(buffer);
		vsc.useDelimiter(",");
		while (vsc.hasNext()) {
			String pair = vsc.next().trim();
			try {
				if (pair.indexOf('=') < 0) {
					throw new Exception("No '=' found");
				} else {
					float beat = Float.parseFloat(pair.substring(0, pair.indexOf('='))) / 4f;
					float value = Float.parseFloat(pair.substring(pair.indexOf('=') + 1));
					df.addBPM(beat, value);
				}
			} catch (Exception e) { // Also catch NumberFormatExceptions
				vsc.close();
				throw new DataParserException(
						e.getClass().getSimpleName(),
						"Improperly formatted #CHANGEBPM pair \"" + pair + "\": " +
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
					float beat = Float.parseFloat(pair.substring(0, pair.indexOf('='))) / 4f;
					float value = Float.parseFloat(pair.substring(pair.indexOf('=') + 1)) / 1000f;
					df.addStop(beat, value);
				}
			} catch (Exception e) { // Also catch NumberFormatExceptions
				vsc.close();
				throw new DataParserException(
						e.getClass().getSimpleName(),
						"Improperly formatted #FREEZE pair \"" + pair + "\": " +
						e.getMessage(), e
						);
			}
		}
		vsc.close();
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
	
	// Confusing hold logic but pretty much its to ensure that holds end when jump is on
	private static LinkedList<Integer> activeHolds;
	private static int osu_num, osu_fraction;
	private static Randomizer rand;
	
	// TODO
	private static void addNotes(DataNotesData nd, Queue<DataNote> notes, NoteType nt,
			boolean jumps, boolean osu, boolean randomize,
			int beatFractionTotal, float beat, float time, float offset, int pitchA)
	throws DataParserException {
		int pitch;
		int fraction;
		int noteTime = (int)(time - offset);
		float[] coords;
		if (osu) {
			coords = rand.nextCoords(beatFractionTotal, 192);
			pitch = osu_num;
			fraction = osu_fraction;
		} else {
			coords = new float[4];
			pitch = pitchA;
			fraction = parseFraction(beatFractionTotal, 192);
			if (randomize) {
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
		notes.add(n);
		//osu_num++; // Do this during the note adding phase
	}
	
	private static final int OSU_FRACTION_MAX = 4;
	public static void parseNotesData(DataFile df, DataNotesData nd,
			boolean jumps, boolean holds, boolean osu, boolean randomize)
	throws DataParserException {
		/*
		 * Step-patterns are defined in the same way as .MSD files - use the numeric keypad as a reference for most patterns:
		 * 7=U+L  8=U  9=U+R
		 * 4=L         6=R
		 * 1=D+L  2=D  3=D+R
		 * (U+D = A and L+R = B)
		 * 
		 * A '0' indicates no step. Each character defaults to one 1/8 of a beat.
		 * Surround a series of characters with the following brackets to change the rate at which the steps come:
		 * (...)  = 1/16 steps
		 * [...]  = 1/24 steps
		 * {...}  = 1/64 steps
		 * `...'  = 1/192 steps
		 * 
		 * Holds:
		 * In the DWI file format a hold arrow is signified with the ! symbol.
		 * The string 8!8 will begin an 'up' hold arrow, and the arrow will be released the next time the program encounters
		 * an 'up' arrow: by itself or combined with another arrow (7, 8, 9, A, etc.) The characters 7!4 would show both 'up'
		 * and 'left' arrows but only the left arrow would be held. The format could best be described as "show!hold".
		 */
		String ndd = nd.getNotesData();
		float beat = 0f;
		float time = 0f;
		float offset = df.getOffset();
		int beatFraction = 192/8;
		int beatFractionTotal = 0; // DWI has no sense of "Measures" but one measure is a full step or beat fraction total of 192/192

		Queue<Float> stopsBeat = df.getStopsBeat();
		Queue<Float> stopsValue = df.getStopsValue();
		activeHolds = new LinkedList<Integer>();
		rand = new Randomizer(df.md5hash.hashCode());
		try {
			int i = 0;
			osu_num = 1;
			osu_fraction = 1;			
			rand.setupNextMeasure();
			Queue<DataNote> notes = new LinkedList<DataNote>();
			while (i < ndd.length()) {
				char c = ndd.charAt(i);
				if (c == '!') { // Hold
					i++;
					char c_hold = ndd.charAt(i);
					int pitchA = -1, pitchB = -1;
					switch(c_hold) {
						case '0': // No step
							break;
						case '1': // D+L
							pitchA = 0; pitchB = 1; break; 
						case '2': // D
							pitchA = 1; break;
						case '3': // D+R
							pitchA = 1; pitchB = 3; break;
						case '4': // L
							pitchA = 0; break;
						//case '5': // Nothing
						case '6': // R
							pitchA = 3; break;
						case '7': // U+L
							pitchA = 0; pitchB = 2; break;
						case '8': // U
							pitchA = 2; break;
						case '9': // U+R
							pitchA = 2; pitchB = 3; break;
						case 'A': // U+D
							pitchA = 1; pitchB = 2; break;
						case 'B': // L+R
							pitchA = 0; pitchB = 3; break;
					}
					// Modify the type to hold starts
					if (pitchA != -1) {
						for (DataNote n : notes) {
							if (n.column == pitchA) {
								n.noteType = NoteType.HOLD_START;
								activeHolds.add(pitchA);
								break;
							}
						}
					}
					if (pitchB != -1) {
						for (DataNote n : notes) {
							if (n.column == pitchB) {
								n.noteType = NoteType.HOLD_START;
								activeHolds.add(pitchB);
								break;
							}
						}
					}
				} else {
					// Add it after the fact in case it needs to be modified by holds
					// Note that if both jumps and holds are off, some taps notes may be missed
					// if they're on the same line as a hold end more left of them. Oh well, not worth fixing ; P
					while (!notes.isEmpty()) {
						DataNote n = notes.remove();
						// Holds
						if (!holds || randomize || osu) {
							if (n.noteType.equals(NoteType.HOLD_START)) {
								n.noteType = NoteType.TAP_NOTE;
								if (osu) {
									n.num = osu_num;
									osu_num++;
								}
								nd.addNote(n);
							} else if (!n.noteType.equals(NoteType.HOLD_END)) { // Don't add hold ends
								if (osu) {
									n.num = osu_num;
									osu_num++;
								}
								nd.addNote(n);
							}
						} else { 
							nd.addNote(n);
						}
					}
					// Measure
					if (beatFractionTotal >= 192) { // Measure obtained!
						osu_num = 1;
						osu_fraction++;
						if (osu_fraction > OSU_FRACTION_MAX) osu_fraction = 1;
						rand.setupNextMeasure();
						beatFractionTotal -= 192;
					}
					int pitchA = -1, pitchB = -1;
					boolean step = true;
					switch(c) {
						case '(': // 1/16
							beatFraction = 192/16; step = false; break;
						case '[': // 1/24
							beatFraction = 192/24; step = false; break;
						case '{': // 1/64
							beatFraction = 192/64; step = false; break;
						case '`': // 1/192
							beatFraction = 192/192; step = false; break;
						case ')': // back to 1/8
						case ']':
						case '}':
						case '\'':
							beatFraction = 192/8; step = false; break;
						// LEFT = 0, DOWN = 1, UP = 2, RIGHT = 3
						case '0': // No step
							break;
						case '1': // D+L
							pitchA = 0; pitchB = 1; break; 
						case '2': // D
							pitchA = 1; break;
						case '3': // D+R
							pitchA = 1; pitchB = 3; break;
						case '4': // L
							pitchA = 0; break;
						//case '5': // Nothing
						case '6': // R
							pitchA = 3; break;
						case '7': // U+L
							pitchA = 0; pitchB = 2; break;
						case '8': // U
							pitchA = 2; break;
						case '9': // U+R
							pitchA = 2; pitchB = 3; break;
						case 'A': // U+D
							pitchA = 1; pitchB = 2; break;
						case 'B': // L+R
							pitchA = 0; pitchB = 3; break;
						default: // New line, etc.
							step = false; break;
					}
					
					if (step) {
						NoteType ntA = NoteType.TAP_NOTE;
						NoteType ntB = NoteType.TAP_NOTE;
						// Note
						rand.setupNextLine();
						if (pitchA != -1) {
							Iterator<Integer> it = activeHolds.iterator();
							while (it.hasNext()) {
								Integer h = it.next();
								if (h.equals(pitchA)) {
									ntA = NoteType.HOLD_END;
									it.remove();
								}
							}
							if (jumps || activeHolds.isEmpty() || ntA.equals(NoteType.HOLD_END)) {
								addNotes(nd, notes, ntA, jumps, osu, randomize,
										beatFractionTotal, beat, time, offset, pitchA);
							}
						}
						if (pitchB != -1) {
							Iterator<Integer> it = activeHolds.iterator();
							while (it.hasNext()) {
								Integer h = it.next();
								if (h.equals(pitchB)) {
									ntB = NoteType.HOLD_END;
									it.remove();
								}
							}
							if ((jumps || ntB.equals(NoteType.HOLD_END)) && !osu) {
								addNotes(nd, notes, ntB, jumps, osu, randomize,
										beatFractionTotal, beat, time, offset, pitchB);
							}
						}
						
						//time += 60f * 1000f * ((float)beatFraction / 192f) / df.getBPM(beat);
						time += (60f * 1000f * (float)beatFraction) / ((192f / 4f) * df.getBPM(beat));
						if (!stopsBeat.isEmpty() && beat >= stopsBeat.peek()) {
							stopsBeat.poll();
							time += stopsValue.poll() * 1000;
						}
						beat += (float)beatFraction / (192f / 4f);
						beatFractionTotal += beatFraction; // For measure
					}
				}
				i++;
			}
			// Add last notes
			while (!notes.isEmpty()) {
				nd.addNote(notes.remove());
			}
		} catch (Exception e) {
			throw new DataParserException(
					e.getClass().getSimpleName(),
					e.getMessage(), e
					);
		}
	}

	private static void parseNotes(DataFile df, String buffer)
	throws DataParserException {
		// #SINGLE:BASIC:X:...;
		//  ^      ^     ^ ^
		//  |      |     | + step patterns.  In doubles, the left pad's steps are given first, 
		//  |      |     |   then the right pad's, separated by a colon (:).
		//  |      |     |
		//  |      |     + difficulty rating.  Should be 1 or higher.
		//  |      |
		//  |      + Difficulty.  Can be one of "BASIC", "ANOTHER", "MANIAC", or "SMANIAC"
		//  |
		//  + Style.  Can be one of "SINGLE", "DOUBLE", "COUPLE", or "SOLO".  "COUPLE" is 
		//    Battle-mode steps.
		
		Scanner ndsc = new Scanner(buffer);
		ndsc.useDelimiter(":");
		String nbuffer = "";
		DataNotesData nd = new DataNotesData();
		
		try {
			// Notes Type
			nd.setNotesType(NotesType.DANCE_SINGLE); // We parse SINGLE only
			
			// Difficulty
			nbuffer = ndsc.next().trim();
			if (nbuffer.equalsIgnoreCase("basic")) {
				nd.setDifficulty(Difficulty.EASY);
			} else if (nbuffer.equalsIgnoreCase("another")) {
				nd.setDifficulty(Difficulty.MEDIUM);
			} else if (nbuffer.equalsIgnoreCase("maniac")) {
				nd.setDifficulty(Difficulty.HARD);
			} else if (nbuffer.equalsIgnoreCase("smaniac")) {
				nd.setDifficulty(Difficulty.CHALLENGE);
			} else {
				nd.setDifficulty(Difficulty.UNKNOWN);
			}
			
			// Difficulty Meter
			nbuffer = ndsc.next().trim();
			if (nbuffer.length() > 0) {
				nd.setDifficultyMeter(Integer.parseInt(nbuffer));
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
	
	private static String stripTag(String buffer) throws DataParserException {
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
						df.setTitle(stripTag(buffer));
					} else if (buffer.contains("#ARTIST:")) {
						df.setArtist(stripTag(buffer));
					} else if (buffer.contains("#DISPLAYTITLE:")) {
						df.setTitleTranslit(stripTag(buffer));
					} else if (buffer.contains("#DISPLAYARTIST:")) {
						df.setArtistTranslit(stripTag(buffer));
					} else if (buffer.contains("#GAP:")) {
						df.setOffset(-Float.parseFloat(stripTag(buffer)));
					} else if (buffer.contains("#BPM:")) {
						df.addBPM(0f, Float.parseFloat(stripTag(buffer)));
					//} else if (buffer.contains("#DISPLAYBPM:")) {
						// Unimplemented
					} else if (buffer.contains("#FILE:")) {
						df.setMusic(stripTag(buffer));
					//} else if (buffer.contains("#MD5:")) {
						// Unimplemented
					} else if (buffer.contains("#FREEZE:")) {
						parseStop(df, stripTag(buffer));
					} else if (buffer.contains("#CHANGEBPM:")) {
						parseBPM(df, stripTag(buffer));
					//} else if (buffer.contains("#STATUS:")) {
						// Unimplemented
					//} else if (buffer.contains("#GENRE:")) {
						// Unimplemented
					//} else if (buffer.contains("#CDTITLE:")) {
						// Unimplemented
					//} else if (buffer.contains("#SAMPLESTART:")) {
						// Unimplemented
					//} else if (buffer.contains("#SAMPLELENGTH:")) {
						// Unimplemented
					//} else if (buffer.contains("#RANDSEED:")) {
						// Unimplemented
					//} else if (buffer.contains("#RANDSTART:")) {
						// Unimplemented
					//} else if (buffer.contains("#RANDFOLDER:")) {
						// Unimplemented
					//} else if (buffer.contains("#RANDLIST:")) {
						// Unimplemented
					} else if (buffer.contains("#SINGLE:")) {
						parseNotes(df, stripTag(buffer));
					//} else if (buffer.contains("#DOUBLE:")) {
						// Unimplemented
					//} else if (buffer.contains("#COUPLE:")) {
						// Unimplemented
					//} else if (buffer.contains("#SOLO:")) {
						// Unimplemented
					} else {
						// Unsupported tag outside of DWI v2.50.00 specifications?
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
