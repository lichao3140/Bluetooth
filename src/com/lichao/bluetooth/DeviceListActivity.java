package com.lichao.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends Activity {// 设备列表视图
	// Debugging
	private static final String TAG = "DeviceListActivity";

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private boolean isDiscoverable;
	private BluetoothAdapter mBtAdapter;
	private MyExpandableListAdapter mPairedDevicesAdapter;
	private String myBluetoothName;
	private String myBluetoothAddress;
	private ExpandableListView pListView;
	private TextView tv_myDeviceName = null;
	private TextView tv_myDeviceState = null;
	private CheckBox cb_isDiscoverable = null;
	private CountDownTimer discoveryableTimer;

	private ArrayList<HashMap<String, Object>> parentListItemData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<ArrayList<HashMap<String, Object>>> childenListItemData = new ArrayList<ArrayList<HashMap<String, Object>>>();/* 在数组中存放已配对蓝牙设备数据 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		setContentView(R.layout.activity_device_list);// 设置视图组件

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);// 设置DeviceListActivity被调用后的返回结果
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();// 获取本地蓝牙适配器

		tv_myDeviceName = (TextView) findViewById(R.id.tv_myDeviceName);// 本地蓝牙适配器名称
		tv_myDeviceState = (TextView) findViewById(R.id.tv_myDeviceState);// 本地蓝牙适配器可见状态
		cb_isDiscoverable = (CheckBox) findViewById(R.id.cb_isDiscoverable);// 勾选使能蓝牙可被发现
		this.getMyBTInfo();// 获得本机蓝牙基础信息
		tv_myDeviceName.setText(myBluetoothName);
		tv_myDeviceState.setText(myBluetoothAddress);
		if (mBtAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			tv_myDeviceState.setText(myBluetoothAddress + "(可见)");

			// 是真正改名可见状态时，checkbox不需要恢复原装，通过isChangeToDiscoverable控制
			cb_isDiscoverable.setChecked(true);
			cb_isDiscoverable.setEnabled(false);

		} else {
			tv_myDeviceState.setText(myBluetoothAddress + "(不可见)");
			cb_isDiscoverable.setChecked(false);
			cb_isDiscoverable.setEnabled(true);
		}
		cb_isDiscoverable
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// Auto-generated method stub
						if (isChecked) {
							discoveryable();
						}
					}
				});
		RelativeLayout myInfoLayout = (RelativeLayout) findViewById(R.id.layout_MyDeviceInfo);
		myInfoLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!cb_isDiscoverable.isChecked()) {
					discoveryable();
				}
			}
		});
		// Initialize the button to perform device discovery
		final Button scanButton = (Button) findViewById(R.id.button_scan);// 获取自定义的搜索按钮
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBtAdapter.isDiscovering()) {
					mBtAdapter.cancelDiscovery();
					scanButton.setText(R.string.button_scan);
					findViewById(R.id.txt_state_search)
							.setVisibility(View.GONE);
				} else {
					doDiscovery();// 执行搜索
					scanButton.setText(R.string.button_stop_scan);
					findViewById(R.id.txt_state_search).setVisibility(
							View.VISIBLE);
				}
			}
		});

		discoveryableTimer = new CountDownTimer(120000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				tv_myDeviceState.setText(myBluetoothAddress + "("
						+ millisUntilFinished / 1000 + ")");
			}

			@Override
			public void onFinish() {
				tv_myDeviceState.setText(myBluetoothAddress + "(不可见)");
			}
		};

		HashMap<String, Object> map1 = new HashMap<String, Object>();
		map1.put("parentName", "已配对设备");
		map1.put("count", "0");
		parentListItemData.add(map1);
		HashMap<String, Object> map2 = new HashMap<String, Object>();
		map2.put("parentName", "搜索到的设备");
		map2.put("count", "0");
		parentListItemData.add(map2);
		childenListItemData.add(new ArrayList<HashMap<String, Object>>());
		childenListItemData.add(new ArrayList<HashMap<String, Object>>());

		mPairedDevicesAdapter = new MyExpandableListAdapter(
				getApplicationContext(), parentListItemData,
				R.layout.list_item_parent,
				new String[] { "parentName", "count" }, new int[] {
						R.id.txt_parentName, R.id.txt_count },
				childenListItemData, R.layout.list_item_childen, new String[] {
						"typeImage", "deviceName", "deviceAddress",
						"onRightImage" }, new int[] { R.id.img_deviceType_1,
						R.id.txt_deviceName_1, R.id.txt_deviceAddress_1,
						R.id.img_onright_1 });// 已配对设备列表

		pListView = (ExpandableListView) findViewById(R.id.expandableListView1);// 已配对设备显示组件
		pListView.setAdapter(mPairedDevicesAdapter);
		pListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View view,
					int groupPosition, int childPosition, long id) {
				mBtAdapter.cancelDiscovery();// 取消搜索
				String address = (String) childenListItemData
						.get(groupPosition).get(childPosition)
						.get("deviceAddress");
				// Create the result Intent and include the MAC address
				Intent intent = new Intent();
				intent.putExtra(EXTRA_DEVICE_ADDRESS, address);// 通知其他Activity被点击的蓝牙设备的地址

				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);// 设置该Activity返回的结果和数据（蓝牙地址）
				finish();// 结束这个Activity
				return false;
			}
		});

		Set<BluetoothDevice> pairedDevices1 = mBtAdapter.getBondedDevices();// 获取已配对的蓝牙设备
		if (pairedDevices1.size() > 0) {// 如果有已配对的蓝牙设备
			for (BluetoothDevice device : pairedDevices1) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE) {
					map.put("typeImage", R.drawable.phone);
				} else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER) {
					map.put("typeImage", R.drawable.notebook);
				} else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
					map.put("typeImage", R.drawable.headphone);
				} else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.WEARABLE) {
					map.put("typeImage", R.drawable.watch);
				} else {
					map.put("typeImage", R.drawable.machine);
				}
				map.put("deviceName", device.getName());
				map.put("deviceAddress", device.getAddress());
				map.put("onRightImage", R.drawable.onright);
				childenListItemData.get(0).add(map);
			}
			pListView.expandGroup(0);
			mPairedDevicesAdapter.notifyDataSetChanged();
		}

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);// 注册广播接收器，当系统有BluetoothDevice.ACTION_FOUND的广播发出时，会执行mReceiver

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);// 注册广播接收器

		filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		this.registerReceiver(mReceiver, filter);// 注册广播接收器

		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);// 注册广播接收器

	}

	@Override
	protected void onDestroy() {// 视图被关闭时执行
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();// 取消搜索
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);// 注销广播接收器
	}

	public void getMyBTInfo() {
		if (mBtAdapter != null && mBtAdapter.isEnabled()) {
			myBluetoothName = mBtAdapter.getName();
			myBluetoothAddress = mBtAdapter.getAddress();
			if (mBtAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				isDiscoverable = true;
			}
		}
	}

	private void discoveryable() {// 使蓝牙设备可见
		if (mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {// SCAN_MODE_CONNECTABLE_DISCOVERABLE表示设备既可以被远程蓝牙设备发现，也可以被其连接
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 请求蓝牙设备可被发现
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);// 设置蓝牙设备可被发现的持续时间
			startActivity(discoverableIntent);
			discoveryableTimer.start();
		}
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {// 搜索蓝牙设备
		if (BluetoothChat.Debuggable)
			Log.d(TAG, "doDiscovery()");

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {// 如果正在搜索
			mBtAdapter.cancelDiscovery();// 取消搜索
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();// 开始搜索蓝牙设备
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View view, int arg2,
				long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();// 取消搜索

			TextView tv = (TextView) view.findViewById(R.id.tv_deviceAddress);
			String address = tv.getText().toString();
			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);// 通知其他Activity被点击的蓝牙设备的地址

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);// 设置该Activity返回的结果和数据（蓝牙地址）
			finish();// 结束这个Activity
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {// 广播接收器
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();// 接收广播内容

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {// 如果找到设备
				updateFoundDevicesListView(intent);
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {// 如果搜索完成
				Button scanButton = (Button) findViewById(R.id.button_scan);
				scanButton.setText(R.string.button_scan);
				findViewById(R.id.txt_state_search).setVisibility(View.GONE);
			} else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
				if (mBtAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					// 是真正改名可见状态时，checkbox不需要恢复原装，通过isChangeToDiscoverable控制
					cb_isDiscoverable.setChecked(true);
					cb_isDiscoverable.setEnabled(false);

				} else {
					tv_myDeviceState.setText(myBluetoothAddress + "(不可见)");
					cb_isDiscoverable.setChecked(false);
					cb_isDiscoverable.setEnabled(true);
				}
			}
		}
	};

	/**
	 * 
	 * Desc: 更新listview Params:mSimpleAdapter 适配器 listview列表 Return:void
	 */
	public void updateListView(SimpleAdapter mSimpleAdapter, ListView listview) {
		mSimpleAdapter.notifyDataSetChanged();
		// setListViewHeightBasedOnChildren(listview);
	}

	/**
	 * 
	 * Desc: 更新搜索设备listview Params:intent 适配器搜索蓝牙的系统广播 Return:void
	 */
	public void updateFoundDevicesListView(Intent intent) {
		BluetoothDevice device = intent
				.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		if (device.getBondState() != BluetoothDevice.BOND_BONDED)// 如果没有配对将设备名称和地址放入array
		{
			if (BluetoothChat.Debuggable)
				Log.d(TAG, device.getAddress());
			for (int i = 0; i < childenListItemData.get(1).size(); i++) {
				String str = (String) childenListItemData.get(1).get(i)
						.get("deviceAddress");
				if (str.equalsIgnoreCase(device.getAddress())) {// 列表中已经存在
					return;
				}
			}

			HashMap<String, Object> map = new HashMap<String, Object>();
			if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE) {
				map.put("typeImage", R.drawable.phone);
			} else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER) {
				map.put("typeImage", R.drawable.notebook);
			} else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
				map.put("typeImage", R.drawable.headphone);
			} else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.WEARABLE) {
				map.put("typeImage", R.drawable.watch);
			} else {
				map.put("typeImage", R.drawable.machine);
			}

			map.put("deviceName", device.getName());
			map.put("deviceAddress", device.getAddress());
			map.put("onRightImage", R.drawable.onright);
			childenListItemData.get(1).add(map);
			pListView.expandGroup(1);
			mPairedDevicesAdapter.notifyDataSetChanged();
		}

	}
}
