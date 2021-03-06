package com.androidcat.catlibs.uncaught;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.androidcat.catlibs.log.LogUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context
 * data and sending crash reports. It registers itself as the Application's
 * Thread default {@link UncaughtExceptionHandler}.
 * </p>
 */
public class ErrorReporter implements UncaughtExceptionHandler {

    private static final String TAG = "ErrorReporter";

    public static final String EXTRA_REPORT_CAUSE_STRING = "REPORT_CAUSE";
    public static final String EXTRA_REPORT_URL = "REPORT_EMAIL";
    private static final String LOG_TAG = ErrorReporter.class.getSimpleName();
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
    private static final String LOCAL_KEY = "Local";
    // Our singleton instance.
    private static ErrorReporter mInstanceSingleton;
    // This is where we collect crash data
    private final Properties mCrashProperties = new Properties();
    // Some custom parameters can be added by the application developer. These
    // parameters are stored here.
    Map<String, String> mCustomParameters = new HashMap<String, String>();
    private String mStackString;
    // private static final String LOG_COMMAND_TEMP =
    // "logcat -d -v time -f %s\n";
    // A reference to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the default exception handling after sending
    // the report.
    private UncaughtExceptionHandler mDfltExceptionHandler;
    // The application context
    private Application mApplication;

    private String mReportUrl;

