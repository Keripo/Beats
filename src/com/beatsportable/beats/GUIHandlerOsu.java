package com.beatsportable.beats;

import java.util.Iterator;
import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.beatsportable.beats.GUIScore.*;

public class GUIHandlerOsu extends GUIHandler {
	
	//private int noteAppearance;
	private boolean debugTapbox;
	private Paint debugTapboxPaint;
	private volatile LinkedList<GUIFallingOsuFading> fadingobjects; // order does matter
	private GUIFallingOsuSliderEnd slider;
	
	public GUIHandlerOsu() {
		super();
		//noteAppearance = Integer.valueOf(
		//		Tools.getSetting(R.string.noteAppearance, R.string.noteAppearanceDefault));
		debugTapbox = Tools.getBooleanSetting(R.string.debugTapbox, R.string.debugTapboxDefault);
		debugTapboxPaint = new Paint();
		debugTapboxPaint.setARGB(32, 0, 0, 0);
		fadingobjects = new LinkedList<GUIFallingOsuFading>();
		GUIFallingOsuBeat.delay = (int) (
			GUIFallingOsuBeat.DELAY_DEFAULT /
			Double.valueOf(Tools.getSetting(R.string.speedMultiplier, R.string.speedMultiplierDefault))
			);
		
		//setupXY(); // Call in GUIGame
	}
	
	@Override
	public void setupXY() {
		super.setupXY();
		// Setup some boundaries
		if (Tools.randomizeBeatmap || DataParser.osuData) {
			GUIFallingOsuBeat.w = Tools.screen_w - Tools.button_w;
			GUIFallingOsuBeat.h = Tools.screen_h - Tools.button_h - Tools.health_bar_h;
		} else {
			GUIFallingOsuBeat.r = Tools.screen_r;
			GUIFallingOsuBeat.w = Tools.screen_w - Tools.button_w - 2 * Tools.screen_r;
			GUIFallingOsuBeat.h = Tools.screen_h - Tools.button_h - Tools.health_bar_h - 2 * Tools.screen_r;
			// If either is < 0
			GUIFallingOsuBeat.w = (GUIFallingOsuBeat.w < 0) ? 0 : GUIFallingOsuBeat.w;
			GUIFallingOsuBeat.h = (GUIFallingOsuBeat.h < 0) ? 0 : GUIFallingOsuBeat.h;
		}
		GUIFallingOsuBeat.NUM_TEXT_HEIGHT = Tools.scale(30);
		GUIFallingOsuBeat.RING_STROKE_WIDTH = Tools.scale(3);
		GUIFallingOsuSliderEnd.BLINK_SPEED = Double.valueOf(Tools.getSetting(R.string.speedMultiplier, R.string.speedMultiplierDefault));
		if (fallingobjects != null) {
			for (GUIFallingObject fo : fallingobjects) {
				if (fo != null) {
					((GUIFallingOsuBeat)fo).setupXY();
				}
			}
		}
	}
	
