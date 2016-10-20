package com.lichao.bluetooth;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FeedBackActivity extends Activity {
    private static final String TAG = "settingsActivity";
    public static final int MESSAGE_STATE_CHANGE = 11;
    private RelativeLayout reLayout1 = null;
    private RelativeLayout reLayout2 = null;
    private RelativeLayout reLayout3 = null;
    private RelativeLayout reLayout4 = null;
    private RelativeLayout reLayout5 = null;
    private RelativeLayout reLayout6 = null;
    private RelativeLayout reLayout7 = null;
    private CheckBox cBox1 = null;
    private CheckBox cBox2 = null;
    private CheckBox cBox3 = null;
    private CheckBox cBox4 = null;
    private CheckBox cBox5 = null;
    private CheckBox cBox6 = null;
    private CheckBox cBox7 = null;
    private EditText etxt = null;
    private Button sendButton = null;
    private Context feedBackContext = null;
    private Handler mHandler;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	if(BluetoothChat.Debuggable) Log.d(TAG, "**** settings super.onCreate ****");
    	if(BluetoothChat.Debuggable) Log.d(TAG, android.os.Build.MODEL);//手机型号
    	if(BluetoothChat.Debuggable) Log.d(TAG, android.os.Build.VERSION.RELEASE);//系统版本号
        feedBackContext = this;
        // Set up the window layout
        setContentView(R.layout.activity_feedback);
        setResult(Activity.RESULT_CANCELED);//设置DeviceListActivity被调用后的返回结果
        final Context c= this;
        cBox1 = (CheckBox)findViewById(R.id.cb_feedback_1);
        cBox2 = (CheckBox)findViewById(R.id.cb_feedback_2);
        cBox3 = (CheckBox)findViewById(R.id.cb_feedback_3);
        cBox4 = (CheckBox)findViewById(R.id.cb_feedback_4);
        cBox5 = (CheckBox)findViewById(R.id.cb_feedback_5);
        cBox6 = (CheckBox)findViewById(R.id.cb_feedback_6);
        cBox7 = (CheckBox)findViewById(R.id.cb_feedback_7);
        reLayout1 = (RelativeLayout)findViewById(R.id.layout_feedback_1);
        reLayout2 = (RelativeLayout)findViewById(R.id.layout_feedback_2);
        reLayout3 = (RelativeLayout)findViewById(R.id.layout_feedback_3);
        reLayout4 = (RelativeLayout)findViewById(R.id.layout_feedback_4);
        reLayout5 = (RelativeLayout)findViewById(R.id.layout_feedback_5);
        reLayout6 = (RelativeLayout)findViewById(R.id.layout_feedback_6);
        reLayout7 = (RelativeLayout)findViewById(R.id.layout_feedback_7);
        OnTouchListener layoutOnTouchListener = new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	  			int actionType = event.getAction();//定义变量保存事件类型
				if(actionType==MotionEvent.ACTION_DOWN){
					switch(v.getId()){
					case R.id.cb_feedback_1:reLayout1.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					case R.id.cb_feedback_2:reLayout2.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					case R.id.cb_feedback_3:reLayout3.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					case R.id.cb_feedback_4:reLayout4.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					case R.id.cb_feedback_5:reLayout5.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					case R.id.cb_feedback_6:reLayout6.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					case R.id.cb_feedback_7:reLayout7.setBackgroundResource(R.drawable.item_bg_middle_pressed);break;
					}
				}
				if(actionType==MotionEvent.ACTION_UP){
					switch(v.getId()){
					case R.id.cb_feedback_1:reLayout1.setBackgroundResource(R.drawable.item_bg_middle);break;
					case R.id.cb_feedback_2:reLayout2.setBackgroundResource(R.drawable.item_bg_middle);break;
					case R.id.cb_feedback_3:reLayout3.setBackgroundResource(R.drawable.item_bg_middle);break;
					case R.id.cb_feedback_4:reLayout4.setBackgroundResource(R.drawable.item_bg_middle);break;
					case R.id.cb_feedback_5:reLayout5.setBackgroundResource(R.drawable.item_bg_middle);break;
					case R.id.cb_feedback_6:reLayout6.setBackgroundResource(R.drawable.item_bg_middle);break;
					case R.id.cb_feedback_7:reLayout7.setBackgroundResource(R.drawable.item_bg_middle);break;
					}
				}
				return false;
			}
		};
		cBox1.setOnTouchListener(layoutOnTouchListener);
		cBox2.setOnTouchListener(layoutOnTouchListener);
		cBox3.setOnTouchListener(layoutOnTouchListener);
		cBox4.setOnTouchListener(layoutOnTouchListener);
		cBox5.setOnTouchListener(layoutOnTouchListener);
		cBox6.setOnTouchListener(layoutOnTouchListener);
		cBox7.setOnTouchListener(layoutOnTouchListener);
		

        mHandler = new Handler() {//在启动蓝牙聊天程序时会新建一个服务器对象，同时把此对象传给服务器

    		@Override								//服务器会在应用程序状态发生改变时（连接、发送信息等）调用此对象的方法，通知主视图更新UI
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_STATE_CHANGE) {
                	if(((String)msg.obj).toString().equals("unable_to_send")){
        				sendButton.setEnabled(true);
						Toast.makeText(feedBackContext, "反馈失败，无法连接服务器", Toast.LENGTH_SHORT).show();
                	}
                	else if(((String)msg.obj).toString().equals("sendSuccess")){
						Toast.makeText(feedBackContext, "提交成功，感谢您的宝贵意见", Toast.LENGTH_SHORT).show();
						finish();
                	}
                }
    		}
        };
		
		etxt = (EditText)findViewById(R.id.etxt_feedback);
		sendButton = (Button)findViewById(R.id.sendmail);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				sendButton.setEnabled(false);
				
				String str = "";
				if(cBox1.isChecked()){
					str += cBox1.getText() + "/";
				}
				if(cBox2.isChecked()){
					str += cBox2.getText() + "/";
				}
				if(cBox3.isChecked()){
					str += cBox3.getText() + "/";
				}
				if(cBox4.isChecked()){
					str += cBox4.getText() + "/";
				}
				if(cBox5.isChecked()){
					str += cBox5.getText() + "/";
				}
				if(cBox6.isChecked()){
					str += cBox6.getText() + "/";
				}
				if(cBox7.isChecked()){
					str += cBox7.getText() + "/";
				}
				if(etxt.getText().toString().equals("")&&str.equals("")){
					Toast.makeText(feedBackContext, "请输入具体内容", Toast.LENGTH_SHORT).show();
					return;
				}
				
				final String sendStr = "手机型号:" + android.os.Build.MODEL + "\n" 
								+ "系统版本号:" + android.os.Build.VERSION.RELEASE + "\n" 
								+ "应用版本号:" + getVersionName() + "\n"  + "/"
								+ "蓝牙地址:" + BluetoothAdapter.getDefaultAdapter().getAddress() + "\n"  + "/"
								+ str
								+ "反馈内容:" + etxt.getText().toString();

		        //耗时操作要起线程...有几个新手都是这个问题
		        new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							EmailSender sender = new EmailSender();
							//设置服务器地址和端口，网上搜的到
							sender.setProperties("smtp.qq.com", "465");
							//分别设置发件人，邮件标题和文本内容
							sender.setMessage("1234567@qq.com", "title", sendStr);
							//设置收件人
							sender.setReceiver(new String[]{"7654321@qq.com"});
							//添加附件，我这里注释掉，因为有人跟我说这行报错...
							//这个附件的路径是我手机里的啊，要发你得换成你手机里正确的路径
							//sender.addAttachment(MyFileManager.internalSdCard+MyLockerActivity.myAppFolder+"/usedDeviceData");
					    	if(BluetoothChat.Debuggable) Log.d("", "fsfsasasdasdsadasdasd");
							//发送邮件
							sender.sendEmail("smtp.qq.com", "1234567@qq.com", "********");
					    	if(BluetoothChat.Debuggable) Log.d("", "f------------------------");
					        mHandler.obtainMessage(MESSAGE_STATE_CHANGE,"sendSuccess").sendToTarget();
						} catch (AddressException e) {
					    	if(BluetoothChat.Debuggable) Log.d("", "反馈失败");
					        mHandler.obtainMessage(MESSAGE_STATE_CHANGE,"unable_to_send").sendToTarget();
							e.printStackTrace();
						} catch (MessagingException e) {
					    	if(BluetoothChat.Debuggable) Log.d("", "反馈失败，无法连接服务器");
					        mHandler.obtainMessage(MESSAGE_STATE_CHANGE,"unable_to_send").sendToTarget();
							e.printStackTrace();
						}
					}
				}).start();
			}
		});

	}
	private String getVersionName() {  
	        // 获取packagemanager的实例  
	        PackageManager packageManager = getPackageManager();  
	        // getPackageName()是你当前类的包名，0代表是获取版本信息  
	        PackageInfo packInfo;
			try {
				packInfo = packageManager.getPackageInfo(getPackageName(),0);
		        String version = packInfo.versionName;  
		        return version;  
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}  
	        return "";  
	}
}
