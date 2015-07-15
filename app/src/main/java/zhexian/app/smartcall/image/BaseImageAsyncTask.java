package zhexian.app.smartcall.image;

/**
 * 图片异步任务基类
 */
abstract class BaseImageAsyncTask implements Runnable {
    static final int SAVE_IMAGE_TASK_ID = 1;
    static final int LOAD_IMAGE_TASK_ID = 2;
    static final int CACHE_PACK_IMAGE_TASK_ID = 3;
    static final int CACHE_IMAGE_TASK_ID = 4;

    public abstract int getTaskId();

    public abstract String getUrl();

    public final String getUniqueUrl() {
        return String.format("%d_%s", getTaskId(), getUrl());
    }
}
