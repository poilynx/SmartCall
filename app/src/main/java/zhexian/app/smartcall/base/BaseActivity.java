package zhexian.app.smartcall.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import zhexian.app.smartcall.lib.ZBroadcast;
import zhexian.app.smartcall.receiver.NetWorkChangeReceiver;
import zhexian.app.smartcall.ui.NotifyBar;

public class BaseActivity extends Activity {
    public BaseApplication baseApp = null;
    public NotifyBar notify;
    private BroadcastReceiver mNetWorkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        baseApp = (BaseApplication) getApplication();
        mNetWorkChangeReceiver = new NetWorkChangeReceiver(baseApp);
        ZBroadcast.registerNetworkStatusChange(this, mNetWorkChangeReceiver);
        notify = new NotifyBar(this);
    }

    @Override
    protected void onDestroy() {
        ZBroadcast.unRegister(this, mNetWorkChangeReceiver);
        notify.destroy();
        super.onDestroy();
    }
}
