package com.beatsportable.beats;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public abstract class GUIDrawingArea {
	/** Stores information required to convert times to pixel locations, and draw stuff. */

	
	//public abstract Canvas getCanvas();
	
	public abstract int timeToY(float time); //converts a time to a y coordinate
	public abstract int pitchToX(int pitch); //converts a pitch to an x coordinate
	//given coordinates are the (0,0) coord of a 64x64 block
	
	public abstract Bitmap getBitmap(String rsrc, int width, int height); //retreive a bitmap by resource id
	public abstract void clearBitmaps(); //clear bitmap cache. grr garbage collector
	
	//replace the current clipping region with the entire screen
	public abstract void setClip_screen(Canvas canvas);
	//replace the current clipping region with the area available for arrows
	public abstract void setClip_arrowSpace(Canvas canvas);
}
