package zhexian.app.smartcall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.lib.DBHelper;
import zhexian.app.smartcall.tools.Utils;

public class MainActivity extends BaseActivity {
    private static final int MSG_BTN_ENABLE_TICK = 0;
    private static int intro_count_second = 8;
    private Timer mBtnEnableTimer;

    public static void actionStart(Activity context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication baseApp = (BaseApplication) getApplication();
        ZImage.Init(baseApp);
        ContactSQLHelper.Init(baseApp);
        DBHelper.init(baseApp.getFileRootDir(), baseApp.getFileCacheDir());

        if (baseApp.getScreenWidth() == 0) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            baseApp.setScreenWidth(dm.widthPixels);
            baseApp.setScreenHeight(dm.heightPixels);
        }

        if (baseApp.getAvatarWidth() == 0) {
            //大于1080p，才启用256px的图片
            boolean isHighDisplay = Utils.isHighDisplay(baseApp.getScreenWidth(), baseApp.getScreenHeight());

            if (isHighDisplay)
                baseApp.setAvatarWidth(256);
            else
                baseApp.setAvatarWidth(128);
        }

        if (!baseApp.isLogin()) {
            JumpToLogin();
            return;
        }
        setContentView(R.layout.activity_main);
        bindIntroduce();
    }


    private void bindIntroduce() {
        if (baseApp.isReadIntroduce())
            return;

        final View introView = View.inflate(this, R.layout.flat_intro_window, null);
        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contact_main_container);
        frameLayout.addView(introView);
        Utils.GenerateColorAnimator(this, R.animator.intro_window_bg, introView).start();
        TextView mConfirmText = (TextView) findViewById(R.id.intro_confirm_text);
        View mConfirmBtn = findViewById(R.id.intro_confirm_btn);

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameLayout.removeView(introView);
                baseApp.setIsReadIntroduce(true);
            }
        });
        mConfirmBtn.setEnabled(false);

        mBtnEnableTimer = new Timer();
        final BtnAvailableHandler btnAvailableHandler = new BtnAvailableHandler(mConfirmText, mConfirmBtn, mBtnEnableTimer);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                btnAvailableHandler.sendEmptyMessage(MSG_BTN_ENABLE_TICK);
            }
        };
        mBtnEnableTimer.schedule(timerTask, 0, 1000);
    }

    public void JumpToLogin() {
        finish();
        LoginActivity.actionStart(this);
    }

    static class BtnAvailableHandler extends Handler {
        WeakReference<TextView> confirmText;
        WeakReference<View> confirmBtn;
        WeakReference<Timer> btnEnableTimer;

        BtnAvailableHandler(TextView _confirmText, View _confirmBtn, Timer _btnEnableTimer) {
            confirmText = new WeakReference<>(_confirmText);
            confirmBtn = new WeakReference<>(_confirmBtn);
            btnEnableTimer = new WeakReference<>(_btnEnableTimer);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_BTN_ENABLE_TICK)
                return;

            TextView _confirmText = confirmText.get();
            View _confirmBtn = confirmBtn.get();
            Timer _btnEnableTimer = btnEnableTimer.get();

            if (_confirmText == null || _confirmBtn == null || _btnEnableTimer == null) {
                if (_btnEnableTimer != null)
                    _btnEnableTimer.cancel();
                return;
            }

            intro_count_second--;

            if (intro_count_second <= 0) {
                _confirmText.setText("知道啦");
                _confirmBtn.setEnabled(true);
                _btnEnableTimer.cancel();
                return;
            }
            _confirmText.setText(String.format("%d秒后可用", intro_count_second));
        }
    }
}
