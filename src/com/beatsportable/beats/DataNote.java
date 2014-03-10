package com.beatsportable.beats;

import java.util.ArrayList;

public class DataNote implements Comparable<DataNote> {
	
	public enum NoteType {
		NO_NOTE, TAP_NOTE, HOLD_START, HOLD_END, // simple
		ROLL, MINE, LIFT, TAP_SPECIAL // advanced, unsupported
	}
	
	public NoteType noteType;
	public int fraction; // "fraction" of 4 = quarter note = red, 8 = eighth = blue
	public int column; // Left Down Up Right
	public int time; // time in ms of arrow event - 10.5s = 10500
	public float beat;
	// For osu!
	public float[] coords; // {cx, cy, sx, sy}
	public int num;
	public String curveType;
	public ArrayList<Float> curvePoints;
	
	public DataNote(NoteType noteType,
		int fraction, int column, int time, float beat,
		float[] coords, int num) {
		this.noteType = noteType;
		this.fraction = fraction;
		this.column = column;
		this.time = time;
		this.beat = beat;
		this.coords = coords;
		this.num = num;
	}
	
	// For debugging
	public String toString() {
		return String.format(
				"%10s 1/%3d   %s @%7d  b%3e",
				noteType.toString(), fraction, "ABCD".charAt(column), time, beat
				);
	}
	
	public int compareTo(DataNote another) {
		return this.time - another.time;
	}
}
