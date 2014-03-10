package com.beatsportable.beats;
import java.util.Random;

@Deprecated
public class ToolsRandomizer {
	
	//private static final float TwoPi = (float)(2*Math.PI);

	//TODO this stuff shouldn't be static, since it's attached to a particular song.
	// Instead, create a new ToolsRandomizer for each song.
	
	private static Random rand;
	
	// I guess we could use some real data structure, but this is only 4 long anyway
	private static int lastCoordIndex;
	private static float[][] prevCoords;
	private static final float MIN_DIST = 0.2f;
	
	public static int lastPitch;
	public static int randomize;
	public static final int OFF = 0;
	public static final int STATIC = 1;
	public static final int DYNAMIC = 2;
	
	public static void randomize(int seed) {
		randomize = Integer.parseInt(
				Tools.getSetting(R.string.randomize, R.string.randomizeDefault));
		if (randomize == DYNAMIC) {
			rand = new Random();
		} else {
			rand = new Random(seed);
		}
		
		lastCoordIndex = -1;
		prevCoords = new float[Tools.PITCHES][2];
		for (int i = 0; i < prevCoords.length; i++) {
			prevCoords[i][0] = Float.MAX_VALUE;
			prevCoords[i][1] = Float.MAX_VALUE;
		}
		
		lastPitch = -1;
	}
	
	public static int nextPitch(int pitch, boolean jumps) {
		int randPitch = rand.nextInt(Tools.PITCHES);
		if (jumps) {
			while (randPitch == lastPitch) {
				randPitch = rand.nextInt(Tools.PITCHES);
			}
			lastPitch = randPitch;
		}
		return randPitch;
	}
	
	/*
	private static float[] cosRange(double mintheta, double maxtheta) {
		// returns [min(cos(x)) with x from mintheta to maxtheta, max(cos(x)) ...]
		double value1 = Math.cos(mintheta);
		double value2 = Math.cos(maxtheta);
		double minvalue = Math.min(value1, value2);
		double maxvalue = Math.max(value1, value2);
		if (mintheta > maxtheta) {
			double temp = mintheta; mintheta = maxtheta; maxtheta = temp;
		}
		int minsegment = (int)Math.floor(mintheta/Math.PI);
		int maxsegment = (int)Math.floor(maxtheta/Math.PI);
		
		float range_min = (float)minvalue;
		float range_max = (float)maxvalue;
		switch (maxsegment - minsegment) {
		case 0: break;
		case 1:
			if ((maxsegment & 1) == 0) 
				range_max = 1;
			else
				range_min = -1;
			break;
		default: range_min = -1; range_max = 1; break;
		}
		return new float[] {range_min, range_max};
	}
	*/
	/*
	private static float[] sinRange(double mintheta, double maxtheta) {
		return cosRange(Math.PI/2 - mintheta, Math.PI/2 - maxtheta);
	}
	*/
	
	// For osu! Mod	
	private static int _nCBm_last_lineIndex = -1;
	private static int _nCBm_last_lineCount = -1;
	
	
	/* TODO this function should be called once per measure and return
	 * positions for all notes in the measure, instead of once per note.
	 * The following is a hack. */
	/*
	private static double _nCBmC_radius,
		_nCBmC_centerx, _nCBmC_centery,
		_nCBmC_startangle, _nCBmC_anglelen;
	private static float[] nextCoordsBeatmapCircle(int lineIndex, int lineCount) {
		boolean newMeasure = (lineCount != _nCBm_last_lineCount) || (lineIndex <= _nCBm_last_lineIndex);
		_nCBm_last_lineIndex = lineIndex;
		_nCBm_last_lineCount = lineCount;
		
		double tapspace_w = Tools.screen_w - Tools.button_w;//1;
		double tapspace_h = Tools.screen_h - Tools.button_h * 2;//1;
		
		if (newMeasure) {
			double tapspace_min = (tapspace_w < tapspace_h) ? tapspace_w : tapspace_h;
			_nCBmC_radius = tapspace_min/2;
			_nCBmC_startangle = (rand.nextInt(8)/8.0)*TwoPi;
			_nCBmC_anglelen = .375*TwoPi * (rand.nextBoolean() ? 1 : -1); //3/8 circle

			float[] crange = cosRange(_nCBmC_startangle, _nCBmC_startangle+_nCBmC_anglelen);
			float[] srange = sinRange(_nCBmC_startangle, _nCBmC_startangle+_nCBmC_anglelen);
			double boundbox_xmin = crange[0]*_nCBmC_radius;
			double boundbox_xmax = crange[1]*_nCBmC_radius;
			double boundbox_ymin = srange[0]*_nCBmC_radius;
			double boundbox_ymax = srange[1]*_nCBmC_radius;
			double boundbox_w = boundbox_xmax - boundbox_xmin;
			double boundbox_h = boundbox_ymax - boundbox_ymin;
			//System.out.println(String.format("%f %f %f %f", crange[0], crange[1], srange[0], srange[1]));
			
			_nCBmC_centerx = rand.nextFloat()*(tapspace_w-boundbox_w) - boundbox_xmin;
			_nCBmC_centery = rand.nextFloat()*(tapspace_h-boundbox_h) - boundbox_ymin;
		}
		
		double measure_pos = lineIndex*1.0/(lineCount-1);

		double angle = _nCBmC_startangle + measure_pos*_nCBmC_anglelen;
		return new float[] {
				(float)((_nCBmC_centerx + _nCBmC_radius*Math.cos(angle))/tapspace_w),
				(float)((_nCBmC_centery + _nCBmC_radius*Math.sin(angle))/tapspace_h),
		};
	}
	*/
	
