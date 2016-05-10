package zhexian.app.smartcall.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ZHttp {
    private static OkHttpClient mOkHttpClient = new OkHttpClient();

    private ZHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(12, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS);
        mOkHttpClient = builder.build();
    }

    private static OkHttpClient getHttpClient() {
        return mOkHttpClient;
    }

    private static Response execute(String url) {
        Request request = new Request.Builder().url(url).build();
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

        try {
            Response response = execute(urlStr);

            if (response == null)
                return null;
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getBytes(String url) {
        try {
            Response response = execute(url);

            if (null == response)
                return null;

            return response.body().bytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
