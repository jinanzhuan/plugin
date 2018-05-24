package com.ljn.plugin;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;

import java.io.File;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {
    private Activity mContext;
    private Button btn_install;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_install = (Button)findViewById(R.id.btn_install);
        mContext = this;
        btn_install.setOnClickListener(this);
        KardiaUtils.getApkFromAssets(mContext);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_install :
                start();
                break;
        }
    }

    private void start() {
        if (PluginManager.getInstance().isConnected()) {
            startLoad();
        } else {
            PluginManager.getInstance().addServiceConnection(this);
        }
    }

    private void startLoad() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startLoadInner();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x1);
            }
        }
    }

    private void startLoadInner() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Download/", "ECG.apk");
        android.util.Log.e("TAG", "file.path="+file.getAbsolutePath());
        doInstall(file);
    }

    public Activity getActivity() {
        return this;
    }

    private void doInstall(final File apkPath) {
        try {
            final PackageInfo info = getPackageManager().getPackageArchiveInfo(apkPath.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (info == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "apk损坏\n" + apkPath.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            final int re = PluginManager.getInstance().installPackage(apkPath.getAbsolutePath(), 0);
            android.util.Log.e("TAG", "re="+re);
            PackageManager pm = getActivity().getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(info.packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.i("DroidPlugin", "start " + info.packageName + "@" + intent);
                startActivity(intent);
            } else {
                Log.e("DroidPlugin", "pm " + pm.toString() + " no find intent " + info.packageName);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        start();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
