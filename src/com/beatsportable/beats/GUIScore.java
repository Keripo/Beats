package com.beatsportable.beats;

public class GUIScore {
	
	// Overall
	public Scoreboard scoreboard;
	public int score;
	public int highScore;
	public boolean newHighScore;
	// Health
	public int healthMax; // max health
	public int health; // current health
	private int healthPenalty; // health loss per N_MISS
	private int healthGain; // health gain per N_MARVELOUS, N_PERFECT = healthGain/2
	public boolean gameOver;
	public boolean isPaused;
	// Combo
	public int comboCount; // current combo count
	public int comboBest; // highest combo count
	public int noteCount; // Notes count
	public int holdCount; // hold count
	private boolean showPercent; // Show show percentage instead of letter grade
	
	// Accuracy
	// TODO - Holds, freezes, mines, rolls, etc. not implemented, only taps
	public enum AccuracyTypes {
		N_BEST_COMBO	(Tools.getString(R.string.Accuracy_best_combo),	64, 128, 255),	// light blue
		N_MARVELOUS		(Tools.getString(R.string.Accuracy_marvelous),	255, 190, 0),	// gold
		N_PERFECT		(Tools.getString(R.string.Accuracy_perfect),	255, 255, 0),	// yellow
		N_GREAT			(Tools.getString(R.string.Accuracy_great),		0, 255, 0),		// green
		N_GOOD			(Tools.getString(R.string.Accuracy_good),		0, 128, 255),	// blue
		N_ALMOST		(Tools.getString(R.string.Accuracy_almost),		255, 0, 255),	// magenta
		N_MISS			(Tools.getString(R.string.Accuracy_miss),		255, 0, 0),		// red
		F_OK			(Tools.getString(R.string.Accuracy_ok),			255, 128, 0),	// orange
		F_NG			(Tools.getString(R.string.Accuracy_no_good),	164, 64, 164),	// purple
		X_IGNORE_ABOVE	("",											0, 0, 0),
		X_IGNORE_BELOW	("",											0, 0, 0),
		X_IGNORE_PASS	("",											0, 0, 0); //hit the note, but doesn't count for scoring purposes (hack)
		 //TODO: figure out semantics for various ignores
		
		public String name;
		public int r, g, b;
		AccuracyTypes(String name, int r, int g, int b) {
			this.name = name;
			this.r = r;
			this.g = g;
			this.b = b;
		}
		public String toString() {
			return this.name;
		}
		//public int getR() {
			//return this.r;
		//}
		//public int getG() {
			//return this.g;
		//}
		//public int getB() {
			//return this.b;
		//}
	}
	public int[] accuracyChart; // individual accuracy scores
	public int accuracyLevel; // higher value = easier rating
	
	// Constructor
	public GUIScore() {
		// Default start settings
		this.score = 0;
		this.healthMax = Integer.valueOf(
				Tools.getSetting(R.string.healthMax, R.string.healthMaxDefault));
		this.health = this.healthMax / 2;
		this.healthPenalty = Integer.valueOf(
				Tools.getSetting(R.string.healthPenalty, R.string.healthPenaltyDefault));
		this.healthGain = this.healthPenalty / 4;
		this.comboCount = 0;
		this.comboBest = 0;
		this.noteCount = 0;
		this.accuracyChart = new int[11]; // size of AccuracyTypes
		this.accuracyLevel = Integer.valueOf(
				Tools.getSetting(R.string.accuracyLevel, R.string.accuracyLevelDefault)); // in ms
		if (this.accuracyLevel == 0) this.accuracyLevel = 1; // Just in case, to prevent divide-by-zero
		if (Tools.gameMode == Tools.OSU_MOD) this.accuracyLevel *= 1.5f; // more lenient with osu! Mod
		this.gameOver = false;
		this.isPaused = false;
		this.showPercent = Tools.getBooleanSetting(R.string.showPercent, R.string.showPercentDefault);
	}
	
	public void loadHighScore(String md5) {
		if (Tools.getBooleanSetting(R.string.resetHighScores, R.string.resetHighScoresDefault)) {
			Scoreboard.clearScores();
			Tools.putSetting(R.string.resetHighScores, "0");
		}
		this.scoreboard = new Scoreboard(md5);
		highScore = scoreboard.getScore();
		newHighScore = false;
	}
	
	public void updateHighScore(boolean autoPlay) {
		if (!autoPlay && score > highScore) {
			scoreboard.setScore(score);
			newHighScore = true;
		}
	}
	
