package com.beatsportable.beats;

import java.util.ArrayList;

import com.beatsportable.beats.GUIScore.AccuracyTypes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.PathMeasure;

public class GUIFallingOsuSliderEnd extends GUIFallingOsuBeat {
	
	public static double BLINK_SPEED = 2d;
	private static final double BLINK_DEFAULT = 300d;
	private static final int MS_END_THRESHOLD = 500;
	
	private int focusRadius;
	private Paint focusPaint;
	private Path curvePath;
	private ArrayList<Float> curvePoints;
	private Paint curvePaint;
	private Paint curvePaintOutline;
	
	public GUIFallingOsuSliderEnd(DataNote n) {
		super(n);
		focusRadius = (int)(Tools.button_w * 1.3 / 2);
		focusPaint = new Paint();
		focusPaint.setAntiAlias(true);
		focusPaint.setAlpha(0);
		focusPaint.setColor(Color.WHITE);
		focusPaint.setStyle(Paint.Style.STROKE);
		focusPaint.setStrokeWidth(RING_STROKE_WIDTH);
		
		curvePath = new Path();
		curvePaint = new Paint();
		curvePaint.setAntiAlias(true);
		curvePaint.setColor(lighten(GUINoteImage.osu_circle(fraction)));
		curvePaint.setStyle(Paint.Style.STROKE);
		//curvePaint.setStrokeWidth(Tools.button_w * 2 / 3);
		curvePaint.setStrokeWidth(Tools.button_w - RING_STROKE_WIDTH * 4);
		curvePaint.setStrokeCap(Cap.ROUND);
		curvePaintOutline = new Paint();
		curvePaintOutline.setAntiAlias(true);
		curvePaintOutline.setColor(Color.WHITE);
		curvePaintOutline.setStyle(Paint.Style.STROKE);
		//curvePaintOutline.setStrokeWidth(Tools.button_w * 2 / 3 + RING_STROKE_WIDTH * 2);
		curvePaintOutline.setStrokeWidth(Tools.button_w - RING_STROKE_WIDTH * 2);
		curvePaintOutline.setStrokeCap(Cap.ROUND);
	}
	
	private int lighten(int color) {
		int r = Color.red(color) + 128;
		int g = Color.green(color) + 128;
		int b = Color.blue(color) + 128;
		r = (r > 255) ? 255 : r;
		g = (g > 255) ? 255 : g;
		b = (b > 255) ? 255 : b;
		return Color.rgb(r, g, b);
	}
	
	public void setupXY() {
		super.setupXY();
		setupPath();
	}
	
	public void setLast(GUIFallingOsuBeat lastBeat) {
		super.setLast(lastBeat);
		setupPath();
	}
	
	public void setupPath() {
		curvePoints = n.curvePoints;
		if (lastBeat != null) {
			curvePath.reset();
			lastBeat.setupXY();
			curvePath.moveTo(lastBeat.cx, lastBeat.cy);
			/*
			for (int i = 0; i < curvePoints.size(); i += 2) {
				float[] nextCoords = {curvePoints.get(i), curvePoints.get(i + 1)};
				float[] nextCoordsScaled = scale(nextCoords);
				curvePath.lineTo(nextCoordsScaled[0], nextCoordsScaled[1]);
			}
			*/
			//float[] midPrev = null;
			if (curvePoints != null) {
				if (curvePoints.size() == 4) { // Arc
					float[] midCoords = new float[2];
					float[] endCoords = new float[2];
					midCoords[0] = curvePoints.get(0);
					midCoords[1] = curvePoints.get(1);
					endCoords[0] = curvePoints.get(2);
					endCoords[1] = curvePoints.get(3);
					float[] midCoordsScaled = scale(midCoords);
					float[] endCoordsScaled = scale(endCoords);
					curvePath.quadTo(
							midCoordsScaled[0],
							midCoordsScaled[1],
							endCoordsScaled[0],
							endCoordsScaled[1]
							);
				} else if (curvePoints.size() == 6) {
					float[] firstCoords = new float[2];
					float[] midCoords = new float[2];
					float[] endCoords = new float[2];
					firstCoords[0] = curvePoints.get(0);
					firstCoords[1] = curvePoints.get(1);
					midCoords[0] = curvePoints.get(2);
					midCoords[1] = curvePoints.get(3);
					endCoords[0] = curvePoints.get(4);
					endCoords[1] = curvePoints.get(5);
					float[] firstCoordsScaled = scale(firstCoords);
					float[] midCoordsScaled = scale(midCoords);
					float[] endCoordsScaled = scale(endCoords);
					curvePath.cubicTo(
							firstCoordsScaled[0],
							firstCoordsScaled[1],
							midCoordsScaled[0],
							midCoordsScaled[1],
							endCoordsScaled[0],
							endCoordsScaled[1]
							);
				} else {
					float[] prevCoords = new float[2];
					float[] nextCoords = new float[2];
					float[] nextCoordsScaled = new float[2];
					float[] midNext = new float[2];
					prevCoords[0] = lastBeat.cx;
					prevCoords[1] = lastBeat.cy;
					for (int i = 0; i < curvePoints.size(); i += 2) {
						nextCoords[0] = curvePoints.get(i);
						nextCoords[1] = curvePoints.get(i + 1);
						nextCoordsScaled = scale(nextCoords);
						midNext[0] = (prevCoords[0] + nextCoordsScaled[0]) / 2; 
						midNext[1] = (prevCoords[1] + nextCoordsScaled[1]) / 2;
						
						curvePath.quadTo(prevCoords[0], prevCoords[1], midNext[0], midNext[1]);
						
						prevCoords[0] = nextCoordsScaled[0];
						prevCoords[1] = nextCoordsScaled[1];
					}
					curvePath.lineTo(nextCoordsScaled[0], nextCoordsScaled[1]);
				}
			} else {
				curvePath.lineTo(cx, cy);
			}
		}
	}
	
