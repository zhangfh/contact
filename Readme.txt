1. How to custom title-bar in android 4.0
   1)define new style.
       <style name="CustomizedWindowTitleBackground"> 
        <item name="android:background">#047BF0</item> 
    </style> 
    <style name="titlebarstyle" parent="android:Theme"> 
        <!--   <item name="android:windowTitleSize">48dp</item>  -->
        <item name="android:windowTitleBackgroundStyle">@style/CustomizedWindowTitleBackground</item> 
        <!-- All customizations that are NOT specific to a particular API-level can go here. --> 
    </style> 
    2) define custom_title.xml
    
    3). in AndroidManifest.xml , add theme in your activity tag.
    android:theme="@style/titlebarstyle" 
    4). in your activity, add below function in onCreate():
        super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_transfer_contact);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
      Please don't change the order of the code.
 2. request to enable bluetooth:
    sent intent to ACTION_REQUEST_ENABLE, of course you can add extra and receive on onActivityResult.
 3. please note how to initialize the final variable, please see mmSocket = tmp;
 4. Create a secure socket on Bluetooth and connect the server
    BluetoothDevice.createRfcommSocketToServiceRecord(UUID).connect()
 5. PBAP UUID: 0000112F-0000-1000-8000-00805F9B34FB
 6. synchronized 
 7. bluetooth socket read/write
    1)get a InputStream and a OutputStream, write operation is on OutputStream and read operation is on InputStream.
    2) read is a block function. return bytes of server sent or -1.
 8. treate activity as a dialog.
    add this code in AndroidManifest.xml
     android:theme="@android:style/Theme.Dialog 
 9. handler
   The handler is used to talk between with UI activity and thread.
   1)override handleMessage , parse Message in this function, it include:
     msg.what,msg.arg1,msg.obj,msg.getData()
   2)thread or other class use this function to sent message:
    mHandler.obtainMessage(TransferContactActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();(what , arg1, arg2)
    or sentdata by this:
        Message msg = mHandler.obtainMessage(TransferContactActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(TransferContactActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    or this:
        mHandler.obtainMessage(TransferContactActivity.MESSAGE_READ, bytes, STATE_CONNECTED_RESPONSE_OK, buffer)(what,arg1,arg2,obj)
              .sendToTarget();
    
 10.PBAP profile
    1) only read phonebook, cannot modify .
    2) PBAP profile is depended on  Generic Object Exchange Profile and SSP and GAP
    3) There are 5 types of phonebook objects:
       pb(phone book object)
       ich(incoming call history object)
       och(outgoing call history object)
       mch(Missed Calls History object)
       cch(Combined Calls History object)
    4) telecom/pb.vcf
       sim1/telecom/pb.vcf
       