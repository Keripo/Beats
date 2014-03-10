package com.beatsportable.beats;

import com.beatsportable.beats.GUIScore.AccuracyTypes;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;

public class GUIFallingHold extends GUIFallingObject {
	
	// can't hold more than an hour, sorry.
	private static final int MAX_HOLD_MS = 1000*60*60;
	
	//time period (in ms) during which you can re-hit a sleeping hold
	private static final int MS_WAIT_TIME_NG = 500;
	
	/*time period (in ms) for which, if you're still holding this far from the end,
	 * it's going to be an ok even if you release afterward */
	private static final int MS_END_THRESHOLD = 500;
	
	public enum FallingMode {
		INACTIVE,  // before you press the hold
		ACTIVE,    // while holding
		SLEEPING,  // immediately after releasing hold
		DEAD       // after hold cannot be pressed any more
	}
	
	private int original_starttime;
	private int time_to_ng; // time at which the hold will be marked dead
	private boolean clicked;
	private FallingMode mode = FallingMode.INACTIVE;
	private boolean hasBeenHit = false;
	private boolean ok_override = false; // set to true if we're close enough to the end to count an ok
	public boolean hasStartedVibrating = false;
	
	GUIFallingHold(DataNote n) {
		super(n, n.fraction, n.column, n.time, n.time + MAX_HOLD_MS);
		original_starttime = n.time;
		clicked = false;
	}

	//public int fraction() { return fraction; }
		
	private static final int holdimg_h = Tools.button_h * 2;
	
	public void draw(GUIDrawingArea drawarea, Canvas canvas) {
		//first-press arrow and release arrow boxes
		int start_rect_top = drawarea.timeToY(start_time);
		//int start_rect_bottom = start_rect_top + Tools.button_h;
		int end_rect_top = drawarea.timeToY(end_time);
		int end_rect_bottom = end_rect_top + Tools.button_h;
		int rect_left = drawarea.pitchToX(pitch);
		int rect_right = rect_left + Tools.button_w;
		
		int win_h = canvas.getHeight();
		//int win_w = canvas.getWidth();
		
		boolean fallingDown = start_rect_top > end_rect_top;
		
		int hold_rect_top, hold_rect_bottom; //bounding box for hold rectangle
		int hold_draw_start, hold_draw_end, hold_draw_add; // parameters to draw loop
		if (fallingDown) { //arrows falling down
		    hold_rect_top = end_rect_top + Tools.button_h/2;
		    hold_rect_bottom = start_rect_top + Tools.button_h/2;
		    
		    hold_draw_start = drawarea.timeToY(original_starttime) + Tools.button_h / 2 - holdimg_h;
		    hold_draw_end = ((end_rect_top > 0) ? end_rect_top : 0) - holdimg_h;
		    hold_draw_add = -holdimg_h;
		} else { //arrows rising up
		    hold_rect_top = start_rect_top + Tools.button_h/2;
		    hold_rect_bottom = end_rect_top + Tools.button_h/2;

		    hold_draw_start = drawarea.timeToY(original_starttime) + Tools.button_h / 2;
		    hold_draw_end = ((end_rect_bottom < win_h) ? end_rect_bottom : win_h);
		    hold_draw_add = holdimg_h;
		}
		
		//body
		
		Path path = new Path();
		//TODO diagonal clip paths for up/down arrows (straight for left/right)
		path.addRect(rect_left, hold_rect_top, rect_right, hold_rect_bottom, Direction.CCW);
		
		canvas.clipPath(path, Op.INTERSECT);
		
		//need to swap comparison direction based on motion direction, hence xor
		for (int y = hold_draw_start; (y <= hold_draw_end) ^ fallingDown; y += hold_draw_add) {
			canvas.drawBitmap(
					drawarea.getBitmap(
							holdRsrc(mode, false),
							Tools.button_w, holdimg_h
							), 
					rect_left, y, null);
		}
		
		drawarea.setClip_arrowSpace(canvas);
		
		//end arrow (top)
		canvas.drawBitmap(
				drawarea.getBitmap(
						holdRsrc(mode, true),
						Tools.button_w, Tools.button_h
						),
				rect_left, end_rect_top, null
				);
		
		//start arrow (bottom)
		canvas.drawBitmap(
				drawarea.getBitmap(
						GUINoteImage.rsrc(pitch, fraction, clicked),
						Tools.button_w, Tools.button_h
						), 
				rect_left, start_rect_top, null
				);
		
		//debug
		
		/*Paint p = new Paint();
		switch (mode) {
		case INACTIVE: p.setARGB(127, 0, 255, 0); break;
		case ACTIVE: p.setARGB(200, 0, 255, 0); break;
		case DEAD: p.setARGB(127, 127, 127, 127); break;
		}
		
		canvas.drawRect(new Rect(rect_left, end_rect_top, rect_right, start_rect_bottom), p);*/
	}
		
