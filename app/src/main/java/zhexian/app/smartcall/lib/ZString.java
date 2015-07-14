package zhexian.app.smartcall.lib;


public class ZString {
    private static final String HEX_STRING = "0123456789ABCDEF";

    /**
     * https://
     */
    private static final int HTTP_FIRST_SPLIT_POS = 8;


    public static String toBrowserCode(String word) {
        byte[] bytes = word.getBytes();


        if (bytes.length == word.length())
            return word;

        StringBuilder browserUrl = new StringBuilder();
        String tempStr = "";

        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);


            if ((int) currentChar <= 256) {

                if (tempStr.length() > 0) {
                    byte[] cBytes = tempStr.getBytes();

                    for (int j = 0; j < cBytes.length; j++) {
                        browserUrl.append('%');
                        browserUrl.append(HEX_STRING.charAt((cBytes[j] & 0xf0) >> 4));
                        browserUrl.append(HEX_STRING.charAt((cBytes[j] & 0x0f)));
                    }
                    tempStr = "";
                }

                browserUrl.append(currentChar);
            } else {

                tempStr += currentChar;
            }
        }
        return browserUrl.toString();
    }


    public static String getFileCachedDir(String url, String cachedDir) {
        //change http://images.cnitblog.com/news_topic/apple.png to news_topic/apple.png
        url = url.substring(url.indexOf('/', HTTP_FIRST_SPLIT_POS) + 1);
        url = cachedDir + url;

        return url;
    }

}
