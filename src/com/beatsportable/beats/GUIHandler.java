package com.beatsportable.beats;

import android.graphics.Canvas;

public class GUIHandler { //TODO is there any reason this isn't abstract?
	
	public boolean done;
	
	protected GUIVibrator v;
	protected double fallpix_per_ms;
	protected int tapboxYOffset;
	protected boolean autoPlay;
	protected boolean debugTime;
	
	public int button_y;
	protected GUIFallingObjectSpace fallingobjects;
	protected GUIFallingObject[] fallingobj_arr;
	
	public GUIScore score;
	protected int missThreshold;
	protected int accuracyLevel;
	
	public static final int MIN_COMBO = 3;
	
	public void loadSongData(DataParser dp) {
		fallingobjects = new GUIFallingObjectSpace(dp);
	}
	
	public int travel_offset_ms() {
		return (int)(((button_y + Tools.button_h))/fallpix_per_ms);
	}
	
	public GUIHandler() {
		fallpix_per_ms = Double.valueOf(
				Tools.getSetting(R.string.speedMultiplier, R.string.speedMultiplierDefault)) / 3;
		if (Tools.gameMode == Tools.STANDARD) fallpix_per_ms = -fallpix_per_ms; // scroll up
		tapboxYOffset = Integer.valueOf(
				Tools.getSetting(R.string.tapboxYOffset, R.string.tapboxYOffsetDefault));
		//tapboxYOffset += Tools.button_h * 0.25; // Shift up even more
		autoPlay = Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault);
		debugTime = Tools.getBooleanSetting(R.string.debugTime, R.string.debugTimeDefault);
		
		this.done = false;
		this.score = new GUIScore();
		this.missThreshold = this.score.getMissThreshold();
		this.accuracyLevel = this.score.accuracyLevel;
		this.v = new GUIVibrator();
	}
	
	public void setupXY() {
		button_y = Tools.screen_h - Tools.button_h - Tools.button_h;
		if (Tools.gameMode == Tools.STANDARD) {
			button_y = Tools.screen_h - button_y - Tools.button_h; // scroll up
		} else {
			button_y -= tapboxYOffset; // positive offset requires subtraction 
		}
		
	}
	
	protected void setDone() {
		done = true;
		fallingobjects.clearArrays();
		score.updateHighScore(autoPlay);
		System.gc();
	}
	
	public void pauseVibrator() {
		v.pause();
	}
	
	public void releaseVibrator() {
		v.release();
	}
	
	public int pitchToX(int pitch) {
		return -1;
	}
	
	public int timeToY(float time) {
		//converts a time to a y coordinate
		return (int)(button_y - (time - GUIGame.currentTime)*fallpix_per_ms);
	}
	
	public int yToTime(float y) {
		//converts a y coordinate to a time
		return (int)((button_y - y)/fallpix_per_ms + GUIGame.currentTime);
	}
	
	public void nextFrame() throws Exception {
	}
	
	public void drawTapboxes(Canvas canvas) {
	}
	
	public void drawFallingObjects(Canvas canvas, GUIDrawingArea drawarea) {
	}
	
	public String msg ="";
	public String combo = "";
	public int msg_frames = 0;
	public int combo_frames = 0;
	public int msg_r, msg_g, msg_b;
	private boolean msg_frozen = false;
	
	public int scoreboard_frames = -1;
	
	public void setMessage(String message, int r, int g, int b) {
		if (msg_frozen) return;
		msg = message;
		msg_frames = 0;
		msg_r = r;
		msg_g = g;
		msg_b = b;
	}
	public void setMessageLong(String message, int r, int g, int b) {
		if (msg_frozen) return;
		setMessage(message, r, g, b);
		msg_frames = -35; // Good enough
	}
	
	public void updateCombo() {
		if (msg_frozen) return;
		if (this.score.comboCount == 0) {
			combo = "";
		} else if (this.score.comboCount > MIN_COMBO) {
			combo = this.score.comboCount + Tools.getString(R.string.GUIHandler_combo);
		}
		combo_frames = 0;
	}

	// returns a bitmap of selected pitches
	// Called by finger touches
	public int onTouch_Down(float x, float y) {
		return -1;
	}
	
	public int onTouch_Up(float x, float y) {
		return -1;
	}
	
	public boolean onTouch_Down_One(int pitch) {
		return false;
	}

	public boolean onTouch_Up(int pitches) {
		return false;
	}
	public boolean onTouch_Up_One(int pitch) {
		return false;
	}
	
}
