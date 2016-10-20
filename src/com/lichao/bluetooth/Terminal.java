package com.lichao.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Terminal extends Activity {
    private static final String TAG = "TerminalActivity";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String MESSAGE_STR = "message_string";
    public static final String TOAST = "toast";
    
    public static Handler handler;
    private static int mRxcount = 0;
    private static int mTxcount = 0;
    private boolean hexsend;
	private boolean hexdis;
    private boolean newline;
    private boolean autoclear;
    private String endStr = "";
    private static TextView recText = null;
    private TextView mRxcountText;
    private TextView mTxcountText;
    private LinearLayout recCount;
    private EditText mOutEditText;
    private EditText mSendPeriodText;
    private Button mSendButton;
    private CheckBox autoSend_check;
    private CheckBox cb_hexsend;
    private CheckBox cb_hexdis;
    private CheckBox cb_newline;
    private CheckBox cb_autoclear;
    private Button clearButton = null;
    private ScrollView svResult;
    private int count = 0;  
    private int firClick = 0;  
    private int secClick = 0;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terminal);
		recText = (TextView) findViewById(R.id.txt_rec);
		clearButton = (Button) findViewById(R.id.button_clear);
        svResult = (ScrollView) findViewById(R.id.scrollView1);
        mRxcountText = (TextView)findViewById(R.id.Rx_count_text);
        mTxcountText = (TextView)findViewById(R.id.Tx_count_text);
        recCount = (LinearLayout)findViewById(R.id.layout_count);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);//获取视图中的文本编辑框
        mSendButton = (Button) findViewById(R.id.button_send);//获取视图中的发送按钮
        mSendPeriodText = (EditText) findViewById(R.id.sendPerid_text);//获取视图中的文本编辑框
        autoSend_check = (CheckBox)findViewById(R.id.autoSend);
        cb_autoclear = (CheckBox) findViewById(R.id.cbox_autoclear);
        cb_hexdis = (CheckBox) findViewById(R.id.cbox_hexdis);
        cb_hexsend = (CheckBox) findViewById(R.id.cbox_hexsend);
        cb_newline = (CheckBox) findViewById(R.id.cbox_newline);
        
        recCount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		        count++;  
		        if (count%2==1) {  
		            firClick = (int) System.currentTimeMillis();  
		        } else if (count%2== 0) {  
		            secClick = (int) System.currentTimeMillis(); 
		        }
                if (secClick!=0&&firClick!=0) {// 双击事件   
                    if (Math.abs(secClick - firClick) < 1000) {// 双击事件  
                        mRxcount=0;
                        mTxcount=0;
                        mRxcountText.setText("Rx:"+mRxcount);
                        mTxcountText.setText("Tx:"+mTxcount);
                    	if(BluetoothChat.Debuggable) Log.d(TAG, "-- c1 --"+count);
                    } 
                    else {
        		        if (count%2==1) {  
                            secClick = 0; 
        		        } else if (count%2== 0) {  
                            firClick = 0;  
        		        } 
        		    	if(BluetoothChat.Debuggable) Log.d(TAG, "-- c2 --"+count); 
                    }
                }   
				
			}
		});
        
        mSendPeriodText.setText("1000");
        mSendPeriodText.setEnabled(false);
        mSendButton.setOnClickListener(new OnClickListener() {//为发送按钮添加一个自定义监听器
            public void onClick(View view) {//点击了发送按钮则执行
            	if(BluetoothChat.Debuggable) Log.d(TAG, "-- mSendButton  onClick--"); 
		    	if(BluetoothChat.mBluetoothChat.mChatService.getState()!=BluetoothChatService.STATE_CONNECTED){
		    		BluetoothChat.mBluetoothChat.startListActivity();
                    return;															
		    	}
		    	if(BluetoothChat.Debuggable) Log.d(TAG, "-- xcxcxxc --"); 
                // Send a message using content of the edit text widget
                String message = mOutEditText.getText().toString();//获取视图中文本编辑框内的字符串
                sendMessage(message);//调用发送程序，将信息通过蓝牙发送出去
            }
        });

		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recText.setText("");
			}
		});
        autoSend_check.setOnCheckedChangeListener(myCheckBoxOnCheckedChangeListener);
        cb_autoclear.setOnCheckedChangeListener(myCheckBoxOnCheckedChangeListener);
        cb_hexdis.setOnCheckedChangeListener(myCheckBoxOnCheckedChangeListener);
        cb_hexsend.setOnCheckedChangeListener(myCheckBoxOnCheckedChangeListener);
        cb_newline.setOnCheckedChangeListener(myCheckBoxOnCheckedChangeListener);

        // The Handler that gets information back from the BluetoothChatService
        handler = new Handler() {//在启动蓝牙聊天程序时会新建一个服务器对象，同时把此对象传给服务器

    		@Override								//服务器会在应用程序状态发生改变时（连接、发送信息等）调用此对象的方法，通知主视图更新UI
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MESSAGE_STATE_CHANGE://状态发生改变
                    switch (msg.arg1) {
                    case BluetoothChatService.STATE_CONNECTED://蓝牙设备已连接
                        mRxcount = 0;
                        mTxcount = 0;
                        recText.setText("");
                        mRxcountText.setText("Rx:"+mRxcount);
                        mTxcountText.setText("Tx:"+mTxcount);
                        mOutEditText.setEnabled(true);
                        autoSend_check.setEnabled(true);
    		            mSendPeriodText.setEnabled(true);
                        break;
                    case BluetoothChatService.STATE_LISTEN://服务器正在监听
                    case BluetoothChatService.STATE_NONE://服务器没有进行任何操作
                        mOutEditText.setEnabled(false);
						autoSend_check.setChecked(false);
                        autoSend_check.setEnabled(false);
    		            mSendPeriodText.setEnabled(false);
                        break;
                    }
                    break;
                case MESSAGE_WRITE://发送信息
                    byte[] writeBuf = (byte[]) msg.obj;//获得发送的信息内容
                    mTxcount += writeBuf.length;
                    mTxcountText.setText("Tx:"+mTxcount);
                    break;
                case MESSAGE_READ://读取信息
                    byte[] readBuf = (byte[]) msg.obj;//获取接收到的信息内容

                    if(recText.getHeight()>=svResult.getHeight()){
 //                   	svResult.scrollTo(0, recText.getHeight()+20);//滚动到最底部
                    	if(autoclear){
                    		recText.setText("");
                    	}
                    }
                	if(hexdis){
                		recText.append(BaseOperations.byteToHex(readBuf,msg.arg1," ")+endStr); 
    				}
                	else {
                        String readMessage = new String(readBuf, 0, msg.arg1);//将接收到的信息转换成字符串
                        recText.append(readMessage+endStr); 
                	}
                    mRxcount += msg.arg1;
                    mRxcountText.setText("Rx:"+mRxcount);
                    break;
                }
            }
        };
	}
    public static String getRecText() {
		return recText.getText().toString();
	}
	private final OnCheckedChangeListener myCheckBoxOnCheckedChangeListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch(buttonView.getId()){
			case R.id.cbox_autoclear:autoclear=isChecked;break;
			case R.id.cbox_hexdis:hexdis=isChecked;break;
			case R.id.cbox_hexsend:BluetoothChat.hexsend=isChecked;break;
			case R.id.cbox_newline:
					if(isChecked){
						endStr = "\n";
					}
					else{
						endStr = "";
					}
					break;
			case R.id.autoSend:
					if(isChecked){
						if(mOutEditText.getText().length()<1){
							Toast.makeText(getApplicationContext(), "请先输入发送信息", Toast.LENGTH_SHORT).show();
							autoSend_check.setChecked(false);
							return;
						}
						if (BluetoothChat.mBluetoothChat.mChatService != null){
							new Thread(new AutoSender()).start();
						}
						mOutEditText.setEnabled(false);
			            mSendPeriodText.setEnabled(false);
					}
					else {
						mOutEditText.setEnabled(true);
			            mSendPeriodText.setEnabled(true);
					}
				break;
			}
		}
	};

    private class AutoSender implements Runnable {

		@Override
		public void run() {
            String message = mOutEditText.getText().toString();//获取视图中文本编辑框内的字符串
            long time = (long)Integer.parseInt(mSendPeriodText.getText().toString());//自动发送周期
			while(autoSend_check.isChecked()&&BluetoothChat.mBluetoothChat.mChatService.getState()==BluetoothChatService.STATE_CONNECTED){
	//			Toast.makeText(getApplicationContext(), "自动发送", Toast.LENGTH_SHORT).show();
	            sendMessage(message);//调用发送程序，将信息通过蓝牙发送出去
	            try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
    	
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {//通过蓝牙发送信息

        // Check that there's actually something to send
        if (message.length() > 0) {//如果信息的长度大于0(信息不为空)
            // Get the message bytes and tell the BluetoothChatService to write
        	byte[] send;
        	if(BluetoothChat.hexsend){
        		send = BaseOperations.stringAsHex(message);//将要发送的信息转换为字符数组
        	}
        	else {
        		send = message.getBytes();//将要发送的信息转换为字符数组
        	}
        	BluetoothChat.mBluetoothChat.mChatService.write(send);//服务器发送信息
        }
    }
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
    	BluetoothChat.mBluetoothChat.onKeyDown(keyCode, event);
        return false;  
          
    } 
}
