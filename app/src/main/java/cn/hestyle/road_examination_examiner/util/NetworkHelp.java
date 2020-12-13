package cn.hestyle.road_examination_examiner.util;

import android.util.Log;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ip地址、port字符串检测
 */
public class NetworkHelp {
    /**
     * ip v4 地址检测
     * @param ipAddressString   ip v4 地址
     * @return                  是否合法
     */
    public static boolean isValidIpv4Address(String ipAddressString) {
        String regex = "(^((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|([0-9]){1,2})"
                + "([.](25[0-5]|2[0-4][0-9]|[0-1][0-9][0-9]|([0-9]){1,2})){3})$)";

        if (ipAddressString == null) {
            return false;
        }
        ipAddressString = Normalizer.normalize(ipAddressString, Normalizer.Form.NFKC);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ipAddressString);
        return matcher.matches();
    }

    public static boolean isValidPort(String portString) {
        try {
            int port = Integer.parseInt(portString);
            if (port < 1 || port > 65535) {
                throw new Exception("端口号非法！");
            }
            return true;
        } catch (Exception e) {
            Log.e("port", "port = 【" + portString + "】非法！");
            return false;
        }
    }
}
