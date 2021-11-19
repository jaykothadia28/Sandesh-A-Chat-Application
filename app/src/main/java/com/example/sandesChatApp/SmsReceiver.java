package com.example.sandesChatApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListner mListener;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    Boolean b;
    String msgOTP, xyz;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(SMS_RECEIVED)) {

            Bundle data = intent.getExtras();

            if(data!=null)
            {
                Object[] pdus = (Object[]) data.get("pdus");
                final SmsMessage[] message = new SmsMessage[pdus.length];

                for(int i=0; i < pdus.length; i++){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        String format = data.getString("format");
                        message[i] = SmsMessage.createFromPdu((byte[])pdus[i], format);
                    }
                    else
                    {
                        message[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    String messageBody = message[i].getMessageBody();
                    msgOTP = messageBody.substring(0,6);
                    mListener.messageReceived(msgOTP);
                }
            }
        }
    }
    public static void bindListener(SmsListner listener){
        mListener = listener;
    }
}
