package com.lichao.bluetooth;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class MyCycle {
    private int x;          // ????????
    private int y;          // ?????????
    private float r;         // ??????
    private int id;     // ???????
    private int state; // false=δ???
	private Paint paintOutCycle;
	private Paint paintOutCycleFill;
	private Paint paintInnerCycle;
	
	public static int OUT_CYCLE_NORMAL    = Color.rgb(108, 119, 138);        // 正常外圆颜色
	public static int OUT_CYCLE_FILL_ERROR= Color.argb(64, 255, 000, 000);  // 连接错误醒目提示颜色
	public static int OUT_CYCLE_FILL_NORMAL   = Color.argb(96, 002, 210, 255);        // 选中外圆颜色
	public static int INNER_CYCLE_ONTOUCH = Color.rgb(002, 180, 255);        // 选择内圆颜色
	public static int LINE_COLOR          = Color.argb(159, 002, 210, 255);  // 连接线颜色
	public static int ERROR_COLOR         = Color.argb(127, 255, 000, 000);  // 连接错误醒目提示颜色
	public static int READY = 0;
	public static int ONTOUCH = 1;
	public static int ERROR = -1;
	
    public MyCycle(int id) {
    	this.id = id;
    	init();
	}
	public MyCycle(int id, int x, int y, float r) {
    	this.id = id;
    	this.x = x;
    	this.y = y;
    	this.r = r;
    	init();
	}
	public void draw(Canvas canvas){
	    switch(state){
	    case -1:paintOutCycle.setColor(ERROR_COLOR);
				paintInnerCycle.setColor(ERROR_COLOR);
				paintOutCycleFill.setColor(OUT_CYCLE_FILL_ERROR);
				canvas.drawCircle(this.getX(), this.getY(), this.getR(), 	paintOutCycle);
				canvas.drawCircle(this.getX(), this.getY(), this.getR(), 	paintOutCycleFill);
				canvas.drawCircle(this.getX(), this.getY(), this.getR() / 3, paintInnerCycle);
				break;
	    case  0:paintOutCycle.setColor(OUT_CYCLE_NORMAL);
	//			paintOutCycle.setStyle(Paint.Style.STROKE);
				paintInnerCycle.setColor(OUT_CYCLE_NORMAL);
				canvas.drawCircle(this.getX(), this.getY(), this.getR(), paintOutCycle);
	    		break;
	    case  1:paintInnerCycle.setColor(INNER_CYCLE_ONTOUCH);
				paintOutCycleFill.setColor(OUT_CYCLE_FILL_NORMAL);
	//			paintOutCycle.setStyle(Paint.Style.FILL);
				canvas.drawCircle(this.getX(), this.getY(), this.getR(), 	paintOutCycle);
				canvas.drawCircle(this.getX(), this.getY(), this.getR(), 	paintOutCycleFill);
				canvas.drawCircle(this.getX(), this.getY(), this.getR() / 3, paintInnerCycle);
	    		break;
	    }
	}

	public void init() {
    	if(BluetoothChat.Debuggable) Log.d("Locker", "**** Start init ****");
	    paintOutCycle = new Paint();
	    paintOutCycle.setAntiAlias(true);
	    paintOutCycle.setStrokeWidth(3);
	    paintOutCycle.setStyle(Paint.Style.STROKE);

		paintInnerCycle = new Paint();
		paintInnerCycle.setAntiAlias(true);
		paintInnerCycle.setStyle(Paint.Style.FILL);
		
		paintOutCycleFill = new Paint();
		paintOutCycleFill.setAntiAlias(true);
		paintOutCycleFill.setStyle(Paint.Style.FILL);
		
	}

	public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public float getR() {
        return r;
    }
    public void setR(float r) {
        this.r = r;
    }
    public int getId() {
        return id;
    }
    /**
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
    public int getState() {
        return state;
    }
    public void setStete(int state) {
        this.state = state;
    }
    public boolean isPointIn(int x, int y) {
        double distance = Math.sqrt((x - this.x) * (x - this.x) + (y - this.y) * (y - this.y));
        return distance < r;
    }
}