package com.lichao.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	// Debugging
	private static final String TAG = "BluetoothChat";
	public static final boolean Debuggable = true;
	public static String myAppFolder = "/Bluetooth/data";
	public static String usedDeviceAddress = "";
	final MyFileManager mFileManager = new MyFileManager();

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String MESSAGE_STR = "message_string";
	public static final String TOAST = "toast";

	// Intent request codes
	static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_START_PWD = 4;
	public static boolean hexsend = false;
	public static boolean hexReceive = false;
	public static BluetoothChat mBluetoothChat = null;

	ActionSheet cActionSheet = null;
	private PopupWindow popupWindow_top = null;
	private ActionSheet mActionSheet;
	// Layout Views
	private TextView mTitle;
	private Handler mHandler;
	private ScrollView svResult;
	private LinearLayout layout_div;
	private TextView txt_disconnect;
	ImageView img1, img2;

	ViewPager pager = null;
	TabHost tabHost = null;
	Context context = null;
	LocalActivityManager manager = null;
	private int currIndex = 0;// 当前页卡编号

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	BluetoothChatService mChatService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Debuggable)
			Log.d(TAG, "+++ ON CREATE +++");
		if (Debuggable)
			Log.d(TAG, android.os.Build.MODEL);// 手机型号
		if (Debuggable)
			Log.d(TAG, android.os.Build.VERSION.RELEASE);// 系统版本号
		setContentView(R.layout.activity_bluetooth_chat);
		context = this;
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
		img1 = (ImageView) findViewById(R.id.img_terminal);
		img2 = (ImageView) findViewById(R.id.img_keyboard);

		LinearLayout l1 = (LinearLayout) findViewById(R.id.tab_terminal);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.tab_keyboard);
		l1.setOnClickListener(new MyOnClickListener(0));
		l2.setOnClickListener(new MyOnClickListener(1));
		pager = (ViewPager) findViewById(R.id.viewpage);
		final ArrayList<View> list = new ArrayList<View>();
		Intent intent1 = new Intent(context, Terminal.class);
		Intent intent2 = new Intent(context, EditKeybord.class);
		list.add(getView("A", intent1));
		list.add(getView("B", intent2));
		pager.setAdapter(new MyPagerAdapter(list));
		pager.setCurrentItem(0);
		img2.setVisibility(View.GONE);
		pager.setOnPageChangeListener(new MyOnPageChangeListener());

		mBluetoothChat = this;
		if (mFileManager.isFileExist("/usedDeviceData",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			usedDeviceAddress = StartActivity.mFileManager
					.readTxtFile(MyFileManager.internalSdCard
							+ BluetoothChat.myAppFolder + "/usedDeviceData");
		}
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);// 获取标题左侧文本视图（对象）
		mTitle.setText(R.string.app_name);// 将标题左侧文本设置为应用名称
		mTitle = (TextView) findViewById(R.id.title_right_text);// 获取标题右侧文本视图（对象），待后文根据状态设置文本内容

		layout_div = (LinearLayout) findViewById(R.id.div_v);
		txt_disconnect = (TextView) findViewById(R.id.txt_disconnect);
		txt_disconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mChatService.disconnect();
			}
		});

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 获取系统默认的蓝牙适配器

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {// 如果蓝牙适配器为空（机器上没有蓝牙适配器）
			Toast.makeText(this, R.string.no_bt, Toast.LENGTH_LONG).show();// 提示机器上没有蓝牙
			finish();// 结束当前Activity，即结束应用程序.
			return;
		}

		// The Handler that gets information back from the BluetoothChatService
		mHandler = new Handler() {// 在启动蓝牙聊天程序时会新建一个服务器对象，同时把此对象传给服务器

			@Override
			// 服务器会在应用程序状态发生改变时（连接、发送信息等）调用此对象的方法，通知主视图更新UI
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MESSAGE_STATE_CHANGE:// 状态发生改变
					if (Debuggable)
						Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1) {
					case BluetoothChatService.STATE_CONNECTED:// 蓝牙设备已连接
						mTitle.setText(R.string.title_connected_to);// 将标题右侧字符串设置为R.string.title_connected_to
						mTitle.append(mConnectedDeviceName);// 将连接到的蓝牙设备的名称追加到标题右侧字符串的末尾
						layout_div.setVisibility(View.VISIBLE);
						txt_disconnect.setVisibility(View.VISIBLE);
						break;
					case BluetoothChatService.STATE_CONNECTING:// 正在连接蓝牙设备
						mTitle.setText(R.string.title_connecting);// 将标题右侧字符串设置为R.string.title_connecting
						break;
					case BluetoothChatService.STATE_LISTEN:// 服务器正在监听
					case BluetoothChatService.STATE_NONE:// 服务器没有进行任何操作
						mTitle.setText(R.string.title_not_connected);// 将标题右侧字符串设置为R.string.title_not_connected
						layout_div.setVisibility(View.GONE);
						txt_disconnect.setVisibility(View.GONE);
						break;
					}
					break;
				case MESSAGE_DEVICE:// 蓝牙设备
					// save the connected device's name
					mConnectedDeviceName = ((BluetoothDevice) msg.obj)
							.getName();// 获取已连接的蓝牙设备的名称
					saveUsedBluetooth(((BluetoothDevice) msg.obj).getAddress());// 成功执行后，保存为上次使用的设备
					Toast.makeText(
							getApplicationContext(),
							getText(R.string.connected_to)
									+ mConnectedDeviceName, Toast.LENGTH_SHORT)
							.show();// 提示已连接到蓝牙设备
					break;
				case MESSAGE_TOAST:// 提示
					if (((String) msg.obj).toString().equals(
							"unable_to_connect"))
						Toast.makeText(getApplicationContext(),
								R.string.unable_to_connect, Toast.LENGTH_SHORT)
								.show();
					if (((String) msg.obj).toString().equals("lost_connect")) {
						Toast.makeText(getApplicationContext(),
								R.string.lost_connect, Toast.LENGTH_SHORT)
								.show();

					}
					break;
				}
			}
		};
		final Button bt_popMenu = (Button) findViewById(R.id.btn_popmenu);
		bt_popMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				myPopupMenu(view);
			}
		});

	}

	private void startService() {
		if (mChatService == null) {
			mChatService = new BluetoothChatService(this, mHandler);// 新建一个蓝牙聊天服务器
			mOutStringBuffer = new StringBuffer("");// 新建一个发送字符串的缓存器
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {// 如果服务器还没有执行任何操作（即新建了一个服务器）
				// Start the Bluetooth chat services
				mChatService.start();// 启动聊天服务器
				tryToConnectUsedDevice();
			}
		}
		if (mChatService != null) { // 如果蓝牙已经打开而聊天服务器还没有启动
			mChatService.setHandler(mHandler);
		}

	}

	@Override
	public void onStart() {// 紧接着onCreate()而执行
		super.onStart();
		if (Debuggable)
			Log.d(TAG, "++ ON START ++");
		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);// 注册广播接收器
	}

	@Override
	public synchronized void onResume() {// synchronized修饰类方法，表示在当前对象中，在同一时间只能有一个线程执行这个方法
		super.onResume(); // 该方法在ACTION_REQUEST_ENABLE activity返回时执行
		if (Debuggable)
			Log.d(TAG, "+ ON RESUME +");
		if (!mBluetoothAdapter.isEnabled()) {// 如果蓝牙适配器没有打开
			mBluetoothAdapter.enable();// 也可以用直接打开，但是需要系统权限,用户没有选择则会一直停留在此
		} else {
			startService();
		}
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON
						&& mBluetoothAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON) {
					Toast.makeText(getApplicationContext(),
							"蓝牙没有打开，应用程序将在5秒后关闭", Toast.LENGTH_SHORT).show();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 5000); // 延迟5秒跳转
				}
			}
		}, 2000); // 延迟5秒跳转
	}

	@Override
	public synchronized void onPause() {// onPause
										// 用于由一个Activity转到另一个Activity、设备进入休眠状态(屏幕锁住了)、或者有dialog弹出时
		super.onPause();
		if (Debuggable)
			Log.d(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (Debuggable)
			Log.d(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {// 应用结束时最后被被执行的程序，finish()方法会调用此方法
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();// 关闭服务器
		if (Debuggable)
			Log.d(TAG, "--- ON DESTROY ---");
		this.unregisterReceiver(mReceiver);// 注销广播接收器
		System.exit(0);// 完全退出应用程序，安卓系统会清理应用程序占用的所有资源
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();// 接收广播内容
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {//
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
					Toast.makeText(getApplicationContext(),
							"蓝牙已关闭，应用程序将在5秒后关闭", Toast.LENGTH_SHORT).show();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 5000); // 延迟5秒跳转
				}
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
					startService();
				}
			}
		}// 广播接收器

	};

	private void ensureDiscoverable() {// 使能可被发现
		if (Debuggable)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {// SCAN_MODE_CONNECTABLE_DISCOVERABLE表示设备既可以被远程蓝牙设备发现，也可以被其连接
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 请求蓝牙设备可被发现
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);// 设置蓝牙设备可被发现的持续时间
			startActivity(discoverableIntent);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {// startActivityForResult()启动的另一个Activity结束之后返回结果
		// 返回的结果传给此方法并执行此方法
		if (Debuggable)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:// 请求加密连接蓝牙设备
			// When DeviceListActivity returns with a device to connect
			// 当DeviceListActivity连接蓝牙设备返回时执行
			if (resultCode == Activity.RESULT_OK) {// 请求被接受
				connectDevice(data, true);// 加密连接蓝牙设备
			}
			break;
		}
	}

	public void connectDevice(Intent data, boolean secure) {// 连接蓝牙设备
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);// 目的蓝牙设备的地址
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);// 获取远程蓝牙设备
		// Attempt to connect to the device
		mChatService.connect(device, secure);// 连接蓝牙设备
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {// 创建一个安卓系统菜单键的菜单
		if (Debuggable)
			Log.d("mengdd", "onCreateMenu");
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Debuggable)
			Log.d("mengdd", "onKey");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);// 程序后台运行
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			myMenu();
			return true;
		}

		return false;

	}

	private void myPopupMenu(View view) {
		// 一个自定义的布局，作为显示的内容
		View contentView = LayoutInflater.from(getApplicationContext())
				.inflate(R.layout.pop_menu, null);
		if (popupWindow_top == null) {
			popupWindow_top = new PopupWindow(contentView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		} else {
			popupWindow_top.showAsDropDown(view, -140, 10);
			final WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.alpha = 0.8f;
			getWindow().setAttributes(lp);
			return;
		}

		// 设置允许在外点击消失
		popupWindow_top.setTouchable(true);
		popupWindow_top.setOutsideTouchable(true);
		// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
		// 我觉得这里是API的一个bug
		popupWindow_top.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_bg_0));
		popupWindow_top.setAnimationStyle(R.style.my_pop_anim);

		// 设置好参数之后再show
		popupWindow_top.showAsDropDown(view, -140, 10);
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.8f;
		getWindow().setAttributes(lp);
		popupWindow_top.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				lp.alpha = 1f;
				getWindow().setAttributes(lp);
			}
		});
		OnClickListener popMenuOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.layout_popmenu_scan:
					startListActivity();
					break;

				case R.id.layout_popmenu_save:
					saveRecText(Terminal.getRecText());
					break;

				case R.id.layout_popmenu_set:
					Intent serverIntent = new Intent(getApplicationContext(),
							MoreSetActivity.class);// Intent（意图）主要是解决Android应用的各项组件之间的通讯。
					startActivity(serverIntent);
					break;
				case R.id.layout_popmenu_friends:
					MyLockerActivity.PWD_CMD = MyLockerActivity.PWD_RESET;
					serverIntent = new Intent(getApplicationContext(),
							MyLockerActivity.class);
					startActivityForResult(serverIntent, REQUEST_START_PWD);
					break;
				}
				popupWindow_top.dismiss();
			}

		};
		contentView.findViewById(R.id.layout_popmenu_scan).setOnClickListener(
				popMenuOnClickListener);
		contentView.findViewById(R.id.layout_popmenu_friends)
				.setOnClickListener(popMenuOnClickListener);
		contentView.findViewById(R.id.layout_popmenu_save).setOnClickListener(
				popMenuOnClickListener);
		contentView.findViewById(R.id.layout_popmenu_set).setOnClickListener(
				popMenuOnClickListener);
	}

	private void myMenu() {
		// 一个自定义的布局，作为显示的内容
		View contentView = LayoutInflater.from(getApplicationContext())
				.inflate(R.layout.my_menu, null);
		contentView.setMinimumWidth(getWallpaperDesiredMinimumWidth());
		if (mActionSheet == null) {
			mActionSheet = new ActionSheet(this, Gravity.BOTTOM,
					R.style.ActionSheetDialog);
			mActionSheet.setContentView(contentView);
			mActionSheet.setAnimation(R.style.ActionSheetDialogAnimation);
			mActionSheet.setCancelable(true);
			mActionSheet.setCanceledOnTouchOutside(true);
			mActionSheet.getWindow().setDimAmount(0.3f);
		}
		mActionSheet.show();
		OnClickListener popMenuOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.layout_pop_update:
					// Log.i("mengdd", "onTouch : 版本更新");
					mActionSheet.dismiss();
					break;

				case R.id.layout_pop_feedback:
					mActionSheet.dismiss();
					Intent serverIntent = new Intent(getApplicationContext(),
							FeedBackActivity.class);// Intent（意图）主要是解决Android应用的各项组件之间的通讯。
					startActivity(serverIntent);
					break;

				case R.id.layout_pop_about:
					// Log.i("mengdd", "onTouch : 关于");
					mActionSheet.dismiss();
					serverIntent = new Intent(getApplicationContext(),
							AboutActivity.class);// Intent（意图）主要是解决Android应用的各项组件之间的通讯。
					startActivity(serverIntent);

					break;

				case R.id.layout_pop_exit:
					exitDialog();
					// Log.i("mengdd", "onTouch : 退出QQ");
					break;

				case R.id.layout_pop_cancle:
					// Log.i("mengdd", "onTouch : 取消");
					break;
				}
				mActionSheet.dismiss();
			}

		};
		contentView.findViewById(R.id.layout_pop_update).setOnClickListener(
				popMenuOnClickListener);
		contentView.findViewById(R.id.layout_pop_feedback).setOnClickListener(
				popMenuOnClickListener);
		contentView.findViewById(R.id.layout_pop_about).setOnClickListener(
				popMenuOnClickListener);
		contentView.findViewById(R.id.layout_pop_exit).setOnClickListener(
				popMenuOnClickListener);
		contentView.findViewById(R.id.layout_pop_cancle).setOnClickListener(
				popMenuOnClickListener);
	}

	private void exitDialog() {
		// 一个自定义的布局，作为显示的内容
		final View contentView = LayoutInflater.from(getApplicationContext())
				.inflate(R.layout.exitdialog, null);
		contentView.setMinimumWidth(getWindowManager().getDefaultDisplay()
				.getWidth() * 5 / 6);
		if (cActionSheet == null) {
			cActionSheet = new ActionSheet(this, Gravity.CENTER,
					R.style.ActionSheetDialog);
			cActionSheet.setGravity(Gravity.CENTER);
			cActionSheet.setContentView(contentView);
			cActionSheet.setCancelable(true);
			cActionSheet.setCanceledOnTouchOutside(true);
			cActionSheet.getWindow().setDimAmount(0.3f);
			contentView.findViewById(R.id.layout_exit_cancle)
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							cActionSheet.dismiss();
						}
					});
			contentView.findViewById(R.id.layout_exit_ok).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (((CheckBox) contentView
									.findViewById(R.id.cb_exit_closebt))
									.isChecked()) {
								mBluetoothAdapter.disable();
							}
							cActionSheet.dismiss();
							finish();
						}
					});
		}
		cActionSheet.show();
	}

	private boolean saveRecText(String recText) {

		if (recText.equals("")) {
			Toast.makeText(getApplicationContext(), "接收数据为空，未保存",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			mFileManager.createFile("/" + mFileManager.getDefaultFileName()
					+ ".txt", MyFileManager.internalSdCard
					+ BluetoothChat.myAppFolder);
			mFileManager.writeTxtFile(recText,
					MyFileManager.internalSdCard + BluetoothChat.myAppFolder
							+ "/" + mFileManager.getDefaultFileName() + ".txt",
					false);
			Toast.makeText(getApplicationContext(),
					"接收数据以保存为" + mFileManager.getDefaultFileName() + ".txt",
					Toast.LENGTH_SHORT).show();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	private void saveUsedBluetooth(String address) {
		if (mFileManager.isFileExist("/usedDeviceData",
				MyFileManager.internalSdCard + BluetoothChat.myAppFolder)) {
			mFileManager.writeTxtFile(address, MyFileManager.internalSdCard
					+ BluetoothChat.myAppFolder + "/usedDeviceData", false);
		}
	}

	private void tryToConnectUsedDevice() {
		if (usedDeviceAddress.equals("")) {
			if (Debuggable)
				Log.d("调试信息", "没有保存信息，不尝试连接");
			// 没有保存信息，不展示最近使用模块
		} else {
			if (Debuggable)
				Log.d("调试信息", "尝试连接最近使用设备");
			BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(usedDeviceAddress);
			if (mChatService != null) {
				mChatService.connect(device, true);// 连接蓝牙设备
			} else {
				Log.e("空", "mChatService=null");
			}
		}
	}

	public void startListActivity() {
		Intent serverIntent = new Intent(getApplicationContext(),
				DeviceListActivity.class);// Intent（意图）主要是解决Android应用的各项组件之间的通讯。
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);// 弹出对话框请求加密连接蓝牙设备，由用户进行确认操作
		// 选择后 对话框会返回结果，然后执行onActivityResult();
	}

	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	public class MyPagerAdapter extends PagerAdapter {
		List<View> list = new ArrayList<View>();

		public MyPagerAdapter(ArrayList<View> list) {
			this.list = list;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			ViewPager pViewPager = ((ViewPager) arg0);
			pViewPager.addView(list.get(arg1));
			return list.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			pager.setCurrentItem(index);
		}
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {

			switch (currIndex) {
			case 0:
				img1.setVisibility(View.GONE);
				break;
			case 1:
				img2.setVisibility(View.GONE);
				break;
			case 2:
				break;
			case 3:
				break;
			}

			currIndex = arg0;

			switch (currIndex) {
			case 0:
				img1.setVisibility(View.VISIBLE);
				break;
			case 1:
				img2.setVisibility(View.VISIBLE);
				break;
			case 2:
				break;
			case 3:
				break;
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

}
