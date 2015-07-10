package zhexian.app.smartcall.tools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class DoAction {
    public static void Call(Activity activity, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    public static void JumpToSMS(Activity activity, String number, String body) {
        Uri uri = Uri.parse("smsto:" + number);
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
        sendIntent.putExtra("sms_body", body);
        activity.startActivity(sendIntent);
    }
}