	private static float[][] _nCBmBz_control = null;
	private static float[] nextCoordsBeatmapBezier(int lineIndex, int lineCount, int controlPoints) {
		boolean newMeasure = (lineCount != _nCBm_last_lineCount) ||
			(lineIndex <= _nCBm_last_lineIndex);
		_nCBm_last_lineIndex = lineIndex;
		_nCBm_last_lineCount = lineCount;
		
		if (_nCBmBz_control == null) {
			_nCBmBz_control = new float[controlPoints][2];
		}
		
		if (newMeasure) {
			for (int i=0; i<controlPoints; i++)
				for (int j=0; j<2; j++)
					_nCBmBz_control[i][j] = -100;
			
			for (int i=0; i<controlPoints; i++) {
				_nCBmBz_control[i] = genFartherThan(_nCBmBz_control, .48f);
			}
		}
		
		float measure_pos = lineIndex*1.0f/(lineCount-1);
		
		float[][] control = _nCBmBz_control;
		while (control.length > 1) {
			int len = control.length - 1;
			float[][] newcontrol = new float[len][2];
			for (int i=0; i<len; i++) {
				newcontrol[i][0] = control[i][0]*(1-measure_pos) + control[i+1][0]*measure_pos;
				newcontrol[i][1] = control[i][1]*(1-measure_pos) + control[i+1][1]*measure_pos;
			}
			control = newcontrol;
		}
		
		return control[0];
	}
	
	private static float[] nextCoordsNoBeatmap() {
		float[] coords = genFartherThan(prevCoords, MIN_DIST);
		lastCoordIndex++;
		lastCoordIndex %= prevCoords.length;
		prevCoords[lastCoordIndex] = coords;
		return coords;
	}
	
	private static float[] genFartherThan(float[][] prev, float min_dist) {
		float[] coords = new float[2];
		boolean tooClose = false;
		do {
			coords[0] = ToolsRandomizer.rand.nextFloat(); // Completely random
			coords[1] = ToolsRandomizer.rand.nextFloat(); // Completely random
			tooClose = false;
			for (int i = 0; i < prev.length; i++) {
				if (Math.abs(prev[i][0] - coords[0]) < min_dist &&
					Math.abs(prev[i][1] - coords[1]) < min_dist) {
					tooClose = true;
				}
				/*
				// Actually calculate the real distance
				float diffX = prev[i][0] - coords[0];
				float diffY = prev[i][1] - coords[1];
				double diff = Math.sqrt(diffX * diffX + diffY * diffY); // a^2 + b^2 = c^2
				if (diff < min_dist) {
					tooClose = true;
				}
				*/
				
			}
		} while (tooClose);
		return coords;
	}
	
	public static float[] nextCoords(int lineIndex, int lineCount) {
		float[] coords;
		if (!Tools.randomizeBeatmap) {
			//coords = nextCoordsBeatmapCircle(lineIndex, lineCount);
			coords = nextCoordsBeatmapBezier(lineIndex, lineCount, 3);
		} else {
			coords = nextCoordsNoBeatmap();
		}
		return coords;
	}
}
