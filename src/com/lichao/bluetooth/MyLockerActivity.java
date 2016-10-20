package com.lichao.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;
import com.lichao.bluetooth.GestureLockView.OnGestureFinishListener;

public class MyLockerActivity extends Activity {
    private static final String TAG = "MyLockerActivity";
	public static boolean PWD = false;
	public static int PWD_CMD ;
	public static String KEY = "0124678";
	public static Context mContext = null;
	private TextView tipTxt;
	private GestureLockView gv = null;

	public static final int PWD_DEL 	= -1;
	public static final int PWD_INPUT = 0;
	public static final int PWD_RESET 	= 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	mContext = this;
	if(BluetoothChat.Debuggable) Log.d(TAG, "**** Start gv ****");
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_gesturelock);
    tipTxt = (TextView)findViewById(R.id.textView1);
	gv = (GestureLockView) findViewById(R.id.gv);

    }

	@Override
	protected void onStart() {
		super.onStart();	if(PWD){
			switch(MyLockerActivity.PWD_CMD){
			case -1:tipTxt.setText("请输入旧密码");break;
			case  0:tipTxt.setText("请输入密码");break;
			case  1:tipTxt.setText("请输入旧密码");break;
			}
			tipTxt.setTextColor(Color.BLUE);
		}
		else{
			tipTxt.setText("请输入新密码");
			tipTxt.setTextColor(Color.BLUE);
		}
    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** Start setkey ****");
		gv.setOnGestureFinishListener(new OnGestureFinishListener() {
			@Override
			public void OnGestureFinish(String key) {
            //	Toast.makeText(MainActivity.this, key, Toast.LENGTH_SHORT).show();
				if(!PWD){
						if(KEY==null){
							if(key.length()<4){
								gv.setState(false);
								tipTxt.setText("密码长度太短，请重新输入新密码");
								tipTxt.setTextColor(Color.RED);
								KEY = null;
							}
							else{
								gv.setState(true);
								tipTxt.setText("请确认新密码");
								tipTxt.setTextColor(Color.BLUE);
								KEY = key;
							}
						}
						else{
							if(KEY.equals(key)){
								gv.setState(true);
								tipTxt.setText("密码设置成功");
								tipTxt.setTextColor(Color.BLUE);
								PWD = true;
								StartActivity.mFileManager.writeTxtFile("true"+KEY, MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_data.",false);
							    setResult(Activity.RESULT_OK);//
								finish();
							}
							else{
								gv.setState(false);
								tipTxt.setText("密码与上次不同，请重新输入新密码");
								tipTxt.setTextColor(Color.RED);
								KEY = null;
							}
						}
				}
				else{
					switch(MyLockerActivity.PWD_CMD){
					case PWD_DEL:
				    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** Start switch(MyLockerActivity.PWD_CMD)1 ****");
								if(KEY.equals(key)){
							    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** Start switch(MyLockerActivity.PWD_CMD)2 ****");
									gv.setState(true);
									tipTxt.setText("解锁成功，密码已删除");
									tipTxt.setTextColor(Color.BLUE);
							    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** Start switch(MyLockerActivity.PWD_CMD)3 ****");
									KEY = null;
									StartActivity.mFileManager.writeTxtFile(TAG, MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_data.",false);
									PWD = false;
							    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** Start switch(MyLockerActivity.PWD_CMD)4 ****");
								    setResult(MyLockerActivity.RESULT_OK);//设置DeviceListActivity被调用后的返回结果
									finish();
								}
								else{
									gv.setState(false);
									tipTxt.setText("密码输入错误，请重新输入");
									tipTxt.setTextColor(Color.RED);
								}
								break;
					case  PWD_INPUT:
								if(KEY.equals(key)){
									gv.setState(true);
									tipTxt.setText("解锁成功");
									tipTxt.setTextColor(Color.BLUE);
								    setResult(Activity.RESULT_OK);//
									finish();
								}
								else{
									gv.setState(false);
									tipTxt.setText("解锁失败，请重新输入");
									tipTxt.setTextColor(Color.RED);
								}
								break;
					case  PWD_RESET:
								if(KEY.equals(key)){
									gv.setState(true);
									tipTxt.setText("旧密码已删除，请输入新密码");
									tipTxt.setTextColor(Color.BLUE);
									KEY = null;
									StartActivity.mFileManager.writeTxtFile("false", MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_data.",false);
									PWD = false;
								}
								else{
									gv.setState(false);
									tipTxt.setText("解锁失败，请重新输入");
									tipTxt.setTextColor(Color.RED);
								}
								break;
					}
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    setResult(Activity.RESULT_CANCELED);//设置DeviceListActivity被调用后的返回结果
		return super.onKeyDown(keyCode, event);
	}
}
