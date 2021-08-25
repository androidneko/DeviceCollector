package com.androidcat.catlibs.utils;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * 通用工具类,封装了很多常用方法
 *
 */
public class CommonMethods {

	public static short[] bytesToShorts(byte[] bytes) {
		if(bytes==null){
			return null;
		}
		short[] shorts = new short[bytes.length/2];
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
		return shorts;
	}
	public static byte[] shortsToBytes(short[] shorts) {
		if(shorts==null){
			return null;
		}
		byte[] bytes = new byte[shorts.length * 2];
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);

		return bytes;
	}

	public static byte[] shortToBytes(short s) {
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			shortBuf[i] = (byte) ((s >>> offset) & 0xff);
		}
		return shortBuf;
	}

	/**
	 * byte字节数组转换Short类型（未严格测试）
	 * 
	 * @param outBuf
	 * @return
	 */
	public static short bytesToShort(byte[] outBuf) {

		if (outBuf.length < 2) {
			return (short) (outBuf[0] < 0 ? outBuf[0] + 256 : outBuf[0]);
		} else {
			return (short) (((outBuf[0] < 0 ? outBuf[0] + 256 : outBuf[0]) << 8) + (outBuf[1] < 0 ? outBuf[1] + 256
					: outBuf[1]));
		}

	}

	/**
	 * 填充XX数据，如果结果数据块是8的倍数，不再进行追加,如果不是,追加0xXX到数据块的右边，直到数据块的长度是8的倍数。
	 * 
	 * @param data
	 *            待填充XX的数据
	 * @return
	 */
	public static String padding(String data, String inData) {
		int padlen = 8 - (data.length() / 2) % 8;
		if (padlen != 8) {
			String padstr = "";
			for (int i = 0; i < padlen; i++)
				padstr += inData;
			data += padstr;
			return data;
		} else {
			return data;
		}
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
		int padlen = 8 - (data.length() / 2) % 8;
		String padstr = "";
		for (int i = 0; i < padlen - 1; i++)
			padstr += "00";
		data = data + "80" + padstr;
		return data;
	}

	/**
	 * 获取当前时间相隔N天的日期,格式yyyymmdd
	 * 
	 * @param distance
	 *            和今天的间隔天数
	 * @return 获取的日期,格式yyyymmdd
	 */
	public static String getDateString(int distance) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, distance);
		//
		return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
	}

	/**
	 * 生成16位的动态链接库鉴权十六进制随机数字符串
	 *
	 * @return String
	 */
	public static String yieldHexRand() {
		StringBuffer strBufHexRand = new StringBuffer();
		Random rand = new Random(System.currentTimeMillis());
		int index;
		// 随机数字符
		char charArrayHexNum[] = { '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'0', 'A', 'B', 'C', 'D', 'E', 'F' };
		for (int i = 0; i < 16; i++) {
			index = Math.abs(rand.nextInt()) % 16;
			if (i == 0) {
				while (charArrayHexNum[index] == '0') {
					index = Math.abs(rand.nextInt()) % 16;
				}
			}
			strBufHexRand.append(charArrayHexNum[index]);
		}
		return strBufHexRand.toString();
	}

	/**
	 * 分析类名
	 *
	 * @param strName
	 *            String
	 * @return String
	 */
	public static String analyseClassName(String strName) {
		String strTemp = strName.substring(strName.lastIndexOf(".") + 1,
				strName.length());
		return strTemp.substring(strTemp.indexOf(" ") + 1, strTemp.length());
	}

	static public String convertInt2String(int n, int len) {
		String str = String.valueOf(n);
		int strLen = str.length();

		String zeros = "";
		for (int loop = len - strLen; loop > 0; loop--) {
			zeros += "0";
		}

		if (n >= 0) {
			return zeros + str;
		} else {
			return "-" + zeros + str.substring(1);
		}
	}

	static public int convertString2Int(String str, int defaultValue) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/** yyyyMMddhhmmss */
	public static String getDateTimeString2() {
		Calendar cal = Calendar.getInstance();

		return new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss")
				.format(cal.getTime());

	}

	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null) {
			return "";
		}
		StringBuffer buff = new StringBuffer();
		int len = bytes.length;
		for (int j = 0; j < len; j++) {
			if ((bytes[j] & 0xff) < 16) {
				buff.append('0');
			}
			buff.append(Integer.toHexString(bytes[j] & 0xff));
		}
		return buff.toString();
	}

	/**
	 * usage: str2bytes("0710BE8716FB"); it will return a byte array, just like
	 * : b[0]=0x07;b[1]=0x10;...b[5]=0xfb;
	 */
	public static byte[] str2bytes(String src) {
		if (src == null || src.length() == 0 || src.length() % 2 != 0) {
			return null;
		}
		int nSrcLen = src.length();
		byte byteArrayResult[] = new byte[nSrcLen / 2];
		StringBuffer strBufTemp = new StringBuffer(src);
		String strTemp;
		int i = 0;
		while (i < strBufTemp.length() - 1) {
			strTemp = src.substring(i, i + 2);
			byteArrayResult[i / 2] = (byte) Integer.parseInt(strTemp, 16);
			i += 2;
		}
		return byteArrayResult;
	}

	public static int strcpy(byte d[], byte s[], int from, int maxlen) {
		int i;
		for (i = 0; i < maxlen; i++) {
			d[i + from] = s[i];
		}

		d[i + from] = 0;
		return i;
	}

	public static int memcpy(byte d[], byte s[], int from, int maxlen) {
		int i;
		for (i = 0; i < maxlen; i++) {
			d[i + from] = s[i];
		}
		return i;
	}

	public static void BytesCopy(byte[] dest, byte[] source, int offset1,
			int offset2, int len) {
		for (int i = 0; i < len; i++) {
			dest[offset1 + i] = source[offset2 + i];
		}
	}

	/**
	 * usage: input: n = 1000000000 ( n = 0x3B9ACA00) output: byte[0]:3b
	 * byte[1]:9a byte[2]:ca byte[3]:00 notice: the scope of input integer is [
	 * -2^32, 2^32-1] ; **In CMPP2.0,the typeof msg id is ULONG,so,need
	 * ulong2Bytes***
	 */
	public static byte[] int2Bytes(int n) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(n);
		return bb.array();
	}

	public static byte[] long2Bytes(long l) {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(l);
		return bb.array();
	}

	/**
	 * 将整数转为16进行数后并以指定长度返回（当实际长度大于指定长度时只返回从末位开始指定长度的值）
	 *
	 * @param val
	 *            int 待转换整数
	 * @param len
	 *            int 指定长度
	 * @return String
	 */
	public static String Int2HexStr(int val, int len) {
		String result = Integer.toHexString(val).toUpperCase();
		int r_len = result.length();
		if (r_len > len) {
			return result.substring(r_len - len, r_len);
		}
		if (r_len == len) {
			return result;
		}
		StringBuffer strBuff = new StringBuffer(result);
		for (int i = 0; i < len - r_len; i++) {
			strBuff.insert(0, '0');
		}
		return strBuff.toString();
	}

	public static String Long2HexStr(long val, int len) {
		String result = Long.toHexString(val).toUpperCase();
		int r_len = result.length();
		if (r_len > len) {
			return result.substring(r_len - len, r_len);
		}
		if (r_len == len) {
			return result;
		}
		StringBuffer strBuff = new StringBuffer(result);
		for (int i = 0; i < len - r_len; i++) {
			strBuff.insert(0, '0');
		}
		return strBuff.toString();
	}

	public static String getResString(Context context, int stringId) {
		return context.getResources().getString(stringId);
	}

	/**
	 * 字符串转换为字节数组
	 * <p>
	 * stringToBytes("0710BE8716FB"); return: b[0]=0x07;b[1]=0x10;...b[5]=0xfb;
	 */
	public static byte[] stringToBytes(String string) {
		if (string == null || string.length() == 0 || string.length() % 2 != 0) {
			return null;
		}
		int stringLen = string.length();
		byte byteArrayResult[] = new byte[stringLen / 2];
		StringBuffer sb = new StringBuffer(string);
		String strTemp;
		int i = 0;
		while (i < sb.length() - 1) {
			strTemp = string.substring(i, i + 2);
			byteArrayResult[i / 2] = (byte) Integer.parseInt(strTemp, 16);
			i += 2;
		}
		return byteArrayResult;
	}

	/**
	 * 字节数组转为16进制
	 *
	 * @param bytes
	 *            字节数组
	 * @return
	 */
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null) {
			return "";
		}
		StringBuffer buff = new StringBuffer();
		int len = bytes.length;
		for (int j = 0; j < len; j++) {
			if ((bytes[j] & 0xff) < 16) {
				buff.append('0');
			}
			buff.append(Integer.toHexString(bytes[j] & 0xff));
		}
		return buff.toString();
	}

	/**
	 * 
	 * ********************************************************************<br>
	 * 方法功能：将用户圈存金额先转换为分，在把分转为16进制，再前补0组装为4字节圈存金额 如1元 为:"00000064" 参数说明：<br>
	 * 作 者：杨明<br>
	 * 开发日期：2013-9-18 上午11:53:56<br>
	 * 修改日期：<br>
	 * 修改人：<br>
	 * 修改说明：<br>
	 * ********************************************************************<br>
	 */
	/**
	 * 将长整数转为16进行数后并以指定长度返回（当实际长度大于指定长度时只返回从末位开始指定长度的值）
	 * 
	 * @param val
	 *            int 待转换长整数
	 * @param len
	 *            int 指定长度
	 * @return String
	 */
	public static String longToHex(long val, int len) {
		String result = Long.toHexString(val).toUpperCase();
		int rLen = result.length();
		if (rLen > len) {
			return result.substring(rLen - len, rLen);
		}
		if (rLen == len) {
			return result;
		}
		StringBuffer strBuff = new StringBuffer(result);
		for (int i = 0; i < len - rLen; i++) {
			strBuff.insert(0, '0');
		}
		return strBuff.toString();
	}

	/**
	 * 循环左移
	 * @param sourceByte 待左移动的值
	 * @param n 左移动的为数
	 * @return
	 */
	public static byte rotateLeft(byte sourceByte, int n) {
		// 去除高位的1
		int temp = sourceByte & 0xFF;
		return (byte) ((temp << n) | (temp >>> (8 - n)));
	}
	/**
	 * 循环右移
	 * @param sourceByte
	 * @param n
	 * @return
	 */
	public static byte rotateRight(byte sourceByte, int n) {
		// 去除高位的1
		int temp = sourceByte & 0xFF;
		return (byte) ((temp >>> n) | (temp << (8 - n)));
	}
	/**
	 * 循环左移
	 * @param sourceBytes
	 * @param n
	 * @return
	 */
	public static byte[] rotateLeft(byte[] sourceBytes, int n) {
		byte[] out = new byte[sourceBytes.length];
		for (int i = 0; i < sourceBytes.length; i++) {
			out[i] = rotateLeft(sourceBytes[i], n);
		}
		return out;
	}

	public static byte[] rotateRight(byte[] sourceBytes, int n) {
		byte[] out = new byte[sourceBytes.length];
		for (int i = 0; i < sourceBytes.length; i++) {
			out[i] = rotateRight(sourceBytes[i], n);
		}
		return out;
	}

	public static void reverseArr(byte[] array,int begin,int end) {
		byte temp;
		do {
			temp = array[begin];
			array[begin] = array[end];
			array[end] = temp;
			begin++;
			end--;
		} while (begin < end);
	}

	/**
	 * 十六进制字符串取反
	 * @param hexStr
	 * @return
	 */
	public static String getHexReverse(String hexStr){

		String tmpStr = hexStr.toUpperCase().replace("0x", "");
		StringBuilder outStr = new StringBuilder();
		for(char c : tmpStr.toCharArray()){
			int tmpInt = 0;
			tmpInt = c - 48;
			tmpInt = tmpInt > 9 ? tmpInt - 7 : tmpInt;
			tmpInt = 15 - tmpInt;
			tmpInt += '0';
			tmpInt = tmpInt > '9' ? tmpInt + 7 : tmpInt;
			outStr.append( ( char )( tmpInt ) );
		}
		return outStr.toString();
	}

	public static void moveRight(byte array[],int k){
		int n=array.length-1;
		k=k%n; //为了防止k>n ,右移K位和右移k%n的结果是一样的
		reverseArr(array, 0, n-k);
		reverseArr(array, n-k+1, n);
		reverseArr(array, 0, n);
	}

	public static void moveLeft(byte array[],int k){
		int n=array.length-1;
		k=k%n; //为了防止k>n ,右移K位和右移k%n的结果是一样的
		reverseArr(array, 0, k-1);
		reverseArr(array, k, n);
		reverseArr(array, 0, n);
	}

	/**
	 * 将字节数组转为long<br>
	 * 如果input为null,或offset指定的剩余数组长度不足8字节则抛出异常
	 * @param input
	 * @param offset 起始偏移量
	 * @param littleEndian 输入数组是否小端模式
	 * @return
	 */
	public static long longFrom8Bytes(byte[] input, int offset, boolean littleEndian){
		long value=0;
		// 循环读取每个字节通过移位运算完成long的8个字节拼装
		for(int  count=0;count<8;++count){
			int shift=(littleEndian?count:(7-count))<<3;
			value |=((long)0xff<< shift) & ((long)input[offset+count] << shift);
		}
		return value;
	}
}
