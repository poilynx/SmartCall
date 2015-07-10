package zhexian.app.smartcall.call;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;

import zhexian.app.smartcall.base.BaseApplication;

public class IncomingCallBroadCastReceiver extends BroadcastReceiver {

    private static View addedView = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        int callState = tManager.getCallState();

        switch (callState) {
            case TelephonyManager.CALL_STATE_RINGING:
                removeView(context);
                CallFlatWindowManager windowManager = new CallFlatWindowManager((BaseApplication) context.getApplicationContext());
                String incomingNumber = intent.getStringExtra("incoming_number");
                addedView = windowManager.OnCall(incomingNumber);
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                removeView(context);
                break;
        }
    }

    void removeView(Context context) {
        if (addedView == null)
            return;

        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeViewImmediate(addedView);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            addedView = null;
        }

    }
}