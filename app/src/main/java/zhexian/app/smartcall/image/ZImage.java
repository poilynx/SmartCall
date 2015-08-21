package zhexian.app.smartcall.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.lib.DBHelper;
import zhexian.app.smartcall.lib.ZIO;

/**
 * 图片管理类，访问网络图片，保存在本地
 * 使用方式参考RequestCreator内部类
 * 完整的使用方式ZImage.ready().want("请求地址").reSize(图片尺寸).cache(缓存方式).empty(图片占位符).into(图片空间);
 */
public class ZImage {

    private static ZImage mZImage;
    private LruCache<String, Bitmap> mMemoryCache;
    private BaseApplication mBaseApp;
    private Bitmap placeHolderBitmap;

    private ZImage(BaseApplication baseApp) {
        mBaseApp = baseApp;
        placeHolderBitmap = BitmapFactory.decodeResource(mBaseApp.getResources(), R.drawable.user_default);

        mMemoryCache = new LruCache<String, Bitmap>(mBaseApp.getImageCachePoolSize()) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public static void Init(BaseApplication baseApp) {
        if (mZImage == null)
            mZImage = new ZImage(baseApp);
    }

    /**
     * 获得图片管理类
     *
     * @return
     */
    public static ZImage ready() {
        if (mZImage == null) {
            throw new RuntimeException("ZImage需要被初始化才能使用，建议在程序的入口处使用Init()");
        }
        return mZImage;
    }

    /**
     * 构造器起手式，从一个资源开始
     *
     * @param url
     * @return
     */
    public RequestCreator want(String url) {
        return new RequestCreator(url);
    }

    /**
     * 使用占位符
     *
     * @param imageView
     * @param placeHolder
     */
    private void loadEmpty(ImageView imageView, int placeHolder) {
        if (placeHolder <= 0) {
            imageView.setImageBitmap(placeHolderBitmap);
            return;
        }

        String key = String.valueOf(placeHolder);
        Bitmap bitmap = getFromMemoryCache(key);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            bitmap = BitmapFactory.decodeResource(mBaseApp.getResources(), placeHolder);
            putToMemoryCache(key, bitmap);
        }
    }

    /**
     * 从本地获取BitMap
     *
     * @param url
     * @return
     */
    public Bitmap getLocalBitMap(String url) {
        if (url.isEmpty())
            return placeHolderBitmap;

        Bitmap bitmap = getFromMemoryCache(url);

        if (bitmap != null)
            return bitmap;

        bitmap = DBHelper.cache().getBitmap(url);
        return bitmap == null ? placeHolderBitmap : bitmap;
    }


    /**
     * 从本地删除图片
     *
     * @param httpUrl
     */
    public void deleteFromLocal(String httpUrl) {
        mMemoryCache.remove(httpUrl);
        String cachedUrl = DBHelper.cache().trans2Local(httpUrl);
        ZIO.deleteFile(cachedUrl);
        ContactSQLHelper.getInstance().deleteFilePath(httpUrl);
    }

    Bitmap getFromMemoryCache(String url) {
        return mMemoryCache.get(url);
    }

    void putToMemoryCache(String url, Bitmap bitmap) {
        if (bitmap != null && bitmap.getByteCount() > 0)
            mMemoryCache.put(url, bitmap);
    }

    /**
     * 加载图片，经过内存、磁盘、两层缓存如果还没找到，则走http访问网络资源
     *
     * @param url
     * @param imageView
     * @param width
     * @param height
     * @param cacheType
     * @param workType
     * @param placeHolder
     */
    private void load(String url, ImageView imageView, int width, int height, CacheType cacheType, ImageTaskManager.WorkType workType, int placeHolder) {
        if (url.isEmpty()) {
            loadEmpty(imageView, placeHolder);
            return;
        }

        Bitmap bitmap = getFromMemoryCache(url);
        imageView.setTag(url);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        loadEmpty(imageView, placeHolder);

        bitmap = DBHelper.cache().getBitmap(url, width, height);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);

            if (cacheType == CacheType.DiskMemory || cacheType == CacheType.Memory)
                putToMemoryCache(url, bitmap);

            return;
        }
        ImageTaskManager.getInstance().addTask(new LoadImageTask(mBaseApp, imageView, url, width, height, cacheType), workType);
    }

    /**
     * 缓存类型
     */
    public enum CacheType {

        /**
         * 不缓存
         */
        None,


        /**
         * 缓存到硬盘
         */
        Disk,

        /**
         * 缓存到内存
         */
        Memory,

        /**
         * 缓存到硬盘和内存
         */
        DiskMemory
    }


    /**
     * 请求构造器
     */
    public class RequestCreator {
        /**
         * 请求地址
         */
        String url;

        /**
         * 优先级,默认后进先出
         */
        ImageTaskManager.WorkType priority = ImageTaskManager.WorkType.LIFO;

        /**
         * 占位图
         */
        int placeHolder = -1;

        /**
         * 缓存类型，默认内存缓存，基于LRU算法，不用担心内存爆炸
         */
        CacheType cacheType = CacheType.Memory;

        /**
         * 图片的宽度
         */
        int width = mBaseApp.getScreenWidth();

        /**
         * 图片的高度
         */
        int height = mBaseApp.getScreenHeight();

        public RequestCreator(String url) {
            this.url = url;
        }

        /**
         * 占位图
         *
         * @param resID 本地图片资源 R.drawable.
         * @return
         */
        public RequestCreator empty(int resID) {
            placeHolder = resID;
            return this;
        }

        /**
         * 缓存
         *
         * @param cacheType 缓存类型，默认不缓存
         * @return
         */
        public RequestCreator cache(CacheType cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        /**
         * 优先级，默认后进先出。使用本方法降低优先级
         *
         * @return
         */
        public RequestCreator lowPriority() {
            priority = ImageTaskManager.WorkType.LILO;
            return this;
        }

        /**
         * 对图片尺寸进行缩放，节约内存
         *
         * @param width  图片宽度，默认屏幕宽度
         * @param height 图片高度，默认屏幕高度
         * @return
         */
        public RequestCreator reSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }


        /**
         * 载入图片到控件
         *
         * @param imageView
         */
        public void into(ImageView imageView) {
            mZImage.load(url, imageView, width, height, cacheType, priority, placeHolder);
        }

        /**
         * 下载图片
         */
        public void save() {
            ImageTaskManager.getInstance().addTask(new SaveImageTask(mBaseApp, url, width, height), priority);
        }
    }
}
