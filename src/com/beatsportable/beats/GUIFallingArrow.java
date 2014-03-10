package com.beatsportable.beats;

import android.graphics.Canvas;

public class GUIFallingArrow extends GUIFallingObject{
	
	private int pitch_to_display;

	public GUIFallingArrow(DataNote n) {
		super(n, n.fraction, n.column, n.time, n.time);
		pitch_to_display = n.column;
	}
	
	@Override
	public void animate(int currentTime) {
		if (missed)
			pitch_to_display = (2 + pitch_to_display*3) % 5;
	}
	
	public void draw(GUIDrawingArea drawarea, Canvas canvas) {
		int rect_top = drawarea.timeToY(start_time);
		//int rect_bottom = rect_top + 64;
		int rect_left = drawarea.pitchToX(pitch);
		//int rect_right = rect_left + 64;
		canvas.drawBitmap(
				drawarea.getBitmap(
						GUINoteImage.rsrc(pitch_to_display, fraction, false),
						Tools.button_w, Tools.button_h  
						),
				rect_left, rect_top, null
				);
	}	
}
