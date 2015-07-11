package zhexian.app.smartcall.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.lib.ZHttp;
import zhexian.app.smartcall.lib.ZIO;

public class BaseApplication extends Application {
    private static final String PARAM_CALL_SHORT = "zhexian.app.smartcall.base.PARAM_CALL_SHORT";
    private static final String PARAM_SERVICE_URL = "zhexian.app.smartcall.base.PARAM_SERVICE_URL";
    private static final String PARAM_USER_NAME = "zhexian.app.smartcall.base.PARAM_USER_NAME";
    private static final String PARAM_PASSWORD = "zhexian.app.smartcall.base.PARAM_PASSWORD";
    private static final String PARAM_IS_LOGIN = "zhexian.app.smartcall.base.PARAM_IS_LOGIN";
    private static final String PARAM_IS_LOAD_MOST_AVATARS = "zhexian.app.smartcall.base.PARAM_IS_LOAD_MOST_AVATARS";
    private SharedPreferences mSp;
    private boolean mIsCallShort;
    private boolean mIsLoadMostAvatars;
    private String mServiceUrl;
    private String mUserName;
    private String mPassword;
    private boolean mIsLogin;
    private String mFilePath;
    private ZHttp.NetworkStatus mNetWorkStatus;
    private int screenWidth;
    private int screenHeight;


    @Override
    public void onCreate() {
        super.onCreate();
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mServiceUrl = mSp.getString(PARAM_SERVICE_URL, getString(R.string.service_url));
        mUserName = mSp.getString(PARAM_USER_NAME, "chenjunjie");
        mPassword = mSp.getString(PARAM_PASSWORD, "");
        mIsCallShort = mSp.getBoolean(PARAM_CALL_SHORT, false);
        mIsLogin = mSp.getBoolean(PARAM_IS_LOGIN, false);
        mIsLoadMostAvatars = mSp.getBoolean(PARAM_IS_LOAD_MOST_AVATARS, false);

        mFilePath = Environment.isExternalStorageEmulated() ? getExternalFilesDir(null).getAbsolutePath() : getFilesDir().getAbsolutePath();
        mFilePath += "/";
        mNetWorkStatus = ZHttp.GetConnectType(this);
    }

    public boolean saveToFile(String key, String content) {
        ZIO.mkDirs(mFilePath);
        return ZIO.writeToFile(mFilePath + key, content);
    }

    public String readFromFile(String key) {
        return ZIO.readFromFile(mFilePath + key);
    }


    public boolean isLocalFileExist(String key) {
        return ZIO.isExist(mFilePath + key);
    }


    public ZHttp.NetworkStatus getNetworkStatus() {
        return mNetWorkStatus;
    }

    public void setNetworkStatus(ZHttp.NetworkStatus mNetworkStatus) {
        this.mNetWorkStatus = mNetworkStatus;
    }

    public boolean isNetworkAvailable() {
        return mNetWorkStatus != ZHttp.NetworkStatus.DisConnect;
    }

    public boolean isNetworkWifi() {
        return mNetWorkStatus == ZHttp.NetworkStatus.Wifi;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public boolean getIsCallShort() {
        return mIsCallShort;
    }

    public void setIsCallShort(boolean isCallShort) {
        if (mIsCallShort == isCallShort)
            return;

        mIsCallShort = isCallShort;
        mSp.edit().putBoolean(PARAM_CALL_SHORT, mIsCallShort).apply();
    }

    public String getServiceUrl() {
        return mServiceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        if (mServiceUrl.equals(serviceUrl))
            return;

        mServiceUrl = serviceUrl;
        mSp.edit().putString(PARAM_SERVICE_URL, mServiceUrl).apply();
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        if (mUserName.equals(userName))
            return;

        mUserName = userName;
        mSp.edit().putString(PARAM_USER_NAME, mUserName).apply();
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        if (mPassword.equals(password))
            return;

        mPassword = password;
        mSp.edit().putString(PARAM_PASSWORD, mPassword).apply();
    }

    public boolean isLogin() {
        return mIsLogin;
    }

    public void setIsLogin(boolean isLogin) {
        if (mIsLogin == isLogin)
            return;

        mIsLogin = isLogin;
        mSp.edit().putBoolean(PARAM_IS_LOGIN, mIsLogin).apply();
    }

    public boolean isLoadMostAvatars() {
        return mIsLoadMostAvatars;
    }

    public void setIsLoadMostAvatars(boolean isLoadMostAvatars) {

        if (mIsLoadMostAvatars == isLoadMostAvatars)
            return;

        mIsLoadMostAvatars = isLoadMostAvatars;
        mSp.edit().putBoolean(PARAM_IS_LOAD_MOST_AVATARS, mIsLoadMostAvatars).apply();
    }
}
