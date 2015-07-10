package zhexian.app.smartcall.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import zhexian.app.smartcall.tools.Utils;

/**
 * Created by kimmy on 2015/5/16.
 */
public class ZHttp {
    private static OkHttpClient mOkHttpClient;

    public static OkHttpClient getHttpClient() {

        if (mOkHttpClient == null) {
            synchronized (ZHttp.class) {
                mOkHttpClient = new OkHttpClient();
                mOkHttpClient.setConnectTimeout(12, TimeUnit.SECONDS);
                mOkHttpClient.setReadTimeout(12, TimeUnit.SECONDS);
            }
        }
        return mOkHttpClient;
    }

    public static Response execute(String url) {
        Request request = new Builder().url(url).build();
        try {
            Response response = getHttpClient().newCall(request).execute();

            if (response.isSuccessful())
                return response;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(String urlStr) {
        Response response = execute(urlStr);

        if (response == null)
            return null;

        try {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(String url, int width, int height) {
        Bitmap bitmap = null;

        try {
            Response response = execute(url);
            if (null == response)
                return null;

            bitmap = Utils.getScaledBitMap(response.body().bytes(), width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static NetworkStatus GetConnectType(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            if (activeInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return NetworkStatus.Wifi;
            else if (activeInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                return NetworkStatus.Mobile;
        }

        return NetworkStatus.DisConnect;
    }

    public enum NetworkStatus {
        DisConnect,

        Mobile,

        Wifi
    }
}
