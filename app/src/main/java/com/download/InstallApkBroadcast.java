package com.download;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.grean.dustctrl.myApplication;

public class InstallApkBroadcast extends BroadcastReceiver{
	private final static String tag = "InstallApkBroadcast";
	private long myDwonloadID;
	public InstallApkBroadcast() {
		// TODO 自动生成的构造函数存根
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自动生成的方法存根
		myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		long sysDownloadID = (Long)myApplication.getInstance().getConfig().get("ID");
		if (myDwonloadID == sysDownloadID) {			
			Log.d(tag, "开始安装");
			install(context);
		}
	}
	
	private void install(Context context) {
        Intent installintent = new Intent();
        installintent.setAction(Intent.ACTION_VIEW);
        // 在Boradcast中启动活动需要添加Intent.FLAG_ACTIVITY_NEW_TASK
        installintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       /* installintent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/myApp.apk")),
                "application/vnd.android.package-archive");//存储位置为Android/data/包名/file/Download文件夹*/
       // Log.d(tag, Environment.getExternalStorageDirectory().toString());
       /* installintent.setDataAndType(Uri.fromFile(new File( Environment.getExternalStorageDirectory() + "/Download/123.apk")),
                "application/vnd.android.package-archive");//存储位置为Android/data/包名/file/Download文件夹*/
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadFileUri = dManager.getUriForDownloadedFile(myDwonloadID);
        installintent.setDataAndType(downloadFileUri,"application/vnd.android.package-archive");
        context.startActivity(installintent);
    }

}