    /**
     * Create or return the singleton instance.
     *
     * @return the current instance of ErrorReporter.
     */
    public static ErrorReporter getInstance() {
        if (mInstanceSingleton == null) {
            mInstanceSingleton = new ErrorReporter();
        }
        return mInstanceSingleton;
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

    /**
     * <p>
     * This is where the ErrorReporter replaces the default
     * {@link UncaughtExceptionHandler}.
     * </p>
     *
     * @param context The android application context.
     */
    public void init(final Application context) {
        mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mApplication = context;
    }

    /**
     * Collects crash data.
     *
     * @param context The application context.
     */
    private void retrieveCrashData(final Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                // Application Version
                mCrashProperties.put(VERSION_NAME_KEY,
                        pi.versionName != null ? pi.versionName : "not set");
                mCrashProperties.put(VERSION_CODE_KEY,
                        Integer.toString(pi.versionCode));
            } else {
                // Could not retrieve package info...
                mCrashProperties.put(PACKAGE_NAME_KEY,
                        "Package info unavailable");
            }
            // Application Package name
            mCrashProperties.put(PACKAGE_NAME_KEY, context.getPackageName());
            // Device model
            mCrashProperties.put(PHONE_MODEL_KEY, android.os.Build.MODEL);
            // Android version
            mCrashProperties.put(ANDROID_VERSION_KEY,
                    android.os.Build.VERSION.RELEASE);
            mCrashProperties.put(SDK_VERSION_KEY, android.os.Build.VERSION.SDK);

            // Android build data
            mCrashProperties.put(BOARD_KEY, android.os.Build.BOARD);
            mCrashProperties.put(BRAND_KEY, android.os.Build.BRAND);
            mCrashProperties.put(DEVICE_KEY, android.os.Build.DEVICE);
            mCrashProperties.put(DISPLAY_KEY, android.os.Build.DISPLAY);
            mCrashProperties.put(FINGERPRINT_KEY, android.os.Build.FINGERPRINT);
            mCrashProperties.put(HOST_KEY, android.os.Build.HOST);
            mCrashProperties.put(ID_KEY, android.os.Build.ID);
            mCrashProperties.put(MODEL_KEY, android.os.Build.MODEL);
            mCrashProperties.put(PRODUCT_KEY, android.os.Build.PRODUCT);
            mCrashProperties.put(TAGS_KEY, android.os.Build.TAGS);
            mCrashProperties.put(TIME_KEY,
                    new Date(System.currentTimeMillis()).toGMTString());
            mCrashProperties.put(TYPE_KEY, android.os.Build.TYPE);
            mCrashProperties.put(USER_KEY, android.os.Build.USER);
            mCrashProperties.put(LOCAL_KEY, Locale.getDefault().toString());

            // Device Memory
            mCrashProperties.put(TOTAL_MEM_SIZE_KEY, Formatter.formatFileSize(
                    context, getTotalInternalMemorySize()));
            mCrashProperties.put(AVAILABLE_MEM_SIZE_KEY, Formatter
                    .formatFileSize(context, getAvailableInternalMemorySize()));

        } catch (final Exception e) {
            LogUtil.e(TAG, e.getMessage(),e);
        }
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        // Generate and send crash report
        try {
            // Log.i("i", "uncaught exception:"+e.getMessage());
            //mApplication.onCrashed(t, e);
            handleException(e);
        } catch (final Throwable e2) {
            e2.printStackTrace();
        } finally {
            Intent intent = new Intent();
            intent.setClassName(mApplication.getPackageName(),"cn.eas.national.deviceapisample.activity.SplashActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("from","crash");
            PendingIntent restartIntent = PendingIntent.getActivity(mApplication, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    /**
     * Try to send a report, if an error occurs stores a report file for a later
     * attempt.
     *
     * @param e The Throwable to be reported. If null the report will contain
     *          a new Exception("Report requested by developer").
     */
    void handleException(Throwable e) {
        try {
            if (e == null) {
                e = new Exception("Report requested by developer");
            }
            retrieveCrashData(mApplication);
            // Date CurDate = new Date();
            // Report += "Error Report collected on : " + CurDate.toString();

            // Add custom info, they are all stored in a single field
            mCrashProperties.put(CUSTOM_DATA_KEY, createCustomInfoString());

            // Build stack trace
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            final String causeString = result.toString();
            mStackString = causeString;
            mCrashProperties.put(STACK_TRACE_KEY, "");
            printWriter.close();
            // Log.e("AndroidRuntime", causeString);
            // Always write the report file
            // final String reportFileName = saveCrashReportFile();
            // saveLogToFile(reportFileName);
            showCrashDialog();
        } catch (final Exception ex) {
        }
    }

    private void showCrashDialog() {
        StringBuilder sb = new StringBuilder();
        for (Object key : mCrashProperties.keySet()) {
            sb.append(key+ "-->" + mCrashProperties.get(key) + "\n");
        }

        CrashEntity crashEntity = new CrashEntity();
        crashEntity.timeMillions = System.currentTimeMillis()+"";
        crashEntity.deviceInfo = sb.toString();
        crashEntity.stackTrace = mStackString;
        CacheDatabase database = CacheDatabase.getInstance(mApplication);
        database.saveCrash(crashEntity);

        //sb.append(mStackString);
        //String causeString = sb.toString();
        //LogUtil.e(TAG,causeString);
        //Logger.file(causeString,false);
        //final Intent intent = new Intent(mApplication, CrashReportDialog.class);
        //intent.putExtra(EXTRA_REPORT_CAUSE_STRING, causeString);
		//intent.putExtra(EXTRA_REPORT_URL, mReportUrl);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//mApplication.startActivity(intent);
    }

    /**
     * When a report can't be sent, it is saved here in a file in the root of
     * the application private directory.
     */
    public String saveCrashReportFile() {
        /*try {
            // Log.d(LOG_TAG, "Writing crash report file.");
            final File file = mApplication.getCrashReportFile();
            final File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final FileOutputStream trace = new FileOutputStream(file);
            mCrashProperties.store(trace, "");
            trace.write(mStackString.getBytes());
            trace.write("\n\n".getBytes());
            trace.flush();
            trace.close();
            return file.getPath();
        } catch (final Exception e) {
        }*/
        return null;
    }

    /**
     * Disable ACRA : sets this Thread's {@link UncaughtExceptionHandler} back
     * to the system default.
     */
    public void disable() {
        if (mDfltExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(mDfltExceptionHandler);
        }
    }

    public void setReportUrl(final String reportUrl) {
        mReportUrl = reportUrl;
    }

}