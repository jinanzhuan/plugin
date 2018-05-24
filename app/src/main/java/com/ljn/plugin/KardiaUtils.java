package com.ljn.plugin;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.RemoteException;
import android.widget.Toast;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

/**
 * Created by songyuqiang on 16/12/28.
 */
public class KardiaUtils {

    private static long downloadId;
    private static DownloadManager downloadManager;

    private KardiaUtils() {
    }

    static String F_ECG_APK = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads" + "/ECG.apk";
    static String FILE_ECG = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads";
    public static void getApkFromAssets(Context context){
        AssetManager assets = context.getAssets();
        try {
            //获取assets资源目录下的himarket.mp3,实际上是himarket.apk,为了避免被编译压缩，修改后缀名。
            InputStream stream = assets.open("ECG.mp3");
            if(stream==null) {
                return;
            }

            String folder = FILE_ECG;
            File f=new File(folder);
            if(!f.exists()) {
                f.mkdir();
            }
            String apkPath = F_ECG_APK;
            deleteApkFile(apkPath);
            File file = new File(apkPath);
            //创建apk文件
            file.createNewFile();
            //将资源中的文件重写到sdcard中
            //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            writeStreamToFile(context, "ECG.mp3", file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean installing = false;

    public static void checkApk(final Activity context){
        PackageManager pm = context.getPackageManager();
        File file = new File(F_ECG_APK);
        if(file.exists() && file.getPath().toLowerCase().endsWith(".apk")) {
            final PackageInfo info = pm.getPackageArchiveInfo(file.getPath(), 0);
            if(installing) {
                return;
            }
            if (!PluginManager.getInstance().isConnected()) {
                Toast.makeText(context, "插件服务正在初始化，请稍后再试。。。", Toast.LENGTH_SHORT).show();
            }
            try {
                if (PluginManager.getInstance().getPackageInfo(info.packageName, 0) != null) {
                    Toast.makeText(context, "已经安装了，不能再安装", Toast.LENGTH_SHORT).show();
                    Intent intent = pm.getLaunchIntentForPackage(info.packageName);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        Log.e("DroidPlugin", "pm " + pm.toString() + " no find intent " + info.packageName);
                    }
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            doInstall(context, info);
                        }
                    }.start();

                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    PluginManager.getInstance().installPackage(F_ECG_APK, 0);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private static void doInstall(Activity context, PackageInfo info) {
        installing = true;
        try {
            int re = PluginManager.getInstance().installPackage(F_ECG_APK, 0);
            installing = false;
            switch (re) {
                case PluginManager.INSTALL_FAILED_NO_REQUESTEDPERMISSION:
                    Toast.makeText(context, "安装失败，文件请求的权限太多", Toast.LENGTH_SHORT).show();
                    break;
                case INSTALL_FAILED_NOT_SUPPORT_ABI:
                    Toast.makeText(context, "宿主不支持插件的abi环境，可能宿主运行时为64位，但插件只支持32位", Toast.LENGTH_SHORT).show();
                    break;
                case INSTALL_SUCCEEDED:
                    Toast.makeText(context, "安装完成", Toast.LENGTH_SHORT).show();
                    PackageManager pm = context.getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage(info.packageName);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        Log.e("DroidPlugin", "pm " + pm.toString() + " no find intent " + info.packageName);
                    }
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     * @param path
     * @return
     */
    public static boolean deleteApkFile(String path){
        File file = new File(path);
        if(file.exists()){
            try {
                file.delete();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将assets下的文件放到sd指定目录下
     *
     * @param context    上下文
     * @param assetsPath assets下的路径
     * @param sdCardPath sd卡的路径
     */
    public static void writeStreamToFile(Context context, String assetsPath,
                                         String sdCardPath) {
        try {
            String mString[] = context.getAssets().list(assetsPath);
            if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件
                InputStream mIs = context.getAssets().open(assetsPath); // 读取流
                byte[] mByte = new byte[1024];
                int bt = 0;
                File file = new File(sdCardPath);
                if (!file.exists()) {
                    file.createNewFile(); // 创建文件
                } else {
                    return;//已经存在直接退出
                }
                FileOutputStream fos = new FileOutputStream(file); // 写入流
                while ((bt = mIs.read(mByte)) != -1) { // assets为文件,从文件中读取流
                    fos.write(mByte, 0, bt);// 写入流到文件中
                }
                fos.flush();// 刷新缓冲区
                mIs.close();// 关闭读取流
                fos.close();// 关闭写入流

            } else { // 当mString长度大于0,说明其为文件夹
                sdCardPath = sdCardPath + File.separator + assetsPath;
                File file = new File(sdCardPath);
                if (!file.exists())
                    file.mkdirs(); // 在sd下创建目录
                for (String stringFile : mString) { // 进行递归
                    writeStreamToFile(context, assetsPath + File.separator
                            + stringFile, sdCardPath);
                }
            }
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }
    }

}
