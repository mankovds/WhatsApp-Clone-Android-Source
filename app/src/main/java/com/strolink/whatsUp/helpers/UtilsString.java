package com.strolink.whatsUp.helpers;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Abderrahim El imame on 6/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UtilsString {

    /**
     * method to remove the last string
     *
     * @param str this is parameter for removelastString  method
     * @return return value
     */
    public static String removelastString(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static boolean checkForUrls(String string) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(string);//replace with string to compare
        return m.find();
    }

    public static String getUrl(String string) {
        Pattern p = Patterns.WEB_URL;
        Matcher matcher = p.matcher(string);//replace with string to compare
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }


    public static boolean isValidUrl(String url) {
        return !(url == null || url.isEmpty()) && Patterns.WEB_URL.matcher(url).matches();
    }


    /**
     * method to unescape string
     *
     * @param escaped this is parameter for unescapeJavaString  method
     * @return return value
     */
    public static String escapeJava(String escaped) {

        StringBuilder sb = new StringBuilder(escaped.length());

        for (int i = 0; i < escaped.length(); i++) {
            char ch = escaped.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == escaped.length() - 1) ? '\\' : escaped
                        .charAt(i + 1);
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < escaped.length() - 1) && escaped.charAt(i + 1) >= '0'
                            && escaped.charAt(i + 1) <= '7') {
                        code += escaped.charAt(i + 1);
                        i++;
                        if ((i < escaped.length() - 1) && escaped.charAt(i + 1) >= '0'
                                && escaped.charAt(i + 1) <= '7') {
                            code += escaped.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    case 'u':
                        if (i >= escaped.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + escaped.charAt(i + 2) + escaped.charAt(i + 3)
                                        + escaped.charAt(i + 4) + escaped.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * method to escape string
     *
     * @param string this is parameter for escapeJavaString  method
     * @return return value
     */
    public static String unescapeJava(String string) {

        StringBuilder sb = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == string.length() - 1) ? '\\' : string.charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < string.length() - 1) && string.charAt(i + 1) >= '0'
                            && string.charAt(i + 1) <= '7') {
                        code += string.charAt(i + 1);
                        i++;
                        if ((i < string.length() - 1) && string.charAt(i + 1) >= '0'
                                && string.charAt(i + 1) <= '7') {
                            code += string.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    case 'u':
                        if (i >= string.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + string.charAt(i + 2) + string.charAt(i + 3)
                                        + string.charAt(i + 4) + string.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
