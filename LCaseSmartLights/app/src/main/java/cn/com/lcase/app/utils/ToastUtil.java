package cn.com.lcase.app.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.widget.Toast;

import cn.com.lcase.app.R;

public class ToastUtil {

	/**
	 * 用dialog形式显示
	 * @param context
	 * @param content
	 */
	public static void showDialog(Context context, String content) {
		if(context==null || content==null)return;
		Builder builder = new Builder(context);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setTitle(R.string.infor);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage(content);
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	private static Toast mToast;
	private static Handler mHandler = new Handler();
	private static Runnable r = new Runnable() {
		public void run() {
			mToast.cancel();
		}
	};

	/**
	 * 字符串形式toast
	 * @param mContext
	 * @param text
	 */
	public static void showToast(Context mContext, String text) {
		if(text == null || mContext == null) return;

		mHandler.removeCallbacks(r);
		if (mToast != null)
			mToast.setText(text);
		else
			mToast = Toast.makeText(mContext, text, 2);
		mHandler.postDelayed(r, 1000);

		mToast.show();
	}

	/**
	 * values内容形式toast
	 * @param mContext
	 * @param resId
	 */
	public static void showToast(Context mContext, int resId) {
		if(mContext == null){
			return;
		}
		showToast(mContext, mContext.getResources().getString(resId));
	}
}
