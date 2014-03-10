package com.beatsportable.beats;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class GUIFallingOsuBeat extends GUIFallingObject{
	
	public static final float OSU_TIME_DIFF = 1.2f;
	public static final int DELAY_DEFAULT = 1300;
	public static int delay;
	public static int r, w, h;
	public static float timeDiffMax, timeDiffMin;
	public static double tapboxSize;
	public static final float TAPBOX_PERCENT_SIZE = 1.0f;
	public static int NUM_TEXT_HEIGHT;
	public static int RING_STROKE_WIDTH;
	
	public int opa;
	public float x, y;
	public boolean tapped;
	public float cx, cy;
	public float hitbox_left, hitbox_top, hitbox_right, hitbox_bottom;
	public Rect hitbox;
	
	protected Paint circlePaint, fadePaint, linePaint;
	protected GUITextPaint numPaint;
	protected String num;
	protected float[] coords;
	protected GUIFallingOsuBeat lastBeat;
	protected GUIFallingOsuSliderEnd slider;
	protected float dx, dy;
	protected double dr;

	public GUIFallingOsuBeat(DataNote n) {
		super(n, n.fraction, n.column, n.time, n.time);
		
		tapped = false;
		circlePaint = new Paint();
		circlePaint.setColor(GUINoteImage.osu_circle(fraction));
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeWidth(RING_STROKE_WIDTH);
		circlePaint.setAntiAlias(true);
		fadePaint = new Paint();
		fadePaint.setAlpha(0);
		linePaint = new Paint();
		linePaint.setColor(Color.WHITE);
		//linePaint.setStyle(Paint.Style.STROKE);
		//linePaint.setStrokeWidth(RING_STROKE_WIDTH * 2);
		linePaint.setAlpha(0);
		numPaint = new GUITextPaint(NUM_TEXT_HEIGHT).alignCenter().sansSerif().bold().strokeWidth(4);
		
		coords = n.coords;
		num = Integer.toString(n.num);
		setupXY();
	}
	
	public float[] scale(float[] coords) {
		float[] scaled = new float[2];
		if (Tools.screen_w > Tools.screen_h) { // landscape
			scaled[0] = (w * coords[0]) + Tools.button_w / 2;
			scaled[1] = (h * coords[1]) + Tools.button_h / 2 + Tools.health_bar_h;
		} else { // portrait
			scaled[0] = Tools.screen_w - ((int)(w * coords[1]) + Tools.button_w / 2);
			scaled[1] = (h * coords[0]) + Tools.button_h / 2 + Tools.health_bar_h;
		}
		return scaled;
	}
	
	public void setupXY() {
		if (Tools.randomizeBeatmap) { // Ignore shift values
			cx = (int)(w * coords[0]) + Tools.button_w / 2;
			cy = (int)(h * coords[1]) + Tools.button_h / 2 + Tools.health_bar_h;
		} else if (DataParser.osuData) {
			float[] scaled = scale(coords);
			cx = scaled[0];
			cy = scaled[1];
		} else {
			cx = (int)(r + w * coords[0] + r * coords[2] + Tools.button_w / 2);
			cy = (int)(r + h * coords[1] + r * coords[3] + Tools.button_h / 2 + Tools.health_bar_h);
		}
		
		x = (float)(cx - (Tools.button_w / 2));
		y = (float)(cy - (Tools.button_h / 2));
		hitbox_left = cx - (int)(Tools.button_w * tapboxSize);
		hitbox_right = cx + (int)(Tools.button_w * tapboxSize);
		hitbox_top = cy - (int)(Tools.button_h * tapboxSize);
		hitbox_bottom = cy + (int)(Tools.button_h * tapboxSize);
	}
	
	public void setSlider(GUIFallingOsuSliderEnd slider) {
		this.slider = slider;
		circlePaint.setColor(Color.WHITE);
	}
	
	public void setLast(GUIFallingOsuBeat lastBeat) {
		this.lastBeat = lastBeat;
		dx = (cx - lastBeat.cx);
		dy = (cy - lastBeat.cy);
		dr = Math.sqrt(dx * dx + dy * dy);
	}
	
	public void draw(GUIDrawingArea drawarea, Canvas canvas) {
		if (missed) return;
		
		float timeDiffPercent;
		timeDiffPercent = (float)(start_time - GUIGame.currentTime) / delay;
		opa = (int)(Tools.MAX_OPA * (1 - timeDiffPercent));
		opa = (opa < 0) ? 0 : (opa > Tools.MAX_OPA) ? Tools.MAX_OPA : opa;
		//opa = (opa < 0) ? 0 : opa;
		
		if (lastBeat != null) {
			if (dr > Tools.button_w) {
				int line_opa = lastBeat.opa / 2 + opa * 2;
				line_opa = (line_opa < 0) ? 0 : (line_opa > Tools.MAX_OPA) ? Tools.MAX_OPA : line_opa;
				linePaint.setAlpha(line_opa);
				
				float rx = (float)(dx * (Tools.button_w / 2) / dr);
				float ry = (float)(dy * (Tools.button_h / 2) / dr);
				
				float beatDiffPercent = (float)(start_time - GUIGame.currentTime) / (float)(start_time - lastBeat.start_time); 
				if (beatDiffPercent > 1) beatDiffPercent = 1f;
				if (beatDiffPercent < 0) beatDiffPercent = 0f;
				
				float x1 = cx - (dx * beatDiffPercent) + rx;
				float y1 = cy - (dy * beatDiffPercent) + ry;
				float x2 = cx - rx;
				float y2 = cy - ry;
				if (((cx >= lastBeat.cx && x2 >= x1) || (cx <= lastBeat.cx && x2 <= x1)) &&
					((cy >= lastBeat.cy && y2 >= y1) || (cy <= lastBeat.cy && y2 <= y1))
					) {
					int pr = RING_STROKE_WIDTH;//(Tools.button_w / 10);
					int points = Math.abs((int)dr / pr);
					if (points > 0) {
						float pdx = dx / points;
						float pdy = dy / points;
						for (int i = 0; i < points; i += 4) {
							float px1 = lastBeat.cx + i * pdx;
							float py1 = lastBeat.cy + i * pdy;
							if (((x1 <= px1 && px1 <= x2) || (x1 >= px1 && px1 >= x2)) &&
								((y1 <= py1 && py1 <= y2) || (y1 >= py1 && py1 >= y2))
								) {
								//canvas.drawLine(px1, py1, px2, py2, linePaint);
								canvas.drawCircle(px1, py1, pr, linePaint);
							}
						}
					}
					//canvas.drawLine(x1, y1, x2, y2, linePaint);
				}
			}
		}
		
		if (timeDiffPercent < timeDiffMax && timeDiffPercent > timeDiffMin) {
			if (opa > 0) {
				fadePaint.setAlpha(opa);
				canvas.drawBitmap(
						drawarea.getBitmap(
								GUINoteImage.osu_beat(fraction),
								Tools.button_w,
								Tools.button_h),
						x, y, fadePaint
						);
				numPaint.ARGB(opa, 255, 255, 255); // white
				numPaint.strokeARGB(opa, 0, 0, 0); // black
				numPaint.draw(canvas, num, cx, cy + NUM_TEXT_HEIGHT/3);
			}
			int radius = (int)(Tools.button_h * 0.95) / 2;
			if (timeDiffPercent >= 0) {
				radius *= (1 + timeDiffPercent);
			}
			canvas.drawCircle(cx, cy, radius, circlePaint);
		}
	}	
}
