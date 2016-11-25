package com.yj.wangjatv.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Base64;
import android.util.Log;


import com.yj.wangjatv.R;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.http.Field;

public class CommonUtil {
    public static void killProcess(Activity activity) {
        if(activity != null) {
            activity.moveTaskToBack(true);
            activity.finish();
        }

        android.os.Process.killProcess(android.os.Process.myPid());

        // // method 1
        // ActivityManager am = (ActivityManager)activity.getSystemService(
        // Activity.ACTIVITY_SERVICE);
        // am.restartPackage(activity.getPackageName());
        // <uses-permission android:name="android.permission.RESTART_PACKAGES"/>

        // method 2
        // ActivityManager am =
        // (ActivityManager)activity.getSystemService(Activity.ACTIVITY_SERVICE);
        // am.killBackgroundProcesses(activity.getPackageName());
        // <uses-permission
        // android:name="android.permission.KILL_BACKGROUND_PROCESSES"/>
    }

    // Get default notification sound list of android system
    public static Cursor loadInternalNotiSound(Activity activity)
            throws Exception {
        String selection = null;

        Cursor cursor = null;
        String[] projection = new String[]{MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                // MediaStore.Audio.Media.IS_NOTIFICATION,
                // MediaStore.Audio.Media.IS_MUSIC,
                // MediaStore.Audio.Media.IS_ALARM
                // MediaStore.Audio.Media.IS_MUSIC,
        };

        // selection = MediaStore.Audio.Media.IS_RINGTONE + " != 0";
        // //?�림?�이�?IS_NOTIFICATION?�로,,,
        // selection = MediaStore.Audio.Media.IS_NOTIFICATION + " != 0";
        selection = /* MediaStore.Audio.Media.IS_RINGTONE + " != 0 || " + */
                MediaStore.Audio.Media.IS_NOTIFICATION + " != 0";

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        cursor = activity.getContentResolver().query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, projection,
                selection, null, sortOrder);

