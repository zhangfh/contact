package com.burns.android.transfercontact;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {

	   private static final String TAG = "CustomProgressDialog";
	private Context context = null;  
	    private static CustomProgressDialog mcustomProgressDialog = null;  
	      
	    public CustomProgressDialog(Context context){  
	        super(context);  
	        this.context = context;  
	    }  
	      
	    public CustomProgressDialog(Context context, int theme) {  
	        super(context, theme);  
	    }  
	      
	    public static CustomProgressDialog createDialog(Context context){  
	    	mcustomProgressDialog = new CustomProgressDialog(context,R.style.CustomProgressDialog);  
	    	mcustomProgressDialog.setContentView(R.layout.customprogressdialog);  
	    	mcustomProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;  
	    	 
	        return mcustomProgressDialog;  
	    }  
	   
	    public void onWindowFocusChanged(boolean hasFocus){  
	         
	    	Log.i(TAG,"onWindowFoucusChanged" + hasFocus);
	        if (mcustomProgressDialog == null){  
	            return;  
	        }  
	        setCanceledOnTouchOutside(false);  
	        ImageView imageView = (ImageView) mcustomProgressDialog.findViewById(R.id.loadingImageView);  
	        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();  
	        animationDrawable.start();  
	    }  
	   
	    /** 
	     *  
	     * [Summary] 
	     *       setTitile 标题 
	     * @param strTitle 
	     * @return 
	     * 
	     */  
	    public CustomProgressDialog setTitile(String strTitle){  
	        return mcustomProgressDialog;  
	    }  
	      
	    /** 
	     *  
	     * [Summary] 
	     *       setMessage 提示内容 
	     * @param strMessage 
	     * @return 
	     * 
	     */  
	    public CustomProgressDialog setMessage(String strMessage){  
	        TextView tvMsg = (TextView)mcustomProgressDialog.findViewById(R.id.id_tv_loadingmsg);  
	          
	        if (tvMsg != null){  
	            tvMsg.setText(strMessage);  
	        }  
	          
	        return mcustomProgressDialog;  
	    } 

}
