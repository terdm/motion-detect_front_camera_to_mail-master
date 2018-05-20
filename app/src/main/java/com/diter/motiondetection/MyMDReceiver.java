package com.diter.motiondetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ASUS on 20.05.2018.
 */

public class MyMDReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


            Log.d("MyTag","MDC message received");

    }
}