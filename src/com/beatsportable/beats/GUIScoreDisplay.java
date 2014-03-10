package com.beatsportable.beats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.beatsportable.beats.GUIScore.AccuracyTypes;

public class GUIScoreDisplay {
	//Endgame score display
	
	private Paint filterPaint;
	private GUITextPaint gameOverPaint;
	private GUITextPaint scorePaint;
	private Paint backBoxPaint;
	private GUITextPaint backPaint;
	private String gameOverText;
	private String back1, back2;
	private GUIScore score;
	
	public GUIScoreDisplay(GUIScore score) {
		this.score = score;
		filterPaint = new Paint();
		gameOverPaint = new GUITextPaint(Tools.scale(36)).alignCenter().
			ARGB(Tools.MAX_OPA, 255, 255, 255).strokeWidth(Tools.scale(4)).strokeARGB(Tools.MAX_OPA, 0, 0, 0);
		scorePaint = new GUITextPaint(Tools.scale(22)).alignCenter().ARGB(Tools.MAX_OPA, 255, 255, 255);
		
		backBoxPaint = new Paint();
		backBoxPaint.setStyle(Paint.Style.FILL);
		backPaint = new GUITextPaint(Tools.scale(22)).alignCenter().serif().italic().ARGB(Tools.MAX_OPA, 255, 255, 255);
	}
	
	public void updateStatus(boolean isMaxCombo) {
		if (score.gameOver) {
			gameOverText = Tools.getString(R.string.GUIGame_score_game_over) + score.getLetterScore();
		} else {
			if (isMaxCombo) {
				gameOverText = Tools.getString(R.string.GUIGame_score_max_combo) + score.getLetterScore();
			} else {
				gameOverText = Tools.getString(R.string.GUIGame_score_complete) + score.getLetterScore();
			}
		}
		
		if (score.gameOver) {
			back1 = Tools.getString(R.string.GUIGame_done_fail_1);
			back2 = Tools.getString(R.string.GUIGame_done_fail_2);
		} else if (score.scoreGood) {
			back1 = Tools.getString(R.string.GUIGame_done_good_1);
			back2 = Tools.getString(R.string.GUIGame_done_good_2);	
		} else {
			back1 = Tools.getString(R.string.GUIGame_done_complete_1);
			back2 = Tools.getString(R.string.GUIGame_done_complete_2);
		}
	}
	
	public void draw(Canvas canvas, int time_ms) {
		
		int opa = time_ms * Tools.MAX_OPA / 3000;
		if (opa > Tools.MAX_OPA) opa = Tools.MAX_OPA;
		
		filterPaint.setARGB(opa/2, 0, 0, 0);
		canvas.drawRect(0,0,Tools.screen_w, Tools.screen_h + Tools.scale(70), filterPaint);
		
		gameOverPaint.ARGB(opa, 255,255,255);
		gameOverPaint.strokeARGB(opa,0,0,0);
		gameOverPaint.draw(canvas, gameOverText, Tools.screen_w/2, Tools.scale(34));
		
		//int[] chart = score.accuracyChart;
		AccuracyTypes[] types = AccuracyTypes.values();
		int i=0;
		for (i=0; i<types.length-3; i++) { //-3 to cut off ignores
			AccuracyTypes at = types[i];
			GUITextPaint textPaint = new GUITextPaint(Tools.scale(18)).alignRight().
				ARGB(opa, at.r, at.g, at.b);
			
			Paint bkgndPaint = new Paint();
			bkgndPaint.setARGB(opa, at.r/8, at.g/8, at.b/8);

			//todo unpublicify
			double dif = 20*Math.sin((i*500.0 + time_ms)*Math.PI/4000);
			float x = Tools.screen_w/2 + Tools.scale((float)dif);
			float y = Tools.scale(60 + i*22);
			canvas.drawRect(0, y - Tools.scale(16), Tools.screen_w, y + Tools.scale(2), bkgndPaint);
			textPaint.draw(canvas, at.toString(), x,y);
			textPaint.alignLeft().draw(canvas, "" + score.accuracyChart[at.ordinal()], x + Tools.scale(20), y);
		}
		
		scorePaint.ARGB(opa, 255, 255, 255);
		String scoreText;
		if (score.newHighScore) {
			scoreText = Tools.getString(R.string.GUIGame_highscore);
		} else {
			scoreText = Tools.getString(R.string.GUIGame_score);
		}
		scorePaint.draw(canvas, 
				scoreText + 
				score.score,
				Tools.screen_w/2, Tools.scale(70 + i*22));
		
		backBoxPaint.setARGB(opa/2, 0, 0, 0);
		canvas.drawRect(
				new Rect(0, Tools.screen_h - Tools.scale(130), Tools.screen_w, Tools.screen_h - Tools.scale(60)),
				backBoxPaint);
		
		backPaint.ARGB(opa, 255,255,255);
		
		backPaint.draw(canvas, back1, Tools.screen_w/2, Tools.screen_h - Tools.scale(100));
		backPaint.draw(canvas, back2, Tools.screen_w/2, Tools.screen_h - Tools.scale(75));
	}
	
}
