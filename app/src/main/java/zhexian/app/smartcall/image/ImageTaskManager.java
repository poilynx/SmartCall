package zhexian.app.smartcall.image;

import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片任务管理类
 */
public class ImageTaskManager {
    private static final int MAX_OPERATE_THREAD_SIZE = 5;
    private static ImageTaskManager imageTaskManager = new ImageTaskManager();
    HashMap<String, BaseImageAsyncTask> taskHaspMap;
    Stack<String> taskUrlList;
    ExecutorService threadPool;
    int currentOperateSize = 0;

    private ImageTaskManager() {
        threadPool = Executors.newFixedThreadPool(MAX_OPERATE_THREAD_SIZE);
        taskHaspMap = new HashMap<>();
        taskUrlList = new Stack<>();
    }

    public static ImageTaskManager getInstance() {
        return imageTaskManager;
    }


    public int getLeftTaskCount() {
        return taskHaspMap.size();
    }

    public synchronized void addTask(BaseImageAsyncTask task) {
        String url = task.getUniqueUrl();
        taskUrlList.remove(url);

        if (task.getTaskId() == BaseImageAsyncTask.LOAD_IMAGE_TASK_ID) {
            BaseImageAsyncTask oldTask = taskHaspMap.remove(url);

            if (oldTask != null)
                oldTask.onCancel();

            String loadKey = String.format("%d_%s", BaseImageAsyncTask.SAVE_IMAGE_TASK_ID, task.getUrl());
            taskUrlList.remove(loadKey);
            taskHaspMap.remove(loadKey);
        }

        taskUrlList.push(url);
        taskHaspMap.put(url, task);
        execTask();
    }


    public synchronized Runnable getTask() {
        int size = taskHaspMap.size();

        if (size == 0)
            return null;

        currentOperateSize++;
        String key = taskUrlList.pop();
        return taskHaspMap.remove(key);
    }

    public void execTask() {
        int threadAvailableCount = MAX_OPERATE_THREAD_SIZE - currentOperateSize;

        if (threadAvailableCount <= 0)
            return;

        for (int i = 0; i < threadAvailableCount; i++) {
            Runnable task = getTask();

            if (null != task)
                threadPool.execute(task);
        }
    }

    public void Done() {
        currentOperateSize--;
        execTask();
    }

}
