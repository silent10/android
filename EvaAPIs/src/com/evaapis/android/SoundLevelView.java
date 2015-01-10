package com.evaapis.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.evature.util.Log;

public class SoundLevelView extends View {

	private static final String TAG = "SoundLevelView";
	private float[] soundBuff;    // cyclic buffer of volume - (0 to index) or (index+1 to index+length (module buff size))
	private int soundBuffIndex;
	
	private float[] velocities; 
	private Paint paint;
	private float peakSound;
	private float minSound;

	private int gravity;
	private float xstep;
	private boolean autoMinMax = true;
	private boolean extendLine = true;
    private float springiness = 0.75f;
    private float damping = 0.92f;
    Path path = new Path();

	public SoundLevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.soundBuff = null;
		this.paint = new Paint();
		this.peakSound = Integer.MIN_VALUE;
		this.minSound = Integer.MAX_VALUE;
		//paint.setColor(Color.GREEN);
		paint.setColor(0xff44aaff);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Join.ROUND);
		paint.setStrokeWidth(4);
		gravity = Gravity.CENTER_HORIZONTAL;
		xstep = 0;
	}
	
	public void setColor(int color) {
		paint.setColor(color);
	}
	
	public void setAlign(int gravity) {
		this.gravity = gravity;
	}
	
	public void setXStep(float xstep) {
		this.xstep = xstep;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (soundBuff == null) {
			return;
		}
		int height = this.getHeight() / 2;
		float width = this.getWidth();

		int startOfBuff = 0;
		int numOfPoints = soundBuffIndex;
		if (soundBuffIndex > soundBuff.length) {
			startOfBuff = soundBuffIndex + 1;
			numOfPoints = soundBuff.length;
		}
			
		float delta = Math.max(500f, peakSound - minSound);

		canvas.save();
		canvas.translate(0, height);

		float xStep = getXStep();
		if (xStep == 0) {
			xStep = width / (4*soundBuff.length);
			setXStep(xStep);
		}
		float xStep2 = 2*xStep;
		float xStep3 = 3*xStep;
		float xStep4 = 4*xStep;
		
		float curX;
		if ((gravity & Gravity.RIGHT) == Gravity.RIGHT){
			curX = width - numOfPoints * xStep4;
		}
		else if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
			curX = 0;
		}
		else {
			curX = (width - numOfPoints * xStep4) / 2;
		}

		path.reset();
		if (extendLine) {
			path.moveTo(0, 0);
			path.lineTo(curX, 0);
		}
		else {
			path.moveTo(curX, 0);
		}
		if (numOfPoints > 4) {
			
			for (int i = 0; i < numOfPoints; i++) {
				float soundLevel = Math.abs(soundBuff[(i + startOfBuff) % soundBuff.length]);
				if (soundLevel > 0) {
					soundLevel -= minSound;
				}
				float normLevel = soundLevel / delta; // normalize
				
				float y = ((i+ startOfBuff) % 2 == 0 ? -1 : 1) * height * normLevel;
				
				
				path.cubicTo(curX+xStep, y, curX+xStep2, y, curX+xStep3, 0);
				curX += xStep4;
			}
		}
		
		if (extendLine) {
			path.lineTo(width, 0);
		}
		
        canvas.drawPath(path, paint);
		
		
//			if (extendLine) {
//				canvas.drawLine(pointsBuff[4*numOfPoints-2], height, width, height, paint);
//			}
		
		canvas.restore();
	}

	private float getXStep() {
		return xstep;
	}

	public void setSoundData(float[] buff, int index) {
		boolean wasNull = false;
		if (soundBuff == null) {
			wasNull = true;
			this.soundBuff = new float[buff.length];
			velocities = new float[buff.length]; // need velocities for y values
		}
		
		// append new data to end of soundBuff
		int numOfPoints = (index-soundBuffIndex);
		int startOfBuff = 0;
		if (index > soundBuff.length) {
			startOfBuff = index+1;
		}
		if (autoMinMax) {
			for (int i=0; i<numOfPoints; i++) {
				float volume = buff[(startOfBuff+i)% buff.length];
				soundBuff[(soundBuffIndex+i)%soundBuff.length] = volume;
				peakSound = Math.max(peakSound, volume);
				minSound = Math.min(minSound, volume);
			}
		}
		else {
			for (int i=0; i<numOfPoints; i++) {
				soundBuff[(soundBuffIndex+i)%soundBuff.length] = buff[(startOfBuff+i)% buff.length];
			}
		}
		soundBuffIndex = index;
		Log.i(TAG, "Setting points buff for " + buff.length
				+ " sound samples");
		
//		if (springiness != 0) {
//			postDelayed(animator, 15);
//        }
		
		invalidate();
	}
	
	@Override 
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (visibility == View.GONE || visibility == View.INVISIBLE) {
			// continue next time with same peak values - better than starting from start
			//peakSound = Integer.MIN_VALUE;
			//minSound = Integer.MAX_VALUE;
			soundBuff = null;
			velocities = null;
		}
	};
	
	public void stopSpringAnimation() {
		lastTime = -1;
		removeCallbacks(animator);
	}
	
	public void startSpringAnimation() {
		lastTime = AnimationUtils.currentAnimationTimeMillis();
		postDelayed(animator, 1);
	}
	
	private long lastTime = 0;
	private Runnable animator = new Runnable() {
	    @Override
	    public void run() {
	        if (soundBuff == null || lastTime == -1) {
	        	return;
	        }
	        
	        float THRESHOLD_TO_CONTINUE = 0.1f;
	        int FREQUENCY_TS = 15; // how often to animate - in ms 
	        
	        float maxVelocity = 0;
	        long now = AnimationUtils.currentAnimationTimeMillis();
	        float dt = Math.min(now - lastTime, 50) / 1000f;
	        float targetPosition = minSound;
	        
	        int startOfBuff = 0;
			int numOfPoints = soundBuffIndex;
			if (soundBuffIndex > soundBuff.length) {
				startOfBuff = soundBuffIndex + 1;
				numOfPoints = soundBuff.length;
			}
			
			for (int i = 0; i < numOfPoints; i++) {
				if (soundBuff == null) {
		        	return;
		        }
				int index = (startOfBuff+i) % soundBuff.length;
				float volume = soundBuff[index];
				float velocity = velocities[index];
				velocity += (targetPosition - volume) * springiness;
				velocity *= damping;
				volume += velocity * dt;
				if (maxVelocity < THRESHOLD_TO_CONTINUE)
					maxVelocity = Math.max(maxVelocity, Math.abs(velocity));
				velocities[index] = velocity;
				soundBuff[index] = volume;
			}
	        lastTime = now;
	        
	        if (maxVelocity > THRESHOLD_TO_CONTINUE) {
	        	postDelayed(animator, FREQUENCY_TS);
	        }
	        
	        invalidate();
	    }
	};

}