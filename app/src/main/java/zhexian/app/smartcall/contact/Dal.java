package zhexian.app.smartcall.contact;

import android.database.sqlite.SQLiteDatabase;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.lib.DBHelper;
import zhexian.app.smartcall.lib.ZHttp;
import zhexian.app.smartcall.tools.PinYinTool;

/**
 * 数据访问层
 */
public class Dal {
    private static final String CONTACTS_FILE_NAME = "user_list.json";

    public static List<ContactEntity> getList() {
        return DBHelper.cache().getList(CONTACTS_FILE_NAME, ContactEntity.class);
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
                String existsFileUrls = sqlHelper.getSavedHttpUrlList();

                for (int i = maxIndex; i >= 0; i--) {
                    ContactEntity entity = list.get(i);

                    boolean isAvatarNew = !entity.getAvatarURL().isEmpty() && !existsFileUrls.contains(entity.getAvatarURL());
                    if (isAvatarNew)
                        ZImage.ready().want(entity.getAvatarURL()).lowPriority().save();
                }
            }
            DBHelper.cache().save(CONTACTS_FILE_NAME, list, ContactEntity.class);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
