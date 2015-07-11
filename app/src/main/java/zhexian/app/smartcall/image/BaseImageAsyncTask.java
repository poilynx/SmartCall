package zhexian.app.smartcall.image;

/**
 * 图片异步任务基类
 */
public abstract class BaseImageAsyncTask implements Runnable {
    public static final int SAVE_IMAGE_TASK_ID = 1;
    public static final int LOAD_IMAGE_TASK_ID = 2;

    public abstract int getTaskId();

    public abstract String getUrl();

    public final String getUniqueUrl() {
        return String.format("%d_%s", getTaskId(), getUrl());
    }

    public void onCancel() {
    }
}
