package com.beatsportable.beats;

import java.util.HashMap;
import java.util.Map;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GUIListenersNoMulti extends GUIListeners{
	
	public GUIListenersNoMulti(GUIHandler handler) {
		super(handler);
	}
	
	public OnTouchListener getOnTouchListener() {
		return new OnTouchListener() {
			private Map<Integer, Integer> finger2pitch = new HashMap<Integer, Integer>();
			public boolean onTouch(View v, MotionEvent e) {
				if (!v.hasFocus()) v.requestFocus();
				if (autoPlay || h.done || h.score.gameOver) return false;
				int pitch;
				
				// Use old, non-multi-touch-supporting SDK 3/4 methods
				int actionpid = e.getAction() >> 8;
				switch(e.getAction()) {
					case MotionEvent.ACTION_DOWN:
						actionpid = 0;
						pitch = h.onTouch_Down(e.getX(), e.getY());
						if (pitch > 0) finger2pitch.put(actionpid, pitch);
						return pitch > 0;
					case MotionEvent.ACTION_UP:
						h.onTouch_Up(e.getX(actionpid), e.getY(actionpid));
						return h.onTouch_Up(finger2pitch.get(actionpid));
					default:
						return false;
				}
			}
		};
	}
	
}
