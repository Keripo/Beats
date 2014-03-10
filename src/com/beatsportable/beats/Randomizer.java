package com.beatsportable.beats;

import java.util.LinkedList;
import java.util.Random;

public class Randomizer {
	
	public static final int OFF = 0;
	public static final int STATIC = 1;
	public static final int DYNAMIC = 2;
	private static final int X = 0, Y = 1;
	private static final int CX = 0, CY = 1, SX = 2, SY = 3;
	
	private Random rand;
	private int randomize;
	
	// SM
	private boolean[] lastPitches;
	
	// osu!
	private LinkedList<float[]> lastCentres;
	private float minDist;
	private float[] currentCentre;
	private int currentPattern;
	private int currentFlip;
	private int currentRotation;
	private int currentOffset;
	
	/**
	 * One Randomizer per stepfile, initialize with stepfile's md5hash
	 */
	public Randomizer(int seed) {
		randomize = Integer.parseInt(
				Tools.getSetting(R.string.randomize, R.string.randomizeDefault));
		if (randomize == DYNAMIC) {
			rand = new Random();
		} else {
			rand = new Random(seed);
		}
		if (Tools.randomizeBeatmap) {
			minDist = 0.25f; // new coords at least 1/4 screen dist
		} else {
			minDist = 0.333f; // new centres at least 1/3 screen dist
		}
		// Initialize just to be safe
		lastPitches = new boolean[Tools.PITCHES];
		lastCentres = new LinkedList<float[]>();
		currentCentre = new float[Tools.PITCHES];
	}
	
	/**
	 * Call after every line, only affects nextPitch() when not in osu! Mod
	 */
	public void setupNextLine() {
		//if (Tools.gameMode != Tools.OSU_MOD) {
			lastPitches = new boolean[Tools.PITCHES];
		//}
	}
	
	/**
	 * Call after every measure, only affects nextCoords() when in osu! Mod
	 */
	public void setupNextMeasure() {
		if (Tools.gameMode == Tools.OSU_MOD) {
			lastCentres = new LinkedList<float[]>();
			
			// select new pattern
			if (!Tools.randomizeBeatmap) {
				currentCentre = nextRandomCentre();
				currentPattern = rand.nextInt(6); // line, semicircle, fullcircle, V, Z, W
				currentFlip = rand.nextInt(4); // normal, horizontal, vertical, diagonal
				currentRotation = rand.nextInt(8); // 360* / 45* 
				currentOffset = rand.nextInt(3); // -29%, 0, +29% 
			}
		}
	}
	
	/**
	 * Returns pitch [0,3] for SM
	 */
	public int nextPitch(boolean jumps) {
		int randPitch = rand.nextInt(Tools.PITCHES);
		if (jumps) {
			// Force refresh to avoid dead-end situation
			boolean available = false;
			for (int i = 0; i < Tools.PITCHES; i++) {
				if (!lastPitches[i]) {
					available = true;
					break;
				}
			}
			if (!available) {
				lastPitches = new boolean[Tools.PITCHES];
			}
			while (lastPitches[randPitch]) {
				randPitch = rand.nextInt(Tools.PITCHES);
			}
			lastPitches[randPitch] = true;
		}
		return randPitch;
	}
	
	/**
	 * Returns coords {cx, cy, sx, sy} for osu! Mod
	 * cx = x-coord of centre, [0.0f, 1.0f)
	 * cy = y-coord of centre, [0.0f, 1.0f)
	 * sx = x shift relative to centre, [-1.0f, 1.0f)
	 * sy = y shift relative to centre, [-1.0f, 1.0f)
	 * See Beats osu! Mod Beatmap Generator Patterns.pdf for more info
	 */
	public float[] nextCoords(int lineIndex, int lineCount) {
		float[] newCoords = new float[4]; // {cx, cy, sx, sy}
		if (Tools.randomizeBeatmap) {
			currentCentre = nextRandomCentre();
			newCoords[CX] = currentCentre[X];
			newCoords[CY] = currentCentre[Y];
			newCoords[SX] = 0;
			newCoords[SY] = 0;
		} else {
			float[] newShift;
			// Create the coords
			switch(currentPattern) {
				default:
				case 0: newShift = nextShiftLine(lineIndex, lineCount); break;
				case 1: newShift = nextShiftSemiCircle(lineIndex, lineCount); break;
				case 2: newShift = nextShiftFullCircle(lineIndex, lineCount); break;
				case 3: newShift = nextShiftV(lineIndex, lineCount); break;
				case 4: newShift = nextShiftZ(lineIndex, lineCount); break;
				case 5: newShift = nextShiftW(lineIndex, lineCount); break;
			}
			// Flip the coords
			switch (currentFlip) { // normal, horizontal, vertical, diagonal
				default:
				case 0: break;
				case 1: newShift = flipHorizontally(newShift); break;
				case 2: newShift = flipVertically(newShift); break;
				case 3: newShift = flipHorizontally(flipVertically(newShift)); break;
			}
			// Rotate the coords
			newShift = rotateShift(newShift);
			
			// Offset the coords
			if (currentRotation % 2 == 0) { // full circle, 90* interval
				if (currentRotation == 0 || currentRotation == 4) { // 0* or 190*
					newShift = offsetShiftVertically(newShift);
				} else if (currentRotation == 2 || currentRotation == 6) { // 90* or 270*
					newShift = offsetShiftHorizontally(newShift);
				}
			}
			
			// Set the coords
			newCoords[CX] = currentCentre[X];
			newCoords[CY] = currentCentre[Y];
			newCoords[SX] = newShift[X];
			newCoords[SY] = newShift[Y];
		}
		return newCoords;
	}
	
