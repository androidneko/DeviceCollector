package com.androidcat.catlibs.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by coolbear on 15/5/11.
 */
public class ConvertUtil {

    private final static String TAG = "ConvertUtil";
    private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * 小端模式
     * @param arrData
     * @param start
     * @param len
     * @return
     */
    public static int toIntR(byte[] arrData, int start, int len) {
        int nRet = 0;

        for (int i = start; (i >= 0 && len > 0); --i, --len) {
            nRet <<= 8;
            nRet |= arrData[i] & 0xFF;
        }
        return nRet;
    }
    /**
     * 大端模式
     * @param arrData
     * @return
     */
    public static int toInt(byte[] arrData) {
        int nRet = 0;

        for (final byte data : arrData) {
            nRet <<= 8;
            nRet |= data & 0xFF;
        }

        return nRet;
    }

    public static int toInt(byte[] arrData, int start, int len) {
        int nRet = 0;

        final int nLen = start + len;
        for (int i = start; i < nLen; ++i) {
            nRet <<= 8;
            nRet |= arrData[i] & 0xFF;
        }

        return nRet;
    }


    /**
     * Hex 转Ascii
     * @param hexValue
     * @return
     */
    public static String hexToASCII(String hexValue)
    {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2)
        {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    /**
     * byte to Hex
     *
     * @param bytes array of byte
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        if (null == bytes) {
            return null;
        }

        StringBuffer sBuff = new StringBuffer();
        int nLen = bytes.length;
        for (int i = 0; i < nLen; i++)
        {
            if ((bytes[i] & 0xFF) < 16)
            {
                sBuff.append('0');
            }
            sBuff.append(Integer.toHexString(bytes[i] & 0xFF));
        }
        return sBuff.toString();
    }


    /**
     * 转十六进制
     * @param arrData
     * @param start
     * @param len
     * @return
     */
    public static String toHexString(byte[] arrData, int start, int len) {
        char[] arrRet = new char[len * 2];
        int nLen = start + len;

        int nPos = 0;
        for (int i = start; i < nLen; ++i) {
            final byte data = arrData[i];
            arrRet[nPos++] = HEX[0x0F & (data >> 4)];
            arrRet[nPos++] = HEX[0x0F & data];
        }
        return new String(arrRet);
    }

    public static String hexToChar(byte[] arrData, int start, int len) {

        char[] arrRet = new char[len];
        int nLen = start + len;

        int nPos = 0;
        for (int i=start; i<nLen; ++i) {
            arrRet[nPos++] = (char)arrData[i];
        }

        return new String(arrRet);
    }

    /**
     * 异或
     * @param dest
     * @param src
     * @return
     */
    public static byte[] Xor(byte[] dest, byte[] src)
    {
        byte[] temp=new byte[dest.length];
        if(dest.length>src.length)
        {
            return null;
        }
        for(int i=0;i<dest.length;i++)
        {
            temp[i]= (byte) (((byte)dest[i])^((byte)src[i]));
        }
        return temp;
    }

    public static byte[] intToBytes(int src) {

        String sSrc = Integer.toHexString(src);
        int nLen = sSrc.length();
        if (nLen % 2 == 0) {
            return strToBytes(sSrc);
        } else {
            return strToBytes("0" + sSrc);
        }
    }

    public static String hexToBinary(String src) {

        if (null == src || 0 == src.length()) {
            return null;
        }

        String sBinaryStr = Integer.toBinaryString(Integer.parseInt(src, 16));
        int nLen = sBinaryStr.length();

        StringBuilder sbBinary = new StringBuilder();
        for (int i = 0; i < 8 - nLen % 8; i++) {
            sbBinary.append("0");
        }
        sbBinary.append(sBinaryStr);

        return sbBinary.toString();
    }

    public String strToHex(String str){
        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }

        return hex.toString();
    }

    /**
     * String switch to byte
     *
     * @param src source string
     * @return array byte
     */
    public static byte[] strToBytes(String src) {

        if (null == src || 0 == src.length() || 0 != src.length() % 2) {
            return null;
        }

        byte[] arrRes = new byte[src.length() / 2];
        StringBuffer sBuff = new StringBuffer(src);

        int i = 0;
        String sTmp = null;
        while (i < sBuff.length() - 1) {
            sTmp = src.substring(i, i + 2);
            arrRes[i / 2] = (byte) Integer.parseInt(sTmp, 16);
            i += 2;
        }

        return arrRes;
    }



    /**
     * md5
     *
     * @param src 待md5的字符串
     * @return md5后的字符串
     */
    public static String encrypByMd5(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(src.getBytes());//update处理
            byte[] encryContext = md.digest();//调用该方法完成计算

            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < encryContext.length; offset++) {//做相应的转化（十六进制）
                i = encryContext[offset];
                if (i < 0) i += 256;
                if (i < 16) buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //System.out.println("32result: " + buf.toString());// 32位的加密
            return buf.toString();
//           System.out.println("16result: " + buf.toString().substring(8, 24));// 16位的加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getUTF8toGBKString(String gbkStr) {
        int n = gbkStr.length();
        byte[] utfBytes = new byte[3 * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int m = gbkStr.charAt(i);
            if (m < 128 && m >= 0) {
                utfBytes[k++] = (byte) m;
                continue;
            }
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
        }
        if (k < utfBytes.length) {
            byte[] tmp = new byte[k];
            System.arraycopy(utfBytes, 0, tmp, 0, k);
            return tmp;
        }
        return utfBytes;
    }


    public static boolean isBlankOrNull(String str){
        if(null==str)return true;
        //return str.length()==0?true:false;
        return str.length()==0;
    }

    public static String replaceSpecialtyStr(String str,String pattern,String replace){
        if(isBlankOrNull(pattern))
            pattern="\\s*|\t|\r|\n";
        if(isBlankOrNull(replace))
            replace="";
        return Pattern.compile(pattern).matcher(str).replaceAll(replace);
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String dateToString(String str,int flag)
    {
        String strdate=null;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format;
        Date date= null;
        try {
            date = format1.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (flag)
        {
            case 1:

                break;
            case 2:
                format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                strdate=format.format(date);
                default:
                    break;
        }
        return strdate;

    }


    /**
     * 获取APDU指令列表
     *
     * @return 返回APDU List
     */
    public static ArrayList<String> getSectionList(String content) {
        ArrayList<String> lstCode = new ArrayList<String>();

        if (content.contains("|")) {
            String[] arrCode = content.split("\\" + "|");
            for (int i = 0; i < arrCode.length; i++) {
                lstCode.add(arrCode[i]);
            }
        } else {
            lstCode.add(content);
        }

        return lstCode;
    }

    public static String ASCIIToHex(String str){

        if (str==null) return null;
        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }

        return hex.toString();
    }
}
