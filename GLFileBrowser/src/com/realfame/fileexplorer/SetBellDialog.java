package com.realfame.fileexplorer;

//TYRD: weina 20150624 add 
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.ConditionVariable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.provider.Settings;
import android.provider.Settings.System;
public class SetBellDialog {
    private final static String TAG="SetBellDialog";
    CheckBox mRingtone;
    CheckBox mNotification;
	CheckBox mRingtone_1;
	CheckBox mRingtone_2;
    private String mFileName;
    private String mFilePath;
    private Context mContext;
    private Uri mFileUri;
    private ConditionVariable mConditionVariable = new ConditionVariable(false);
    private boolean isRSelected;
    private boolean isNSelected;
    public static final int GEMINI_SET_AS_RINGTONE_1=0;
    public static final int GEMINI_SET_AS_RINGTONE_2=1;
    public static final int GEMINI_SET_AS_NOTIFICATION=2;
    public static final int GEMINI_SET_AS_TYPE_MAX=3;
    public static boolean sRingTypeCheckedItems[]=new boolean[GEMINI_SET_AS_TYPE_MAX];
	private static final int TYPE_RINGTONE_2 = 8;
    TelephonyManager mTelephonyManager ;
    
	private AlertDialog alertDialog;
    View mView;
    
    public SetBellDialog (Context context,String fileName){
        this.mFileName =fileName;
        this.mContext =context;
    }
    public void showDialog(){
    	File file = new File(mFileName);
    	if(file!=null&&file.exists()&&file.isFile()){
    		mConditionVariable.close();
    		scanFile(file);
			//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,R.array.bell_type_multi_card
			//	,R.layout.select_dialog_multichoice);
    		
    		if(!isMultiSimCard()){
    			LayoutInflater factory = LayoutInflater.from(mContext);
    			View mMyView = factory.inflate(R.layout.set_bell, null);
    			mRingtone = (CheckBox)mMyView.findViewById(R.id.ringtone_bell);
    			mNotification =(CheckBox)mMyView.findViewById(R.id.notification_bell);
    			AlertDialog setBellDlg = new AlertDialog.Builder(mContext).create();
    			setBellDlg.setView(mMyView);
    			setBellDlg.setTitle(R.string.set_bell);
    			setBellDlg.setIcon(android.R.drawable.ic_menu_set_as);
    			//setBellDlg.setButton(mContext.getResources().getString(R.string.ok), new OKButtonListener());
    			//setBellDlg.setButton(mContext.getResources().getString(R.string.cancle), new CancleButtonListener());
    			setBellDlg.show();
    		}else{
    			/*new AlertDialog.Builder(mContext)
    			.setTitle(R.string.set_bell)
    			.setIcon(android.R.drawable.ic_menu_set_as)
    			//.setMultiChoiceItems(R.array.bell_type_multi_card,sRingTypeCheckedItems,setBellCheckedListener)
    			.setMultiChoiceItems(adapter,sRingTypeCheckedItems,setBellCheckedListener)
    			
    			.setPositiveButton(mContext.getResources().getString(R.string.ok), new OKButtonListener())
    			.setNegativeButton(mContext.getResources().getString(R.string.cancle), new CancleButtonListener())
    			.create()
    			.show();*/
    			mView = LayoutInflater.from(mContext).inflate(R.layout.ty_material_multchoice_alertdialog, null);
				View view = mView.findViewById(R.id.message);
				mRingtone_1= (CheckBox)view.findViewById(R.id.ringtone_bell_1);
				mRingtone_2= (CheckBox)view.findViewById(R.id.ringtone_bell_2);
				mNotification =(CheckBox)view.findViewById(R.id.notification_bell);
				
				TextView titleView =(TextView) mView.findViewById(R.id.alertTitle);
		        titleView.setText(R.string.set_bell);
				
				Button positiveButton = (Button)mView.findViewById(R.id.button1);
                Button cancleButton = (Button)mView.findViewById(R.id.button3);
                positiveButton.setText(R.string.confirm);
                positiveButton.setOnClickListener(new OKButtonListener());
        		cancleButton.setText(R.string.cancel);
                cancleButton.setOnClickListener(new CancleButtonListener());
    			AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
        	    alertDialog=builder.setView(mView)
					.create();
        		alertDialog.show();
    		}
    	}else{
    		String message = mContext.getResources().getString(R.string.alert_nofindfile);
    		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    	}
    }
   /* private static DialogInterface.OnMultiChoiceClickListener setBellCheckedListener =
    		new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					// TODO Auto-generated method stub
					if(which >=0 && which<GEMINI_SET_AS_TYPE_MAX){
						sRingTypeCheckedItems[which]= isChecked;
					}
				}
			};*/
    private void scanFile(File file){
    	mFileUri = getRingUri(file,mContext);//getRingUri(file,mContext);
    	if(mFileUri==null){
    		Log.d(TAG,"scanFile mFileUri==null");
    		
    	}else{
    		Log.d(TAG,"scanFile mFileUri="+mFileUri.toString());
    	}
    }
    class OKButtonListener implements  View.OnClickListener{
    	public void onClick(View v){
    		if(!isMultiSimCard()){
    			isRSelected =mRingtone.isChecked();
    			isNSelected = mNotification.isChecked();
    			if(!(isRSelected||isNSelected)){
					dismissDialog ();
    				String message = mContext.getResources().getString(R.string.no_select_set_type);
    	    		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    	    		return;
    			}
    		}else{
    		    sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_1]= mRingtone_1.isChecked();
				sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_2]= mRingtone_2.isChecked();
				sRingTypeCheckedItems[GEMINI_SET_AS_NOTIFICATION] = mNotification.isChecked();
    			if(!(sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_1]||sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_2]||sRingTypeCheckedItems[GEMINI_SET_AS_NOTIFICATION])){
					dismissDialog ();
    				String message = mContext.getResources().getString(R.string.no_select_set_type);
    	    		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    	    		return;
    			}
    		}
    		mConditionVariable.block(500);
    		if(mFileUri!=null){
    			if(!isPlayMedia(mContext,mFileUri)){//isPlayMedia
    			    dismissDialog ();
    				String message = mContext.getResources().getString(R.string.alert_nosupport);
    	    		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    	    		return;
    			}
    			
        		if(!isMultiSimCard()){
        			if(isRSelected){
        				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, mFileUri);
        			}
        			if(isNSelected){
        				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, mFileUri);
        			}
        		}else{
        			if(sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_1]){
        				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, mFileUri);
        				sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_1]=false;
        			}
        			if(sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_2]){
        			
        				RingtoneManager.setActualDefaultRingtoneUri(mContext, TYPE_RINGTONE_2, mFileUri);
        				sRingTypeCheckedItems[GEMINI_SET_AS_RINGTONE_2]=false;
        			}
        			if(sRingTypeCheckedItems[GEMINI_SET_AS_NOTIFICATION]){
        				RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, mFileUri);
        				sRingTypeCheckedItems[GEMINI_SET_AS_NOTIFICATION]=false;
        			}
        			ContentResolver cr = mContext.getContentResolver();
        			ContentValues values= new ContentValues(3);
        			values.put(MediaStore.Audio.Media.IS_RINGTONE, 1);
        			values.put(MediaStore.Audio.Media.IS_ALARM, 1);
        			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, 1);
        			cr.update(mFileUri,values,null,null);
        			String message = mContext.getResources().getString(R.string.opok);
    	    		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        		}
    		}else{
    			String message = mContext.getResources().getString(R.string.operror);
	    		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    		}
            dismissDialog (); 
    	}
    }
    class CancleButtonListener implements View.OnClickListener{
        public void onClick(View v) {
            dismissDialog ();
        }
    }
    public static Uri getRingUri(File f,Context context){
    	Uri ringUri = null;
    	ContentResolver cr = context.getContentResolver();
    	Cursor c= cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
    			new String[]{MediaStore.MediaColumns._ID,MediaStore.MediaColumns.DATA}, 
                MediaStore.MediaColumns.DATA+"=?", 
                new String[]{f.getAbsolutePath()}, null);
    	if(c!=null){
    		int size = c.getCount();
    		if(size<=0){
    			c.close();
    			c=null;
    			return null;
    		}
    		try{
    			c.moveToFirst();
    			int id = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID));
    			ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    		}catch(Exception e){
    			
    		}finally{
    			c.close();
    			c=null;
    		}
    	}
    	return ringUri;
    }
    public static boolean isPlayMedia(Context context,Uri uri){
    	MediaPlayer mp = MediaPlayer.create(context, uri);
    	if(mp==null){
    		return false;
    	}else{
    		mp.release();
    		mp=null;
    		return true;
    	}
    }
    public boolean isMultiSimCard(){
    	
    	/*try{
    	Class<?> mClass = Class.forName("android.telephony.TelephonyManager");
    		
    		//Method mGetPhoneCount = mClass.getMethod("getPhoneType", int.class);
    		//Method mGetDefault =mClass.getMethod("getDefault");
    		//Object mPro = mGetDefault.invoke(mClass);
			//int count=0;
    	//	int phoneCount = (Integer)mGetPhoneCount.invoke(mPro, count);
    		Constructor constru = mClass.getDeclaredConstructor(Context.class);
    		constru.setAccessible(true);
    		Object mObject = constru.newInstance(mContext);
    		Method mGetPhoneCount = mClass.getDeclaredMethod("getPhoneCount", int.class);
    		
    		int phoneCount = (Integer)mGetPhoneCount.invoke(mClass, 1);
    		
    		if(phoneCount>1){
    			return true;
    		}else{
    			return false;
    		}
    	}catch(Exception e){
    		return false;
    	}*/
    	return true;
    }
	public void dismissDialog (){
		if(alertDialog!=null){
			alertDialog.dismiss();
			alertDialog=null;
		}
	}
}
