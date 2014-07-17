package com.burns.android.transfercontact;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {

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
	          
	        if (mcustomProgressDialog == null){  
	            return;  
	        }  
	          
	        ImageView imageView = (ImageView) mcustomProgressDialog.findViewById(R.id.loadingImageView);  
	        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();  
	        animationDrawable.start();  
	    }  
	   
	    /** 
	     *  
	     * [Summary] 
	     *       setTitile ���� 
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
	     *       setMessage ��ʾ���� 
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
