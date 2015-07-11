package zhexian.app.smartcall;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import zhexian.app.smartcall.base.BaseActivity;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.contact.Dal;
import zhexian.app.smartcall.tools.Utils;
import zhexian.app.smartcall.ui.NotifyBar;


public class LoginActivity extends BaseActivity implements View.OnClickListener {

    EditText mService;
    EditText mUserName;
    EditText mPassword;
    CheckBox mIsCallShort;
    View mSubmit;

    String serviceStr;
    String userNameStr;
    String passwordStr;
    boolean isSubmitEnabled = true;

    public static void actionStart(Activity context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mService = (EditText) findViewById(R.id.login_service);
        mUserName = (EditText) findViewById(R.id.login_user_name);
        mPassword = (EditText) findViewById(R.id.login_password);
        mIsCallShort = (CheckBox) findViewById(R.id.login_call_short);
        mSubmit = findViewById(R.id.login_submit);
        baseApp = (BaseApplication) getApplication();
        bindTextListener();
        mSubmit.setOnClickListener(this);

        if (!baseApp.isNetworkAvailable())
            Utils.toast(baseApp, R.string.alert_network_not_available);
    }

    @Override
    public void onClick(View view) {
        int senderID = view.getId();

        switch (senderID) {
            case R.id.login_submit:
                submitUser();
                break;
        }
    }

    private void submitUser() {
        if (!baseApp.isNetworkAvailable()) {
            Utils.toast(baseApp, R.string.alert_network_not_available);
            return;
        }
        new LoginTask().execute();
    }

    private void refreshSubmit() {
        boolean isAllInput = !serviceStr.isEmpty() && !userNameStr.isEmpty() && !passwordStr.isEmpty();

        if (isAllInput != isSubmitEnabled) {
            mSubmit.setEnabled(isAllInput);
            isSubmitEnabled = isAllInput;
        }
    }

    private void bindTextListener() {
        serviceStr = baseApp.getServiceUrl();
        userNameStr = baseApp.getUserName();
        passwordStr = baseApp.getPassword();

        if (serviceStr.isEmpty())
            mService.requestFocus();
        else if (userNameStr.isEmpty())
            mUserName.requestFocus();
        else
            mPassword.requestFocus();

        mService.setText(serviceStr);
        mUserName.setText(userNameStr);
        mPassword.setText(passwordStr);
        mIsCallShort.setChecked(baseApp.getIsCallShort());

        refreshSubmit();
        mService.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                serviceStr = s.toString();
                refreshSubmit();
            }
        });

        mUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                userNameStr = s.toString();
                refreshSubmit();
            }
        });

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordStr = s.toString();
                refreshSubmit();
            }
        });
    }

    class LoginTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            notify.show(R.string.notify_login, NotifyBar.IconType.Progress);
            mSubmit.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String result = Dal.readFromHttp(serviceStr, userNameStr, passwordStr);
            if (result == null || result.isEmpty())
                return false;

            if (!Dal.SaveToFile(baseApp, result))
                return false;

            baseApp.setServiceUrl(serviceStr);
            baseApp.setUserName(userNameStr);
            baseApp.setPassword(passwordStr);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (!aBoolean) {
                notify.show(R.string.alert_login_failed, NotifyBar.DURATION_MIDDLE, NotifyBar.IconType.Error);
                mSubmit.setEnabled(true);
                return;
            }
            baseApp.setIsCallShort(mIsCallShort.isChecked());
            Utils.toast(baseApp, "登陆成功");
            baseApp.setIsLogin(true);
            //登陆成功，跳回到主界面
            finish();
            MainActivity.actionStart(LoginActivity.this);
        }
    }
}
