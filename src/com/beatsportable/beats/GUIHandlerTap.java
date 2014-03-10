package com.beatsportable.beats;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.beatsportable.beats.DataNote.NoteType;
import com.beatsportable.beats.GUIScore.*;

public class GUIHandlerTap extends GUIHandler {
	
	private int[] button_x;
	private int[] hitbox_left, hitbox_right, hitbox_top, hitbox_bottom;
	private GUIFallingObject[] object_held; //the object held under button p, or null
	private GUITapArrow[] arrows;
	
	private int tapboxFading;
	private double tapboxOverlap;
	private Rect[] debugTapboxRects;
	private Paint debugTapboxPaint;
	
	private boolean dark;
	private boolean debugTapbox;
	
	private enum TapboxLayout {
		D_PAD,
		CURVED,
		CURVED_R,
		STANDARD,
		FULLSCREEN
	};
	private TapboxLayout tapboxLayout;
	
	private TapboxLayout getTapboxLayout(String s) {
		if (s.equals("d-pad")) {
			return TapboxLayout.D_PAD;
		} else if (s.equals("curved")) {
			return TapboxLayout.CURVED;
		} else if (s.equals("curved-r")) {
			return TapboxLayout.CURVED_R;
		} else if (s.equals("standard")) {
			return TapboxLayout.STANDARD;
		} else if (s.equals("fullscreen")) {
			return TapboxLayout.FULLSCREEN;
		} else {
			return TapboxLayout.STANDARD;
		}
	}
	
	public GUIHandlerTap() {
		super();
		
		dark = Tools.getBooleanSetting(R.string.dark, R.string.darkDefault);
		debugTapbox = Tools.getBooleanSetting(R.string.debugTapbox, R.string.debugTapboxDefault);
		tapboxFading = Integer.valueOf(
				Tools.getSetting(R.string.tapboxFading, R.string.tapboxFadingDefault));
		if (Tools.getBooleanSetting(R.string.jumps, R.string.jumpsDefault)) {
			tapboxOverlap = Double.valueOf(
					Tools.getSetting(R.string.tapboxOverlap, R.string.tapboxOverlapDefault));
		} else {
			tapboxOverlap = 0;
		}
		tapboxLayout = getTapboxLayout(
				Tools.getSetting(R.string.tapboxLayout, R.string.tapboxLayoutDefault));
		
		button_x = new int[Tools.PITCHES];
		arrows = new GUITapArrow[Tools.PITCHES];
		object_held = new GUIFallingObject[Tools.PITCHES];
		hitbox_left = new int[Tools.PITCHES];
		hitbox_right = new int[Tools.PITCHES];
		hitbox_top = new int[Tools.PITCHES];
		hitbox_bottom = new int[Tools.PITCHES];
		
		debugTapboxPaint = new Paint();
		debugTapboxPaint.setARGB(32, 0, 0, 0);
		debugTapboxRects = new Rect[Tools.PITCHES];
		
		//setupXY(); // Call in GUIGame
	}
	