	private float[] nextRandomCentre() {
		float[] newCentre = new float[2];
		boolean tooClose = false;
		do {
			// Random float [0.0, 1.0)
			newCentre[X] = rand.nextFloat();
			newCentre[Y] = rand.nextFloat();
			tooClose = false;
			for (float[] prevCentres : lastCentres) {
				// Not absolute distance but just simple square
				if (Math.abs(prevCentres[X] - newCentre[X]) < minDist &&
					Math.abs(prevCentres[Y] - newCentre[Y]) < minDist) {
					tooClose = true;
				}
			}
		} while (tooClose);
		if (lastCentres.size() >= 8) { // Don't hold that big of a history...
			lastCentres = new LinkedList<float[]>();
		}
		lastCentres.add(newCentre);
		return newCentre;
	}
	
	private float[] flipHorizontally(float[] origShift) {
		origShift[X] = -origShift[X];
		return origShift;
	}
	
	private float[] flipVertically(float[] origShift) {
		origShift[Y] = -origShift[Y];
		return origShift;
	}
	
	private float[] rotateShift(float[] origShift) {
		float[] newShift = new float[2]; // {sx, sy}
		double angle = currentRotation * Math.PI / 4; // 45* rotations
		newShift[X] = (float)(
				Math.cos(angle) * origShift[X]
				- Math.sin(angle) * origShift[Y]);
		newShift[Y] = (float)(
				Math.sin(angle) * origShift[X]
				+ Math.cos(angle) * origShift[Y]);
		return newShift;
	}
	
	private float[] offsetShiftHorizontally(float[] origShift) {
		switch(currentOffset) {
			default:
			case 0: break;
			case 1: origShift[X] -= 0.5f; break;
			case 2: origShift[X] += 0.5f; break;
		}
		return origShift;
	}
	
	private float[] offsetShiftVertically(float[] origShift) {
		switch(currentOffset) {
			default:
			case 0: break;
			case 1: origShift[Y] -= 0.5f; break;
			case 2: origShift[Y] += 0.5f; break;
		}
		return origShift;
	}
	
	// TODO - tweak sizes for equidistant beats
	
	private float[] nextShiftLine(int lineIndex, int lineCount) {
		float[] newShift = new float[2]; // {sx, sy}
		float dist = 2 * (lineIndex - ((float)lineCount / 2)) / lineCount; // [-1.0f, 1.0f)
		if (currentRotation % 2 == 1) { // 45* interval
			dist *= 1.414f; // sqrt(2)
		}
		
		newShift[X] = dist;
		newShift[Y] = 0; // this will be affected by rotation and maybe shift
		return newShift;
	}
	
	private float[] nextShiftSemiCircle(int lineIndex, int lineCount) {
		float[] newShift = new float[2]; // {sx, sy}
		// curve from left to right, upward, [-PI,0)
		double angle = Math.PI * ((lineIndex / (double)lineCount) - 1);
		newShift[X] = ((float)Math.cos(angle)) * 0.9f;
		newShift[Y] = ((float)Math.sin(angle) + 0.5f) * 0.9f; // centre it
		return newShift;
	}
	
	private float[] nextShiftFullCircle(int lineIndex, int lineCount) {
		float[] newShift = new float[2]; // {sx, sy}
		double angle = 2 * Math.PI * ((lineIndex / (double)lineCount) - 1);
		newShift[X] = (float)Math.cos(angle) * 0.55f;
		newShift[Y] = (float)Math.sin(angle) * 0.55f;
		return newShift;
	}
	
	private float[] nextShiftV(int lineIndex, int lineCount) {
		float[] newShift = new float[2]; // {sx, sy}
		float dist = 2 * (lineIndex - ((float)lineCount / 2)) / lineCount; // [-1.0f, 1.0f)
		newShift[X] = dist;
		if (dist < 0) {
			newShift[Y] = dist + 1.0f;
		} else {
			newShift[Y] = -dist + 1.0f;
		}
		newShift[Y] -= 0.5f; // centre it
		return newShift;
	}
	
	private float[] nextShiftZ(int lineIndex, int lineCount) {
		float[] newShift = new float[2]; // {sx, sy}
		float dist = 2 * (lineIndex - ((float)lineCount / 2)) / lineCount; // [-1.0f, 1.0f)
		newShift[X] = dist;
		if (dist < -0.5f) {
			newShift[Y] = -dist - 1.0f;
		} else if (dist >= -0.5f && dist < 0.5f) {
			newShift[Y] = dist; 
		} else {
			newShift[Y] = -dist + 1.0f;
		}
		return newShift;
	}
	
	private float[] nextShiftW(int lineIndex, int lineCount) {
		float[] newShift = new float[2]; // {sx, sy}
		float dist = 2 * (lineIndex - ((float)lineCount / 2)) / lineCount; // [-1.0f, 1.0f)
		newShift[X] = dist;
		if (dist < -0.5f) {
			newShift[Y] = dist + 0.75f;
		} else if (dist >= -0.5f && dist < 0.0f) {
			newShift[Y] = -dist - 0.25f;
		} else if (dist >= 0.0f && dist < 0.5f) {
			newShift[Y] = dist - 0.25f;
		} else {
			newShift[Y] = -dist + 0.75f;
		}
		return newShift;
	}

}
