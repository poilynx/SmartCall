package zhexian.app.smartcall.async;

import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池管理类
 */
public class ThreadPoolManager {
    private static final int MAX_OPERATE_THREAD_SIZE = 5;
    private static ThreadPoolManager threadPoolManager;
    Stack<Runnable> taskList;
    ExecutorService threadPool;
    int currentOperateSize = 0;

    public ThreadPoolManager() {
        threadPool = Executors.newFixedThreadPool(MAX_OPERATE_THREAD_SIZE);
        taskList = new Stack<>();
    }

    public static ThreadPoolManager getInstance() {
        if (threadPoolManager == null) {
            synchronized (ThreadPoolManager.class) {
                threadPoolManager = new ThreadPoolManager();
            }
        }
        return threadPoolManager;
    }

    public int getLeftTaskCount() {
        return taskList.size();
    }

    public void addTask(Runnable task) {
        taskList.add(task);
        execTask();
    }

    public synchronized Runnable getTask() {
        if (taskList.size() == 0)
            return null;

        currentOperateSize++;
        return taskList.pop();
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
