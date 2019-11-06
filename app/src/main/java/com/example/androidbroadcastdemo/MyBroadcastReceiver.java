package com.example.androidbroadcastdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Test1，收到一条，本APP的标准广播", Toast.LENGTH_LONG).show();
        Log.i("MyBroadcastReceiver","Test1，收到一条，本APP的标准广播");
        abortBroadcast();//有序广播里阻断广播
    }
}
