package com.burns.android.transfercontact;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothCommandService {
	// Debugging
    private static final String TAG = "BluetoothCommandService";
    private static final boolean D = true;
    
    // Unique UUID for this application
    //PBAP UUID.
    private static final UUID MY_UUID = UUID.fromString("0000112F-0000-1000-8000-00805F9B34FB");
    
    //obex
	private final byte[] init_cmd = { 
			(byte) 0x80, //connect
			(byte) 0x00, // packet length = 31
			(byte) 0x1f, //packet length = 31
			(byte) 0x10, //obex version 1.0
			(byte) 0x00, //flag
			(byte) 0x20, //0x2000 8K is the max OBEX packet size client can accept
			(byte) 0x00, //0x2000 8K is the max OBEX packet size client can accept
			(byte) 0x46, //HI(Header Indentifier) for Target
			(byte) 0x00, //0x0013 Length of TargetHeader
			(byte) 0x13, //0x0013 Length of TargetHeader
			(byte) 0x79, //UUID 796135-F0F0-C511-D809-660800200C9A66 (PBAP Target UUID, please see BluetoothPbapObexServer.java )
			(byte) 0x61,
			(byte) 0x35,
			(byte) 0xf0,
			(byte) 0xf0,
			(byte) 0xc5,
			(byte) 0x11,
			(byte) 0xd8,
			(byte) 0x09,
			(byte) 0x66,
			(byte) 0x08,
			(byte) 0x00,
			(byte) 0x20,
			(byte) 0x0c,
			(byte) 0x9a,
			(byte) 0x66, //UUID end
			(byte) 0xc3, //HI for Lengthheader (optional header)
			(byte) 0x00, //total length of hex F483 bytes
			(byte) 0x00,
			(byte) 0xf4,
			(byte) 0x83};
	
	private byte[] get_contactsize_cmd = {
			(byte) 0x83, //get
			(byte) 0x00, // packet length = 79
			(byte) 0x4f, //packet length = 79
			(byte) 0xcb, //HI for connect ID
			(byte) 0x00, //connect id start
			(byte) 0x65, //
			(byte) 0x9c, //
			(byte) 0x5c, //connect id end, please replace connnect id.
			
			(byte) 0x01, //name
			(byte) 0x00, //length of name = 33
			(byte) 0x21, //length of name = 33
			
			(byte) 0x00, //unicode start
			(byte) 0x74,
			(byte) 0x00,
			(byte) 0x65,
			(byte) 0x00,
			(byte) 0x6c,
			(byte) 0x00,
			(byte) 0x65,
			(byte) 0x00,
			(byte) 0x63,
			(byte) 0x00,
			(byte) 0x6f,
			(byte) 0x00,
			(byte) 0x6d,
			(byte) 0x00, 
			(byte) 0x2f,
			(byte) 0x00,
			(byte) 0x70,
			(byte) 0x00,
			(byte) 0x62,
			(byte) 0x00,
			(byte) 0x2e,
			(byte) 0x00,
			(byte) 0x76,
			(byte) 0x00,
			(byte) 0x63,
			(byte) 0x00,
			(byte) 0x66,
			(byte) 0x00,
			(byte) 0x00, //unicode for  telecom/pb.vcf , the last two byte must be 0000
			
			(byte) 0x42, //type
			(byte) 0x00,
			(byte) 0x12, //length of type
			
			(byte) 0x78,
			(byte) 0x2d,
			(byte) 0x62,
			(byte) 0x74, 
			(byte) 0x2f, 
			(byte) 0x70,
			(byte) 0x68,
			(byte) 0x6f,
			(byte) 0x6e,
			(byte) 0x65,
			(byte) 0x62,
			(byte) 0x6f,
			(byte) 0x6f,
			(byte) 0x6b,
			(byte) 0x00, //x-bt/phonebook, the last byte must be 00\
			
			(byte) 0x4c, //app params
			(byte) 0x00, //length of app params = 20
			(byte) 0x14, //length of app params = 20
			
			(byte) 0x06, //vard filter
			(byte) 0x08, //8bit
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00, //mask, 64bit, all 
			
			(byte) 0x07, //vard version
			(byte) 0x01, //length
			(byte) 0x01, //Format 0x01-->3.1, 00-->2.0
			
			(byte) 0x04, //MaxListCount
			(byte) 0x02, //length
			(byte) 0x00,
			(byte) 0x00  //0x0000
	};
	private  byte[] get_cmd = { 
			/*
			//x-bt/vard
			//if send x-bt/vard, server response c30003, it means Forbidden - operation is understood but refused
			(byte) 0x83, //get
			(byte) 0x00, // packet length = 70
			(byte) 0x46, //packet length = 70
			
			(byte) 0xcb, //HI for connect ID
			(byte) 0x00, //connect id start
			(byte) 0x65, //
			(byte) 0x9c, //
			(byte) 0x5c, //connect id end, please replace connnect id.
			
			(byte) 0x01, //name
			(byte) 0x00, //length of name = 33
			(byte) 0x21, //length of name = 33
			
			(byte) 0x00, //unicode start
			(byte) 0x74,  //t
			(byte) 0x00,
			(byte) 0x65,  //e
			(byte) 0x00,
			(byte) 0x6c,  //l
			(byte) 0x00,
			(byte) 0x65,  //e
			(byte) 0x00,
			(byte) 0x63,  //c
			(byte) 0x00,
			(byte) 0x6f,  //o
			(byte) 0x00,
			(byte) 0x6d,  //m
			(byte) 0x00, 
			(byte) 0x2f,  // /
			(byte) 0x00,
			(byte) 0x70,  //p
			(byte) 0x00,
			(byte) 0x62,  //b
			(byte) 0x00,
			(byte) 0x2e,  //.
			(byte) 0x00,
			(byte) 0x76,  //v
			(byte) 0x00,
			(byte) 0x63,  //c
			(byte) 0x00,
			(byte) 0x66,  //f
			(byte) 0x00,
			(byte) 0x00, //unicode for  telecom/pb.vcf , the last two byte must be 0000
			
			(byte) 0x42, //type
			(byte) 0x00,
			(byte) 0x0d, //length of type = 13
			
			(byte) 0x78, //x
			(byte) 0x2d, //-
			(byte) 0x62, //b
			(byte) 0x74, //t
			(byte) 0x2f, // /
			(byte) 0x76, //v
			(byte) 0x63, //c
			(byte) 0x72, //r
			(byte) 0x64, //d
			(byte) 0x00, //x-bt/vcard, the last byte must be 00\
			
			(byte) 0x4c, //app params
			(byte) 0x00, //length of app params = 16
			(byte) 0x10, //length of app params = 16
			
			(byte) 0x06, //vard filter
			(byte) 0x08, //8bit
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00, //mask, 64bit, all 
			
			(byte) 0x07, //vard version
			(byte) 0x01, //length
			(byte) 0x01 //Format 0x01-->3.1, 00-->2.0
			*/

			
			 
			 // x-bt/phonebook
			//if  send x-bt/phonebook(MaxListCount=0xffff), server response 900003, it means continue, I don't how to do the next
			//I change MaxListCount = 0x0000, server response  A0 00 0A 4C 00 07 08 02 00 E1 
            /*
             * 0xA0 //sucess
             * 0x00
             * 0x0A  //packet length=10
             * 0x4c  //app params
             * 0x00
             * 0x07 //length of app params
             * 0x08 //PhonebookSize
             * 0x02 //length of Phonebooksize
             * 0x00
             * 0xE1 //phonebook length= 225, actually my iphone has 224 contact.
             */
             /*
              * I do a experiment, after receiving 0x90, I send GET command again(see getcontinue_cmd), it can get vcard.
              * I change MaxListCount=0x1/0x02, it will get 1/2 vcard.
              * 
A0 00 71 49 00 6E   //0x49 means HI for body end.
42 45 47 49 4E 3A 56 43 41 52 44 //BEGIN:VCARD
0D 0A //回车 换行
56 45 52 53 49 4F 4E 3A 33 2E 30 //VERSION:3.0
0D 0A //回车 换行
46 4E 3A E6 88 91 E7 9A 84 E7 BC 96 E5 8F B7 //FN:
0D 0A 
4E 3A E6 88 91 E7 9A 84 E7 BC 96 E5 8F B7 //N:
0D 0A 
54 45 4C 3B 54 59 50 45 3D 43 45 4C 4C 3A 2B 38 36 31 38 36 31 32 39 34 33 30 30 31 //TEL;TYPE=CELL:+8618612943001
0D 0A 
55 49 44 3A 30 //UID:0
0D 0A 
45 4E 44 3A 56 43 41 52 44 //END:VCARD
0D 0A                   //回车 换行


2:
A0 00 D0 49 00 CD 
42 45 47 49 4E 3A 56 43 41 52 44 //BEGIN:VCARD
0D 0A 
56 45 52 53 49 4F 4E 3A 33 2E 30 //VERSAON:3.0
0D 0A 
46 4E 3A E6 88 91 E7 9A 84 E7 BC 96 E5 8F B7 //FN:我的编号
0D 0A 
4E 3A E6 88 91 E7 9A 84 E7 BC 96 E5 8F B7 0D 0A  //N:
54 45 4C 3B 54 59 50 45 3D 43 45 4C 4C 3A 2B 38 36 31 38 36 31 32 39 34 33 30 30 31 //TEL;TYPE=CELL:+8618612943001
0D 0A 
55 49 44 3A 30 //UID:0
0D 0A
45 4E 44 3A 56 43 41 52 44 //END:VCARD
0D 0A 
42 45 47 49 4E 3A 56 43 41 52 44 //BEGIN:VCARD
0D 0A 
56 45 52 53 49 4F 4E 3A 33 2E 30 //VERSAON:3.0
0D 0A 
46 4E 3A E9 98 BF E8 8E B2 //FN:utf-8 encode.
0D 0A 
   4E 3A E9 98 BF E8 8E B2 //N:
0D 0A 
54 45 4C 3B 54 59 50 45 3D 43 45 4C 4C 3A 31 33 38 2D 31 31 30 30 2D 38 30 38 36 //TEL;TYPE=CELL:
0D 0A 
55 49 44 3A 33 66 //UID:3f
0D 0A 
45 4E 44 3A 56 43 41 52 44  //END:VCARD
0D 0A                    //回车 换行
              */
			(byte) 0x83, //get
			(byte) 0x00, // packet length = 79
			//(byte) 0x4f, //packet length = 79
			(byte) 0x53, // packet length=83
			(byte) 0xcb, //HI for connect ID
			(byte) 0x00, //connect id start
			(byte) 0x65, //
			(byte) 0x9c, //
			(byte) 0x5c, //connect id end, please replace connnect id.
			
			(byte) 0x01, //name
			(byte) 0x00, //length of name = 33
			(byte) 0x21, //length of name = 33
			
			(byte) 0x00, //unicode start
			(byte) 0x74,
			(byte) 0x00,
			(byte) 0x65,
			(byte) 0x00,
			(byte) 0x6c,
			(byte) 0x00,
			(byte) 0x65,
			(byte) 0x00,
			(byte) 0x63,
			(byte) 0x00,
			(byte) 0x6f,
			(byte) 0x00,
			(byte) 0x6d,
			(byte) 0x00, 
			(byte) 0x2f,
			(byte) 0x00,
			(byte) 0x70,
			(byte) 0x00,
			(byte) 0x62,
			(byte) 0x00,
			(byte) 0x2e,
			(byte) 0x00,
			(byte) 0x76,
			(byte) 0x00,
			(byte) 0x63,
			(byte) 0x00,
			(byte) 0x66,
			(byte) 0x00,
			(byte) 0x00, //unicode for  telecom/pb.vcf , the last two byte must be 0000
			
			(byte) 0x42, //type
			(byte) 0x00,
			(byte) 0x12, //length of type
			
			(byte) 0x78,
			(byte) 0x2d,
			(byte) 0x62,
			(byte) 0x74, 
			(byte) 0x2f, 
			(byte) 0x70,
			(byte) 0x68,
			(byte) 0x6f,
			(byte) 0x6e,
			(byte) 0x65,
			(byte) 0x62,
			(byte) 0x6f,
			(byte) 0x6f,
			(byte) 0x6b,
			(byte) 0x00, //x-bt/phonebook, the last byte must be 00\
			
			(byte) 0x4c, //app params
			//(byte) 0x00, //length of app params = 20
			//(byte) 0x14, //length of app params = 20
			(byte) 0x00,
			(byte) 0x18, //length=24
			
			(byte) 0x06, //vard filter
			(byte) 0x08, //8bit
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00,
			(byte) 0x00, //mask, 64bit, all 
			
			(byte) 0x07, //vard version
			(byte) 0x01, //length
			(byte) 0x01, //Format 0x01-->3.1, 00-->2.0
			
			(byte) 0x04, //MaxListCount
			(byte) 0x02, //length
			(byte) 0x00,
			(byte) 0x01,  //0xffff fetch all
			(byte) 0x05,  //ListStartOffset
			(byte) 0x02,
			(byte) 0x00,
			(byte) 0x01   //
			
			//see pcba spec
	};
	private  byte[] get_continue_cmd = { 
			(byte)0x83,
			(byte) 0x00, // packet length = 79
			(byte) 0x03 //packet length = 79
	};
	private  byte[] disconnect_cmd = { 

			
			(byte) 0x81, //disconnect
			(byte) 0x00, // packet length = 8
			(byte) 0x08, //packet length = 8
			(byte) 0xcb, //HI for connect ID
			(byte) 0x00, //connect id start
			(byte) 0x65, //
			(byte) 0x9c, //
			(byte) 0x5c //connect id end, please replace connnect id.

			
			
	};
    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
//    private BluetoothDevice mSavedDevice;
//    private int mConnectionLostCount;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    //constans that indicate the obex state
    public static final byte OBEX_RESPONSE_RESULT_OK = (byte)0xa0;  
    public static final byte OBEX_RESPONSE_RESULT_CONTINUE = (byte)0x90;  
    
    public static final int STATE_CONNECTED_RESPONSE_OK = 0;  
    public static final int STATE_CONNECTED_RESPONSE_FAIL = 1; 
    
    private int mObexState;
    
    public static final int OBEX_STATE_NONE = 0;           // we're doing nothing
    public static final int OBEX_STATE_CONNECTING = 1;     // now connect obex server
    public static final int OBEX_STATE_CONNECTED = 2;      // now connected to a remote obex server
    public static final int  OBEX_STATE_GET_CONTACT_SIZE= 3;            // now get size from  a remote obex server
    public static final int  OBEX_STATE_GET_CONTACT_SIZE_DONE= 4;            // now get size from  a remote obex server
    public static final int  OBEX_STATE_GET= 5;            // now get from  a remote obex server
    public static final int  OBEX_STATE_GET_DONE= 6;            // now get from  a remote obex server
    public static final int  OBEX_STATE_GET_FINISH= 7;     //now finish all contact get
    public static final int  OBEX_STATE_DISCONNECT= 8;     // now disconnect from a remote obex server
    public static final int  OBEX_STATE_DISCONNECT_DONE= 9; 
    // Constants that indicate command to computer
    public static final int EXIT_CMD = -1;
    public static final int VOL_UP = 1;
    public static final int VOL_DOWN = 2;
    public static final int MOUSE_MOVE = 3;
    
    
    private byte[] mConnect_id; //use mConnect_id to get phonebook from obex server
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothCommandService(Context context, Handler handler) {
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	mState = STATE_NONE;
    	//mConnectionLostCount = 0;
    	mHandler = handler;
    	
    	mObexState = OBEX_STATE_NONE;
    	mConnect_id = new byte[4];
    }
    
    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(TransferContactActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }
    
    public int getObexState() {
		return mObexState;
	}

	public void setObexState(int obexState) {
		if (D) Log.d(TAG, "setObexState() " + mObexState + " -> " + obexState);
		mObexState = obexState;
		
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(TransferContactActivity.MESSAGE_OBEX_STATE_CHANGE, obexState, -1).sendToTarget();
	}

	public byte[] getConnect_id() {
		return mConnect_id;
	}

	public void setConnect_id(byte[] connect_id) {
		mConnect_id = connect_id;
	}

	/**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
    	if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(TransferContactActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(TransferContactActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // save connected device
        //mSavedDevice = device;
        // reset connection lost count
        //mConnectionLostCount = 0;
        
        setState(STATE_CONNECTED);
        //for pbap profile, send below command is necessary.
        //if not, iphone will disconnect about 2 seconds. android phone(I use MX3) don't disconnect.
        //but I'm not sure where this place is right.
        write(init_cmd);
        setObexState(OBEX_STATE_CONNECTING);
    }

    public void getContact(int index)
    {
    	Log.i(TAG,"getContact");
    	
    	
    	//replat get_cmd 4bit-7bit by mConnect_id
    	for (int i=0;i<4;i++)
    	{
    		get_cmd[i+4] = getConnect_id()[i];
    	}
    	//fill ListStartOffset
    	get_cmd[get_cmd.length-1] =(byte)( index & 0xff);
    	get_cmd[get_cmd.length-2] =(byte)( index>>8 & 0xff);
    	write(get_cmd);
    	setObexState(OBEX_STATE_GET);
    }
    
    public void get_continue_cmd()
    {
    	write(get_continue_cmd);
    }
    public void getContactsize()
    {
    	Log.i(TAG,"getContactsize");
    	

    	//replat get_cmd 4bit-7bit by mConnect_id
    	for (int i=0;i<4;i++)
    	{
    		get_contactsize_cmd[i+4] = getConnect_id()[i];
    	}
    	write(get_contactsize_cmd);
    	setObexState(OBEX_STATE_GET_CONTACT_SIZE);
    }  
    public void obex_disconnect()
    {
    	Log.i(TAG,"obex_disconnect");
    	

    	//replat get_cmd 4bit-7bit by mConnect_id
    	for (int i=0;i<4;i++)
    	{
    		disconnect_cmd[i+4] = getConnect_id()[i];
    	}
    	write(disconnect_cmd);
    	setObexState(OBEX_STATE_DISCONNECT);
    }
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        
        setState(STATE_NONE);
        setObexState(OBEX_STATE_NONE);
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
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    public void write(int out) {
    	// Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        setObexState(OBEX_STATE_NONE);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(TransferContactActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TransferContactActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
//        mConnectionLostCount++;
//        if (mConnectionLostCount < 3) {
//        	// Send a reconnect message back to the Activity
//	        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
//	        Bundle bundle = new Bundle();
//	        bundle.putString(RemoteBluetooth.TOAST, "Device connection was lost. Reconnecting...");
//	        msg.setData(bundle);
//	        mHandler.sendMessage(msg);
//	        
//        	connect(mSavedDevice);   	
//        } else {
        	setState(STATE_LISTEN);
        	setObexState(OBEX_STATE_NONE);
	        // Send a failure message back to the Activity
	        Message msg = mHandler.obtainMessage(TransferContactActivity.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(TransferContactActivity.TOAST, "Device connection was lost");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);
//        }
    }
    
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothCommandService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothCommandService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                	// Read from the InputStream
                    int bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    Log.i(TAG,"string : "+ Utils.ConvertByteToString(buffer,bytes));
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(TransferContactActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    /*
                    if(buffer[0] == OBEX_RESPONSE_RESULT_OK)
                    {
                        if(getObexState() == OBEX_STATE_CONNECTING)
                        {
                        	setObexState(OBEX_STATE_CONNECTED);
                        }
                        if(getObexState() == OBEX_STATE_GET)
                        {
                        	setObexState(OBEX_STATE_GET_DONE);
                        }
                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(TransferContactActivity.MESSAGE_READ, bytes, STATE_CONNECTED_RESPONSE_OK, buffer)
                                .sendToTarget();

                    }
                    else if(buffer[0] == OBEX_RESPONSE_RESULT_CONTINUE){
                    	Log.i(TAG,"continue read..");
                    	mmOutStream.write(get_continue_cmd);
                    }               
                    else
                    {
                    	//fixme
                    	//mHandler.obtainMessage(TransferContactActivity.MESSAGE_READ, bytes, -1, buffer)
                            //.sendToTarget();
                    	setObexState(OBEX_STATE_NONE);
                    }
                    */
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
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
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        
        public void write(int out) {
        	try {
                mmOutStream.write(out);

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
            	//mmOutStream.write(EXIT_CMD);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}