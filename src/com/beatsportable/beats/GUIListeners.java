package com.beatsportable.beats;

import android.view.KeyEvent;
import android.view.View.OnTouchListener;

public class GUIListeners {
	//Stores event listener objects, etc.
	
	protected GUIHandler h;
	protected boolean autoPlay;

	public GUIListeners(GUIHandler handler) {
		h = handler;
		autoPlay = Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault);
	}
	
	//protected GUIHandler getHandler() {
		//return h;
	//}
	
	//protected boolean getAutoPlay() {
		//return autoPlay;
	//}
	
	public OnTouchListener getOnTouchListener() {
		return null; // Overwrite this!
	}

	static public int keyCode2Direction(int keyCode) {
		/* interprets a keyCode as a direction
		 * input:   keyCode  - a key code passed to handler           
		 * output: [0 1 2 3] -> [left down up right]; -1 -> unknown
		 */
		switch (keyCode) {
		// WASD, ZX-NM spread, AS-KL spread
		// 12-90 spread, D-Pad
		// Headphone music controller
		case KeyEvent.KEYCODE_A: case KeyEvent.KEYCODE_Z:
		case KeyEvent.KEYCODE_1: case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS: case KeyEvent.KEYCODE_MEDIA_REWIND:
		case KeyEvent.KEYCODE_BUTTON_X:
			return 0;
		case KeyEvent.KEYCODE_S: case KeyEvent.KEYCODE_X:
		case KeyEvent.KEYCODE_2: case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_MEDIA_STOP:
		case KeyEvent.KEYCODE_BUTTON_A:
			return 1;
		case KeyEvent.KEYCODE_W: case KeyEvent.KEYCODE_N: case KeyEvent.KEYCODE_K:
		case KeyEvent.KEYCODE_9: case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		case KeyEvent.KEYCODE_BUTTON_Y:
			return 2;
		case KeyEvent.KEYCODE_D: case KeyEvent.KEYCODE_M: case KeyEvent.KEYCODE_L:
		case KeyEvent.KEYCODE_0: case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_MEDIA_NEXT: case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
		case KeyEvent.KEYCODE_BUTTON_B:
			return 3;
		default:
			return -1;
		}
	}
	
	
	
}
