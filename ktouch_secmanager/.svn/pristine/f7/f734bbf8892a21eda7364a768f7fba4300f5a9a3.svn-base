package com.guli.secmanager.flowmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.gsm.SmsMessage;
import android.util.Log;


/**
 * Created by wangqch on 16-4-20.
 */
public class SmsReceiver extends BroadcastReceiver {

    public static Handler han;
    public static String mQueryPort = null;

    public void setHandle(Handler handle,String queryPort){
           this.han = handle;
        this.mQueryPort = queryPort;
        Log.d("FlowAutoCorrectService","now is on setHandle");
    }
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (SMS_RECEIVED_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                //解析短信
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    byte[] pdu = (byte[]) pdus[i];
                    messages[i] = SmsMessage.createFromPdu(pdu);
                }
                StringBuilder messageBody = new StringBuilder();
                String sender="";
                for (SmsMessage msg : messages) {
                    messageBody.append(msg.getMessageBody());
                    sender = msg.getOriginatingAddress();
                }
                if ( null != mQueryPort && mQueryPort.equals(sender)) {
                    Message message = new Message();
                    Log.d("wangqch","now message is onRecive"+messageBody.toString());
                    message.obj = messageBody.toString();
                    message.what = 3;
                    han.sendMessage(message);
                    mQueryPort = null;
                }
            }
        }
    }
}
