package com.lichao.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ToggleButton;

public class EditKeybord extends Activity {
    ActionSheet btnEditSheet = null;
    //定义使用十六进制的标志字符串
    public static final String HEX_ON = "1";
    public static final String HEX_OFF = "0";
    public static final String FLAG_LABLE = "↑";
	public MyFileManager mFileManager = new MyFileManager();
	//需要自定义的按钮
	private ToggleButton tButton11,tButton12,tButton13,tButton14;
	private ToggleButton tButton21,tButton22,tButton23,tButton24;
	private ToggleButton tButton31,tButton32,tButton33,tButton34;
	private ToggleButton tButton41,tButton42,tButton43,tButton44;
	private ToggleButton tButton51,tButton52,tButton53,tButton54;
	//需要自定义的按钮数组，与上面的按钮相同，方便编程使用
	private ToggleButton[] tButton = {tButton11,tButton12,tButton13,tButton14,
										tButton21,tButton22,tButton23,tButton24,
										tButton31,tButton32,tButton33,tButton34,
										tButton41,tButton42,tButton43,tButton44,
										tButton51,tButton52,tButton53,tButton54};
	//需要自定义的按钮的id数组，方便编程使用
	private int[] tButtonId = {R.id.tbutton11,R.id.tbutton12,R.id.tbutton13,R.id.tbutton14,
							R.id.tbutton21,R.id.tbutton22,R.id.tbutton23,R.id.tbutton24,
							R.id.tbutton31,R.id.tbutton32,R.id.tbutton33,R.id.tbutton34,
							R.id.tbutton41,R.id.tbutton42,R.id.tbutton43,R.id.tbutton44,
							R.id.tbutton51,R.id.tbutton52,R.id.tbutton53,R.id.tbutton54
									};
	
	public static String[] btLabOff = {"","","","","","","","","","","","","","","","","","","",""};//关状态标签
	public static String[] btMsgOff = {"","","","","","","","","","","","","","","","","","","",""};//关状态信息
	public static String[] btLabOn = {"","","","","","","","","","","","","","","","","","","",""};//开状态标签
	public static String[] btMsgOn = {"","","","","","","","","","","","","","","","","","","",""};//开状态信息
	public static String[] hexOn_Off = {HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,
										HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF};//关状态十六进制标志
	public static String[] hexOn_On = {HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,
										HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF,HEX_OFF};//开状态十六进制标志
	InputMethodManager inputKeybord = null;

