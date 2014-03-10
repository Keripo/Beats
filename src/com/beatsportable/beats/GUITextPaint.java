package com.beatsportable.beats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class GUITextPaint {

	private Paint textPaint = new Paint();
	private Paint strokePaint = null;
	
	private Typeface family = Typeface.DEFAULT;
	private boolean bold = false, italic = false;
	private Typeface t = null;
	
	private Typeface getTypeface() {
		if (t == null) t = Typeface.create(
				family,
				bold ? (italic ? Typeface.BOLD_ITALIC : Typeface.BOLD) :
					   (italic ? Typeface.ITALIC : Typeface.NORMAL));
		return t;
	}
	
	public GUITextPaint(int size) {
		textPaint.setTextSize(size);
		textPaint.setAntiAlias(true);
	}
	
	public GUITextPaint bold() {
		bold = true;
		return this;
	}
	public GUITextPaint italic() {
		italic = true;
		return this;
	}
	public GUITextPaint serif() {
		family = Typeface.SERIF;
		return this;
	}
	public GUITextPaint sansSerif() {
		family = Typeface.SANS_SERIF;
		return this;
	}
	public GUITextPaint monospace() {
		family = Typeface.MONOSPACE;
		return this;
	}
	
	
	public GUITextPaint alignLeft() {
		textPaint.setTextAlign(Paint.Align.LEFT);
		return this;
	}
	public GUITextPaint alignCenter() {
		textPaint.setTextAlign(Paint.Align.CENTER);
		return this;
	}
	public GUITextPaint alignRight() {
		textPaint.setTextAlign(Paint.Align.RIGHT);
		return this;
	}
	public GUITextPaint ARGB(int a, int r, int g, int b) {
		textPaint.setARGB(a, r, g, b);
		return this;
	}
	public GUITextPaint strokeARGB(int a, int r, int g, int b) {
		if (strokePaint != null)
			strokePaint.setARGB(a, r, g, b);
		return this;
	}
	
	
	public GUITextPaint strokeWidth(int w) {
		strokePaint = new Paint(textPaint);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeWidth(w);
		return this;
	}
	
	public void draw(Canvas canvas, String msg, float x, float y) {
		if (t == null) {
			textPaint.setTypeface(getTypeface());
			if (strokePaint != null)
				strokePaint.setTypeface(getTypeface());
		}
		if (strokePaint != null)
			canvas.drawText(msg, x, y, strokePaint);
		canvas.drawText(msg, x, y, textPaint);
	}
	
}
