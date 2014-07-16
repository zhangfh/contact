package com.burns.android.transfercontact;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class TransferContactActivity extends Activity {

	private static final String TAG = "TransferContact";
	// Layout view
	private TextView mTitle;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Member object for Bluetooth Command Service
    private BluetoothCommandService mCommandService = null;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_OBEX_STATE_CHANGE = 6;
    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
	// Name of the connected device
    private String mConnectedDeviceName = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreate");
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_transfer_contact);
		
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
		// If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		// otherwise set up the command service
		else {
			if (mCommandService==null)
				setupCommand();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mCommandService != null) {
			if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
				mCommandService.start();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mCommandService != null)
			mCommandService.stop();
	}
	
	private void setupCommand() {
		// Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(this, mHandler);
	}
	
	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            /*
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothCommandService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothCommandService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothCommandService.STATE_LISTEN:
                case BluetoothCommandService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
                */
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_OBEX_STATE_CHANGE:
            	 switch (msg.arg1) {
            	 	case BluetoothCommandService.OBEX_STATE_CONNECTED:
            	 		mTitle.setText(R.string.title_connected_to);
            	 		mTitle.append(mConnectedDeviceName);
            	 		break;
            	 	case BluetoothCommandService.OBEX_STATE_CONNECTING:
            	 		mTitle.setText(R.string.title_connecting);
            	 		break;
            	 	case BluetoothCommandService.OBEX_STATE_NONE:
            	 		mTitle.setText(R.string.title_not_connected);
            	 		break;
            	 	case BluetoothCommandService.OBEX_STATE_GET:
            	 		mTitle.setText(R.string.title_get_contact);
            	 		break;
            	 	case BluetoothCommandService.OBEX_STATE_GET_DONE:
            	 		mTitle.setText(R.string.title_get_contact_done);
            	 		break;
            	 }
            	break;
            case MESSAGE_READ:
    			
    			int bytes = msg.arg1;
    			byte[] buffer = new byte[bytes];
    			buffer = (byte[])msg.obj;
    			switch(mCommandService.getObexState()){
    				case BluetoothCommandService.OBEX_STATE_CONNECTING:
                    if(buffer[0] == BluetoothCommandService.OBEX_RESPONSE_RESULT_OK)
                    {
                    	mCommandService.setObexState(BluetoothCommandService.OBEX_STATE_CONNECTED);
                    	
                        //iphone
                        // A0 00 1F 10 00 0F A0 CB 00 65 9C 5C 4A 00 13 79 61 35 F0 F0 C5 11 D8 09 66 08 00 20 0C 9A 66 
                        /*
                         * 0xA0 connect success
                         * 0x001f connect response packet length
                         * 0x10 obex version, version 1.0
                         * 0x00 flag
                         * 0x0fa0 maximum OBEX packet length
                         * 0xcb HI(header identifer) for Connection Idheader
                         * 0x00659C5C connect id = 0x00659C5C
                         * 0x4A HI for who
                         * 0x0013 Length of WhoHeader
                         * 0x796135-F0F0-C511-D809-660800200C9A66 uuid
                         */
							
            			mCommandService.setConnect_id(Utils.copyOfRange(buffer, 8, 12));
            			//Log.i(TAG,"connect_id is " + mCommandService.getConnect_id().toString());
            				
            			mCommandService.getContactsize();

                    }
                    else{
                    	mCommandService.setObexState(BluetoothCommandService.OBEX_STATE_NONE);
                    }
                    break;
    				case BluetoothCommandService.OBEX_STATE_GET_CONTACT_SIZE:
    				if (buffer[0] == BluetoothCommandService.OBEX_RESPONSE_RESULT_CONTINUE)
    				{
    					Log.i(TAG,"continue to read contact size");
    					mCommandService.get_continue_cmd();
    				}else if(buffer[0] == BluetoothCommandService.OBEX_RESPONSE_RESULT_OK)
    				{
    					Log.i(TAG,"get contact size done");
    					
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
    					 if(buffer[6] == 0x08)//phonebook size identifier
    					 {
    						 int length = (buffer[8]&0x000000FF << 8)  + buffer[9]&0x000000FF;
    						 Log.i(TAG,  "length:"+ length);
    						 mCommandService.setObexState(BluetoothCommandService.OBEX_STATE_GET_CONTACT_SIZE_DONE);
    						 
    						 //fetch 1 vcard
    						 mCommandService.getContact();
    					 }
    				}else
    				{
    					mCommandService.setObexState(BluetoothCommandService.OBEX_STATE_NONE);
    				}
    				break;
    				case BluetoothCommandService.OBEX_STATE_GET:
    	   				if (buffer[0] == BluetoothCommandService.OBEX_RESPONSE_RESULT_CONTINUE)
        				{
        					Log.i(TAG,"continue to get contact ");
        					mCommandService.get_continue_cmd();
        				}
    	   				else if(buffer[0] == BluetoothCommandService.OBEX_RESPONSE_RESULT_OK)
        				{
        					Log.i(TAG,"get contact  done");
        					/*

A0 00 65 49 00 62 
42 45 47 49 4E 3A 56 43 41 52 44 //BEGIN:VCARD
0D 0A 
56 45 52 53 49 4F 4E 3A 33 2E 30 //VERSAON:3.0
0D 0A 
46 4E 3A E9 98 BF E8 8E B2 //FN:utf-8 encode.
0D 0A 
4E 3A E9 98 BF E8 8E B2//N:
0D 0A 
54 45 4C 3B 54 59 50 45 3D 43 45 4C 4C 3A 31 33 38 2D 31 31 30 30 2D 38 30 38 36 //TEL;TYPE=CELL:
0D 0A 
55 49 44 3A 33 66 
0D 0A 
45 4E 44 3A 56 43 41 52 44 
0D 0A //»Ø³µ »»ÐÐ

        					 */
            				Log.i(TAG, "buffer[4]:" + buffer[4] + "buffer[5]:" + buffer[5]);

            				int length =(buffer[4]&0x000000FF << 8 )+ buffer[5]&0x000000FF  ;
            				length -=3;
            				
            				Log.i(TAG, "vcard length = " + length);
            				byte[] vcard_buffer= new byte[length];
            			    System.arraycopy(buffer, 6, vcard_buffer, 0,   length);
            				FileService fservice = new FileService(getApplicationContext());
            				try {
								fservice.save("1.vcf", vcard_buffer);//it's ok, I can browser in file manager.
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
            				mCommandService.setObexState(BluetoothCommandService.OBEX_STATE_GET_DONE);
   
        				}else
        				{
        					mCommandService.setObexState(BluetoothCommandService.OBEX_STATE_NONE);
        				}
    	   			break;
    			}
    			break;
    		default:
    			break;
    			
        }
        }
    };
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                Log.i(TAG,"selected bt address: " + address);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                //TODO
                mCommandService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupCommand();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
	
	  public void onBtnScan(View view) {
		 Log.i(TAG,"onBtnScan");
         // Launch the DeviceListActivity to see devices and do scan
     	Intent serverIntent = new Intent(this, DeviceListActivity.class);
         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		    
	}
}
