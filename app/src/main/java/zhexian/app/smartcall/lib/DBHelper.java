package zhexian.app.smartcall.lib;

import zhexian.app.smartcall.tools.Utils;

/**
 * Created by 陈俊杰 on 2015/8/20.
 * 存储管理类
 */
public class DBHelper {
    private static ZDisk diskPermanent;
    private static ZDisk diskCache;

    public static void init(String path) {
        if (diskPermanent == null)
            diskPermanent = new ZDisk(String.format("%s/%s/", path, Utils.DIR_PERMANENT));

        if (diskCache == null)
            diskCache = new ZDisk(String.format("%s/%s/", path, Utils.DIR_CACHE));
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