	// Calculate letter score
	// Based on the DDR Max 2 system: http://aaronin.jp/taren/scoring/ss7.html
	/*
	Each song has a certain number of "Dance Points" assigned to it. For regular arrows, this is 2 per arrow. For freeze arrows, it is 6 per arrow. When you add this all up, you get the maximum number of possible "Dance Points".

	Your "Dance Points" are calculated as follows:

	  A "Perfect" is worth 2 points
	  A "Great" is worth 1 points
	  A "Good" is worth 0 points
	  A "Boo" will subtract 4 points
	  A "Miss" will subtract 8 points
	  An "OK" (Successful Freeze step) will add 6 points
	  A "NG" (Unsuccessful Freeze step) is worth 0 points

	Based on the percentage of your total "Dance Points" to the maximum possible number, the following rank is assigned:

	  100% - AAA
	  93 % - AA
	  80 % - A
	  65 % - B
	  45 % - C
	  Less - D
	  Fail - E
	*/
	public boolean scoreGood = false;
	//public boolean isScoreGood() {
		//return scoreGood;
	//}
	public String getLetterScore() {
		if (gameOver) {
			if (this.showPercent) {
				return "0%";
			} else {
				return Tools.getString(R.string.Letter_E);
			}
		} else {
			int maxScore = noteCount * 2 + holdCount * 6;
			if (maxScore == 0) maxScore = 1; // Safeguard to prevent dividing by 0...
			int currentScore = 0;
			
			currentScore += this.accuracyChart[AccuracyTypes.N_MARVELOUS.ordinal()] * 2;
			currentScore += this.accuracyChart[AccuracyTypes.N_PERFECT.ordinal()] * 2;
			currentScore += this.accuracyChart[AccuracyTypes.N_GREAT.ordinal()];
			// + 0 for GOODs
			currentScore -= this.accuracyChart[AccuracyTypes.N_ALMOST.ordinal()] * 4;
			currentScore -= this.accuracyChart[AccuracyTypes.N_MISS.ordinal()] * 8;
			
			currentScore += this.accuracyChart[AccuracyTypes.F_OK.ordinal()] * 6;
			// + 0 for NGs
			
			currentScore *= 100; // Make it out of 100%
			int percent = currentScore / maxScore;
			if (percent >= 80) scoreGood = true; // A or higher
			
			if (this.showPercent) {
				return percent + "%";
			} else {
				if (percent < 45) {
					return Tools.getString(R.string.Letter_D);
				} else if (percent < 65) {
					return Tools.getString(R.string.Letter_C);
				} else if (percent < 80) {
					return Tools.getString(R.string.Letter_B);
				} else if (percent < 93) {
					return Tools.getString(R.string.Letter_A);
				} else if (percent < 100) {
					return Tools.getString(R.string.Letter_AA);
				} else if (percent == 100) {
					return Tools.getString(R.string.Letter_AAA);
				} else {
					return Tools.getString(R.string.Letter_unknown) + "/" + percent + "%";
				}
			}
		}
	}
	
	// Access
	public void setHealthMax(int healthMax) {
		this.healthMax = healthMax;
		this.health = healthMax / 2;
	}
	//public void setAccuracyLevel(int accuracyLevel) {
		//this.accuracyLevel = accuracyLevel;
	//}
	public void setHealthPenalty(int healthPenalty) {
		this.healthPenalty = healthPenalty;
		this.healthGain = this.healthPenalty / 4;
	}
	
	// Combos
	//public int getComboCount() {
		//return this.comboCount;
	//}
	//public int getComboBest() {
		//return this.comboBest;
	//}
	private void updateComboBest() {
		if (this.comboCount > this.comboBest) {
			this.comboBest = this.comboCount;
			this.accuracyChart[AccuracyTypes.N_BEST_COMBO.ordinal()] = this.comboBest;
		}
	}
	//public void increaseNoteCount() {
		//noteCount++;
	//}
	//public int getNoteCount() {
		//total # of hittable notes (including hold starts, not including mines)
		//return noteCount;
	//}
	
	//public void increaseHoldCount() {
		//holdCount++;
	//}
	//public int getHoldCount() {
		//return holdCount;
	//}
	 
	private final int IGNORE_ABOVE_THRESHOLD = 6;  
	private final int IGNORE_BELOW_THRESHOLD = -9;
	
	// If a note goes this level, call newEventMiss
	public int getMissThreshold() {
		return accuracyLevel * (IGNORE_BELOW_THRESHOLD - 1) / 2;
	}
	
