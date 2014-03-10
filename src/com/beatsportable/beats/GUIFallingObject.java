package com.beatsportable.beats;

import com.beatsportable.beats.GUIScore.AccuracyTypes;
import android.graphics.Canvas;

public abstract class GUIFallingObject {
	public int pitch; //0..4 - column
	public int start_time; //time at which the note starts (i.e. the time to hit it)
	public int end_time; //time at which the note ends (usu. same as start_time, except w/ holds)
	public int fraction;
	public DataNote n; // for debugging purposes
	public boolean missed;
	GUIFallingObject(DataNote n, int fraction, int pitch, int start_time, int end_time) {
		this.n = n;
		this.fraction = fraction;
		this.pitch = pitch;
		this.start_time = start_time;
		this.end_time = end_time;
		this.missed = false;
	}
	//public DataNote getDataNote() { return n; }
	
	//public int pitch() {return pitch;}
	//public int start_time() {return start_time;}
	//public int end_time() {return end_time;}
	//public void end_time(int e) {end_time = e;}
	
	//public boolean wasMissed() {return missed;}
	//public void miss() {missed = true;} //once a note is missed, it cannot be unmissed.
	
	public void animate(int currentTime) {
		/* Called every frame. */
	}
	
	public abstract void draw(GUIDrawingArea drawarea, Canvas canvas);
	
	public AccuracyTypes onFirstFrame(int currentTime, GUIScore score) {
		/* Called on the first frame during which the object MIGHT be pressed.
		   If the object object actually was pressed, update the score.
		   Returns an accuracy if the score was updated, otherwise X_IGNORE_ABOVE. */
		AccuracyTypes acc = score.newEventHit(start_time - currentTime);
		
		//TODO this should never happen. but it might.
		//TODO: Merge X_IGNORE_ABOVE and _BELOW together, since we aren't really using _BELOW
		if (acc == AccuracyTypes.X_IGNORE_BELOW) return AccuracyTypes.X_IGNORE_ABOVE;
		
		return acc;
	}
	
	public void onHold(int currentTime, GUIScore score) {
		/* Called on every subsequent frame during which the object is pressed. */
		
	}
	
	public AccuracyTypes onLastFrame(int currentTime, GUIScore score, boolean timeout) {
		/* Called on the last frame during which the object is pressed.
		 * timeout = false if the object was released early
		 *         = true if the object was not released but instead its end_time has passed
		 * Returns an accuracy if the score was updated, otherwise X_IGNORE_ABOVE. */
		return AccuracyTypes.X_IGNORE_ABOVE;
	}

	public AccuracyTypes onMiss(GUIScore score) {
		/* Called when the object is missed. */
		score.newEventMiss();
		return AccuracyTypes.N_MISS;
	}
}
