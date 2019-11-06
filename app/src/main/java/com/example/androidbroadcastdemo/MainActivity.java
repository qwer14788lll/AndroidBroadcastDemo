package com.example.androidbroadcastdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        intentFilter=new IntentFilter();//动态声明意图
//        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");//添加指定的会响应的意图
        intentFilter=new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");//上面两句可简化为这句，使用有参构造直接声明意图
        networkChangeReceiver = new NetworkChangeReceiver();//内部类对象
        registerReceiver(networkChangeReceiver,intentFilter);//注册广播

        mButton=findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent("com.my.broadcast");//隐式意图注册的广播从 Android 8 开始是不能用了，需要加下面这句
                intent.setComponent(new ComponentName("com.example.androidbroadcastdemo","com.example.androidbroadcastdemo.MyBroadcastReceiver"));//new ComponentName(广播接收器的包名,广播接收器的路径)
                //intent.addFlags(0x01000000);//强行突破隐式广播限制
                //sendBroadcast(intent);//发送标准广播
                //intent.setComponent(new ComponentName("com.example.androidactivitydemo","com.example.androidactivitydemo.MyReceiver"));//跨APP传递广播
                //sendBroadcast(intent);//发送标准广播
                sendOrderedBroadcast(intent,null);//有序广播,第一个参数为Intent，第二个参数为与权限相关的字符串
                //sendOrderedBroadcast(intent, null, networkChangeReceiver, null, Activity.RESULT_OK, null, null);
            }
        });


//        IntentFilter filter1 = new IntentFilter("com.my.broadcast");
//        filter1.setPriority(1000);
//        registerReceiver(new Test1Receiver(), filter1);
//
//        IntentFilter filter2 = new IntentFilter("com.my.broadcast");
//        filter2.setPriority(100);
//        registerReceiver(new Test2Receiver(), filter2);

//        NetworkCallbackImpl networkCallback = new NetworkCallbackImpl();
//        NetworkRequest.Builder builder = new NetworkRequest.Builder();
//        NetworkRequest request = builder.build();
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connMgr != null) {
//            connMgr.registerNetworkCallback(request, networkCallback);
//        }
    }

//    //广播接收者：有序广播-1
//    public class Test1Receiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context, "有序广播1111111111", Toast.LENGTH_SHORT).show();
//            //有序广播里终止广播
//            abortBroadcast();
//        }
//    }
//
//    //广播接收者：有序广播-2
//    public class Test2Receiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context, "有序广播2", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {//每当收到指定的广播类型时调用
            //Toast.makeText(context, "网络状态发生变化", Toast.LENGTH_SHORT).show();
            ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);//使用getSystemService方法获取ConnectivityManager（这是一个系统服务类）
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();//通过系统服务类获取网络状态，会提示添加网络权限，API达到29会提示NetworkInfo类过时
            if (networkInfo != null && networkInfo.isAvailable()) {//isAvailable方法课判断是否有网络
                Toast.makeText(context, "网络已连接", Toast.LENGTH_SHORT).show();
                String typeName = "";
                if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                    typeName = networkInfo.getTypeName();//==> WIFI
                    Toast.makeText(getApplicationContext(), "网络类型为"+typeName+"网络", Toast.LENGTH_SHORT).show();
                }else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE) {
                    typeName = networkInfo.getTypeName();//==> MOBILE
                    Toast.makeText(getApplicationContext(), "网络类型为"+typeName+"移动数据网络", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT).show();
            }
            // Android 7 行为变更上明确说明
            // Android 7 移除了三项隐式广播，因为隐式广播会在后台频繁启动已注册侦听这些广播的应用。删除这些广播可以显著提升设备性能和用户体验。
            // 为缓解这些问题，Android 7.0 应用了以下优化措施：
            // 面向 Android 7.0 开发的应用不会收到 CONNECTIVITY_ACTION 广播，即使它们已有清单条目来请求接受这些事件的通知。
            // 在前台运行的应用如果使用 BroadcastReceiver 请求接收通知，则仍可以在主线程中侦听 CONNECTIVITY_CHANGE。
            // 应用无法发送或接收 ACTION_NEW_PICTURE 或 ACTION_NEW_VIDEO 广播。此项优化会影响所有应用，而不仅仅是面向 Android 7.0 的应用。
            // Android文档中描述，通过在AndroidManifest.xml中注册方式(静态注册广播)，App在前后台都无法接收到广播。
            // 通过register的注册方式（动态注册广播），当App在运行时，是可以接收到广播的。
        }
    }

    //API29开始要求使用NetworkCallback网络监听框架
//    class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
//        @Override
//        public void onAvailable(@NonNull Network network) {//网络连接成功时回调
//            super.onAvailable(network);
//            Toast.makeText(getApplicationContext(), "网络已连接", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onLost(@NonNull Network network) {//网络不可用时回调
//            super.onLost(network);
//            Toast.makeText(getApplicationContext(), "网络已断开", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onCapabilitiesChanged(@NonNull Network network,@NonNull NetworkCapabilities networkCapabilities) {//连接网络时回调
//            super.onCapabilitiesChanged(network, networkCapabilities);
//            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                Toast.makeText(getApplicationContext(), "网络类型为WIFI", Toast.LENGTH_SHORT).show();
//            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                Toast.makeText(getApplicationContext(), "网络类型为移动数据网络", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "网络类型为其他网络", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
//            super.onLinkPropertiesChanged(network, linkProperties);
//            Toast.makeText(getApplicationContext(), "网络状态发生变化", Toast.LENGTH_SHORT).show();
//        }
//    }
}
