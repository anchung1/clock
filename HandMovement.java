package com.example.clock;

import org.andengine.entity.Entity;

import android.util.Log;



public class HandMovement {

	private static final String TAG = "HandMovement";
	private static final boolean D = true;

	Entity mHourHand;
	Entity mMinHand;
	
	//these rotations are in range (0 <= rot < 360)
	double mHourRotation;
	double mMinRotation;
	
	//keeps time in 24 hours
	double mTimeInMinutes;
	
	//hour and minute hand discretely move one tick.  There are 60 total ticks on a clock.  
	static final double ROTATIONAL_STEP = 360/60;  
	
	static final int HALFDAY_IN_MINUTES = 12 * 60;
	
	//following are used to calculate am/pm
	static final int AM = 0;
	static final int PM = 1;
	int mDayPhase = 0;
	
	
	public HandMovement (Entity hourHand, Entity minHand) {
		mHourHand = hourHand;
		mMinHand = minHand;
		
		mHourRotation = mHourHand.getRotation();
		mMinRotation = mMinHand.getRotation();
		
		mTimeInMinutes = 0;
		mDayPhase = 0;
		
	}
	
	
	public double getTimeInMinutes() {
		return mTimeInMinutes;
	}
	
	//called on hour hand movement event
	public void hourHandMove(double angle) {
		
		calculateAMPM(angle);
		double steps = Math.floor(angle / ROTATIONAL_STEP);

		//move the hour hand here
		mHourRotation = (steps * ROTATIONAL_STEP);
		//reset the min hand to the top
		mMinRotation = 0;
		calculateTime();
	}
	
	//returns rotational value of hour hand
	public double hourRotation() {
		return mHourRotation;
	}
	

	//after finish moving hour hand, snap min hand into position
	public void relativeMinHandMovement() {
		
		//get total ticks traveled by hour hand
		double hourStep = mHourRotation / ROTATIONAL_STEP;
		//find tick within the hour (5 ticks per hour)
		hourStep %= 5;
		
		//printLog(Double.valueOf(mHourRotation).toString() + " , " + Double.valueOf(hourStep).toString());
		//each step is 12 mins
		mMinRotation = 12 * hourStep * ROTATIONAL_STEP;
		calculateTime();
	}
	
	//called on min hand movement event
	public void minHandMove(double angle) {
		double oldRotation = mMinRotation;
		
		double steps = Math.floor(angle / ROTATIONAL_STEP);
		double newRotation = steps * ROTATIONAL_STEP;
		
		int newQuad = 0, oldQuad = 0;
		
		newQuad = findQuad(newRotation);
		oldQuad = findQuad(oldRotation);


		//clockwise rotation through 12 marker
		if (oldQuad == 4 && newQuad ==1) {
//			printLog("clockwise rotation through 12 ");
			calculateAMPM(mHourRotation+ROTATIONAL_STEP);
			mHourRotation += ROTATIONAL_STEP;
		}
		
		//counter clockwise rotation through 12 marker
		if (oldQuad == 1 && newQuad == 4) {
//			printLog("counter clockwise rotation through 12 " + Double.valueOf(mHourRotation).toString());
			calculateAMPM(mHourRotation-ROTATIONAL_STEP);
			mHourRotation -= ROTATIONAL_STEP;
			if (mHourRotation < 0) {
				mHourRotation = 360 - Math.abs(mHourRotation);
			}
		}

		if (mHourRotation >= 360) {
			mHourRotation -= 360;
		}
		
		mMinRotation = steps * ROTATIONAL_STEP;
		
		calculateTime();
		relativeHourHandMovement();
	}	

	public double minRotation() {
		return mMinRotation;
	}
	
	//calculate time in minutes
	void calculateTime() {
		//one hour is 5 ticks * 6 degrees = 30 degrees
		double hourSteps = Math.floor(mHourRotation/(ROTATIONAL_STEP*5));
		double minSteps = Math.floor(mMinRotation/ROTATIONAL_STEP);
		
		mTimeInMinutes = hourSteps *60 + minSteps;
		if (mDayPhase == PM ) {
			mTimeInMinutes += HALFDAY_IN_MINUTES;
		}
				
	}

	void calculateAMPM(double angle) {
		if (angle < 0) {
			angle = 360 - Math.abs(angle);
		}
		
		if (angle >= 360) {
			angle -= 360;
		}
		
		double oldRotation = mHourRotation;
		double newRotation = angle;
		
		int newQuad = findQuad(newRotation);
		int oldQuad = findQuad(oldRotation);
		
		//check for 12 hour crossing
		if ((oldQuad == 4 && newQuad == 1) ||
				(oldQuad == 1 && newQuad == 4)){
			mDayPhase++;
			mDayPhase %= 2;
		}
	}
	
	//used to track hour hand when min hand is moved
	void relativeHourHandMovement() {
		
		double timeInMinutes = mTimeInMinutes;
		
		//change it to a 12 hour clock
		if (timeInMinutes >= 12 * 60) {
			timeInMinutes -= 12 * 60;
		} 
		
		//each 12 minutes constitutes a step
		double steps = Math.floor(timeInMinutes/12);
		mHourRotation = (steps * ROTATIONAL_STEP);
	}

	int findQuad(double rotation) {
		int ret_val = 0;
	
		
		if (rotation >=0 && rotation < 90) {
			ret_val = 1;
		} else if (rotation >=90 && rotation < 180) {
			ret_val = 2;
		} else if (rotation >=180 && rotation < 270) {
			ret_val = 3;
		} else if (rotation >= 270 && rotation < 360) {
			ret_val = 4;
		}
		
		return ret_val;
	}
	
	void printLog(String s) {
		if (D) Log.d(TAG, s);
	}
}
