package com.example.clock;

import org.andengine.entity.Entity;

import android.util.Log;

public class ReadHand {
	
	private static final String TAG = "ReadHand";
	private static final boolean D = true;
	
	HandMovement mHandMovement; 
	
	public ReadHand(HandMovement handMovement) {
		mHandMovement = handMovement;
	}
	
	public String getTime() {
		double totalMinutes = mHandMovement.getTimeInMinutes();
		double hour = totalMinutes / 60;
		double minutes = totalMinutes % 60;
		int hourInt;
		String timeString;
		
		String minString = String.valueOf((int)minutes);
		if (minString.length()==1) {
			minString = "0" + minString;
		}

		hourInt = (int)hour;
		
		if (hourInt==0) {
			hourInt = 12;
		}
		
		if (hourInt >= 13) {
			hourInt -= 12;
		}
		
		String hourString = String.valueOf((int)hourInt);
		if (hourString.length()==1) {
			hourString = "0" + hourString;
		}
		
		//printLog("totalMinutes: " + Double.valueOf(totalMinutes).toString());
		//printLog("hour: " + hourString);
		//printLog("min: " + minString);
		
		timeString = new String(hourString + ":" + minString);
		if (hour < 12) {
			timeString = timeString + "  AM";
		} else {
			timeString = timeString + "  PM";
		}
		
		return timeString;
	}
	
	void printLog(String s) {
		if (D) Log.d(TAG, s);
	}
}
