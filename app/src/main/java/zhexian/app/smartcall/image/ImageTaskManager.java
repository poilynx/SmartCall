package zhexian.app.smartcall.image;

import android.support.v4.util.ArrayMap;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片任务管理类
 */
public class ImageTaskManager {

    private static final int MAX_OPERATE_THREAD_SIZE = 5;
    private static final ImageTaskManager imageTaskManager = new ImageTaskManager();
    private ArrayMap<String, BaseImageAsyncTask> taskHaspMap;
    private LinkedList<String> taskUrlList;
    private ExecutorService threadPool;
    private int currentOperateSize = 0;
    private int saveTaskCount = 0;
    private int loadTaskCount = 0;
    private ImageTaskManager() {
        threadPool = Executors.newFixedThreadPool(MAX_OPERATE_THREAD_SIZE);
        taskHaspMap = new ArrayMap<>();
        taskUrlList = new LinkedList<>();
    }

    public static ImageTaskManager getInstance() {
        return imageTaskManager;
    }

    public int getLeftSaveTaskCount() {
        return saveTaskCount;
    }

    public synchronized void addTask(BaseImageAsyncTask task, WorkType workType) {
        String url = task.getUniqueUrl();
        int taskID = task.getTaskId();

        //不包含该任务，则任务计数器+1
        if (!taskUrlList.remove(url)) {
            if (taskID == BaseImageAsyncTask.SAVE_IMAGE_TASK_ID)
                saveTaskCount++;
            else if (taskID == BaseImageAsyncTask.LOAD_IMAGE_TASK_ID)
                loadTaskCount++;
        }

        if (saveTaskCount > 0 && taskID == BaseImageAsyncTask.LOAD_IMAGE_TASK_ID) {
            taskHaspMap.remove(url);

            String loadKey = String.format("%d_%s", BaseImageAsyncTask.SAVE_IMAGE_TASK_ID, task.getUrl());
            if (taskUrlList.remove(loadKey)) {
                taskHaspMap.remove(loadKey);
                saveTaskCount--;
            }
        }

        if (workType == WorkType.LIFO)
            taskUrlList.addLast(url);
        else
            taskUrlList.addFirst(url);

        taskHaspMap.put(url, task);
        execTask();
    }

    private synchronized Runnable getTask() {
        int size = taskHaspMap.size();

        if (size == 0)
            return null;

        currentOperateSize++;
        String key = taskUrlList.removeLast();
        return taskHaspMap.remove(key);
    }

    private void execTask() {
        int threadAvailableCount = MAX_OPERATE_THREAD_SIZE - currentOperateSize;

        if (threadAvailableCount <= 0)
            return;

        for (int i = 0; i < threadAvailableCount; i++) {
            Runnable task = getTask();

            if (null != task)
                threadPool.execute(task);
        }
    }

    public void Done(int taskID) {
        currentOperateSize--;

        if (taskID == BaseImageAsyncTask.SAVE_IMAGE_TASK_ID)
            saveTaskCount--;
        else if (taskID == BaseImageAsyncTask.LOAD_IMAGE_TASK_ID)
            loadTaskCount--;

        execTask();
    }

    public enum WorkType {

        /**
         * 后进先出，类似栈
         */
        LIFO,


        /**
         * 后进后出，类似队列
         */
        LILO
    }

}
