package cn.com.lcase.app.utils;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.L;

import java.io.File;
import java.io.IOException;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Version;
import cn.com.lcase.app.net.LCaseConstants;

public class UpdateUtil extends BroadcastReceiver {
	private Version updateInfo;
	private Context context;
	private boolean isDownloading = false;
	private DownloadManager manager;
	private String apkPath;
	private long currentTaskId;

	public UpdateUtil(Version updateInfo, Context context) {
		super();
		this.updateInfo = updateInfo;
		this.context = context;
		context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	public void downloadApk() {
		if (isDownloading)
			return;

		String downloadUrl = LCaseConstants.SERVER_URL + updateInfo.getUrl();
		apkPath = getApkFileStoragePath();
		if (TextUtils.isEmpty(apkPath)) {
			Toast.makeText(getContext(), "存储空间不足", Toast.LENGTH_SHORT).show();
			return;
		}

		File apkFile = new File(apkPath);
		if (apkFile.exists()) {
			installPackage(apkPath);
			return;
		}

		manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
		request.setDestinationUri(Uri.parse("file://" + apkPath));

		// 设置下载路径和文件名

		// request.setDestinationInExternalPublicDir("download",
		// "time2plato.apk");
		
		// 表示允许MediaScanner扫描到这个文件，默认不允许。
		request.allowScanningByMediaScanner();
		
		// 设置下载中通知栏提示的标题
		request.setTitle(context.getResources().getString(R.string.app_name));
		
		// 设置下载中通知栏提示的介绍
		request.setDescription(context.getResources().getString(R.string.app_name));
		
		// 表示下载进行中和下载完成的通知栏是否显示。默认只显示下载中通知。VISIBILITY_VISIBLE_NOTIFY_COMPLETED表示下载完成后显示通知栏提示。
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		request.setMimeType("application/vnd.android.package-archive");

		// request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);//
		// 表示下载允许的网络类型，默认在任何网络下都允许下载。

		// 设置为可见和可管理
		request.setVisibleInDownloadsUi(true);
		currentTaskId = manager.enqueue(request);
		isDownloading = true;
	}

	private void installPackage(String apkPath) {
		File apkFile = new File(apkPath);
		if (!apkFile.exists()) {
			return;
		}
		apkFile.setExecutable(true, false);
		apkFile.setReadable(true, false);
		apkFile.setWritable(true, false);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		getContext().startActivity(intent);
	}

	private String getApkFileStoragePath() {
		String path = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File dir = getExternalCacheDir(getContext());
			if (dir != null) {
				path = dir.getAbsolutePath() + "/" + getApkFileName();
			} else {
				path = getContext().getDir("temp", Context.MODE_PRIVATE).getAbsolutePath() + "/" + getApkFileName();
			}
		} else {
			path = getContext().getDir("temp", Context.MODE_PRIVATE).getAbsolutePath() + "/" + getApkFileName();
		}
		return path;
	}

	private File getExternalCacheDir(Context context) {
		File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
		File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
		if (!appCacheDir.exists()) {
			if (!appCacheDir.mkdirs()) {
				L.e("Unable to create external cache directory");
				return null;
			}
			try {
				new File(appCacheDir, ".nomedia").createNewFile();
			} catch (IOException e) {
				L.e("Can't create \".nomedia\" file in application external cache directory");
			}
		}
		return appCacheDir;
	}

	private String getApkFileName() {
		return getContext().getPackageName() + "_v" + updateInfo.getVersionName() + ".apk";
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null)
			return;

		long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
		if (currentTaskId != id)
			return;

		isDownloading = false;

		Query query = new Query();
		query.setFilterById(currentTaskId);

		Cursor cursor = manager.query(query);
		if (cursor == null)
			return;

		if (cursor.moveToFirst()) {
			String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
			int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
			int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			Log.e("TAG", "file uri is " + uri + " file status is " + status + " file reason " + reason);
			switch (status) {
			case DownloadManager.STATUS_SUCCESSFUL:
				ToastUtil.showToast(context,"下载成功");
				installPackage(apkPath);
				break;
			default:
				ToastUtil.showToast(context,"下载失败");
				break;
			}
		}
		cursor.close();

	}

}
