package com.evaapis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.evature.util.Log;

public class SoundLevelView extends View {

		private static final String TAG = "SoundLevelView";
		private int[] soundBuff;
		private float[] pointsBuff = null;
		private Paint paint;
		private int soundBuffIndex;
		private int peakSound;
		private int numOfPoints;

		public SoundLevelView(Context ctx, AttributeSet attrSet) {
			super(ctx, attrSet);
			this.soundBuff = null;
			this.paint = new Paint();
			paint.setColor(Color.GREEN);
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (soundBuff != null && pointsBuff != null) {
				int min = 99999;
				int firstNoneZero = -1;
				for (int i=0; i<soundBuff.length; i++) {
					float soundLevel = soundBuff[(i+this.soundBuffIndex+1) % soundBuff.length];
					if (soundLevel > 0) {
						if (soundLevel < min) {
							min = soundBuff[i];
						}
						if (firstNoneZero == -1) {
							firstNoneZero = i;
						}
					}
				}
				int max = peakSound;
				int delta = max - min;
				// build points array
				float prevX = 0;
				int qi = 0;
				int height = (canvas.getHeight()-2)/2;
				float prevY = height;
				float width = canvas.getWidth()-2;
				numOfPoints = soundBuff.length - firstNoneZero;
				float xStep = width / soundBuff.length;
				for (int i=firstNoneZero; i<soundBuff.length; i++) {
					pointsBuff[qi] = prevX;
					pointsBuff[qi+1] = prevY;
					pointsBuff[qi+2] = prevX+xStep;
					float soundLevel = soundBuff[(i+this.soundBuffIndex+1) % soundBuff.length];
					if (soundLevel > 0) {
						soundLevel -= min;
					}
					float normLevel = soundLevel / delta;
					float y = height + (i%4 < 2 ? -1 : 1) * height*normLevel;
					pointsBuff[qi+3] = prevY = y;
					qi += 4;
					prevX += xStep;					
				}
				this.soundBuff = null;
			}
			if (pointsBuff != null) {
				canvas.drawLines(pointsBuff, 0, numOfPoints*4, paint);
			}
		}

		public void setSoundData(int[] buff, int index, int peakSound) {
			this.soundBuff = buff;
			this.soundBuffIndex = index;
			this.peakSound = peakSound;
			if (pointsBuff == null || buff.length*4 != pointsBuff.length) {
				Log.i(TAG, "points buff for "+buff.length+" sound buff");
				pointsBuff = new float[buff.length*4];
			}
		}
		
	}