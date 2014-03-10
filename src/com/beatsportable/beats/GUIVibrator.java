package com.beatsportable.beats;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;

import com.immersion.uhl.Launcher;

public class GUIVibrator {
	
	private Vibrator v;
	private Launcher vm;
	private int vibrateMiss;
	private int vibrateTap;
	private int vibrateHold;
	private boolean vibrateTouchSense;
	private int holdsCount;
	
	// This is called in GUIHandler's constructor, which is called by GUIGame's onCreate 
	public GUIVibrator() {
		try {
			vibrateMiss = Integer.valueOf(Tools.getSetting(R.string.vibrateMiss, R.string.vibrateMissDefault));
			vibrateTap = Integer.valueOf(Tools.getSetting(R.string.vibrateTap, R.string.vibrateTapDefault));
			vibrateHold = Integer.valueOf(Tools.getSetting(R.string.vibrateHold, R.string.vibrateHoldDefault));
			vibrateTouchSense =
				Tools.getBooleanSetting(R.string.vibrateTouchSense, R.string.vibrateTouchSenseDefault)
				&& Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE;
			holdsCount = 0;
			if (vibrateTouchSense) {
				v = null;
				vm = new Launcher(Tools.c);
			} else {
				v = (Vibrator)Tools.c.getSystemService(Context.VIBRATOR_SERVICE);
				vm = null;
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.init", e, "SDK version: " + Build.VERSION.SDK_INT);
		}
	}
	
	// Call this in GUIHandler's releaseVibrator(), which is called by GUIGame's onDestroy
	public void release() {
		try {
			pause();
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.release", e, "");
		}
	}
	
	public void endHold() {
		try {
			holdsCount--;
			if (holdsCount <= 0) {
				holdsCount = 0;
				if (v != null) {
					v.cancel();
				} else if (vm != null) {
					vm.stop();
				}
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.endHold", e, "");
		}
	}
	
	public void pause() {
		try {
			holdsCount = 0;
			if (v != null) {
				v.cancel();
			} else if (vm != null) {
				vm.stop();
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.pause", e, "");
		}
	}
	
	public void vibrateTap() {
		try {
			switch(vibrateTap) {
				case 0:
					break;
				case 1:
					if (v != null) {
						v.vibrate(15);
					} else if (vm != null) {
						vm.play(Launcher.SHARP_CLICK_33);
					}
					break;
				case 2:
					if (v != null) {
						v.vibrate(30);
					} else if (vm != null) {
						vm.play(Launcher.SHARP_CLICK_66);
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.vibrateTap", e, "");
		}
	}
	
	public void vibrateHold(boolean hasStartedVibrating) {
		try {
			if (!hasStartedVibrating) {
				holdsCount++;
			}
			switch (vibrateHold) {
				case 0:
					break;
				case 1:
					if (v != null) {
						v.vibrate(10000);
					} else if (vm != null) {
						vm.play(Launcher.ENGINE1_33);
					}
					break;
				case 2:
					if (v != null) {
						v.vibrate(10000);
					} else if (vm != null) {
						vm.play(Launcher.ENGINE1_66);
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.vibrateHold", e, "");
		}
	}
	
	public void vibrateMiss() {
		try {
			switch (vibrateMiss) {
				case 0:
					break;
				case 1:
					if (v != null) {
						v.vibrate(25);
					} else if (vm != null) {
						vm.play(Launcher.BUMP_66);
					}
					break;
				case 2:
					if (v != null) {
						v.vibrate(50);
					} else if (vm != null) {
						vm.play(Launcher.BUMP_100);
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.vibrateMiss", e, "");
		}
	}
}
