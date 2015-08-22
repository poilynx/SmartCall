package zhexian.app.smartcall.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.util.Date;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.lib.ZHttp;

public class BaseApplication extends Application {
    private static final String PARAM_CALL_SHORT = "zhexian.app.smartcall.base.PARAM_CALL_SHORT";
    private static final String PARAM_SERVICE_URL = "zhexian.app.smartcall.base.PARAM_SERVICE_URL";
    private static final String PARAM_USER_NAME = "zhexian.app.smartcall.base.PARAM_USER_NAME";
    private static final String PARAM_PASSWORD = "zhexian.app.smartcall.base.PARAM_PASSWORD";
    private static final String PARAM_IS_LOGIN = "zhexian.app.smartcall.base.PARAM_IS_LOGIN";
    private static final String PARAM_IS_READ_INTRODUCE = "zhexian.app.smartcall.base.PARAM_IS_READ_INTRODUCE";
    private static final String PARAM_LAST_MODIFY_TIME = "zhexian.app.smartcall.base.PARAM_LAST_MODIFY_TIME";
    private static final String PARAM_AVATAR_WIDTH = "zhexian.app.smartcall.base.PARAM_AVATAR_WIDTH";
    private static final String PARAM_IMAGE_POOL_SIZE = "zhexian.app.smartcall.base.PARAM_IMAGE_POOL_SIZE";
    private static final String PARAM_SCREEN_WIDTH = "zhexian.app.smartcall.base.PARAM_SCREEN_WIDTH";
    private static final String PARAM_SCREEN_HEIGHT = "zhexian.app.smartcall.base.PARAM_SCREEN_HEIGHT";

    private SharedPreferences mSp;
    private int mAvatarWidth;
    private int mImageCachePoolSize;
    private boolean mIsCallShort;
    private boolean mIsReadIntroduce;
    private long mLastModifyTime;
    private String mServiceUrl;
    private String mUserName;
    private String mPassword;
    private boolean mIsLogin;
    private String mFileRootPath;
    private String mFileCachePath;
    private ZHttp.NetworkStatus mNetWorkStatus;
    private int mScreenWidth;
    private int mScreenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mServiceUrl = mSp.getString(PARAM_SERVICE_URL, getString(R.string.service_url));
        mUserName = mSp.getString(PARAM_USER_NAME, "");
        mPassword = mSp.getString(PARAM_PASSWORD, "");
        mIsCallShort = mSp.getBoolean(PARAM_CALL_SHORT, true);
        mIsLogin = mSp.getBoolean(PARAM_IS_LOGIN, false);
        mAvatarWidth = mSp.getInt(PARAM_AVATAR_WIDTH, 0);
        mImageCachePoolSize = mSp.getInt(PARAM_IMAGE_POOL_SIZE, 0);

        mIsReadIntroduce = mSp.getBoolean(PARAM_IS_READ_INTRODUCE, false);
        mLastModifyTime = mSp.getLong(PARAM_LAST_MODIFY_TIME, new Date().getTime());

        if (Environment.isExternalStorageEmulated()) {
            mFileRootPath = getExternalFilesDir(null).getAbsolutePath();
            mFileCachePath = getExternalCacheDir().getAbsolutePath();
        } else {
            mFileRootPath = getFilesDir().getAbsolutePath();
            mFileCachePath = getCacheDir().getAbsolutePath();
        }

        mNetWorkStatus = ZHttp.GetConnectType(this);
        mScreenWidth = mSp.getInt(PARAM_SCREEN_WIDTH, 0);
        mScreenHeight = mSp.getInt(PARAM_SCREEN_HEIGHT, 0);

        if (mImageCachePoolSize == 0)
            setImageCachePoolSize();
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

    public String getFileRootPath() {
        return mFileRootPath;
    }

    public String getFileCachePath() {
        return mFileCachePath;
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

    public boolean isReadIntroduce() {
        return mIsReadIntroduce;
    }

    public void setIsReadIntroduce(boolean isReadIntroduce) {
        if (mIsReadIntroduce == isReadIntroduce)
            return;

        mIsReadIntroduce = isReadIntroduce;
        mSp.edit().putBoolean(PARAM_IS_READ_INTRODUCE, mIsReadIntroduce).apply();
    }

    public long getLastModifyTime() {
        return mLastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        if (mLastModifyTime == lastModifyTime)
            return;

        mLastModifyTime = lastModifyTime;
        mSp.edit().putLong(PARAM_LAST_MODIFY_TIME, mLastModifyTime).apply();
    }

    public int getAvatarWidth() {
        return mAvatarWidth;
    }

    public void setAvatarWidth(int avatarWidth) {
        if (mAvatarWidth == avatarWidth)
            return;

        mAvatarWidth = avatarWidth;
        mSp.edit().putInt(PARAM_AVATAR_WIDTH, mAvatarWidth).apply();
    }

    public int getImageCachePoolSize() {
        return mImageCachePoolSize;
    }

    private void setImageCachePoolSize() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        mImageCachePoolSize = activityManager.getMemoryClass() * 1024 * 1024 / 8;
        mSp.edit().putInt(PARAM_IMAGE_POOL_SIZE, mImageCachePoolSize).apply();
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        if (mScreenWidth == screenWidth)
            return;

        mScreenWidth = screenWidth;
        mSp.edit().putInt(PARAM_SCREEN_WIDTH, mScreenWidth).apply();
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        if (mScreenHeight == screenHeight)
            return;

        mScreenHeight = screenHeight;
        mSp.edit().putInt(PARAM_SCREEN_HEIGHT, mScreenHeight).apply();
    }
}



