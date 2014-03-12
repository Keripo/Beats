package com.beatsportable.beats;

import java.util.HashMap;
import java.util.Map;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GUIListenersMulti extends GUIListeners {

	public GUIListenersMulti(GUIHandler handler) {
		super(handler);
	}
	
	public OnTouchListener getOnTouchListener() {
		return new OnTouchListener() {
			private Map<Integer, Integer> finger2pitch = new HashMap<Integer, Integer>();
			public boolean onTouch(View v, MotionEvent e) {
				if (!v.hasFocus()) v.requestFocus();
				if (autoPlay || h.done || h.score.gameOver) return false;
				int pitch;
				
				// Normal multi-touch
				int actionmask = e.getAction() & MotionEvent.ACTION_MASK;
				@SuppressWarnings("deprecation")
				int actionpid = e.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				switch (actionmask) {
				case MotionEvent.ACTION_DOWN:
					actionpid = 0;
					//fallthru
				case MotionEvent.ACTION_POINTER_DOWN:
					pitch = h.onTouch_Down(e.getX(actionpid), e.getY(actionpid));
					if (pitch > 0) finger2pitch.put(actionpid, pitch);
					return pitch > 0;
				case MotionEvent.ACTION_POINTER_UP:
					h.onTouch_Up(e.getX(actionpid), e.getY(actionpid));
					if (finger2pitch.containsKey(actionpid)) {
						return h.onTouch_Up(finger2pitch.get(actionpid));
					} else {
						return h.onTouch_Up(0xF);
					}
				case MotionEvent.ACTION_UP:
					h.onTouch_Up(e.getX(actionpid), e.getY(actionpid));
					return h.onTouch_Up(0xF);
				default:
					return false;
				}
			}
		};
	}	
}
