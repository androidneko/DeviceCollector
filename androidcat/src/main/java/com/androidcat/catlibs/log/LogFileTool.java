package com.androidcat.catlibs.log;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;

import com.androidcat.catlibs.persistance.SharePreferenceUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 
 * **********************************************************<br>
 * 模块功能: 封装了一些访问文件系统的方法，用来管理该应用在文件系统上需要的操作和功能，
 * 包括sp的logo和图片的保存等。<br>
 * 作 者: 薛龙<br>
 * 开发日期：2013-8-12 下午11:27:33
 * 单 位：武汉天喻信息 研发中心
 * 修改日期：<br>
 * 修改人：<br>
 * 修改说明：<br>
 * *********************************************************<br>
 */
public class LogFileTool extends ContextWrapper {

	public static final String TAG = "LogFileTool";
	public static final String FILE_NAME = "pos_android_log.dat";
	public static final String ZIP_FILE_NAME = "pos_android_log.zip";
	public static final String KEY_LOG_DATE = "key_log_date";
	public static final int APPEND_SIZE_LIMIT = 50 * 1024;
	public static final int FILE_SIZE_LIMIT = 5 * 1024 * 1024;
	private static final String VERSION_NAME_KEY = "VersionName";
	private static final String VERSION_CODE_KEY = "VersionCode";
	private static final String PACKAGE_NAME_KEY = "PackageName";
	private static final String PHONE_MODEL_KEY = "PhoneModel";
	private static final String ANDROID_VERSION_KEY = "AndroidVersion";
	private static final String SDK_VERSION_KEY = "SDKVersion";
	private static final String BOARD_KEY = "Board";
	private static final String BRAND_KEY = "Brand";
	private static final String DEVICE_KEY = "Device";
	private static final String DISPLAY_KEY = "Display";
	private static final String FINGERPRINT_KEY = "FingerPrint";
	private static final String HOST_KEY = "Host";
	private static final String ID_KEY = "Id";
	private static final String MODEL_KEY = "Model";
	private static final String PRODUCT_KEY = "Product";
	private static final String TAGS_KEY = "Tags";
	private static final String TIME_KEY = "Time";
	private static final String TYPE_KEY = "Type";
	private static final String USER_KEY = "User";
	private static final String TOTAL_MEM_SIZE_KEY = "TotalMem";
	private static final String AVAILABLE_MEM_SIZE_KEY = "AvailableMem";
	private static final String CUSTOM_DATA_KEY = "CustomData";
	private static final String STACK_TRACE_KEY = "StackTrace";
	private static final String FIRMWARE_VERSION_KEY = "firmware_version";
	private static final String LOCAL_KEY = "Local";
	private static final int BUFF_SIZE = 1024;
	public static String FILE_CACHE_DIR = "/";
	public static String TRANS_RECORD_BAK_DIR = "/.CACHE/";
	public static String TRANS_RECORD_FILE_NAME = "dat";
	public static String LOG_DIR = "log";
	private static LogFileTool sInstance;
	private static SharedPreferences mPreferences;
	private static Context context;
	private static ExecutorService m_esThread = null;
	private final Map<String, String> mSystemProperties = new HashMap<String, String>();
	private static StringBuffer CACHE;
	private static final int CACHE_SIZE = 1024 * 2;

	// Some custom parameters can be added by the application developer. These
	// parameters are stored here.
	Map<String, String> mCustomParameters = new HashMap<String, String>();
	File logFile;
	File zipLogFile;
	private File mFileCacheDir;
	private File mLogDir;

	public File getZipLogFile() {
		return zipLogFile;
	}

	/**
	 * @param base
	 */
	public LogFileTool(Context base) {
		super(base);
		ensureCacheDir();
	}