	private OnLongClickListener myButtonOnLongClickListener ;
	private OnClickListener myButtonOnClickListener ;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editkeybord);
		inputKeybord = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);//得到InputMethodManager的实例   
		//找到视图中的按钮实例
		for(int i=0;i<tButtonId.length;i++){
			tButton[i] = (ToggleButton) findViewById(tButtonId[i]);
		}
		//位每一个按钮添加标签
		for(int i=0;i<tButtonId.length;i++){
			if(btMsgOff[i].equals(FLAG_LABLE)||btMsgOn[i].equals(FLAG_LABLE)){
	    		continue;
	    	}
			tButton[i].setTextOff(btLabOff[i]);
			tButton[i].setTextOn(btLabOn[i]);
			tButton[i].setText(tButton[i].getTextOff());
		}
		//自定义的长按监听器
		myButtonOnLongClickListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				editDialog(v);
				return false;
			}
			
		};
		//自定义的点击监听器
		myButtonOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	final ToggleButton cbtn= (ToggleButton)v;
		    	int index_btn = serchTab(v.getId(),tButtonId);
		    	if(BluetoothChat.mBluetoothChat.mChatService.getState()!=BluetoothChatService.STATE_CONNECTED){
		    		cbtn.setChecked(!cbtn.isChecked());
		    		BluetoothChat.mBluetoothChat.startListActivity();
                    return;															
		    	}
		    	if(btMsgOff[index_btn].equals(FLAG_LABLE)||btMsgOn[index_btn].equals(FLAG_LABLE)){
		    		return;
		    	}
    			if(cbtn.isChecked()){
    				if(hexOn_Off[index_btn].equals(HEX_ON)){
    					BluetoothChat.mBluetoothChat.mChatService.write(BaseOperations.stringAsHex(btMsgOff[index_btn]));
    				}
    				else{
    					BluetoothChat.mBluetoothChat.mChatService.write(btMsgOff[index_btn].getBytes());
    				}
    			}
    			else{
    				if(hexOn_On[index_btn].equals(HEX_ON)){
    					BluetoothChat.mBluetoothChat.mChatService.write(BaseOperations.stringAsHex(btMsgOn[index_btn]));
    				}
    				else{
    					BluetoothChat.mBluetoothChat.mChatService.write(btMsgOn[index_btn].getBytes());
    				}
    			}
			}
			
		};
		//位每一个按钮添加监听器
		for(int i=0;i<tButtonId.length;i++){
			tButton[i].setOnLongClickListener(myButtonOnLongClickListener);
		}
		for(int i=0;i<tButtonId.length;i++){
			tButton[i].setOnClickListener(myButtonOnClickListener);
		}
	}
	private void editDialog(final View btn){
    	final ToggleButton cbtn= (ToggleButton)btn;
    	final int index_button = serchTab(btn.getId(),tButtonId);
        // 一个自定义的布局，作为显示的内容
        final View contentView = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.button_editer, null);
        contentView.setMinimumWidth(getWindowManager().getDefaultDisplay().getWidth()*1/2);
			btnEditSheet = new ActionSheet(this,Gravity.CENTER,R.style.ActionSheetDialog);
			btnEditSheet.setGravity(Gravity.CENTER);
			btnEditSheet.setContentView(contentView);
			btnEditSheet.setCancelable(true);
			btnEditSheet.setCanceledOnTouchOutside(true);
			btnEditSheet.getWindow().setDimAmount(0.3f);
			//找到自定义布局中的组件实例
			final EditText lableOff = (EditText) contentView.findViewById(R.id.etxt_lable_off);
			final EditText msgOff = (EditText) contentView.findViewById(R.id.etxt_msg_off);
			final EditText lableOn = (EditText) contentView.findViewById(R.id.etxt_lable_on);
			final EditText msgOn = (EditText) contentView.findViewById(R.id.etxt_msg_on);
			final RadioButton strOnOff = (RadioButton) contentView.findViewById(R.id.radio_hex_off);
			final RadioButton hexOnOff = (RadioButton) contentView.findViewById(R.id.radio_hex_off);
			final RadioButton strOnOn = (RadioButton) contentView.findViewById(R.id.radio_hex_on);
			final RadioButton hexOnOn = (RadioButton) contentView.findViewById(R.id.radio_hex_on);

			
			//编辑窗口弹出后设置每个组件的显示内容
			lableOff.setText(cbtn.getTextOff());
			lableOn.setText(cbtn.getTextOn());
			if(btMsgOff[index_button].equals(FLAG_LABLE)){
				msgOff.setText("");
			}
			else{
				msgOff.setText(btMsgOff[index_button]);
			}
			if(btMsgOn[index_button].equals(FLAG_LABLE)){
				msgOn.setText("");
			}
			else{
				msgOn.setText(btMsgOn[index_button]);
			}
			if(hexOn_Off[index_button].equals(HEX_ON)){
				strOnOff.setChecked(false);
				hexOnOff.setChecked(true);
			}
			if(hexOn_On[index_button].equals(HEX_ON)){
				strOnOn.setChecked(false);
				hexOnOn.setChecked(true);
			}
			//取消按钮添加监听器
            contentView.findViewById(R.id.tbutton_cancle).setOnClickListener(
            		new OnClickListener(){
            			public void onClick(View v) {
		            		inputKeybord.hideSoftInputFromWindow(contentView.getWindowToken(), 0);
	            			btnEditSheet.dismiss();		
            			}
            		});
            //去顶按钮添加监听器
            contentView.findViewById(R.id.tbutton_ok).setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v) {
            		inputKeybord.hideSoftInputFromWindow(contentView.getWindowToken(), 0);
        			cbtn.setTextOff(lableOff.getText().toString());
        			cbtn.setTextOn(lableOn.getText().toString());
        			if(cbtn.isChecked()){
        				cbtn.setText(cbtn.getTextOn());
        			}
        			else{
        				cbtn.setText(cbtn.getTextOff());
        			}
        			//按钮编辑完成后保存编辑的信息
        			btLabOff[index_button] = lableOff.getText().toString();
        			btLabOn[index_button] = lableOn.getText().toString();
        			btMsgOff[index_button] = msgOff.getText().toString();
        			btMsgOn[index_button] = msgOn.getText().toString();
        			if(hexOnOff.isChecked()){
        				hexOn_Off[index_button] = HEX_ON;
        			}
        			else{
        				hexOn_Off[index_button] = HEX_OFF;
        			}
        			if(hexOnOn.isChecked()){
        				hexOn_On[index_button] = HEX_ON;
        			}
        			else{
        				hexOn_On[index_button] = HEX_OFF;
        			}
        			//将信息保存到手机文件中
    				mFileManager.writeTxtFile(tabToCsvString(btLabOff),MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_btLabOff",false);
    				mFileManager.writeTxtFile(tabToCsvString(btLabOn),MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_btlabOn",false);
    				mFileManager.writeTxtFile(tabToCsvString(btMsgOff),MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_btMsgOff",false);
    				mFileManager.writeTxtFile(tabToCsvString(btMsgOn),MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_btMsgOn",false);
    				mFileManager.writeTxtFile(tabToCsvString(hexOn_Off),MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_hexOnOff",false);
    				mFileManager.writeTxtFile(tabToCsvString(hexOn_On),MyFileManager.internalSdCard+BluetoothChat.myAppFolder+"/_hexOnOn",false);
        			
                	btnEditSheet.dismiss();
            		
        		}
            });
		btnEditSheet.show();
    }
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
    	BluetoothChat.mBluetoothChat.onKeyDown(keyCode, event);
        return false;  
          
    }
    /*------------------------------------------------
	   查表
	------------------------------------------------*/
	private int serchTab(int mbyte,int[] tab){ 
		int i=0;
		for(;i<tab.length;i++){
			if(tab[i]==mbyte){
				return i;
			}
		}
		return -1;
	} 
	//将字符串数组转换成csv文件格式的字符串
	private String tabToCsvString(String[] str0){
		String str1 = str0[0];
		for(int i=1;i<str0.length;i++){
			str1 += ","+str0[i];
		}
		return str1;
	}
}
