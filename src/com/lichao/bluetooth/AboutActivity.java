package com.lichao.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class AboutActivity extends Activity {
    private static final String TAG = "aboutActivity";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(BluetoothChat.Debuggable) Log.d(TAG, "**** settings super.onCreate ****");

        setContentView(R.layout.activity_about);
        setResult(Activity.RESULT_CANCELED);//设置DeviceListActivity被调用后的返回结果
        TextView abt_url = (TextView)findViewById(R.id.about_url);
        abt_url.setText(  //给文本添加超链接
        		Html.fromHtml("<a href=\"https://shop102412417.taobao.com/?spm=a230r.7195193.1997079397.2.2eaqBj\">通旺科技淘宝店铺</a> "));
        abt_url.setMovementMethod(LinkMovementMethod.getInstance());
        TextView abt_url_mail = (TextView)findViewById(R.id.about_url_mail);
        abt_url_mail.setText(  //给文本添加超链接
        		Html.fromHtml("<a href=\"mailto:909318264@qq.com\">开发者邮箱@似水年华</a> "));  
        abt_url_mail.setMovementMethod(LinkMovementMethod.getInstance());
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            finish(); 
        }  
		return super.onKeyDown(keyCode, event);         
          
	}

}