	public static LogFileTool init(Context context) {
		LogFileTool.context = context;
		if (null == sInstance) {
			String pacakgeName = context.getPackageName();
			FILE_CACHE_DIR = pacakgeName + FILE_CACHE_DIR;
			LOG_DIR = FILE_CACHE_DIR + "log";
			mPreferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			m_esThread = Executors.newSingleThreadExecutor();
			sInstance = new LogFileTool(context);
		}
		return sInstance;
	}

	public static LogFileTool getInstance() {
		if (null == sInstance) {
			throw new IllegalStateException("You must init first");
		}
		return sInstance;
	}


	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 *
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize() {
		final File path = Environment.getDataDirectory();
		final StatFs stat = new StatFs(path.getPath());
		final long blockSize = stat.getBlockSize();
		final long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 *
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		final File path = Environment.getDataDirectory();
		final StatFs stat = new StatFs(path.getPath());
		final long blockSize = stat.getBlockSize();
		final long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * Ensure the CACHE directory.
	 */
	public void ensureCacheDir() {
		try {
			final File storageDir = context.getCacheDir();
			if (storageDir != null) {
				final File fileCacheDir = new File(storageDir, FILE_CACHE_DIR);
				if (!fileCacheDir.exists()) {
					fileCacheDir.mkdirs();
				}
				mFileCacheDir = fileCacheDir;
				final File fileTempDir = new File(storageDir, LOG_DIR);
				if (!fileTempDir.exists()) {
					fileTempDir.mkdirs();
				}
				mLogDir = fileTempDir;
			}
		} catch (Exception e) {
			//Logger.e(e, e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Collects crash data.
	 *
	 * @param context The application context.
	 */
	private void retrieveSystemData(final Context context) {
		/*try {
			final PackageManager pm = context.getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			if (pi != null) {
				// Application Version
				mSystemProperties.put(VERSION_NAME_KEY,
						pi.versionName != null ? pi.versionName : "not set");
				mSystemProperties.put(VERSION_CODE_KEY,
						Integer.toString(pi.versionCode));
			} else {
				// Could not retrieve package info...
				mSystemProperties.put(PACKAGE_NAME_KEY,
						"Package info unavailable");
			}
			// Application Package name
			mSystemProperties.put(PACKAGE_NAME_KEY, context.getPackageName());
			// Device model
			mSystemProperties.put(PHONE_MODEL_KEY, android.os.Build.MODEL);
			// Android version
			mSystemProperties.put(ANDROID_VERSION_KEY,
					android.os.Build.VERSION.RELEASE);
			mSystemProperties.put(SDK_VERSION_KEY, android.os.Build.VERSION.SDK);

			// Android build data
			mSystemProperties.put(BOARD_KEY, android.os.Build.BOARD);
			mSystemProperties.put(BRAND_KEY, android.os.Build.BRAND);
			mSystemProperties.put(DEVICE_KEY, android.os.Build.DEVICE);
			mSystemProperties.put(DISPLAY_KEY, android.os.Build.DISPLAY);
			mSystemProperties.put(FINGERPRINT_KEY, android.os.Build.FINGERPRINT);
			mSystemProperties.put(HOST_KEY, android.os.Build.HOST);
			mSystemProperties.put(ID_KEY, android.os.Build.ID);
			mSystemProperties.put(MODEL_KEY, android.os.Build.MODEL);
			mSystemProperties.put(PRODUCT_KEY, android.os.Build.PRODUCT);
			mSystemProperties.put(TAGS_KEY, android.os.Build.TAGS);
			mSystemProperties.put(TIME_KEY,
					new Date(System.currentTimeMillis()).toGMTString());
			mSystemProperties.put(TYPE_KEY, android.os.Build.TYPE);
			mSystemProperties.put(USER_KEY, android.os.Build.USER);
			mSystemProperties.put(LOCAL_KEY, Locale.getDefault().toString());
			mSystemProperties.put(FIRMWARE_VERSION_KEY, SharePreferencesUtil.getValue(SPConsts.FIRMWARE_VER));
			// Device Memory
			mSystemProperties.put(TOTAL_MEM_SIZE_KEY, Formatter.formatFileSize(
					context, getTotalInternalMemorySize()));
			mSystemProperties.put(AVAILABLE_MEM_SIZE_KEY, Formatter
					.formatFileSize(context, getAvailableInternalMemorySize()));

		} catch (final Exception e) {
			Logger.e(e, e.getMessage());
		}*/
	}

	/**
	 * <p>
	 * Use this method to provide the ErrorReporter with data of your running
	 * application. You should call this at several key places in your code the
	 * same way as you would output important debug data in a log file. Only the
	 * latest value is kept for each key (no history of the values is sent in
	 * the report).
	 * </p>
	 * <p>
	 * The key/value pairs will be stored in the GoogleDoc spreadsheet in the
	 * "custom" column, as a text containing a 'key = value' pair on each line.
	 * </p>
	 *
	 * @param key   A key for your custom data.
	 * @param value The value associated to your key.
	 */
	public void addCustomData(final String key, final String value) {
		mCustomParameters.put(key, value);
	}

	/**
	 * Generates the string which is posted in the single custom data field in
	 * the GoogleDocs Form.
	 *
	 * @return A string with a 'key = value' pair on each line.
	 */
	private String createCustomInfoString() {
		String CustomInfo = "";
		final Iterator<String> iterator = mCustomParameters.keySet().iterator();
		while (iterator.hasNext()) {
			final String CurrentKey = iterator.next();
			final String CurrentVal = mCustomParameters.get(CurrentKey);
			CustomInfo += CurrentKey + " = " + CurrentVal + "\n";
		}
		return CustomInfo;
	}

	public void appendContextInfo() {
		retrieveSystemData(context);
		// Add custom info, they are all stored in a single field
		//mSystemProperties.put(CUSTOM_DATA_KEY, createCustomInfoString());
		StringBuilder sb = new StringBuilder();
		for (Object key : mSystemProperties.keySet()) {
			sb.append(key + "--->" + mSystemProperties.get(key) + "\n");
		}

		logAsync(sb.toString());
	}

	/**
	 * Determine whether the image file is valid.
	 * 
	 * @param file
	 *            the image file.
	 * @return true if the file exits and can be decoded, false otherwise.
	 */
	public boolean isCacheFileValid(final File file) {
		if (file != null && file.exists()) {
			return isCacheFileValidInternal(file);
		}
		return false;
	}

	private boolean isCacheFileValidInternal(final File cacheFile) {
		if (cacheFile.isFile()) {
			return true;
		} else {
			cacheFile.delete();
		}
		return false;
	}

	/**
	 * @return the getCacheFileDir
	 */
	public File getCacheFileDir() {
		return mFileCacheDir;
	}

	public File getmLogDir() {
		return mLogDir;
	}

	/**
	 * @param file
	 * @return
	 */
	private String loadCache(File file) {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			return inputStream2String(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String loadStringFile(String path){
		File file = new File(path);
		return loadCache(file);
	}

	String inputStream2String(InputStream is) {
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		try {
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	private String checkSize(File logDir){
		StringBuilder append = new StringBuilder("\n");
		File file = new File(logDir, FILE_NAME);
		if (file.exists()){
			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(),"rw");
				long fileLen = randomAccessFile.length();
				if (fileLen > FILE_SIZE_LIMIT){
					byte[] buf = new byte[APPEND_SIZE_LIMIT];
					randomAccessFile.seek(fileLen- APPEND_SIZE_LIMIT -1);
					randomAccessFile.read(buf);
					append.append(new String(buf));
					append.append("\n");
					randomAccessFile.close();
					file.delete();
				}
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			}
			return append.toString();
		}else {
			return append.toString();
		}
	}

	public void logAsync(final String data){
		m_esThread.submit(new Runnable() {
			@Override
			public void run() {
				log(data);
			}
		});
	}

	/*public void uploadLogAsync(final String userName){
		m_esThread.submit(new Runnable() {
			@Override
			public void run() {
				if (mLogDir != null && logFile!= null) {
					if (logFile.exists()) {
						Configuration config = new Configuration.Builder()
								.chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认256K
								.putThreshhold(512 * 1024)  // 启用分片上传阀值。默认512K
								.connectTimeout(20) // 链接超时。默认10秒
								.responseTimeout(60) // 服务器响应超时。默认60秒
								.zone(Zone.zone2)
								.build();
						//.recorder(recorder);  // recorder分片上传时，已上传片记录器。默认null
						UploadManager uploadManager = new UploadManager(config);
						String key = userName + "_" + DateFormat.format("yyyyMMddHHmmss", new Date()) + "_log.txt";
						String token = Auth.create(Constants.ACCESSKEY, Constants.SECRETKEY).uploadToken(Constants.BUCKET);
						final long now = System.currentTimeMillis();
						uploadManager.put(logFile, key, token,
								new UpCompletionHandler() {
									@Override
									public void complete(String key, ResponseInfo info, JSONObject res) {
										//res包含hash、key等信息，具体字段取决于上传策略的设置。
										Logger.e(key + ",\r\n " + info + ",\r\n " + res + ",\r\n 用时:" + (System.currentTimeMillis() - now) + "ms");
									}
								}, null);
					}
				}
			}
		});
	}*/


	public void zipFile(){
		String userName= SharePreferenceUtil.getString("userName");
		appendContextInfo();
		zipLogFile = new File(mLogDir, userName+"_"+ZIP_FILE_NAME);
		if (zipLogFile.exists()){
			zipLogFile.delete();
		}
		BufferedInputStream bis = null;
		ZipOutputStream zos = null;
		try{
			byte[] buffer = new byte[BUFF_SIZE];
			zos = new ZipOutputStream(new FileOutputStream(zipLogFile));
			bis = new BufferedInputStream(new FileInputStream(logFile));
			ZipEntry zipEntry = new ZipEntry(logFile.getName());
			zipEntry.setSize(logFile.length());
			zos.putNextEntry(zipEntry);
			while (bis.read(buffer) != -1){
				zos.write(buffer);
			}
			bis.close();
			zos.flush();
			zos.closeEntry();
			zos.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		} finally {
				if (bis != null){
					try{
						bis.close();
					}catch(IOException e){
						e.printStackTrace();
					}
				}

				if (zos != null){
					try{
						zos.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
		}

	}

	private synchronized  void doLog(String data){
		Log.d(TAG,"----doLog----");
		data = checkSize(mLogDir) + data;
		//FileOutputStream fos = null;
		//OutputStreamWriter osw = null;
		try {
			if (logFile == null) {
				logFile = new File(mLogDir, FILE_NAME);
			}
			File parent = logFile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			if (!logFile.exists()){
				logFile.createNewFile();
			}

			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(logFile.getAbsolutePath(), "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.write(data.getBytes());
			randomFile.close();
		} catch (Exception ex) {
//			Logger.e("LogFileTool catch method executed!");
			ex.printStackTrace();
		} finally {
//			Logger.e("LogFileTool finally method executed!");
		}
	}
	public synchronized void log(String data) {
		if (CACHE == null){
			CACHE = new StringBuffer();
		}
		CACHE.append(data);
		CACHE.append("\n");
		if (CACHE.length() > CACHE_SIZE){
			String log = CACHE.toString();
			doLog(log);
			CACHE = null;
			System.gc();
		}
	}

	public synchronized void flushCache(){
		if (CACHE != null){
			String log = CACHE.toString();
			doLog(log);
			CACHE = null;
			System.gc();
		}
	}

	/**
	 * Clean the CACHE directory.
	 */
	public void cleanCache() {
		FileSystemUtil.cleanDir(mFileCacheDir);
		FileSystemUtil.cleanDir(mLogDir);
	}
}