	public void draw(GUIDrawingArea drawarea, Canvas canvas) {
		if (missed) return;
		
		int timeDiff;
		//float timeDiffPercent;
		timeDiff = start_time - GUIGame.currentTime;
		//timeDiffPercent = (float)(timeDiff) / delay;
		
		if (lastBeat != null && !lastBeat.missed && (lastBeat.tapped || lastBeat.opa > 0)) {			
			float beatDiffPercent = (float)(timeDiff) / (float)(start_time - lastBeat.start_time); 
			if (beatDiffPercent > 1) beatDiffPercent = 1f;
			if (beatDiffPercent < 0) beatDiffPercent = 0f;
			
			if (lastBeat.tapped) {
				opa = Tools.MAX_OPA;
			} else {
				opa = lastBeat.opa;
			}
			curvePaint.setAlpha(opa);
			curvePaintOutline.setAlpha(opa);
			canvas.drawPath(curvePath, curvePaintOutline);
			canvas.drawPath(curvePath, curvePaint);
			
			fadePaint.setAlpha(opa);
			canvas.drawBitmap(
					drawarea.getBitmap(
							GUINoteImage.osu_beat(fraction),
							Tools.button_w,
							Tools.button_h),
					lastBeat.x, lastBeat.y, fadePaint
					);
			
			numPaint.ARGB(opa, 255, 255, 255); // white
			numPaint.strokeARGB(opa, 0, 0, 0); // black
			//numPaint.draw(canvas, num, cx, cy + NUM_TEXT_HEIGHT/3);
			numPaint.draw(canvas, num, lastBeat.cx, lastBeat.cy + NUM_TEXT_HEIGHT/3);
			
			canvas.drawBitmap(
					drawarea.getBitmap(
							GUINoteImage.osu_beat(fraction),
							Tools.button_w,
							Tools.button_h),
					x, y, fadePaint
					);
			
			if (GUIGame.currentTime > lastBeat.end_time && lastBeat.tapped) {
				focusPaint.setAlpha((int)(Math.abs(Math.sin(timeDiff / (BLINK_DEFAULT / BLINK_SPEED) ) * Tools.MAX_OPA))); // Fade in and out!
			} else {
				focusPaint.setAlpha(0);
			}
			
			/*
			if (timeDiffPercent < timeDiffMax && timeDiffPercent > timeDiffMin) {
				int radius = (int)(Tools.button_h * 0.95) / 2;
				if (timeDiffPercent >= 0) {
					radius *= (1 + timeDiffPercent);
				}
				canvas.drawCircle(cx, cy, radius, circlePaint);
			}
			*/
			
			float[] coords = new float[2];
			PathMeasure measure = new PathMeasure(curvePath, false);
			float length = measure.getLength();
			measure.getPosTan(length - beatDiffPercent * length, coords, null);
			canvas.drawCircle(coords[0], coords[1], focusRadius, focusPaint);
		}
	}
	
	@Override
	public AccuracyTypes onFirstFrame(int currentTime, GUIScore score) {
		int timediff = Math.abs(start_time - currentTime);
		if (timediff < MS_END_THRESHOLD) {
			return score.newEventHoldEnd(true);
		} else {
			return score.newEventHoldEnd(false);
		}
		//return score.newEventHoldEnd(score.withinHitRange(timediff));
	}
	
	@Override
	public AccuracyTypes onMiss(GUIScore score) {
		score.newEventMiss();
		return AccuracyTypes.N_MISS;
	}
}