        return cursor;
    }

    // Get sound list of SD card
    public static Cursor loadExternalNotiSound(Activity activity)
            throws Exception {
        String selection = null;

        Cursor cursor = null;
        String[] projection = new String[]{MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME};

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        cursor = activity.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, sortOrder);

        return cursor;
    }

    // 텍스트에 각이한 스타일 지정하기
    public static SpannableStringBuilder setTextStyle(String szTxt, int nStyle,
                                                      int nColor, int nSize) {

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.clear();

        if (szTxt == null || szTxt.equals("")) {
            return ssb;
        }

        ssb.append(szTxt); // 주의 : 본문에 "."이 들어가면 행바꾸기됩니다.
        try {
            ssb.setSpan(new StyleSpan(nStyle), 0, szTxt.length(),
                    Spannable.SPAN_COMPOSING); // nStyle : Typeface.BOLD_ITALIC
            ssb.setSpan(new ForegroundColorSpan(nColor), 0, szTxt.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new AbsoluteSizeSpan(nSize), 0, szTxt.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        } catch (Exception e) {
            Log.d("", "setTextStyle() -->  " + e.getMessage());
        }

        return ssb;
        // textView.append(setTextStyle("테스트.스타일", Typeface.BOLD_ITALIC,
        // Color.RED, 22));
    }

    public static void sendEmail(Context context, String mail) {
        // 이메일 발송
        Uri uri = Uri.parse("mailto:" + mail);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        context.startActivity(it);
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static int getAppVerionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    // 사진 촬영
    public static void takePicture(Fragment fragment, String sampleImg,
                                   int reqCode) {
        // 카메라 호출 intent 생성
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(),
                sampleImg);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        fragment.startActivityForResult(intent, reqCode);
    }

    // 사진 불러오기
    public static void photoAlbum(Fragment fragment, int reqCode) {
        // photo Album 호출 intent 생성
        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType(Images.Media.CONTENT_TYPE);
        intent.setData(Images.Media.EXTERNAL_CONTENT_URI);
        fragment.startActivityForResult(intent, reqCode);
    }

    // Convert dip to pixel
    public static int getDipToPx(Resources res, int dip) {
        int pixel = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
        return pixel;
    }

    // Convert sp to pixel
    public static int getSpToPx(Resources res, int dip) {
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                dip, res.getDisplayMetrics());
        return pixel;
    }

    // Get device id
    public static String getDeviceId(Context context) {
        String deviceId = "";

        // Get device ID
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();

        // ANDROID_ID for 2.2 above
        if (deviceId == null) {
            deviceId = Secure.getString(context.getContentResolver(),
                    Secure.ANDROID_ID);
        }

        // BluetoothAdapter mBtAdapter;
        // mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // String deviceMacAddress = mBtAdapter.getAddress();
        //
        // WifiManager mng =
        // (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        // WifiInfo info = mng.getConnectionInfo();
        // String mac = info.getMacAddress();

        return deviceId;
    }


    // Show progress dialog
    private static ProgressDialog loadingDialog = null;

    public static final void showLoadingDialog(Context context, String title,
                                               String msg) {
        if (loadingDialog != null)
            hideLoadingDialog();
        loadingDialog = ProgressDialog.show(context, title, msg, true, false);
        if (!msg.equals(""))
            return;

        Window v = loadingDialog.getWindow();
        WindowManager.LayoutParams param = v.getAttributes();
        param.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        param.y = ((Activity) context).getWindowManager().getDefaultDisplay()
                .getHeight() * 11 / 16;
        param.dimAmount = 0.0f;
        v.setAttributes(param);
    }

    /**
     * Progress Dialog ����
     */
    public static final void hideLoadingDialog() {
        try {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap LoadImage(String imagePath) {
        // TODO Auto-generated method stub
        if (imagePath == null || imagePath.equals(""))
            return null;
        // test code
        // imagePath =
        // "http://sstatic.naver.net/people/28/20071005181335197040222.jpg";
        Bitmap bit = null;
        try {

            InputStream is = new URL(imagePath).openStream();
            bit = BitmapFactory.decodeStream(is);

            is.close();

        } catch (Exception e)

        {

            e.printStackTrace();

        }

        return bit;
    }

    public static Bitmap LoadImage2(String urlString) throws IOException,
            MalformedURLException {
        final URL url = new URL(urlString);
        final URLConnection conn = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setAllowUserInteraction(true);
        httpConn.setInstanceFollowRedirects(true);
        httpConn.setRequestMethod("GET");
        httpConn.connect();

        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
        } catch (Exception e) {
            bitmap = null;
            System.gc();
        }

        return bitmap;
    }

    /**
     * Close keyboard
     */
    public static void hideKeyboard(Context ctx, EditText edit) {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    /**
     * Show keyboard
     */
    public static void showKeyboard(Context ctx, EditText edit) {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED);
        // imm.showSoftInputFromInputMethod (edit.getApplicationWindowToken(),
        // InputMethodManager.SHOW_FORCED);
        // imm.toggleSoftInputFromWindow(edit.getApplicationWindowToken(),
        // InputMethodManager.SHOW_FORCED, 0);
    }

    // Encode string
    public static String encode(String str, String enc) {
        try {
            str = URLEncoder.encode(str, enc);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return str;
    }

    // Show center toast
    public static void showCenterToast(Context ctx, String msg, int duration) {
        Toast toast = Toast.makeText(ctx, msg, duration);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    public static void showCenterToast(Context context, int message_id, int duration) {
        Toast toast = Toast.makeText(context, context.getResources().getString(message_id), duration);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    public static boolean isScreenOn(Context ctx) {
        PowerManager pm = (PowerManager) ctx
                .getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output = null;
        if (bitmap != null) {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = 20; // ���� ���� : �ȼ�
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
        }
        return output;
    }

    public static boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();

        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    // Get real path from URL
    public static String getRealPathFromURI(Activity act, Uri contentUri) {
        String[] proj = {Images.Media.DATA};
        Cursor cursor = act.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static void setLocale(Context ctx, String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = ctx.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    /**
     * Apply font
     *
     * @param root
     * @param tf
     */
    public static void setFont(ViewGroup root, Typeface tf) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView)
                ((TextView) child).setTypeface(tf);
            else if (child instanceof ViewGroup)
                setFont((ViewGroup) child, tf);
        }
    }

    /**
     * 금액(double)을 금액표시타입(소숫점2자리)으로 변환한다.
     *
     * @param moneyString 금액(double 형)
     * @return 변경된 금액 문자렬
     */
    public static String makeMoneyType(String moneyString) {
        String format = "#,###"/* "#.##0.00" */;
        DecimalFormat df = new DecimalFormat(format);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        dfs.setGroupingSeparator(','); // �����ڸ� ,��
        df.setGroupingSize(3); // 3�ڸ� �������� ������ó�� �Ѵ�.
        df.setDecimalFormatSymbols(dfs);

        try {
            return (df.format(Float.parseFloat(moneyString))).toString();
        } catch (Exception e) {
            return moneyString;
        }
    }

    public static void deleteDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] childFileList = dir.listFiles();
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    deleteDir(childFile.getAbsolutePath()); // 하위 디렉토리 루프
                } else {
                    childFile.delete(); // 하위 파일삭제
                }
            }
            dir.delete(); // root 삭제
        }
    }

    public static void deleteFiles(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] childFileList = dir.listFiles();
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    continue;
                } else {
                    childFile.delete(); // 하위 파일삭제
                }
            }
        }
    }

    public static int getOrientation(Context ctx, Uri uri) {
        ExifInterface ei;
        try {
            ei = new ExifInterface(uri.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static void getDeviceInfo(Context ctx, String[] strOSVer,
                                     String[] strModel, String[] strId) {
        try {
            strOSVer[0] = Build.VERSION.RELEASE;
            strModel[0] = Build.MODEL;
            String serial = "empty";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                serial = Build.SERIAL;
            }

            TelephonyManager telManager = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);

            Log.w("deviceid=", telManager.getDeviceId() + "");

            strId[0] = Build.MODEL + "_" + telManager.getDeviceId() + "_"
                    + serial;

        } catch (Exception e) {
        }
    }

    public static String getAppVersionName(Context context) {
        // 버전 정보 얻기
        PackageInfo pInfo;
        String version = "";
        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return version;
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static String diffOfDate(Date begin, Date end) throws Exception {
        if (begin == null || end == null)
            return "";

        long diff = end.getTime() - begin.getTime();
        long diffMins = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        if (diffHours < 1) {
            return diffMins + "m";
        } else if (diffDays < 1) {
            return diffHours + "hour";
        } else {
            return diffDays + "day";
        }
    }

    public static long diffOfDate2(Date begin, Date end) {
        if (begin == null || end == null)
            return 0;

        long diff = end.getTime() - begin.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
    }

    /**
     * Crop된 이미지가 저장될 파일을 만든다.
     *
     * @return Uri
     */
    public static File createSaveCropFile(Context context, String saveFolder) {
        // Uri uri;
        // String url = "tmp_" + String.valueOf(System.currentTimeMillis())
        // + ".jpg";
        // uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()
        // + File.separator + sdcardFolder, url));
        // return uri;

        String url = "tmp_" + String.valueOf(System.currentTimeMillis())
                + ".jpg";
        File crop_file;
        if (isSDCARDMounted() && CommonUtil.getExternalMemorySize() > 0) {
            crop_file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + saveFolder, url);
        } else {
            File directory = context.getFilesDir();
            crop_file = new File(directory, url);
            try {
                crop_file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                FileOutputStream fos = context.openFileOutput(url,
                        Context.MODE_WORLD_WRITEABLE);
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return crop_file;
    }

    /**
     * Crop된 이미지가 저장될 파일을 만든다.
     *
     * @return Uri
     */
    public static File createFile(Context context, String saveFolder,
                                  String fileName) {
        // Uri uri;
        // String url = "tmp_" + String.valueOf(System.currentTimeMillis())
        // + ".jpg";
        // uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()
        // + File.separator + sdcardFolder, url));
        // return uri;

        File file;
        if (isSDCARDMounted() && CommonUtil.getExternalMemorySize() > 0) {
            file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + saveFolder, fileName);
        } else {
            File directory = context.getFilesDir();
            file = new File(directory, fileName);
            if (file.exists()) {
                return file;
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                FileOutputStream fos = context.openFileOutput(fileName,
                        Context.MODE_WORLD_WRITEABLE);
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 선택된 uri의 사진 Path를 가져온다. uri 가 null 경우 마지막에 저장된 사진을 가져온다.
     *
     * @param uri
     * @return
     */
    public static File getImageFile(Context ctx, Uri uri) {
        String[] projection = {Images.Media.DATA};
        if (uri == null) {
            uri = Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor mCursor = ctx.getContentResolver().query(uri, projection, null,
                null, Images.Media.DATE_MODIFIED + " desc");
        if (mCursor == null || mCursor.getCount() < 1) {
            return null; // no cursor or no record
        }
        int column_index = mCursor
                .getColumnIndexOrThrow(Images.Media.DATA);
        mCursor.moveToFirst();

        String path = mCursor.getString(column_index);

        if (path == null || path.length() <= 0)
            return null;

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        return new File(path);
    }

    /**
     * 파일 복사
     *
     * @param srcFile  : 복사할 File
     * @param destFile : 복사될 File
     * @return
     */
    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * Copy data from a source stream to destFile. Return true if succeed,
     * return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Bitmap decodeFile(File f) {

        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 800;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE
                    && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * bitmap 를 jpeg 파일로 저장; 출처 : http://snowbora.com/418
     */
    public static boolean SaveBitmapToFileCache(Bitmap bitmap,
                                                String strFilePath, boolean bPNG) {

        if (bitmap == null) {
            return false;
        }

        boolean bRet;

        bRet = false;

        File fileItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileItem.getParentFile().mkdirs();
            fileItem.createNewFile();
            out = new FileOutputStream(fileItem);
            if (bPNG) {
                bitmap.compress(CompressFormat.PNG, 100, out);
            } else {
                bitmap.compress(CompressFormat.JPEG, 100, out);
            }
            out.flush();
            bRet = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bRet;
    }

    /**
     * SHA1 encryption
     */
    public static String encrypt_sha1(String text)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * ***********************************************
     * 전체 내장 메모리 크기를 가져온다
     *
     * @return 전체 내장 메모리 크기
     * ************************************************
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        return totalBlocks * blockSize;
    }

    /**
     * ***********************************************
     * 사용가능한 내장 메모리 크기를 가져온다
     *
     * @return 사용가능한 내장 메모리 크기
     * ************************************************
     */
    public static long getInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    /**
     * ***********************************************
     * 전체 외장 메모리 크기를 가져온다
     *
     * @return 전체 외장 메모리 크기
     * ************************************************
     */
    public static long getTotalExternalMemorySize() {
        if (isStorage(true) == true) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();

            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * ***********************************************
     * 사용가능한 외장 메모리 크기를 가져온다
     *
     * @return 사용가능한 외장 메모리 크기
     * ************************************************
     */
    public static long getExternalMemorySize() {
        if (isStorage(true) == true) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * ***********************************************
     * 외장메모리 sdcard 사용가능한지에 대한 여부 판단
     *
     * @return sdcard 사용가능 여부
     * ************************************************
     */
    public static boolean isStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else if (!requireWriteAccess
                && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * ***********************************************
     * 보기 좋게 MB,KB 단위로 축소시킨다
     *
     * @return ************************************************
     */
    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) {
            resultBuffer.append(suffix);
        }

        return resultBuffer.toString();
    }

    public static boolean isServiceRunning(Context ctx, String s_service_name) {
        ActivityManager manager = (ActivityManager) ctx
                .getSystemService(Activity.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (s_service_name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;

    }

    public static Point getDisplaySize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Map<String, Typeface> typefaceCache = new HashMap<String, Typeface>();

    public static void setTypeface(AttributeSet attrs, TextView textView) {
        if (textView.isInEditMode()) return;

        Context context = textView.getContext();

        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.AnyTextView);
        String typefaceName = values.getString(R.styleable.AnyTextView_typeface);

        if (typefaceCache.containsKey(typefaceName)) {
            textView.setTypeface(typefaceCache.get(typefaceName));
        } else {
            Typeface typeface;
            try {
                typeface = Typeface.createFromAsset(textView.getContext().getAssets(), context.getString(R.string.assets_fonts_folder) + typefaceName);
            } catch (Exception e) {
                Log.w(context.getString(R.string.app_name), String.format(context.getString(R.string.typeface_not_found), typefaceName));
                return;
            }

            typefaceCache.put(typefaceName, typeface);
            textView.setTypeface(typeface);
        }

        values.recycle();
    }

    /**
     * cnt 자리수 문자열 발생.
     */
    public static String randomString(int cnt) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = cnt;
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (new Random().nextFloat() * (rightLimit - leftLimit));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

    public static String randomString2(int length) {
        int index = 0;
        char[] charSet = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                , 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M'
                , 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
                , 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm'
                , 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            index = (int) (charSet.length * Math.random());
            sb.append(charSet[index]);
        }

        return sb.toString();

    }

    public static Bitmap loadImageFromWebUrl(String url, int inSampleSize) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true; // declare as purgeable to disk
            options.inSampleSize = inSampleSize;
            InputStream is = (InputStream) new URL(url).getContent();
            Bitmap b = BitmapFactory.decodeStream(is, null, options);
            return b;
        } catch (Exception e) {
            System.out.println("Exc2=" + e);
            return null;
        }
    }

    public static boolean saveBitmapToFileCache(Bitmap bitmap,
                                                String strFilePath, boolean bPNG) {

        if (bitmap == null) {
            return false;
        }

        boolean bRet;

        bRet = false;

        File fileItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileItem.getParentFile().mkdirs();
            fileItem.createNewFile();
            out = new FileOutputStream(fileItem);
            if (bPNG) {
                bitmap.compress(CompressFormat.PNG, 100, out);
            } else {
                bitmap.compress(CompressFormat.JPEG, 100, out);
            }
            out.flush();
            bRet = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bRet;
    }

    // 선택 팝업창 띄우기
    public static void showSelectDialog(Context context, String[] strings, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
        alt_bld.setItems(strings, listener);
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    public static boolean isShowKeyboard(Activity activity){
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        return imm.isActive();
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideKeyboardInActivity(Activity activity) {
        if(activity.getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showKeyboardInActivity(Activity activity, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public static String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        calendar.setTimeInMillis(currentTime);
        return formatter.format(calendar.getTime());
    }


    //start monday: mode=0
    //start sunday: mode=1
    public static Date firstDayOfWeek(Date date, int mode) {
        Calendar calendar =  Calendar.getInstance(Locale.GERMANY); // 월요일을 주의 시작일로 지정
        if(mode == 1) {
            calendar = Calendar.getInstance();
        }
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        // get start of this week in millisecond
        return calendar.getTime();
    }

    public static Date lastDayOfWeek(Date startWeekDate, int mode) {
        Calendar calendar = Calendar.getInstance(Locale.GERMANY); // 월요일을 주의 시작일로 지정
        if(mode == 1) {
            calendar = Calendar.getInstance();
        }
        calendar.setTime(startWeekDate);
        calendar.add(Calendar.DAY_OF_YEAR, 6);

        return calendar.getTime();
    }

    public static Date weekDayOfWeek(Date startWeekDate, int day) {
        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        calendar.setTime(startWeekDate);
        calendar.add(Calendar.DAY_OF_YEAR, day);

        return calendar.getTime();
    }

    public static int getDayOfWeek(String date, String format) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat(format);
            Date dt1 = format1.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(dt1);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek;
        }
        catch (Exception e) {
            return  -1;
        }
    }

    public static int getDateOfMonth(String date, String format) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat(format);
            Date dt1 = format1.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(dt1);
            int dateOfMonth = c.get(Calendar.DATE);
            return dateOfMonth;
        }
        catch (Exception e) {
            return  -1;
        }
    }

    public static int getMonthOfYear(String date, String format) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat(format);
            Date dt1 = format1.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(dt1);
            int dateOfMonth = c.get(Calendar.MONTH);
            return dateOfMonth;
        }
        catch (Exception e) {
            return  -1;
        }
    }

    public static int getYear(String date, String format) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat(format);
            Date dt1 = format1.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(dt1);
            int dateOfMonth = c.get(Calendar.YEAR);
            return dateOfMonth;
        }
        catch (Exception e) {
            return  -1;
        }
    }


    public static String getParamValue(int []arrInt){
        if(arrInt == null) {
            return  null;
        }

        String retVal = "";
        if(arrInt.length > 0) {
            retVal = String.format("%d", arrInt[0]);
            for (int i = 1; i < arrInt.length; i++) {
                retVal = String.format("%s, %d",retVal, arrInt[i]);
            }
        }
        return retVal;
    }

    public static String getParamValue(String []arrInt){
        if(arrInt == null) {
            return  null;
        }

        String retVal = "";
        if(arrInt.length > 0 && arrInt[0].isEmpty() == false) {
            retVal = String.format("%s", arrInt[0]);
            for (int i = 1; i < arrInt.length; i++) {
                if(arrInt[i].isEmpty() == false) {
                    retVal = String.format("%s,%s", retVal, arrInt[i]);
                }
            }

        }
        return retVal;
    }

    public static Date getDateFromString(String strDate, String strFromat) {
        SimpleDateFormat format = new SimpleDateFormat(strFromat);
        try {
            Date date = format.parse(strDate);

            return date;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static void startURLActivity(Activity context, String strURL) {

        if (strURL == null) return;

        if (URLUtil.isValidUrl(strURL) == false) return;

        Uri url = Uri.parse(strURL);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(url);
        context.startActivity(i);
    }



    /**
     * URL Encoder
     * @param str
     * @return
     */
    public static String urlEncoder(String str){
        String result = null;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String mapToQueryString(Map<String, String> map) {
        StringBuilder string = new StringBuilder();

        if (map.size() > 0) {
            string.append("?");
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            string.append(entry.getKey());
            string.append("=");
            string.append(entry.getValue());
            string.append("&");
        }

        return string.toString();
    }

    public static String getTimeStamp(){
        try {
//            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
//            return df.format(new Date());
            return String.valueOf(System.currentTimeMillis());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String rpad(String strSrc, int length, String strPad){
        byte[] bytes = strSrc.getBytes();
        int len = bytes.length;
        int nTemp = length - len;
        for(int i = 0; i < nTemp; i++){
            strSrc += strPad;
        }
        return strSrc;
    }

    public static boolean ValidateBitcoinAddress(String addr) {
        if (addr.length() < 26 || addr.length() > 35) return false;
        byte[] decoded = DecodeBase58(addr, 58, 25);
        if (decoded == null) return false;

        byte[] hash = Sha256(decoded, 0, 21, 2);

        return Arrays.equals(Arrays.copyOfRange(hash, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
    }

    private final static String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static byte[] DecodeBase58(String input, int base, int len) {
        byte[] output = new byte[len];
        for (int i = 0; i < input.length(); i++) {
            char t = input.charAt(i);

            int p = ALPHABET.indexOf(t);
            if (p == -1) return null;
            for (int j = len - 1; j > 0; j--, p /= 256) {
                p += base * (output[j] & 0xFF);
                output[j] = (byte) (p % 256);
            }
            if (p != 0) return null;
        }

        return output;
    }

    private static byte[] Sha256(byte[] data, int start, int len, int recursion) {
        if (recursion == 0) return data;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Arrays.copyOfRange(data, start, start + len));
            return Sha256(md.digest(), 0, 32, recursion - 1);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


    // DP사이즈 구해오기
    public static int getDpSize(Context context, int dp) {
        int iDp = 0;
        final float scale = context.getResources().getDisplayMetrics().density;
        iDp = (int) (dp * scale + 0.5f);
        return iDp;
    }


    // Get phone number
    public static String getPhoneNumber(Context context) {
        String phoneNumber;

        TelephonyManager tManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = tManager.getLine1Number();
        if (phoneNumber != null) {
            // String newNumber = phoneNumber;
            // if (phoneNumber.length() == 10)
            // {
            // newNumber = phoneNumber.substring(0, 3) + "-";
            // newNumber += phoneNumber.substring(3, 6) + "-";
            // newNumber += phoneNumber.substring(6, phoneNumber.length());
            // }
            // else if (phoneNumber.length() > 10)
            // {
            // newNumber = phoneNumber.substring(0, 3) + "-";
            // newNumber += phoneNumber.substring(3, 7) + "-";
            // newNumber += phoneNumber.substring(7, phoneNumber.length());
            // }
            // phoneNumber = newNumber;
        }

        return phoneNumber;
    }

    // 입력되어진 글자에 앞뒤로 ' 을 붙임 - 테이터베이스 필드 접근시 사용
    public static String AddQuote(String a_sData) {
        return "'" + a_sData + "'";
    }


    /**
     * @brief 숫자 앞에 0으로 채우기
     * @return
     */
    public static String getZeroFormat(int data)
    {
        String result = "";

        try
        {
            result = String.format("%02d", data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result = "";
        }

        return result;
    }

    //금액에 콤마찍기
    public static String changeAmt(String amt)
    {
        if(amt.length() == 0)
        {
            return "";
        }

        long value = Long.parseLong(amt);
        DecimalFormat format = new DecimalFormat("###,###");


        return format.format(value);
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final java.lang.reflect.Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        }
        catch (Exception e) {

        }
    }

    public static void setAutoRoate(Context context, boolean enabled) {
        Settings.System.putInt( context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

}
