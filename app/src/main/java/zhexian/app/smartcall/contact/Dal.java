package zhexian.app.smartcall.contact;

import android.database.sqlite.SQLiteDatabase;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.image.ImageTaskManager;
import zhexian.app.smartcall.image.SaveImageTask;
import zhexian.app.smartcall.lib.ZHttp;
import zhexian.app.smartcall.lib.ZIO;
import zhexian.app.smartcall.tools.PinYinTool;
import zhexian.app.smartcall.tools.Utils;

/**
 * 数据访问层
 */
public class Dal {
    public static final String CONTACTS_FILE_NAME = "user_list.json";

    public static List<ContactEntity> getList(BaseApplication baseApp) {
        return readListFromJson(baseApp);
    }

    public static Boolean SaveToFile(BaseApplication baseApp, String content) {
        try {
            List<ContactEntity> list = LoganSquare.parseList(content, ContactEntity.class);

            if (list == null || list.size() == 0)
                return false;

            ContactSQLHelper sqlHelper = ContactSQLHelper.getInstance();
            sqlHelper.clearContact();
            SQLiteDatabase db = sqlHelper.getDb(false);
            db.beginTransaction();

            for (ContactEntity entity : list) {
                entity.setUserNamePY(PinYinTool.getPingYin(entity.getUserName()));
                entity.setUserNameHeadPY(PinYinTool.getPinYinHeadChar(entity.getUserName()));

                db.execSQL("insert into contact (phoneNo,shortPhoneNo,userName,jobTitle,avatarUrl) values(?,?,?,?,?)",
                        new String[]{entity.getPhone(), entity.getShortPhone(), entity.getUserName(), entity.getJobTitle(), entity.getAvatarURL()});
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            Collections.sort(list);

            if (baseApp.isNetworkWifi()) {
                int maxIndex = list.size() - 1;
                String existsFileUrls = sqlHelper.getSavedUrlList();
                for (int i = maxIndex; i >= 0; i--) {
                    ContactEntity entity = list.get(i);

                    boolean isAvatarNew = !entity.getAvatarURL().isEmpty() && !existsFileUrls.contains(entity.getAvatarURL());
                    if (isAvatarNew)
                        ImageTaskManager.getInstance().addTask(new SaveImageTask(baseApp, entity.getAvatarURL(), Utils.AVATAR_IMAGE_SIZE, Utils.AVATAR_IMAGE_SIZE));
                }
            }
            baseApp.saveToFile(CONTACTS_FILE_NAME, LoganSquare.serialize(list, ContactEntity.class));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static List<ContactEntity> readListFromJson(BaseApplication baseApp) {
        boolean isFileExist = baseApp.isLocalFileExist(CONTACTS_FILE_NAME);

        if (!isFileExist)
            return null;

        try {
            String content = baseApp.readFromFile(CONTACTS_FILE_NAME);
            return LoganSquare.parseList(content, ContactEntity.class);

        } catch (IOException e) {
            e.printStackTrace();
            ZIO.deleteFile(baseApp.getFilePath() + CONTACTS_FILE_NAME);
        }

        return null;
    }

    /**
     * @param baseUrl  地址
     * @param userName 用户名
     * @param password 密码
     * @return json字符串，登陆失败，则返回空''
     */
    public static String readFromHttp(String baseUrl, String userName, String password) {
        try {
            String url = String.format(baseUrl, userName, password);
            return ZHttp.getString(url);
        } catch (Exception e) {
            return null;
        }
    }
}
