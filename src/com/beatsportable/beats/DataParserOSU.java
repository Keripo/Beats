package com.beatsportable.beats;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.beatsportable.beats.DataNote.NoteType;
import com.beatsportable.beats.DataNotesData.Difficulty;
import com.beatsportable.beats.DataNotesData.NotesType;


// See osuspecv5mod.txt

public class DataParserOSU {

	private static int parseFraction(float noteTime, DataNotesData nd) {
		int index = 0;
		for (int i = 0; i < nd.bpmBeat.size(); i++) {
			if (nd.bpmBeat.get(i) <= noteTime) {
				index = i;
			} else {
				break;
			}
		}
		float bpmTime = nd.bpmBeat.get(index);
		float bpmValue = nd.bpmValue.get(index) * 4;		
		float fraction = noteTime - bpmTime; 
		float difference; // Rounding error?
		if ((difference = fraction % (bpmValue / 4)) < 2 || difference > (bpmValue / 4) - 2) {
			return 4;
		} else if ((difference = fraction % (bpmValue / 8)) < 2 || difference > (bpmValue / 8) - 2) {
			return 8;
		} else if ((difference = fraction % (bpmValue / 12)) < 2 || difference > (bpmValue / 12) - 2) {
			return 12;
		} else if ((difference = fraction % (bpmValue / 16)) < 2 || difference > (bpmValue / 16) - 2) {
			return 16;
		} else if ((difference = fraction % (bpmValue / 24)) < 2 || difference > (bpmValue / 24) - 2) {
			return 24;
		} else if ((difference = fraction % (bpmValue / 32)) < 2 || difference > (bpmValue / 32) - 2) {
			return 32;
		} else if ((difference = fraction % (bpmValue / 48)) < 2 || difference > (bpmValue / 48) - 2) {
			return 48;
		} else if ((difference = fraction % (bpmValue / 64)) < 2 || difference > (bpmValue / 64) - 2) {
			return 64;
		//} else if (fraction % (192/192) == 0) {
		} else { // Whatever
			return 192;
		}
	}
	
	private static int osu_num, osu_fraction;
	private static Randomizer rand;
	
