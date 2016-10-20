package com.lichao.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.TextView;

public class MoreSetActivity extends Activity {
    private static final String TAG = "MoreSet";
	public MyFileManager mFileManager = new MyFileManager();
	RadioButton defaultStrOn;
	RadioButton defaultHexOn;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** settings super.onCreate ****");

        setContentView(R.layout.activity_moreset);
        setResult(Activity.RESULT_CANCELED);//设置DeviceListActivity被调用后的返回结果
    	defaultStrOn = (RadioButton) findViewById(R.id.radio_str);
    	defaultHexOn = (RadioButton) findViewById(R.id.radio_hex);
		if(mFileManager.readTxtFile(MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_defaultdis").contains("true")){
			defaultHexOn.setChecked(true);
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
			if(defaultStrOn.isChecked()){
				mFileManager.writeTxtFile("false",MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_defaultdis",false);
			}
			else if(defaultHexOn.isChecked()){
				mFileManager.writeTxtFile("true",MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_defaultdis",false);
			}
            finish(); 
        }  
		return super.onKeyDown(keyCode, event);         
          
	}

}
