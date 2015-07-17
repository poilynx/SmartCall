package zhexian.app.smartcall.call;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite帮助类，管理用户手机号、头像索引
 */
public class ContactSQLHelper {
    private static ContactSQLHelper mContactSQLHelper;
    private ContactSQLiteDal contactSQLiteDal;

    private ContactSQLHelper(Context context) {
        contactSQLiteDal = new ContactSQLiteDal(context);
    }

    public static void Init(Context context) {
        if (mContactSQLHelper == null) {
            mContactSQLHelper = new ContactSQLHelper(context);
        }
    }

    public static ContactSQLHelper getInstance() {
        return mContactSQLHelper;
    }

    public SQLiteDatabase getDb(boolean isRead) {
        if (isRead)
            return contactSQLiteDal.getReadableDatabase();
        else
            return contactSQLiteDal.getWritableDatabase();
    }

    public void clearContact() {
        getDb(false).execSQL("delete from contact");
    }

    public CallUserEntity getContact(String phoneNo) {
        Cursor cursor = getDb(true).rawQuery("select userName,jobTitle,avatarUrl from contact where phoneNo=? or shortPhoneNo=?", new String[]{phoneNo, phoneNo});

        if (!cursor.moveToFirst())
            return null;

        CallUserEntity entity = new CallUserEntity();
        entity.setName(cursor.getString(0));
        entity.setJob(cursor.getString(1));
        entity.setAvatarUrl(cursor.getString(2));
        cursor.close();
        return entity;
    }

    public String getSavedHttpUrlList() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = getDb(true).rawQuery("select httpPath from savedFile", null);

        while (cursor.moveToNext()) {
            sb.append(String.format("%s;", cursor.getString(0)));
        }
        cursor.close();
        return sb.toString();
    }

    public void addFilePath(String httpPath, String localPath) {
        try {
            getDb(false).execSQL("insert into savedFile(httpPath,localPath) values(?,?)", new String[]{httpPath, localPath});
        } catch (Exception e) {
            //重复数据不插
            e.printStackTrace();
        }
    }

    public void deleteFilePath(String httpPath) {
        getDb(false).execSQL("delete from savedFile where httpPath=?", new String[]{httpPath});
    }

    class ContactSQLiteDal extends SQLiteOpenHelper {
        public ContactSQLiteDal(Context context) {
            super(context, "companyContact.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table contact (phoneNo text,shortPhoneNo text,userName text,jobTitle text,avatarUrl text);");
            sqLiteDatabase.execSQL("create table savedFile(httpPath text UNIQUE,localPath text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }


}