	private static final int OSU_FRACTION_MAX = 4;
	private static final float OSU_MAX_X = 512; // Determined experimentally
	private static final float OSU_MAX_Y = 384; // Determined experimentally
	public static void parseNotesData(DataFile df, DataNotesData nd,
			boolean jumps, boolean holds, boolean osu, boolean randomize)
	throws DataParserException {
		/*
		 * [HitObjects]
		 * //HitObjectSoundType {Normal = 0, Whistle = 2, Finish = 4, WhistleFinish = 6, Clap = 8}
		 * // For a hit circle: x,y,startTime,objectType,soundType
		 * // For a slider: x,y,startTime,objectType,soundType,curveType|x1,y1|x2,y2|...xn,yn,repeatCount,pixelLength
		 * // For a spinner: x,y,startTime,objectType,soundType,endTime
		 * 
		 * //Bitwise flag enums:
		 * //HitObjectType {Normal = 1, Slider = 2, NewCombo = 4, NormalNewCombo = 5, SliderNewCombo = 6, Spinner = 8}
		 * //HitObjectSoundType {Normal = 0, Whistle = 2, Finish = 4, WhistleFinish = 6}
		 * 
		 * //SliderCurveType { C = Catmull, B = Bezier, L = Linear }
		 * //See http://up.ppy.sh/files/sliderstuff.cs and http://pe.ppy.sh/bezier.cs for curve creation specifics.
		 * //Post if you need more slider creation help.
		 * 
		 * 128,192,4056,1,0
		 * //HitCircle: xPos,yPos,startTimeMs,objectType,soundType
		 * 256,192,7748,6,2,B|256:96|384:96,1,170
		 * //Slider: xPos,yPos,startTimeMs,objectType,soundType,curveType|x1,y1|x2,y2|...|xn,yn,repeatCount,sliderLengthPixels
		 * 256,192,199292,12,0,202523
		 * //Spinner: xPos(redundant),yPod(redundant),startTimeMs,objectType,soundType,endTimeMs
		 * 
		 * Example:
		 * 72,192,1333,1,0
		 * 168,312,1767,1,0
		 * 344,312,2202,1,0
		 * 440,192,2637,6,0,B|400:192,4,37.5
		 */
		try {
			// For credits, also show version
			if (df.getCredit().length() > 2 && nd.getDescription().length() > 2) {
				df.setCredit(df.getCredit() + " - " + nd.getDescription());
			}
			
			String ndd = nd.getNotesData();
			rand = new Randomizer(df.md5hash.hashCode());
			rand.setupNextMeasure();
			rand.setupNextLine();
			
			osu_num = 1;
			osu_fraction = 1;
			Scanner nsc = new Scanner(ndd);
			String nbuffer = "";
			float lastx = -1f;
			float lasty = -1f;
			float lastOffset = 0f;
			int lastHoldEnd = -1;
			while(nsc.hasNextLine()) {
				nbuffer = nsc.nextLine().trim();
				if (!nbuffer.startsWith("//") && nbuffer.length() > 2) { // Not a comment and sanity check
					// Everything is a tap note for osu! Mod
					// We're assuming perfectly formatted data lines via osu! v5 specs
					// Sliders and spinners probably for Beats2 as Beats' hold implementation is already pretty hacked up
					// Also don't implement sounds yet, maybe experiment with them later
					// Jumps + hold logic is ignored as there are rare cases where 4 holds are active at once, but I'm
					// too lazy to hack around that
					Scanner sc = new Scanner(nbuffer);
					sc.useDelimiter(",");
					
					// Coords
					float x = Float.parseFloat(sc.next());
					float y = Float.parseFloat(sc.next());
					float[] coords = new float[4];
					if (randomize) {
						coords = rand.nextCoords(0, 0);
					} else {
						float cx = x;
						float cy = y;
						if (x == lastx && y == lasty) { // If same position, offset by a little bit
							lastOffset += 20f; // arbitrary number
							cx += lastOffset;
							cy += lastOffset;
						} else {
							lastOffset = 0f;
						}
						lastx = x;
						lasty = y;
						coords[0] = cx / OSU_MAX_X;
						coords[1] = cy / OSU_MAX_Y;
					}
					
					// Time
					int noteTime = Integer.parseInt(sc.next());
					
					// Note Type
					int objectType = Integer.parseInt(sc.next());
					
					// Sound - unimplemented at the moment
					//int soundType = Integer.parseInt(sc.next());
					sc.next();
					
					// Add notes depending on type
					//HitObjectType {Normal = 1, Slider = 2, NewCombo = 4, NormalNewCombo = 5, SliderNewCombo = 6, Spinner = 8}
					// Note: 12 = Spinner too it seems
					NoteType nt;
					int fraction;
					int pitch;
					DataNote n;
					if (objectType == 5 || objectType == 6) {
						osu_num = 1;
						osu_fraction++;
						if (osu_fraction > OSU_FRACTION_MAX) osu_fraction = 1;
						rand.setupNextMeasure();
					}
					if (jumps || noteTime > lastHoldEnd) {
						if ((objectType == 8 || objectType == 12) && !osu && holds) { // Spinner in SM mode
							int endTime = Integer.parseInt(sc.next());
							nt = NoteType.HOLD_START;
							if (osu) {
								fraction = osu_fraction;
							} else {
								fraction = parseFraction(noteTime, nd);
							}
							pitch = rand.nextPitch(jumps);
							
							// Add hold start
							nt = NoteType.HOLD_START;
							n = new DataNote(
									nt,
									fraction,
									pitch,
									noteTime,
									0, //beat, // osu! has no concept of beats
									coords,
									osu_num
									);
							nd.addNote(n);
							
							// Add hold end
							nt = NoteType.HOLD_END;
							n = new DataNote(
									nt,
									fraction,
									pitch,
									endTime,
									0, //beat, // osu! has no concept of beats
									coords,
									osu_num
									);
							nd.addNote(n);
							
							osu_num++;
							lastHoldEnd = endTime + 25; // arbitrary delay
						} else if ((objectType == 2 || objectType == 6) && holds) { // Slider
							/*
							 * http://osu.ppy.sh/forum/viewtopic.php?f=31&t=49974
							 * Given:
							 * SliderVelocity: SliderMutiplier in Options; measured in hundreds of osupixels per beat
							 * SliderMultiplier: -100 / beatLength of the last inheriting sections, or 1.0 if the last section
							 * is non-inheriting; a unitless coefficient
							 * BeatLength: beatLength of the last non-inheriting section; measured in milliseconds per beat
							 * 
							 * The slider travels at a speed of:
							 * AbsoluteSliderVelocity = (SliderVelocity / 100) * SliderMultiplier / BeatLength
							 * This has a unit of osupixels per millisecond.
							 * 
							 * Given:
							 * AbsoluteSliderVelocity: from above; measured in osupixels per millisecond
							 * StartTime: startTimeMs from the slider def; measured in milliseconds
							 * SliderLength: sliderLengthPixel from the slider def; measured in osupixels
							 * RepeatCount: repeatCount from the slider def; unitless
							 * 
							 * The slider begins at StartTime.
							 * The slider ends at StartTime + RepeatCount * SliderLength / AbsoluteSliderVelocity
							 * There is a rebound at times StartTime + n * SliderLength / AbsoluteSliderVelocity; 0 < n <= RepeatCount
							 * 
							 * ALTERNATIVE:
							 * SliderMultiplier:1.7 //Speed of slider movement, where 1.0 is 100pixels per beatLength.
							 * //beatLength - length of a single beat in milliseconds (double accuracy)
							 */
							
							// Sliders aren't implemented yet graphically ;<
							String sliderParams = sc.next();
							Scanner coordsScanner = new Scanner(sliderParams);
							//coordsScanner.useDelimiter("\\|");
							coordsScanner.useDelimiter(":|\\|");
							String curveType = coordsScanner.next();
							ArrayList<Float> curvePoints = new ArrayList<Float>();
							float x2 = 0;
							float y2 = 0; 
							while (coordsScanner.hasNext()) {
								/*
								String nextCoords = coordsScanner.next();
								Scanner colonScanner = new Scanner(nextCoords);
								colonScanner.useDelimiter(":");
								float x3 = colonScanner.nextInt() / OSU_MAX_X;
								float y3 = colonScanner.nextInt() / OSU_MAX_Y;
								*/
								float x3 = Float.parseFloat(coordsScanner.next()) / OSU_MAX_X;
								float y3 = Float.parseFloat(coordsScanner.next()) / OSU_MAX_Y;
								// I don't understand how the bezier calculations work, but there's redundancy...
								if (x3 != x2 || y3 != y2) {
									curvePoints.add(x3);
									curvePoints.add(y3);
									x2 = x3;
									y2 = y3;
								}
							}
							coordsScanner.close();
							float endCoords[] = new float[4];
							endCoords[0] = x2;
							endCoords[1] = y2;
							
							// Rest of data
							int repeatCount = Integer.parseInt(sc.next());
							float sliderLengthPixels = Float.parseFloat(sc.next());
							float sliderMultiplier = nd.getRasdarValue(0); // stored here for lack of better location
							float beatLength = df.getBPM(nd, noteTime);
							
							// I don't get MetalMario201's explanation, so lets just do it this way
							int endTime = (int)(noteTime + (sliderLengthPixels / sliderMultiplier) * beatLength * repeatCount);
							
							nt = NoteType.HOLD_START;
							if (osu) {
								fraction = osu_fraction;
							} else {
								fraction = parseFraction(noteTime, nd);
							}
							pitch = rand.nextPitch(jumps);
							
							// Add hold start
							nt = NoteType.HOLD_START;
							n = new DataNote(
									nt,
									fraction,
									pitch,
									noteTime,
									0, //beat, // osu! has no concept of beats
									coords,
									osu_num
									);
							nd.addNote(n);
							
							// Add hold end
							nt = NoteType.HOLD_END;
							n = new DataNote(
									nt,
									fraction,
									pitch,
									endTime,
									0, //beat, // osu! has no concept of beats
									endCoords,
									osu_num
									);
							n.curveType = curveType;
							n.curvePoints = curvePoints;
							nd.addNote(n);
							
							osu_num++;
							lastHoldEnd = endTime + 25; // arbitrary delay
						} else { // Whatever, just all taps
						/*
						} else if (objectType == 1 || objectType == 5 // Normal
								|| ((objectType == 8 || objectType == 12) && osu) // Spinner in osu! Mod (unimplemented graphically)
								|| ((objectType == 2 || objectType == 6) && osu) // Slider in osu! Mod (unimplemented graphically) 
								) {
						 */
							nt = NoteType.TAP_NOTE;
							if (osu) {
								fraction = osu_fraction;
							} else {
								fraction = parseFraction(noteTime, nd);
							}
							pitch = rand.nextPitch(jumps);
							n = new DataNote(
									nt,
									fraction,
									pitch,
									noteTime,
									0, //beat, // osu! has no concept of beats
									coords,
									osu_num
									);
							nd.addNote(n);
							osu_num++;
						}
					}
					sc.close();
				}
			}
			nsc.close();
		} catch (Exception e) {
			throw new DataParserException(
					e.getClass().getSimpleName(),
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
	
	private static String getDataString(String buffer) {
		return buffer.substring(buffer.indexOf("]") + 1).trim();
	}
	
	public static void parse(DataFile df, String filename) throws DataParserException, FileNotFoundException {
		// Setup
		File f = new File(filename);
		Scanner bsc = new Scanner(f, "UTF-8"); // For all us otaku out there!
		bsc.useDelimiter("\n\\["); // Include newline char cause sometimes the music file has '[' in it
		String bbuffer = "";
		DataNotesData nd = new DataNotesData(); // Each osu file actually defines only one notes data file
		try {
			while (bsc.hasNext()) { // Yes, there will be re-setting of values, and only the last values will be saved
				bbuffer = bsc.next().trim();
				if (bbuffer.startsWith("General]")) {
					bbuffer = getDataString(bbuffer);
					Scanner sc = new Scanner(bbuffer);
					while (sc.hasNextLine()) {
						String buffer = sc.nextLine().trim();
						if (!buffer.startsWith("//") && buffer.length() > 2) {
							//System.out.println("General - " + buffer);
							if (buffer.startsWith("AudioFilename:")) {
								df.setMusic(stripTag(buffer));
							} else if (buffer.startsWith("Mode:")) {
								if (Integer.parseInt(stripTag(buffer)) == 0) {
									nd.setNotesType(NotesType.DANCE_SINGLE); // Whatever, we're faking things here
								} else {
									// Taiko mode? Don't parse
									nd.setNotesType(NotesType.DANCE_UNKNOWN);
									sc.close();
									bsc.close();
									return;
								}
							}
							// Other fields unimplemented
							// Note: AudioLeadIn is for the PREVIEW, NOT the offset! osu! has no offset time
						}
					}
					sc.close();
				} else if (bbuffer.startsWith("Metadata]")) {
					bbuffer = getDataString(bbuffer);
					Scanner sc = new Scanner(bbuffer);
					while (sc.hasNextLine()) {
						String buffer = sc.nextLine().trim();
						if (!buffer.startsWith("//") && buffer.length() > 2) {
							//System.out.println("Metadata - " + buffer);
							if (buffer.startsWith("Title:")) {
								df.setTitle(stripTag(buffer));
							} else if (buffer.startsWith("Artist:")) {
								df.setArtist(stripTag(buffer));
							} else if (buffer.startsWith("Creator:")) {
								df.setCredit(stripTag(buffer));
							} else if (buffer.startsWith("Version:")) {
								String version = stripTag(buffer);
								nd.setDescription(stripTag(buffer));
								// Lets do some guess work
								if (version.equalsIgnoreCase("Easy") || version.contains("Easy")) {
									nd.setDifficulty(Difficulty.EASY);
								} else if (version.equalsIgnoreCase("Normal") || version.contains("Normal")) {
									nd.setDifficulty(Difficulty.MEDIUM);
								} else if (version.equalsIgnoreCase("Hard") || version.contains("Hard")) {
									nd.setDifficulty(Difficulty.HARD);
								} else if (version.equalsIgnoreCase("Maximum") || version.contains("Maximum")) {
									nd.setDifficulty(Difficulty.CHALLENGE);
								} else if (version.equalsIgnoreCase("Insane") || version.contains("Insane")) {
									nd.setDifficulty(Difficulty.CHALLENGE);
								} 
							}
							// Other fields unimplemented
						}
					}
					sc.close();
				} else if (bbuffer.startsWith("Difficulty]")) {
					bbuffer = getDataString(bbuffer);
					Scanner sc = new Scanner(bbuffer);
					while (sc.hasNextLine()) {
						String buffer = sc.nextLine().trim();
						if (!buffer.startsWith("//") && buffer.length() > 2) {
							//System.out.println("Difficulty - " + buffer);
							if (buffer.startsWith("OverallDifficulty:")) {
								/*
								 * There is no set standard due to osu! allowing
								 * multiple custom versions, but this is the result
								 * of surveying random songs.
								 * Yes, there will be potential overlaps within a folder
								 * but oh well, the selector will choose the first,
								 * which is sorted by difficultyMeter
								 * 1 = easy
								 * 2 = easy
								 * 3 = normal
								 * 4 = normal
								 * 5 = normal
								 * 6 = hard
								 * 7 = hard
								 * 8 = insane/maximum
								*/
								try {
									int difficultyMeter = Integer.parseInt(stripTag(buffer));
									nd.setDifficultyMeter(difficultyMeter);
									if (nd.getDifficulty() == Difficulty.BEGINNER) {
										switch (difficultyMeter) {
											case 0:
											case 1:
												nd.setDifficulty(Difficulty.BEGINNER);
												break;
											case 2:
												nd.setDifficulty(Difficulty.EASY);
												break;
											case 3:
											case 4:
											case 5:
												nd.setDifficulty(Difficulty.MEDIUM);
												break;
											case 6:
											case 7:
												nd.setDifficulty(Difficulty.HARD);
												break;
											case 8:
											default:
												nd.setDifficulty(Difficulty.CHALLENGE);
												break;
										}
									}
								} catch (NumberFormatException e) {
									nd.setDifficulty(Difficulty.UNKNOWN);
								}
							} else if (buffer.startsWith("SliderMultiplier:")) {
								// Lets just use the radar for this, out of laziness
								nd.addRadarValue(Float.parseFloat(stripTag(buffer)) * 100f);
							}
							// Other fields unimplemented
						}
					}
					sc.close();
				} else if (bbuffer.startsWith("Events]")) {
					bbuffer = getDataString(bbuffer);
					Scanner sc = new Scanner(bbuffer);
					while (sc.hasNextLine()) {
						String buffer = sc.nextLine().trim();
						if (!buffer.startsWith("//") && buffer.length() > 2) {
							// http://osu.ppy.sh/forum/viewtopic.php?f=20&t=1869
							// Lets assume the background image is the first event as posted specifications aren't clear
							if (buffer.contains(",\"")) { // Guess - its a background image reference?
								df.setBackground(buffer.substring(buffer.indexOf('\"') + 1, buffer.lastIndexOf('\"')));
								break;
							}
						}
					}
					sc.close();
				} else if (bbuffer.startsWith("TimingPoints]")) {
					bbuffer = getDataString(bbuffer);
					Scanner sc = new Scanner(bbuffer);
					//float lastValue = -1f;
					while (sc.hasNextLine()) {
						String buffer = sc.nextLine().trim();
						if (!buffer.startsWith("//") && buffer.length() > 2) {
							Scanner tsc = new Scanner(buffer);
							tsc.useDelimiter(",");
							// Assume perfect format
							float beat = Float.parseFloat(tsc.next()); // actually ms offset
							float value = Float.parseFloat(tsc.next());
							// instead of checking the rest, lets just assume all real timing changes are positive values
							if (value > 0f) {// && value != lastValue) {
								df.addBPM(beat, value);
								//lastValue = value;
							}
							tsc.close();
						}
					}
					sc.close();
				//} else if (bbuffer.startsWith("Colours]")) {
					// Unimplemented
				} else if (bbuffer.startsWith("HitObjects]")) {
					nd.setNotesData(getDataString(bbuffer)); // This is not stripped of comments
					//System.out.println(nd.getNotesData());
				}
			}
			
			// Because some lazy people don't fully add all tags
			df.setMusicBackup();
			df.setBackgroundBackup();
			
			// Add the NotesData to the DataFile if no errors
			df.clearBPMs(nd); // Clear for next .osu file
			df.addNotesData(nd);
		} catch (Exception e) {
			bsc.close();
			throw new DataParserException(e.getMessage(), e);
		}
		bsc.close();
	}
	
}