	public void newEventMiss() {
		if (!gameOver && !isPaused) {
			this.health -= this.healthPenalty;
			this.accuracyChart[AccuracyTypes.N_MISS.ordinal()]++;
			this.comboCount = 0;
			if (this.health < 0) {
				this.gameOver = true;
			}
		}
	}
	//public boolean isGameOver() {
		//return this.gameOver;
	//}
	
	// Returns whether the time difference is within range of a HIT; i.e. if
	// newEventHit will return something other than X_IGNORE_*
	public boolean withinHitRange(int timeDifference) {
		int accuracy = 2 * timeDifference / accuracyLevel;
		if (gameOver) return false;
		return (accuracy >= IGNORE_BELOW_THRESHOLD) && (accuracy <= IGNORE_ABOVE_THRESHOLD);
	}
	
	// Triggered upon arrow press
	// Returns the accuracy type, use array AccuracyTypeNames for text
	public AccuracyTypes newEventHit(int timeDifference) {
		int accuracy = 2 * timeDifference / accuracyLevel;
		if (gameOver || isPaused) {
			return AccuracyTypes.X_IGNORE_ABOVE;
		}
		// Not GameOver
		if (accuracy < IGNORE_BELOW_THRESHOLD) {
			return AccuracyTypes.X_IGNORE_BELOW; // Underneath the arrows
		} else if (accuracy > IGNORE_ABOVE_THRESHOLD && Tools.gameMode != Tools.OSU_MOD) {
			return AccuracyTypes.X_IGNORE_ABOVE; // Above the GOOD range
		} else {
			int scoreIncrease;
			if (timeDifference > 0) {
				scoreIncrease = (int)((200 - timeDifference) * (400d - timeDifference) / 200d) * 10; 
			} else {
				scoreIncrease = (int)((200 + timeDifference) * (400d + timeDifference) / 200d) * 10;
			}
			if (scoreIncrease < 0) scoreIncrease = 0;
			switch (accuracy) { // arbitrary multiplying factors
				case 0: case -1:
					this.accuracyChart[AccuracyTypes.N_MARVELOUS.ordinal()]++;
					this.health += this.healthGain;
					if (this.health > this.healthMax) {
						this.health = this.healthMax;
					}
					this.score += scoreIncrease;
					this.comboCount++;
					updateComboBest();
					return AccuracyTypes.N_MARVELOUS;
				case 1: case 2: case -2: case -3:
					this.accuracyChart[AccuracyTypes.N_PERFECT.ordinal()]++;
					this.health += this.healthGain / 2;
					if (this.health > this.healthMax) {
						this.health = this.healthMax;
					}
					this.score += scoreIncrease;
					this.comboCount++;
					updateComboBest();
					return AccuracyTypes.N_PERFECT;
				case 3: case 4: case -4: case -5:
					this.accuracyChart[AccuracyTypes.N_GREAT.ordinal()]++;
					this.score += scoreIncrease;
					this.comboCount++;
					updateComboBest();
					return AccuracyTypes.N_GREAT;
				case 5: case 6: case -6: case -7:
					this.accuracyChart[AccuracyTypes.N_GOOD.ordinal()]++;
					this.score += scoreIncrease;
					this.comboCount = 0;
					return AccuracyTypes.N_GOOD;
				case -8: case -9: default:
					this.accuracyChart[AccuracyTypes.N_ALMOST.ordinal()]++;
					this.score += scoreIncrease;
					this.comboCount = 0;
					return AccuracyTypes.N_ALMOST;
			}
		}
	}
	
	public AccuracyTypes newEventHoldEnd(boolean ok) {
		//Called when a hold is over.
		//ok = true if the hold was completed (OK), false if player released early (NG)
		if (gameOver || isPaused) {
			return AccuracyTypes.X_IGNORE_ABOVE;
		} else {
			AccuracyTypes acc = ok ? AccuracyTypes.F_OK : AccuracyTypes.F_NG;
			this.accuracyChart[acc.ordinal()]++;
			if (acc == AccuracyTypes.F_OK) {
				this.score += 4000; // score of a marvelous with 0 timediff
				this.health += this.healthGain;
				if (this.health > this.healthMax) {
					this.health = this.healthMax;
				}
			}
			return acc;
		}
	}
	
	// Score
	//public int getHealth() {
		//return this.health;
	//}
	//public int getHealthMax() {
		//return this.healthMax;
	//}
	public float getHealthPercent() {
		return (float)this.health / (float)this.healthMax;
	}
	//public int getScore() {
		//return this.score;
	//}
	// Accuracy Chart - use AccuracyType.____.ordinal() for the array index
	//public int[] getAccuracyChart() {
		//return this.accuracyChart;
	//}
	
}
