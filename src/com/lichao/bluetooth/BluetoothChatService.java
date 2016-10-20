package com.lichao.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private Context mContext;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
    	mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙适配器
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
    	if(BluetoothChat.Debuggable) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();//通知主视图服务器的当前状态
        Terminal.handler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();//通知主视图服务器的当前状态
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {//synchronized修饰类方法，表示在当前对象中，在同一时间只能有一个线程执行这个方法
    	if(BluetoothChat.Debuggable) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection，取消所有企图连接的线程
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        	}

        // Cancel any thread currently running a connection，取消所有正在运行的连接线程
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        	}

        setState(STATE_LISTEN);//设置服务器状态为正在监听

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {//mSecureAcceptThread 用于监听加过密的连接请求，mInsecureAcceptThread 用于监听未加密的连接请求。
        									//它们最大的区别在于：创建服务器套接字时，使用的函数不同，代码在 AcceptThread 的构造函数里
            mSecureAcceptThread = new AcceptThread(true);//新建一个监听加密连接请求的线程
            mSecureAcceptThread.start();//监听线程启动
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
    	if(BluetoothChat.Debuggable) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection，取消所有企图连接的线程
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}//取消正在连接的线程
        }

        // Cancel any thread currently running a connection，取消所有正在运行的连接线程
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}//取消已经连接的线程

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);//新建一个蓝牙连接线程
        mConnectThread.start();//启动蓝牙连接线程
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {//已连接处理函数
    	if(BluetoothChat.Debuggable) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection，取消已完成的连接线程
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        	}

        // Cancel any thread currently running a connection，取消正在运行的连接线程
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        	}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {//取消接受线程，因为我们只想连接一个设备
            mSecureAcceptThread.cancel();//取消接受线程
            mSecureAcceptThread = null;//删除接受线程
        }

        mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE,device).sendToTarget();

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);//新建一个已连接线程
        mConnectedThread.start();//启动已连接线程

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {//停止服务器
    	if(BluetoothChat.Debuggable) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();//取消连接线程
            mConnectThread = null;//删除连接线程
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();//取消已连接线程
            mConnectedThread = null;//删除已连接线程
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();//取消加密接受线程
            mSecureAcceptThread = null;//删除加密接受线程
        }

        setState(STATE_NONE);//改变服务器状态
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {//synchronized修饰代码区块，表示在当前对象中，在同一时间只能有一个线程执行大括号中的代码
            if (mState != STATE_CONNECTED) return;//如果设备没有连接则返回
            r = mConnectedThread;//获取蓝牙通信的连接线程
        }
        // Perform the write unsynchronized
        r.write(out);//通过蓝牙通信连接线程的写数据方法把数据发送出去
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {//蓝牙连接失败处理函数
        mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST,"unable_to_connect").sendToTarget();
        // Start the service over to restart listening mode
        BluetoothChatService.this.start();//重新启动服务器
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {//连接断开
        mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST, "lost_connect").sendToTarget();

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();//重新启动服务器
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {//接受连接线程
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = "Secure";//判断是加密连接还是不加密连接

            // Create a new listening server socket
            try {
                if(mAdapter.isEnabled()){
                	tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                    MY_UUID_SECURE);//创建加密连接
                }
                else{
                    Log.e(TAG, "mAdapter is disabled");
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
        	if(BluetoothChat.Debuggable) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED&&mmServerSocket!=null) {//如果服务器还没有连接，确保服务器只有一个连接
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();//等待连接，这是一个阻塞方法，没有连接请求会一直停在此处，知道有新的连接请求
                    mState = STATE_CONNECTING;
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {//如果已经建立socket
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());//连接处理
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();//关闭socket
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if(BluetoothChat.Debuggable) Log.d(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
        	if(BluetoothChat.Debuggable) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                if(mmServerSocket!=null){
                	mmServerSocket.close();//关闭服务器socket
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {//连接蓝牙设备的线程
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {//连接蓝牙设备的线程类的构造函数
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);//生成一个加密的BluetoothSocket
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
        	if(BluetoothChat.Debuggable) Log.d(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");//设置线程的名称

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();//取消搜索设备，蓝牙连接前必须取消搜索

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();//BluetoothSocket连接
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();//如果发生异常(连接失败)则将BluetoothSocket关闭
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }
                connectionFailed();//蓝牙连接失败处理
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();//关闭socket
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {//连接以后获取输入输出流
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
        	if(BluetoothChat.Debuggable) Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();//获取输入流
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {//获取输出流
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            setState(STATE_CONNECTED);//改变服务器状态
        }

        public void run() {
        	if(BluetoothChat.Debuggable) Log.d(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);//读取接收到的信息

                    // Send the obtained bytes to the UI Activity
                    Terminal.handler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
                    .sendToTarget();//将接收到的信息发送到主视图
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();//连接断开
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);//写数据

                // Share the sent message back to the UI Activity
                Terminal.handler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();//向主视图发送写出的数据
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();//关闭socket
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    public void disconnect(){
    	this.stop();
    }

	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}
}
