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
            	switch (msg.arg2) 
            	{
            		case BluetoothCommandService.STATE_CONNECTED_RESPONSE_OK:
            			byte[] buffer = new byte[1024];
            			int bytes = msg.arg1;
            			buffer = (byte[])msg.obj;
            			Log.i(TAG, "bytes is " + bytes);
            			if(mCommandService.getObexState() == BluetoothCommandService.OBEX_STATE_CONNECTED){
                    	//this is the response of first command.

                        //meizu
                        // A0 00 1F 10 00 FF FE CB 00 00 00 01 4A 00 13 79 61 35 F0 F0 C5 11 D8 09 66 08 00 20 0C 9A 66 
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

            				
            				Log.i(TAG,"string : "+ Utils.ConvertByteToString(buffer,bytes));
							
            				mCommandService.setConnect_id(Utils.copyOfRange(buffer, 8, 12));
            				//Log.i(TAG,"connect_id is " + mCommandService.getConnect_id().toString());
            				
            				mCommandService.getContact();
            				//mCommandService.obex_disconnect();
            			}
            			if(mCommandService.getObexState() == BluetoothCommandService.OBEX_STATE_GET_DONE){
            				//0x90 0x00 0x03
            				//bug ,fixme
            				Log.i(TAG,"string : "+ Utils.ConvertByteToString(buffer,bytes));
							
            			}
            			if(mCommandService.getObexState() == BluetoothCommandService.OBEX_STATE_DISCONNECT){

            				Log.i(TAG,"string : "+ Utils.ConvertByteToString(buffer,bytes));
							
            			}
            			break;
            		default:
            			break;
            	}

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
