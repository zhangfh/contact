package com.burns.android.transfercontact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileService {

	    private static final String TAG = "FileService";
		private Context context;
	    
	    public FileService(Context context) {
	    	super();
	    	this.context = context;
	    }
	                                                                                                                               
	    public void save(String filename, byte[] content) throws Exception{

	    //FileOutputStream outStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
	    //outStream.write(content);
	    //outStream.close();
	    
	      try {
	           if (Environment.getExternalStorageState().equals(
	                  Environment.MEDIA_MOUNTED)) {
	              File dirPath = Environment.getExternalStorageDirectory();//get internal storage 
	              Log.i(TAG,dirPath.toString());// /storage/emulated/0 
	          
	              File file = new File(dirPath.toString(), filename);
	              FileOutputStream outStream = new FileOutputStream(file);
	              outStream.write(content);
	              outStream.close();
	           }
	       } catch (FileNotFoundException e) {
	           // TODO Auto-generated catch block
	           e.printStackTrace();
	       } catch (UnsupportedEncodingException e) {
	           // TODO Auto-generated catch block
	           e.printStackTrace();
	       } catch (IOException e) {
	           // TODO Auto-generated catch block
	           e.printStackTrace();
	       }
	    
	    }
}
