package com.lichao.bluetooth;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

public class StartActivity extends Activity {

	private static final String TAG = "StartActivity";
	public static MyFileManager mFileManager = new MyFileManager();
	public static final int REQUEST_SETPWD = 1;
	public static final int REQUEST_DELPWD = 2;
	public static final int REQUEST_PASSWORD = 3;
	public static Boolean cmdMode = false;
	private Context context;
	private Intent serverIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		MyFileManager.internalSdCard = mFileManager.getSdCardPath();
		if (!mFileManager.isFileExist("/_data.txt",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createDir(MyFileManager.internalSdCard
						+ BluetoothChat.myAppFolder);
				mFileManager.createFile("/_data.txt",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				mFileManager.writeTxtFile("蓝牙串口数据文件夹",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_data.txt",
						true);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "start0");
		if (!mFileManager.isFileExist("/_data.", MyFileManager.internalSdCard
				+ BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_data.", MyFileManager.internalSdCard
						+ BluetoothChat.myAppFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!mFileManager.isFileExist("/usedDeviceData",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/usedDeviceData",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "start1");
		// 按键标签off
		if (!mFileManager.isFileExist("/_btLabOff",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_btLabOff",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				MyFileManager.writeTxtFile(
						"↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_btLabOff");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 按键信息off
		if (!mFileManager.isFileExist("/_btMsgOff",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_btMsgOff",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				MyFileManager.writeTxtFile(
						"↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_btMsgOff");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 按键标签on
		if (!mFileManager.isFileExist("/_btLabOn", MyFileManager.internalSdCard
				+ BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_btLabOn",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				MyFileManager.writeTxtFile(
						"↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_btLabOn");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 按键信息on
		if (!mFileManager.isFileExist("/_btMsgOn", MyFileManager.internalSdCard
				+ BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_btMsgOn",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				MyFileManager.writeTxtFile(
						"↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑,↑",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_btMsgOn");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 十六进制标志off
		if (!mFileManager.isFileExist("/_hexOnOff",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_hexOnOff",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				MyFileManager.writeTxtFile(
						"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_hexOnOff");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 十六进制标志on
		if (!mFileManager.isFileExist("/_hexOnOn", MyFileManager.internalSdCard
				+ BluetoothChat.myAppFolder)) {
			// Toast.makeText(getApplicationContext(),mFileManager.readTxtFileFromSDCard(internalSdCard+myAppFolder+"data.csv"),Toast.LENGTH_SHORT).show();
			try {
				mFileManager.createFile("/_hexOnOn",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder);
				MyFileManager.writeTxtFile(
						"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0",
						MyFileManager.internalSdCard
								+ BluetoothChat.myAppFolder + "/_hexOnOn");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "start2");
		EditKeybord.btLabOff = mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_btLabOff").split(",");
		EditKeybord.btLabOn = mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_btLabOn").split(",");
		EditKeybord.btMsgOff = mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_btMsgOff").split(",");
		EditKeybord.btMsgOn = mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_btMsgOn").split(",");
		EditKeybord.hexOn_Off = mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_hexOnOff").split(",");
		EditKeybord.hexOn_On = mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_hexOnOn").split(",");
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "start22");
		if (mFileManager.readTxtFile(
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder
						+ "/_data.").contains("true")) {
			MyLockerActivity.KEY = StartActivity.mFileManager.readTxtFile(
					MyFileManager.internalSdCard + BluetoothChat.myAppFolder
							+ "/_data.").substring(4);
			MyLockerActivity.PWD = true;
			serverIntent = new Intent(context, MyLockerActivity.class);
		} else {
			MyLockerActivity.PWD = false;
			MyLockerActivity.KEY = null;
			serverIntent = new Intent(context, BluetoothChat.class);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_start);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (MyLockerActivity.PWD) {
					startActivityForResult(serverIntent, REQUEST_PASSWORD);
				} else {
					startActivity(serverIntent);
					finish();
				}
			}
		}, 2000); // 延迟2秒跳转
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "+++ ON CREATE +++");
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "+++ ON onActivityResult +++");
		switch (requestCode) {
		case REQUEST_PASSWORD:
			if (resultCode == Activity.RESULT_OK) {
				serverIntent = new Intent(context, BluetoothChat.class);
				startActivity(serverIntent);
			}
			finish();
			break;
		}
	}

}
