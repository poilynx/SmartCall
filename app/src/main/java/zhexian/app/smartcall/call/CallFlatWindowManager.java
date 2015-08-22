package zhexian.app.smartcall.call;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.lib.ZContact;
import zhexian.app.smartcall.lib.ZIO;
import zhexian.app.smartcall.lib.ZString;
import zhexian.app.smartcall.tools.Utils;

/**
 * 来电悬浮窗管理类
 */
class CallFlatWindowManager {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams layoutParams;
    private View view;
    private TextView mUserName;
    private TextView mJob;
    private BaseApplication baseApp;
    private ImageView imageView;

    public CallFlatWindowManager(BaseApplication baseApp) {
        this.baseApp = baseApp;
        initWindow();
        ContactSQLHelper.Init(baseApp);

    }

    public View OnCall(String incomingNumber) {

        if (ZContact.isPhoneExists(baseApp, incomingNumber))
            return null;

        CallUserEntity entity = ContactSQLHelper.getInstance().getContact(incomingNumber);

        if (entity == null)
            return null;

        return attachWindow(entity.getName(), entity.getJob(), entity.getAvatarUrl());
    }


    private View attachWindow(String userName, String job, String url) {
        mUserName.setText(userName);
        mJob.setText(job);
        loadImage(url);
        mWindowManager.addView(view, layoutParams);

        return view;
    }

    private void initWindow() {
        mWindowManager = (WindowManager) baseApp.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        LayoutInflater inflater = (LayoutInflater) baseApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.flat_call_window, null);

        mUserName = (TextView) view.findViewById(R.id.flat_user_name);
        imageView = (ImageView) view.findViewById(R.id.flat_user_avatar);
        mJob = (TextView) view.findViewById(R.id.flat_user_job);
    }


    private void loadImage(String url) {

        if (url.isEmpty()) {
            imageView.setVisibility(View.GONE);
            return;
        }
        String cachedUrl = ZString.getFileCachedDir(url, String.format("%s/", baseApp.getFileCachePath()));
        int IMAGE_SIZE = 128;

        if (ZIO.isExist(cachedUrl))
            imageView.setImageBitmap(Utils.getScaledBitMap(cachedUrl, IMAGE_SIZE, IMAGE_SIZE));
        else
            imageView.setVisibility(View.GONE);
    }
}
