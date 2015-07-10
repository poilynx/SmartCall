package zhexian.app.smartcall.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class ZBroadcast {

    public static void registerNetworkStatusChange(Context context, BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, intentFilter);
    }

    public static void unRegister(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }
}
