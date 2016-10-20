package com.lichao.bluetooth;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureLockView extends View {
	private MyCycle[] cycles;
	private Path linePath = new Path();
	private OnGestureFinishListener onGestureFinishListener;
	private String inputkey = "";
	private int eventX, eventY;
	private boolean inputOver = false;
	private boolean state = true;
	private Timer timer;
	private Paint paintLines;
	
	public void setOnGestureFinishListener(
			OnGestureFinishListener onGestureFinishListener) {
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start setOnGestureFinishListener ****");
		this.onGestureFinishListener = onGestureFinishListener;
	}

	public interface OnGestureFinishListener {
		public void OnGestureFinish(String key);
	}

	public GestureLockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start GestureLockView ****");
		init();
	}

	public GestureLockView(Context context, AttributeSet attrs) {
		super(context, attrs);
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start GestureLockView2 ****");
		init();
	}

	public GestureLockView(Context context) {
		super(context);
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start GestureLockView3 ****");
		init();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start onMeasure ****");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int perSize = 0;
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start onLayout ****");
		if (cycles == null && (perSize = getWidth() / 40) > 0) {
			cycles = new MyCycle[9];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					cycles[i * 3 + j] = new MyCycle(i*3+j,perSize*(j*12+8),perSize*(i*12+8),perSize*4);
				}
			}
		}
	}

	public void init() {
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start init ****");
		
		paintLines = new Paint();
		paintLines.setAntiAlias(true);
		paintLines.setStyle(Paint.Style.STROKE);
		paintLines.setStrokeWidth(6);
	}

	public void setState(boolean state) {
		this.state = state;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start onDraw ****");
		for (int i = 0; i < cycles.length; i++) {
			if (inputOver && !state) {
				if(inputkey.contains(cycles[i].getId()+"")){
					cycles[i].setStete(MyCycle.ERROR);
				}
				paintLines.setColor(MyCycle.ERROR_COLOR);
			} else {
				paintLines.setColor(MyCycle.LINE_COLOR);
			}
			cycles[i].draw(canvas);
		}
		// drawLine
		drawLine(canvas);
	}
	
	
	private void drawLine(Canvas canvas) {
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start drawLine ****");
		linePath.reset();
		if (inputkey.length() > 0) {
			for (int i = 0; i < inputkey.length(); i++) {
				int index = inputkey.charAt(i)-48;
				if (i == 0) {
					linePath.moveTo(cycles[index].getX(), cycles[index].getY());
				} else {
					linePath.lineTo(cycles[index].getX(), cycles[index].getY());
				}
			}
			if(!inputOver){
				linePath.lineTo(eventX, eventY);
			}
			canvas.drawPath(linePath, paintLines);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start onTouchEvent ****");
		if (!inputOver) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE: {
				eventX = (int) event.getX();
				eventY = (int) event.getY();
				for (int i = 0; i < cycles.length; i++) {
					if (cycles[i].isPointIn(eventX, eventY)) {
						if (!inputkey.contains(cycles[i].getId()+"")) {
							cycles[i].setStete(MyCycle.ONTOUCH);
							inputkey = inputkey + cycles[i].getId();
						}
					}
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 暂停触碰
				inputOver = true;
					if (onGestureFinishListener != null) {
						onGestureFinishListener.OnGestureFinish(inputkey);
					}
				/***/
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						// 还原
				    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start 还原 ****");
						eventX = eventY = 0;
						for (int i = 0; i < cycles.length; i++) {
							cycles[i].setStete(MyCycle.READY);
						}
						inputkey = "";
						linePath.reset();
						inputOver = false;
						postInvalidate();
					}
				}, 1000);//*/
				break;
			}
			}
			invalidate();
		}
		return true;
	}
}
