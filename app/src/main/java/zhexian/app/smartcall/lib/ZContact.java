package zhexian.app.smartcall.lib;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import zhexian.app.smartcall.contact.ContactEntity;
import zhexian.app.smartcall.image.ZImage;
import zhexian.app.smartcall.tools.Utils;

import static android.provider.ContactsContract.Data.CONTENT_URI;
import static android.provider.ContactsContract.Data.MIMETYPE;
import static android.provider.ContactsContract.Data.RAW_CONTACT_ID;


public class ZContact {

    public static boolean isPhoneExists(Context context, String phone) {
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + phone);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);

        boolean isExist = cursor.moveToFirst();
        cursor.close();
        return isExist;
    }

    public static void Add(Activity activity, ContactEntity entity) {
        ContentValues values = new ContentValues();

        ContentResolver content = activity.getContentResolver();
        Uri rawContactUri = content.insert(
                ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        //name
        values.clear();
        values.put(RAW_CONTACT_ID, rawContactId);
        values.put(MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, entity.getUserName());
        content.insert(CONTENT_URI, values);

        // dept
        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Organization.COMPANY, entity.getCompany());
        values.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, entity.getDepartment());
        values.put(ContactsContract.CommonDataKinds.Organization.TITLE, entity.getJobTitle());
        values.put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK);
        content.insert(CONTENT_URI, values);

        //phone
        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, entity.getPhone());
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE);
        content.insert(CONTENT_URI, values);


        //short phone
        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, entity.getShortPhone());
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        content.insert(CONTENT_URI, values);

        // avatar
        values.clear();
        values.put(
                android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                rawContactId);
        values.put(MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, Utils.ConvertBitMapToByte(ZImage.getInstance().getBitMap(entity.getAvatarURL())));
        content.insert(CONTENT_URI, values);
    }
}
