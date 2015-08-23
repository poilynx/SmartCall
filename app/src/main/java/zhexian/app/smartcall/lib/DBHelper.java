package zhexian.app.smartcall.lib;

/**
 * Created by 陈俊杰 on 2015/8/20.
 * 存储管理类
 */
public class DBHelper {
    public static final String DIR_CORE = "core";
    public static final String DIR_CACHE = "zCache";
    private static ZDisk diskCore;
    private static ZDisk diskCache;

    public static void init(String path) {
        if (diskCore == null)
            diskCore = new ZDisk(String.format("%s%s/", path, DIR_CORE));

        if (diskCache == null)
            diskCache = new ZDisk(String.format("%s%s/", path, DIR_CACHE));
    }

    /**
     * 核心文件区，不受清理文件影响
     *
     * @return
     */
    public static ZDisk core() {
        return diskCore;
    }

    /**
     * 缓存文件，清理了也没关系。
     *
     * @return
     */
    public static ZDisk cache() {
        return diskCache;
    }
}
