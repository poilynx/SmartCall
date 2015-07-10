package zhexian.app.smartcall.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.lib.ZHttp;

/**
 * Created by kimmy on 2015/5/19.
 */
public class NetWorkChangeReceiver extends BroadcastReceiver {
    private BaseApplication mBaseApplication;

    public NetWorkChangeReceiver(BaseApplication baseApplication) {
        mBaseApplication = baseApplication;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mBaseApplication.setNetworkStatus(ZHttp.GetConnectType(context));
    }
}
