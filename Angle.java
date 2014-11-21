package com.example.clock;

import android.util.Log;

public class Angle {
	private static final String TAG = "game";
	private static final boolean D = true;
	
	int mCenterX, mCenterY;
	int mWidth, mHeight;
	
	//screen is set up as quadrant 4
	//we want to set it up as quadrant 1
	public Angle (int x, int y, int width, int height) {
		mCenterX = x;
		mCenterY = y;
		mWidth = width;
		mHeight = height;
	}
	
	public double getAngle(float x, float y) {
		//convert to local coord
		double localX, localY;
		double angleRad, angleDeg;
		
		localX = (double)x - mCenterX;
		localY = (double)(mHeight-y) - mCenterY;
		
		double z;
		
		//distance formula: x^2 + y^2 = z^2
		//z = Math.sqrt((localX*localX) + (localY*localY));
		
		angleRad = Math.atan2(localX, localY);
		
		//angleRad = Math.acos(localX/z);
		angleDeg = angleRad * 180/Math.PI;
		
		return angleDeg;
	}
	
	public int getQuadrant(float x, float y) {
		
		double localX, localY;
		localX = (double)x - mCenterX;
		localY = (double)(mHeight-y) - mCenterY;
		int ret_val = 0; //0 is error
		
		//printLog(Float.valueOf(x).toString() + "," + Float.valueOf(y).toString());
		//printLog(Integer.valueOf(mCenterX).toString() + "," + Integer.valueOf(mCenterY).toString());
		//printLog(Double.valueOf(localX).toString() + "," + Double.valueOf(localY).toString());
		
		if (localX==0 || localY==0) {
			ret_val = 0;
		}
		
		if (localX>0 && localY>0) {
			ret_val = 1;
		}
		if (localX>0 && localY<0) {
			ret_val = 4;
		}
		if (localX<0 && localY>0) {
			ret_val = 2;
		}
		if (localX<0 && localY<0) {
			ret_val = 3;
		}
		
		return ret_val;
	}
	
	
	void printLog(String s) {
		if (D) Log.d(TAG, s);
	}

}