	@Override
	public void setupXY() {
		super.setupXY();
		
		int screen_q = Tools.screen_w / 4;
		
		int hitbox_w = screen_q;
		int hitbox_h = Tools.button_h * 2;
		int overlap_x = (int)(hitbox_w * tapboxOverlap);
		int hitbox_y =
			Tools.screen_h
			- (Tools.button_h + tapboxYOffset)
			- (Tools.button_h + (hitbox_h - Tools.button_h) / 2
			);
		
		for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
			arrows[pitch] = new GUITapArrow(pitch, this);
			button_x[pitch] = (screen_q * pitch) + (screen_q / 2) - (Tools.button_w / 2);
			
			int hitbox_x = hitbox_w * pitch;
			hitbox_left[pitch] = hitbox_x - overlap_x;
			hitbox_right[pitch] = hitbox_x + hitbox_w + overlap_x;
			if (tapboxLayout.equals(TapboxLayout.D_PAD)) {
				// Its only 4 times anyway - no need to pull this outside the if statement
				int pad_w = (int)(Tools.button_w * (1.5 + tapboxOverlap));
				int pad_h = (int)(Tools.button_h * (1.0 + tapboxOverlap));
				int pad_x = Tools.screen_w / 2;
				int pad_y = Tools.screen_h - (Tools.button_h + tapboxYOffset) - (Tools.button_h / 2);
				int pad_overlap_x = (int)(pad_w * tapboxOverlap);
				int pad_overlap_y = (int)(pad_h * tapboxOverlap);
				switch(pitch) {
					case 0: // left
						hitbox_left[pitch] = pad_x - (Tools.button_w / 2) - pad_w - pad_overlap_x;
						hitbox_right[pitch] = hitbox_left[pitch] + pad_w + pad_overlap_x * 2;
						hitbox_top[pitch] = pad_y - pad_h / 2 - pad_overlap_y;
						hitbox_bottom[pitch] = hitbox_top[pitch] + pad_h + pad_overlap_y * 2;
						break;
					case 1: // down
						hitbox_left[pitch] = pad_x - (pad_w / 2) - pad_overlap_x;
						hitbox_right[pitch] = hitbox_left[pitch] + pad_w + pad_overlap_x * 2;
						hitbox_top[pitch] = pad_y + (Tools.button_h / 3) - pad_overlap_y;
						hitbox_bottom[pitch] = hitbox_top[pitch] + pad_h + pad_overlap_y * 2;
						break;
					case 2: // up
						hitbox_left[pitch] = pad_x - (pad_w / 2) - pad_overlap_x;
						hitbox_right[pitch] = hitbox_left[pitch] + pad_w + pad_overlap_x * 2;
						hitbox_top[pitch] = pad_y - (Tools.button_h / 3) - pad_h - pad_overlap_y;
						hitbox_bottom[pitch] = hitbox_top[pitch] + pad_h + pad_overlap_y * 2;
						break;
					case 3: // right
						hitbox_left[pitch] = pad_x + (Tools.button_w / 2) - pad_overlap_x;
						hitbox_right[pitch] = hitbox_left[pitch] + pad_w + pad_overlap_x * 2;
						hitbox_top[pitch] = pad_y - pad_h / 2 - pad_overlap_y;
						hitbox_bottom[pitch] = hitbox_top[pitch] + pad_h + pad_overlap_y * 2;
						break;
				}
			} else if (tapboxLayout.equals(TapboxLayout.STANDARD)) {
				hitbox_top[pitch] = hitbox_y;
				hitbox_bottom[pitch] = hitbox_y + hitbox_h;
			} else if (tapboxLayout.equals(TapboxLayout.CURVED)) {
				hitbox_top[pitch] = hitbox_y + Tools.button_h / 4;
				hitbox_bottom[pitch] = hitbox_y + hitbox_h + Tools.button_h / 4;
				switch (pitch) {
					case 0: case 3:
						hitbox_top[pitch] += Tools.button_h / 2;
						hitbox_bottom[pitch] += Tools.button_h / 2;
						break;
					case 1: case 2:
						hitbox_top[pitch] -= Tools.button_h / 2;
						hitbox_bottom[pitch] -= Tools.button_h / 2;
						break;
				}
			} else if (tapboxLayout.equals(TapboxLayout.CURVED_R)) {
				hitbox_top[pitch] = hitbox_y - Tools.button_h / 4;
				hitbox_bottom[pitch] = hitbox_y + hitbox_h - Tools.button_h / 4;
				switch (pitch) {
					case 0: case 3:
						hitbox_top[pitch] -= Tools.button_h / 2;
						hitbox_bottom[pitch] -= Tools.button_h / 2;
						break;
					case 1: case 2:
						hitbox_top[pitch] += Tools.button_h / 2;
						hitbox_bottom[pitch] += Tools.button_h / 2;
						break;
				}
			} else if (tapboxLayout.equals(TapboxLayout.FULLSCREEN)) {
				hitbox_top[pitch] = 0;
				hitbox_bottom[pitch] = Tools.screen_h;
			}
			
			debugTapboxRects[pitch] = new Rect(
					hitbox_left[pitch],
					hitbox_top[pitch],
					hitbox_right[pitch],
					hitbox_bottom[pitch]
					);
		}
	}
	
	@Override
	protected void setDone() {
		super.setDone();
		onTouch_Up_All();
	}
	@Override
	public int pitchToX(int pitch) {
		return button_x[pitch];
	}
	
	//if autoplay is on: the frame at which each pitch key is unpressed
	private int[] autoPlayKeyUnpress = new int[Tools.PITCHES];
	@Override
	public void nextFrame() throws Exception {
		
		//todo get rid of the *_frames vars
		msg_frames++;
		combo_frames++;
		if (scoreboard_frames>=0) {
			scoreboard_frames++;
		}
		
		int currentTime = GUIGame.currentTime;
		int onScreenTime, offScreenTime;
		if (Tools.gameMode == Tools.STANDARD) { // scroll up
			onScreenTime = yToTime(Tools.screen_h);
			offScreenTime = yToTime(-Tools.button_h);
		} else {
			onScreenTime = yToTime(-Tools.button_h);
			offScreenTime = yToTime(Tools.screen_h);
		}
		
		// TODO
		// I think logic flow of events should be changed so onTouchDown_One
		// combo updating code is here instead of in reaction to the touch event
		
		if (!done) {
			
			// check pressed pitches
			for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
				GUIFallingObject o = object_held[pitch];
				if (o == null || o.missed) continue;
				
				o.onHold(currentTime, score);
				
				if (currentTime > o.end_time || o.start_time == o.end_time) {
					AccuracyTypes acc = o.onLastFrame(currentTime, score, true);
					
					if (acc != AccuracyTypes.X_IGNORE_ABOVE) {
						setMessage(acc.name, acc.r, acc.g, acc.b);
					}
					if (acc == AccuracyTypes.F_NG || acc == AccuracyTypes.F_OK) {
						// TODO - vibrate
						v.endHold();
					}
					
					fallingobjects.popColumn(pitch);
					object_held[pitch] = null;
				}
			}
			
			//set miss statuses
			for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
				while (true) {
					GUIFallingObject o = fallingobjects.peekColumn(pitch);
					if (o == null) break;
					
					int timediff = o.start_time - currentTime;
					if (timediff > missThreshold && o.end_time >= offScreenTime) break; // Not a miss
					
					fallingobjects.missColumn(pitch);
					
					AccuracyTypes acc = o.onMiss(score);
					
					if (acc == AccuracyTypes.F_NG || acc == AccuracyTypes.F_OK) {
						// TODO - vibrate
						v.endHold();
					} else if (acc == AccuracyTypes.N_MISS) {
						// TODO - vibrate
						v.vibrateMiss();
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
				}
			}
		}
		
		fallingobj_arr = fallingobjects.fetchAll(fallingobj_arr);
		for (GUIFallingObject o: fallingobj_arr) {
			if (o == null) break;
			o.animate(currentTime);
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
		
		if (autoPlay) {
			for (int i = 0; i < Tools.PITCHES; i++) {
				if (currentTime >= autoPlayKeyUnpress[i]) {
					onTouch_Up_One(i);
				}
			}
			int toPress = 0;
			fallingobj_arr = fallingobjects.fetchAll(fallingobj_arr);
			for (GUIFallingObject o: fallingobj_arr) {
				if (o == null) break;						
				int pitch = o.pitch;
				int timediff = o.start_time - currentTime;
				//int ydiff = button_y - timeToY(o.time())

				if (timediff <= Tools.AUTOPLAY_WINDOW) {
					toPress |= (1<<pitch);
					if (o.n.noteType.equals(NoteType.HOLD_START)) {
						autoPlayKeyUnpress[pitch] = o.end_time; // hold starts continue on
					} else {
						autoPlayKeyUnpress[pitch] = o.end_time + score.accuracyLevel;
					}
				}
			}
			onTouch_Down(toPress);
			//onTouch_Up(toPress); //uncomment this to better see where the button gets hit
		}
		
	}
	
	@Override
	public void drawTapboxes(Canvas canvas) {
		float tapboxBitmap_x = 0;
		float tapboxBitmap_y;
		int tapboxBitmap_w = Tools.screen_w;
		Bitmap tapboxBitmap = null;
		Bitmap tapboxBitmapUnscaled = null;
		
		if (tapboxFading > 0) {
			if (tapboxLayout.equals(TapboxLayout.D_PAD)) {
				tapboxBitmapUnscaled =
					BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_d_pad);
			} else if (tapboxLayout.equals(TapboxLayout.STANDARD)) {
				if (Tools.gameMode == Tools.REVERSE) {
					tapboxBitmapUnscaled =
						BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_standard_down);
				} else {
					tapboxBitmapUnscaled =
						BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_standard_up);
				}
			} else if (tapboxLayout.equals(TapboxLayout.CURVED)) {
				if (Tools.gameMode == Tools.REVERSE) {
					tapboxBitmapUnscaled =
						BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_curved_down);
				} else {
					tapboxBitmapUnscaled =
						BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_curved_up);
				}
			} else if (tapboxLayout.equals(TapboxLayout.CURVED_R)) {
				if (Tools.gameMode == Tools.REVERSE) {
					tapboxBitmapUnscaled =
						BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_curved_r_down);
				} else {
					tapboxBitmapUnscaled =
						BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_curved_r_up);
				}
			} else if (tapboxLayout.equals(TapboxLayout.FULLSCREEN)) {
				tapboxBitmapUnscaled =
					BitmapFactory.decodeResource(Tools.res, R.drawable.tapbox_fullscreen);
			}
			
			tapboxBitmap_y = // Very top
				Tools.screen_h
				- Tools.button_h * 3
				- tapboxYOffset;
			
			if (tapboxLayout.equals(TapboxLayout.D_PAD)) {				
				tapboxBitmap_x = (float)(Tools.screen_w / 2 - Tools.button_w * 2.5);
				tapboxBitmap_w = Tools.button_w * 5; // 1/2 + 1/2 + 3/2 + 3/2
			} else {
				if (tapboxLayout.equals(TapboxLayout.CURVED)) {
					tapboxBitmap_y += Tools.button_h / 4; // slight downward shift to look nice with tap arrows
				} else if (tapboxLayout.equals(TapboxLayout.CURVED_R)) {
					tapboxBitmap_y -= Tools.button_h / 4; // slight upward shift to look nice with tap arrows
				}
			}
			
			if (tapboxBitmapUnscaled != null) {	
				tapboxBitmap = Bitmap.createScaledBitmap(
						tapboxBitmapUnscaled,
						tapboxBitmap_w, 
						Tools.button_h * 3,
						true);
				tapboxBitmapUnscaled.recycle();
				tapboxBitmapUnscaled = null; // GC
			} else {
				tapboxBitmap = null;
			}
			
			if (tapboxBitmap != null) {
				Paint tapboxAlpha = new Paint();
				tapboxAlpha.setAlpha(Tools.MAX_OPA * tapboxFading / 100);
				tapboxAlpha.setDither(true); // Needed to avoid grey boxes
				canvas.drawBitmap(tapboxBitmap, tapboxBitmap_x, tapboxBitmap_y, tapboxAlpha);
				tapboxBitmap.recycle();
				tapboxBitmap = null;
			}
		}
		
		if (debugTapbox) {
			for (Rect tapbox : debugTapboxRects) {
				canvas.drawRect(tapbox, debugTapboxPaint);
			}
		}
	}
	
	@Override
	public void drawFallingObjects(Canvas canvas, GUIDrawingArea drawarea) {		
		drawarea.setClip_arrowSpace(canvas);
		fallingobj_arr = fallingobjects.fetchAll(fallingobj_arr);
		for (GUIFallingObject o: fallingobj_arr) {
			if (o == null) break;
			o.draw(drawarea, canvas);
		}
		drawarea.setClip_screen(canvas);
				
		for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
			if (!dark || arrows[pitch].clicked) {
				arrows[pitch].draw(drawarea, canvas);
			}
		}
	}
	
	// returns a bitmap of selected pitches
	// Called by finger touches
	@Override
	public int onTouch_Down(float x, float y) {
		int selected = 0;
		for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
			if (y >= hitbox_top[pitch] &&
				y <= hitbox_bottom[pitch] &&
				x >= hitbox_left[pitch] &&
				x <= hitbox_right[pitch]) {
				onTouch_Down_One(pitch);
				selected |= (1 << pitch);
			}
		}
		return selected;
	}
	
	// Called by autoPlay
	private boolean onTouch_Down(int pitches) {
		for (int xpitch = 0; xpitch < Tools.PITCHES; xpitch++) {
			if ((pitches & (1<<xpitch)) != 0) {
				onTouch_Down_One(xpitch);
			}
		}
		return true;
	}
	
	@Override
	public boolean onTouch_Down_One(int pitch) {
		//if we're already clicking, can't click again
		//if (button_clicked[pitch] == true) return false;
		if (arrows[pitch].clicked) return false;
		//note: if this breaks on real hardware, try object_held[pitch] != null
		
		//button_clicked[pitch] = true;
		arrows[pitch].clicked = true;
		
		GUIFallingObject o = fallingobjects.peekColumn(pitch);
		if (o == null) return false;
		
		int currentTime = GUIGame.currentTime;
		int timediff = o.start_time - currentTime;
		
		AccuracyTypes acc = o.onFirstFrame(currentTime, score);
		
		if (acc != AccuracyTypes.X_IGNORE_ABOVE) {
			updateCombo();
	
			if (debugTime) {
				setMessage(acc.name + " " + timediff, acc.r, acc.g, acc.b);
			} else {
				setMessage(acc.name, acc.r, acc.g, acc.b);
			}
			
			object_held[pitch] = o; //todo: what if there's already a held object?	
			
			if (o.n.noteType.equals(DataNote.NoteType.HOLD_START)) {
				// TODO - vibrate
				try {
					GUIFallingHold ho = (GUIFallingHold)o;
					if (ho.hasStartedVibrating) {
						v.vibrateHold(true);
					} else {
						ho.hasStartedVibrating = true;
						v.vibrateHold(false);
					}
				} catch (Exception e) { // Recast exception?
					v.vibrateHold(false);
				}
			} else {
				// TODO - vibrate
				v.vibrateTap();
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouch_Up(int pitches) {
		for (int xpitch = 0; xpitch < Tools.PITCHES; xpitch++) {
			if ((pitches & (1<<xpitch)) != 0)
				onTouch_Up_One(xpitch);
		}
		return true;
	}
	@Override
	public boolean onTouch_Up_One(int pitch) {
		//button_clicked[pitch] = false;
		arrows[pitch].clicked = false;
		if (object_held[pitch] != null) {
			AccuracyTypes acc = object_held[pitch].onLastFrame(GUIGame.currentTime, score, false);
			if (acc != AccuracyTypes.X_IGNORE_ABOVE) {
				setMessage(acc.name, acc.r, acc.g, acc.b);
			}
			object_held[pitch] = null;
		}
		return true;
	}
	private void onTouch_Up_All() {
		for (int i = 0; i < Tools.PITCHES; i++) {
			onTouch_Up_One(i);
		}
	}
	
}
