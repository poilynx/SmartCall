package zhexian.app.smartcall.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by 陈俊杰 on 2015/8/20.
 * 本地磁盘管理类
 */
public class ZDisk {
    /**
     * https://
     */
    private static final int HTTP_FIRST_SPLIT_POS = 8;

    /**
     * 存储绝对路径的地址
     */
    private String mStoreDir;

    public ZDisk(String mStoreDir) {
        this.mStoreDir = mStoreDir;
        ZIO.mkDirs(mStoreDir);
    }

    /**
     * 获取资源关键路径，并和存储路径拼接
     * 如 http://images.cnitblog.com/news_topic/apple.png 到 news_topic/apple.png
     *
     * @param url 资源地址
     * @return
     */
    public String trans2Local(String url) {

        //不用截取
        if (url.indexOf(':') < 0)
            return mStoreDir + url;

        url = url.substring(url.indexOf('/', HTTP_FIRST_SPLIT_POS) + 1);
        url = url.replace('/', '_');
        url = mStoreDir + url;
        return url;
    }

    /**
     * 文件是否被保存在本地
     *
     * @param url
     * @return
     */
    public boolean exist(String url) {
        url = trans2Local(url);
        File file = new File(url);
        return file.exists();
    }

    /**
     * 写入Bitmap
     *
     * @param url
     * @param bitmap
     * @return
     */
    public boolean save(String url, Bitmap bitmap) {
        if (bitmap == null || bitmap.getByteCount() == 0)
            return true;

        url = trans2Local(url);
        File file = new File(url);
        ZIO.createNewFile(file);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            Bitmap.CompressFormat format = url.toLowerCase().indexOf("png") > 0 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
            bitmap.compress(format, 75, fos);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("存储出错", e.getMessage());
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 写入对象
     *
     * @param url
     * @param obj
     */
    public <T> boolean save(String url, T obj) {
        if (obj == null)
            return true;

        url = trans2Local(url);
        File file = new File(url);
        ZIO.createNewFile(file);
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(file);
            LoganSquare.serialize(obj, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 写入数组
     *
     * @param url
     * @param list
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> boolean save(String url, List<T> list, Class<T> tClass) {
        if (list == null || list.size() == 0)
            return true;

        url = trans2Local(url);
        File file = new File(url);
        ZIO.createNewFile(file);
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(file);
            LoganSquare.serialize(list, fos, tClass);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取本地图片
     *
     * @param url 路径
     * @return
     */
    public Bitmap getBitmap(String url) {
        url = trans2Local(url);

        if (!new File(url).exists())
            return null;

        return BitmapFactory.decodeFile(url);
    }

    /**
     * 获取缩放后的本地图片
     *
     * @param url    路径
     * @param width  宽
     * @param height 高
     * @return
     */
    public Bitmap getBitmap(String url, int width, int height) {
        url = trans2Local(url);

        if (!new File(url).exists())
            return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;

        if (srcHeight > height || srcWidth > width) {
            if (srcWidth > srcHeight)
                inSampleSize = Math.round(srcHeight / height);
            else
                inSampleSize = Math.round(srcWidth / width);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(url, options);
    }

    /**
     * 获取对象
     *
     * @param url    路径
     * @param tClass 类型
     * @param <T>
     * @return
     */
    public <T> T getObj(String url, Class<T> tClass) {
        url = trans2Local(url);

        File file = new File(url);
        if (!file.exists())
            return null;

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            T t = LoganSquare.parse(fileInputStream, tClass);
            fileInputStream.close();
            return t;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取对象数组
     *
     * @param url    路径
     * @param tClass 类型
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String url, Class<T> tClass) {
        url = trans2Local(url);

        File file = new File(url);
        if (!file.exists())
            return null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            List<T> t = LoganSquare.parseList(fileInputStream, tClass);
            fileInputStream.close();
            return t;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文件
     *
     * @param url
     */
    public void delete(String url) {
        url = trans2Local(url);
        ZIO.deleteFile(url);
    }

    /**
     * 清空文件夹
     */
    public void clean() {
        ZIO.emptyDir(new File(mStoreDir));
    }


    /**
     * 获取文件夹容量描述
     * 单位取决于容量数据，比如100MB，1GB
     *
     * @return
     */
    public String getDirSize() {
        return ZIO.getDirSizeInfo(mStoreDir, "空空如也");
    }
}
