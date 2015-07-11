package zhexian.app.smartcall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.image.ZImage;

public class MainActivity extends BaseActivity {
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
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        baseApp.setScreenWidth(dm.widthPixels);
        baseApp.setScreenHeight(dm.heightPixels);
    }

    public void JumpToLogin() {
        finish();
        LoginActivity.actionStart(this);
    }
}
