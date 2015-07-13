package zhexian.app.smartcall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.tools.Utils;

public class MainActivity extends BaseActivity {

    private int intro_count_second = 8;
    private TextView mConfirmText;
    private int MSG_BTN_ENABLE_TICK = 0;
    private View confirmBtn;
    private Timer introConfirmBtnEnableTimer;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BTN_ENABLE_TICK) {
                intro_count_second--;

                if (intro_count_second == 0) {
                    mConfirmText.setText("知道啦");
                    confirmBtn.setEnabled(true);
                    introConfirmBtnEnableTimer.cancel();
                    return;
                }

                mConfirmText.setText(String.format("%d秒后可用", intro_count_second));
            }
        }
    };

    public static void actionStart(Activity context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZImage.Init(this);
        ContactSQLHelper.Init(this);

        if (!baseApp.isLogin()) {
            JumpToLogin();
            return;
        }
        setContentView(R.layout.activity_main);
        bindIntroduce();
    }

    void bindIntroduce() {
        if (!baseApp.isReadIntroduce()) {
            final View introView = View.inflate(this, R.layout.flat_intro_window, null);
            final FrameLayout linearLayout = (FrameLayout) findViewById(R.id.contact_main_container);
            linearLayout.addView(introView);
            Utils.GenerateColorAnimator(this, R.animator.intro_window_bg, introView).start();
            mConfirmText = (TextView) findViewById(R.id.intro_confirm_text);
            confirmBtn = findViewById(R.id.intro_confirm_btn);

            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayout.removeView(introView);
                    baseApp.setIsReadIntroduce(true);
                }
            });
            confirmBtn.setEnabled(false);

            introConfirmBtnEnableTimer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(MSG_BTN_ENABLE_TICK);
                }
            };
            introConfirmBtnEnableTimer.schedule(timerTask, 0, 1000);
        }
    }

    public void JumpToLogin() {
        finish();
        LoginActivity.actionStart(this);
    }
}