	@Override
	public void nextFrame() throws Exception {
		
		//todo get rid of the *_frames vars
		msg_frames++;
		combo_frames++;
		if (scoreboard_frames>=0) {
			scoreboard_frames++;
		}
		
		int onScreenTime, offScreenTime;
		onScreenTime = yToTime(-(Tools.screen_h * 2));
		offScreenTime = yToTime(Tools.screen_h);
		
		GUIFallingOsuBeat o;
		if (!done && (o = (GUIFallingOsuBeat)fallingobjects.peekColumn(0)) != null) {
			int currentTime = GUIGame.currentTime;
			
			//for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
				//GUIFallingOsuBeat o = (GUIFallingOsuBeat)fallingobjects.peekColumn(0);//(pitch);
				//if (o == null) break;
				int timediff = o.start_time - currentTime;
				
				if (autoPlay &&
					timediff <= Tools.AUTOPLAY_WINDOW &&  // 20 ms
					!o.missed
					) {
					o.tapped = true;
				}
				
				AccuracyTypes acc;
				if (o.missed || timediff <= missThreshold || o.end_time < offScreenTime) { // A miss!
					if (!(o instanceof GUIFallingOsuSliderEnd) || ((GUIFallingOsuSliderEnd)o).tapped) { 
						fadingobjects.add(new GUIFallingOsuFading(
								o.x,
								o.y,
								true,
								GUIGame.currentTime
								)
						);
					}
					
					fallingobjects.missColumn(0);//(pitch);
					
					o.missed = true;
					acc = o.onMiss(score);
					// TODO - vibrate
					v.vibrateMiss();
					if (o instanceof GUIFallingOsuSliderEnd) {
						v.endHold();
					} else if (o.slider != null) {
						o.slider.missed = true;
					}
					
					if (debugTime) {
						Tools.toast_long(o.n.toString());
						setMessage(acc.name + " " + timediff,
								acc.r, acc.g, acc.b);
					} else {
						setMessage(acc.name,
								acc.r, acc.g, acc.b);
					}
					updateCombo();
				} else if (o.tapped) {
					acc = o.onFirstFrame(currentTime, score);
					
					//if (acc == AccuracyTypes.X_IGNORE_ABOVE) {
						//acc = AccuracyTypes.N_ALMOST; // I'm harsh ;3
					//}
					setMessage(acc.name, acc.r, acc.g, acc.b);
					if (o.slider == null && acc != AccuracyTypes.F_NG) {
						fadingobjects.add(new GUIFallingOsuFading(
								o.x,
								o.y,
								false,
								GUIGame.currentTime
								)
						);
					}
					fallingobjects.popColumn(0);//(pitch);
					updateCombo();
					// TODO - vibrate
					if (o instanceof GUIFallingOsuSliderEnd) {
						v.endHold();
					} else if (o.slider == null) {
						v.vibrateTap();
					}
				}
			//}
		}
		
		if (score.gameOver && !done) {
			setMessageLong(Tools.getString(R.string.GUIHandler_game_over), 128, 0, 0); // dark red
			if (scoreboard_frames < 0) scoreboard_frames = 0;
			//freezeMessage();
			setDone();
		}
		fallingobjects.update(onScreenTime, offScreenTime, score);
		
		if (fallingobjects.isDone() && !done) {
			setMessageLong(Tools.getString(R.string.GUIHandler_complete), 255, 255, 128); // light yellow
			if (scoreboard_frames < 0) scoreboard_frames = 0;
			//freezeMessage();
			setDone();
		}
	}
	
	@Override
	public synchronized void drawFallingObjects(Canvas canvas, GUIDrawingArea drawarea) {
		
		Iterator<GUIFallingOsuFading> it = fadingobjects.iterator();
		while (it.hasNext()) {
			if (!it.next().draw(drawarea, canvas)) {
				it.remove();
			}
		}
		
		if (debugTapbox) {
			for (GUIFallingObject fo: fallingobjects) {
				GUIFallingOsuBeat o = (GUIFallingOsuBeat)fo;
				if (o != null) {
					if (o.hitbox == null) {
						o.hitbox = new Rect(
								(int)o.hitbox_left,
								(int)o.hitbox_top,
								(int)o.hitbox_right,
								(int)o.hitbox_bottom
								);
					}
					canvas.drawRect(o.hitbox, debugTapboxPaint);
				}
			}
		}
		
		for (GUIFallingObject o: fallingobjects) {
			if (o != null) {
				o.draw(drawarea, canvas);
			}
		}
	}
	
	// returns a bitmap of selected pitches
	// Called by finger touches
	@Override
	public int onTouch_Down(float x, float y) {
		for (GUIFallingObject fo: fallingobjects) {
			GUIFallingOsuBeat o = (GUIFallingOsuBeat)fo;
			if (o.opa > 0 &&
				!(fo instanceof GUIFallingOsuSliderEnd) &&
				x >= o.hitbox_left &&
				x <= o.hitbox_right &&
				y >= o.hitbox_top &&
				y <= o.hitbox_bottom
				) {
				o.tapped = true;
				if (o.slider != null) {
					slider = o.slider;
					v.vibrateHold(true);
				}
				return 1;
			}
		}
		return 0;
	}
	
	@Override
	public int onTouch_Up(float x, float y) {
		if (slider != null) {
			if (slider.opa > 0 && 
				x >= slider.hitbox_left &&
				x <= slider.hitbox_right &&
				y >= slider.hitbox_top &&
				y <= slider.hitbox_bottom
				) {
				slider.tapped = true;
				slider = null;
			}
		}
		return 0;
	}
}