	@Override
	public void animate(int currentTime) {
		if (mode == FallingMode.SLEEPING) {
			if (currentTime < time_to_ng)
				start_time = currentTime;
			else
				mode = FallingMode.DEAD;
		}
	}
	
	@Override
	public AccuracyTypes onFirstFrame(int currentTime, GUIScore score) {
		AccuracyTypes acc = AccuracyTypes.X_IGNORE_ABOVE;
		int timediff = start_time - currentTime;
		
		if (score.withinHitRange(timediff) && mode != FallingMode.DEAD) {
			mode = FallingMode.ACTIVE;
			acc = AccuracyTypes.X_IGNORE_PASS; //TODO this is a hack, fix it. 
		}
		
		if (!hasBeenHit) 
			acc = score.newEventHit(timediff);
		
		hasBeenHit = true;
		return acc;
	}

	@Override
	public void onHold(int currentTime, GUIScore score) {
		start_time = currentTime;
	}
	
	@Override
	public AccuracyTypes onLastFrame(int currentTime, GUIScore score, boolean timeout) {
		if (timeout) {
			return score.newEventHoldEnd(true); //F_OK
		} else {
			mode = FallingMode.SLEEPING;
			
			time_to_ng = currentTime + MS_WAIT_TIME_NG;
			if (time_to_ng > end_time) time_to_ng = end_time;
			
			ok_override |= (currentTime + MS_END_THRESHOLD > end_time); //close enough
			
			return AccuracyTypes.X_IGNORE_ABOVE;
		}
	}
	
	@Override
	public AccuracyTypes onMiss(GUIScore score) {
		mode = FallingMode.DEAD;
		if (hasBeenHit) {
			return score.newEventHoldEnd(ok_override); //probably F_NG, maybe F_OK
		} else {
			score.newEventMiss();
			return AccuracyTypes.N_MISS;
		}
	}
	
	public static String holdRsrc(FallingMode _mode, boolean end) {
		if (Tools.gameMode == Tools.REVERSE) {
			if (end) {
				switch (_mode) {
					case ACTIVE:
						return "/step/hold_end_active_up.png";
					case SLEEPING:
						return "/step/hold_end_sleeping_up.png";
					case INACTIVE:
						return "/step/hold_end_inactive_up.png";
					case DEAD:
						return "/step/hold_end_dead_up.png";
				}
			} else {
				switch (_mode) {
					case ACTIVE:
						return "/step/hold_active_up.png";
					case SLEEPING:
						return "/step/hold_sleeping_up.png";
					case INACTIVE:
						return "/step/hold_inactive_up.png";
					case DEAD:
						return "/step/hold_dead_up.png";
				}
			}
		} else {
			if (end) {
				switch (_mode) {
					case ACTIVE:
						return "/step/hold_end_active_down.png";
					case SLEEPING:
						return "/step/hold_end_sleeping_down.png";
					case INACTIVE:
						return "/step/hold_end_inactive_down.png";
					case DEAD:
						return "/step/hold_end_dead_down.png";
				}
			} else {
				switch (_mode) {
					case ACTIVE:
						return "/step/hold_active_down.png";
					case SLEEPING:
						return "/step/hold_sleeping_down.png";
					case INACTIVE:
						return "/step/hold_inactive_down.png";
					case DEAD:
						return "/step/hold_dead_down.png";
				}
			}
		}
		return null;
	}
}
