package com.androidcat.catlibs.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * *****************************************************************************<br>
 * 模块功能：包含des加解密的工具方法，String和byte相互转换的工具方法<br>
 * 作 者: ac
 * 单 位：武汉天喻信息 研发中心 <br>
 * 开发日期：2013-5-16 上午10:40:54 <br>
 * *****************************************************************************<br>
 */
public final class DesTools {
    private static final String TAG = "DesTools";

    private DesTools() {

    }

    /**
     * ECB模式中的DES/3DES/TDES算法(数据不需要填充),支持8、16、24字节的密钥 说明：3DES/TDES解密算法 等同与
     * 先用前8个字节密钥DES解密 再用中间8个字节密钥DES加密 最后用后8个字节密钥DES解密 一般前8个字节密钥和后8个字节密钥相同
     *
     * @param key
     *            加解密密钥(8字节:DES算法 16字节:3DES/TDES算法
     *            24个字节:3DES/TDES算法,一般前8个字节密钥和后8个字节密钥相同)
     * @param data
     *            待加/解密数据(长度必须是8的倍数)
     * @param mode
     *            0-加密，1-解密
     * @return 加解密后的数据 为null表示操作失败
     */
    public static String desecb(final String key, final String data, final int mode) {
        try {

            // 判断加密还是解密
        	final int opmode = (mode == 0) ? Cipher.ENCRYPT_MODE
                    : Cipher.DECRYPT_MODE;

            SecretKey keySpec = null;

            Cipher enc = null;

            // 判断密钥长度
            if (key.length() == 16) {// 8位
                // 生成安全密钥
                keySpec = new SecretKeySpec(str2bytes(key), "DES");// key

                // 生成算法
                enc = Cipher.getInstance("DES/ECB/NoPadding");
            } else if (key.length() == 32) {// 16位
                // 计算加解密密钥，即将16个字节的密钥转换成24个字节的密钥，24个字节的密钥的前8个密钥和后8个密钥相同,并生成安全密钥
                keySpec = new SecretKeySpec(str2bytes(key
                        + key.substring(0, 16)), "DESede");// 将key前8个字节复制到keyecb的最后8个字节

                // 生成算法
                enc = Cipher.getInstance("DESede/ECB/NoPadding");
            } else if (key.length() == 48) {// 24位
                // 生成安全密钥
                keySpec = new SecretKeySpec(str2bytes(key), "DESede");// key

                // 生成算法
                enc = Cipher.getInstance("DESede/ECB/NoPadding");
            } else {
                Log.e(TAG, "Key length is error");
                return null;
            }

            // 初始化
            enc.init(opmode, keySpec);

            // 返回加解密结果
            return (bytesToHexString(enc.doFinal(str2bytes(data))))
                    .toUpperCase();// 开始计算
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * 填充80数据，首先在数据块的右边追加一个
     * '80',如果结果数据块是8的倍数，不再进行追加,如果不是,追加0x00到数据块的右边，直到数据块的长度是8的倍数。
     *
     * @param data
     *            待填充80的数据
     * @return
     */
    public static String padding80(String data) {
    	final int padlen = 8 - (data.length() / 2) % 8;
        String padstr = "";
        for (int i = 0; i < padlen - 1; i++) {
            padstr += "00";
        }
        data = data + "80" + padstr;
        return data;
    }

    /**
     * 对数据进行加密
     *
     * @param key
     *            加密的key
     * @param data
     *            要加密的数据
     * @return
     */
    public static String encrypt(final String key, final String data) {
        String dData = null;
        try {
            if (data != null) {
            	final byte[] dataByte = data.getBytes();
            	final String hexStr = bytesToHexString(dataByte);
            	final String tempData = padding80(hexStr);
                dData = desecb(key, tempData, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return dData;
    }

    /**
     * 对数据进行解密
     *
     * @param key
     *            解密的key
     * @param data
     *            要解密的数据
     * @return
     */
    public static String decrypt(final String key, final String data) {
        String result = null;
        try {
            if (data != null) {
                String value = desecb(key, data.trim(), 1);
                value = unPadding80(value);
                final byte[] edata = str2bytes(value);
                result = new String(edata, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

    /**
     * 逆向去掉填80
     *
     * @param data
     * @return
     */
    public static String unPadding80(final String data) {
    	final int len = data.length();
    	final int padlen = (len / 2) % 8;
        if (padlen != 0) {
            return data;
        } else {
        	final String tempStr = data.substring(data.length() - 16);
            for (int i = 0; i < 8; i++) {
            	final int start = 2 * i;
            	final String temp = tempStr.substring(start, start + 2);
                if ("80".equals(temp)) {
                    if (i == 7) {
                        return data.substring(0, len - 2);
                    } else {
                    	final int xInt = Integer.parseInt(tempStr.substring(start + 2),
                                16);
                        if (xInt == 0) {
                            return data.substring(0, len - 16 + start);
                        }
                    }
                }
            }
            return data;
        }
    }

    /**
     * 将16进制组成的字符串转换成byte数组 例如 hex2Byte("0710BE8716FB"); 将返回一个byte数组
     * b[0]=0x07;b[1]=0x10;...b[5]=0xFB;
     *
     * @param src
     *            待转换的16进制字符串
     * @return
     */
    public static byte[] str2bytes(final String src) {
        if (src == null || src.length() == 0 || src.length() % 2 != 0) {
            return null;
        }
        final int nSrcLen = src.length();
        byte byteArrayResult[] = new byte[nSrcLen / 2];
        final StringBuilder strBufTemp = new StringBuilder(src);
        String strTemp;
        int iIndex = 0;
        while (iIndex < strBufTemp.length() - 1) {
            strTemp = src.substring(iIndex, iIndex + 2);
            byteArrayResult[iIndex / 2] = (byte) Integer.parseInt(strTemp, 16);
            iIndex += 2;
        }
        return byteArrayResult;
    }

    /**
     * 将byte数组转换成16进制组成的字符串 例如 一个byte数组 b[0]=0x07;b[1]=0x10;...b[5]=0xFB;
     * byte2hex(b); 将返回一个字符串"0710BE8716FB"
     *
     * @param bytes
     *            待转换的byte数组
     * @return
     */
    public static String bytesToHexString(final byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        final StringBuilder buff = new StringBuilder();
        final int len = bytes.length;
        for (int j = 0; j < len; j++) {
            if ((bytes[j] & 0xff) < 16) {
                buff.append('0');
            }
            buff.append(Integer.toHexString(bytes[j] & 0xff));
        }
        return buff.toString().toLowerCase();
    }

    public static byte[] hexToBytes(String hex) {
        byte[] buffer = new byte[hex.length() / 2];
        String strByte;

        for (int i = 0; i < buffer.length; i++) {
            strByte = hex.substring(i * 2, i * 2 + 2);
            buffer[i] = (byte) Integer.parseInt(strByte, 16);
        }

        return buffer;
    }

    /**
     * 生成1-8长度得阿斯克码随机数
     *
     * @param length
     *            1-8
     * @return ascii码随机数
     */
    public static String generalStringToAscii(int length) {

        int num = 1;
        for (int i = 0; i < length; i++) {
            num *= 10;
        }

        Random rand = new Random((new Date()).getTime());
        String strRandom = Integer.toString(rand.nextInt(num));

        strRandom = leftPad(strRandom, length, '0');

        StringBuffer sb = new StringBuffer();
        char[] chars = strRandom.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append((int) chars[i]);
        }

        return sb.toString();
    }

    /*
	 * 将字符串编码成16进制数字,适用于所有字符（包括中文）
	 */
    public static String encodeHexString(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        final String hexString = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数将字节数组中每个字节拆解成2位16进制整数将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /*
	 * 将16进制数字解码成字符串,适用于所有字符（包括中文）
	 */
    public static String hexStringToString(String bytes) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        final String hexString = "0123456789abcdef";
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray(), "UTF-8");
    }

    /**
     * <p>Left pad a String with a specified character.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padChar  the character to pad with
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     * @since 2.0
     */
    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > 8192) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    /**
     * <p>Left pad a String with a specified String.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padStr  the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= 8192) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method does not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using instead.
     * </p>
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    /**
     * A String for a space character.
     *
     * @since 3.2
     */
    public static final String SPACE = " ";

    /**
     * The empty String {@code ""}.
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * confound data with random bytes,make sure the original data is hard to recognize
     * @param data the original data
     * @param factor determine how many random bytes will be added
     * @since 2.0
     */
    public static byte[] confound (byte[] data,int factor){
        if (data == null){
            return data;
        }
        Random random = new Random();
        int newLen = data.length + data.length*factor;
        byte[] newData = new byte[newLen];
        int start = 0;
        for (int i = 0;i < data.length;i++){
            int newIndex = (factor + i* factor) + i;
            for (int j = start; j < newIndex;j++){
                int tmp = random.nextInt(256);
                newData[j] = (byte) tmp;
            }
            newData[newIndex] = data[i];
            start = newIndex+1;
        }
        //LogUtil.d("confound","add random-->"+bytesToHexString(newData));
        //do reverse
        for (int i = 0; i < newData.length; i++) {
            newData[i] = (byte) (~newData[i] & 0xff);
        }
        //LogUtil.d("confound","do reverse-->"+bytesToHexString(newData));
        //do exchange
        for (int i = 0; i < newData.length; i++){
            byte itemp = newData[i];
            newData[i]= newData[i + 1];
            newData[i + 1] = itemp;
            i++;
        }
        //LogUtil.d("confound","do exchange-->"+bytesToHexString(newData));
        return newData;
    }

    public static byte[] unconfound (byte[] newData,int factor){
        if (newData == null || (newData.length%(factor+1) != 0)){
            return newData;
        }
        //do exchange
        for (int i = 0; i < newData.length; i++){
            byte itemp = newData[i];
            newData[i]= newData[i + 1];
            newData[i + 1] = itemp;
            i++;
        }
        //LogUtil.d("confound","un exchange-->"+bytesToHexString(newData));
        //do reverse
        for (int i = 0; i < newData.length; i++) {
            newData[i] = (byte) (~newData[i] & 0xff);
        }

        //LogUtil.d("confound","un reverse-->"+bytesToHexString(newData));

        int len = newData.length/(factor+1);
        byte[] data = new byte[len];
        for (int i = 0;i < len;i++){
            int newIndex = (factor + i* factor) + i;
            data[i] = newData[newIndex];
        }
        //LogUtil.d("confound","del random-->"+bytesToHexString(data));
        return data;
    }

}
