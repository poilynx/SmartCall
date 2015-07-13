package zhexian.app.smartcall.call;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 本地通讯录sqlite管理类
 */
public class ContactSQLHelper {

    private static ContactSQLHelper mContactSQLHelper;
    private Context mContext;
    private ContactSQLiteDal contactSQLiteDal;

    public ContactSQLHelper(Context context) {
        mContext = context;
        contactSQLiteDal = new ContactSQLiteDal(mContext);
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
        return entity;
    }

    public String getSavedUrlList() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = getDb(true).rawQuery("select filePath from savedFile", null);

        while (cursor.moveToNext()) {
            sb.append(String.format("%s;", cursor.getString(0)));
        }
        return sb.toString();
    }

    public void addFilePath(String path) {
        try {
            getDb(false).execSQL("insert into savedFile(filePath) values(?)", new String[]{path});
        } catch (Exception e) {
            //重复数据不插
            e.printStackTrace();
        }
    }

    public void deleteFilePath(String path) {
        getDb(false).execSQL("delete from savedFile where filePath=?", new String[]{path});
    }

    class ContactSQLiteDal extends SQLiteOpenHelper {
        public ContactSQLiteDal(Context context) {
            super(context, "companyContact.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table contact (phoneNo text,shortPhoneNo text,userName text,jobTitle text,avatarUrl text);");
            sqLiteDatabase.execSQL("create table savedFile(filePath text UNIQUE);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }


}
