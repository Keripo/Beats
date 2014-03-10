package com.beatsportable.beats;

import android.graphics.Canvas;

public class GUITapArrow {
	public boolean clicked;
	private int pitch;
	private GUIHandler h;

	GUITapArrow(int pitch, GUIHandler h) {
		this.pitch = pitch;
		this.clicked = false;
		this.h = h;
	}
	
	public void draw(GUIDrawingArea drawarea, Canvas canvas) {
		int rect_top = h.button_y;
		int rect_left = drawarea.pitchToX(pitch);
		canvas.drawBitmap(
				drawarea.getBitmap(
						GUINoteImage.rsrc(pitch, 0, clicked),
						Tools.button_w, Tools.button_h
						),
				rect_left, rect_top, null);
	}	
}
