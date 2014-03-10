package com.beatsportable.beats;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class GUIFallingOsuFading {
	
	private static final int FADE_DELAY = 200;
	
	private boolean missed;
	private float x, y;
	private int cx, cy;
	private int end_time;
	private int circle_time;
	private Paint fadePaint, circlePaint;
	
	public GUIFallingOsuFading(float x, float y, boolean missed, int end_time) {
		this.x = x;
		this.y = y;
		this.cx = (int)(x + (Tools.button_w / 2));
		this.cy = (int)(y + (Tools.button_h / 2));
		this.missed = missed;
		this.end_time = end_time + FADE_DELAY;
		this.circle_time = end_time;
		
		fadePaint = new Paint();
		fadePaint.setAlpha(Tools.MAX_OPA);
		
		circlePaint = new Paint();
		circlePaint.setColor(Color.WHITE);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeWidth(GUIFallingOsuBeat.RING_STROKE_WIDTH);
		fadePaint.setAlpha(Tools.MAX_OPA);
	}
	
	public boolean draw(GUIDrawingArea drawarea, Canvas canvas) {
		float timeDiffPercent = (float)(GUIGame.currentTime - end_time) / FADE_DELAY;
		int opa = (int)(Tools.MAX_OPA * (1 - timeDiffPercent));
		opa = (opa < 0) ? 0 : (opa > Tools.MAX_OPA) ? Tools.MAX_OPA : opa;
		if (opa > 0) {
			if (!missed) {
				float timeDiffPercentCircle = (float)(GUIGame.currentTime - circle_time) / FADE_DELAY;
				int opac = (int)(Tools.MAX_OPA * (1 - timeDiffPercentCircle));
				opac = (opa < 0) ? 0 : (opac > Tools.MAX_OPA) ? Tools.MAX_OPA : opac;
				if (opac > 0) {
					int radius = Tools.button_h / 2;
					if (timeDiffPercentCircle  >= 0) {
						radius *= (1 + timeDiffPercentCircle * 0.8);
					}
					circlePaint.setAlpha(opac);
					canvas.drawCircle(cx, cy, radius, circlePaint);
				}
			}
			
			fadePaint.setAlpha(opa);
			canvas.drawBitmap(
					drawarea.getBitmap(
							missed ? "/osu/osu_beat_miss.png" : "/osu/osu_beat_hit.png",
							Tools.button_w, Tools.button_h
							),
					x, y, fadePaint
					);
			return true;
		} else {
			return false;
		}
	}
}
