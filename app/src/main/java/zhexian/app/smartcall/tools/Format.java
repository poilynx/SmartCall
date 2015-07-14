package zhexian.app.smartcall.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本格式化工具类
 */
public class Format {

    public static boolean isChinese(String str) {
        byte[] bytes = str.getBytes();
        return bytes.length != str.length();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static boolean isMobilePhone(String str) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
