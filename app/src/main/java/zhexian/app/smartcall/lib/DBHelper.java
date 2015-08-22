package zhexian.app.smartcall.lib;

/**
 * Created by 陈俊杰 on 2015/8/20.
 * 存储管理类
 */
public class DBHelper {
    public static final String DIR_PERMANENT = "permanent";

    private static ZDisk diskPermanent;
    private static ZDisk diskCache;

    public static void init(String rootDir, String cacheDir) {
        if (diskPermanent == null)
            diskPermanent = new ZDisk(String.format("%s%s/", rootDir, DIR_PERMANENT));

        if (diskCache == null)
            diskCache = new ZDisk(cacheDir);
    }

    /**
     * 核心文件区，不受清理文件影响
     *
     * @return
     */
    public static ZDisk permanent() {
        return diskPermanent;
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
