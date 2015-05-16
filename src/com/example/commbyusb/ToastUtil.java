package com.example.commbyusb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class ToastUtil {

	private static Toast toast;
	private static View view;

	@SuppressLint("ShowToast")
	private static void getToast(Context context) {
		if (toast == null) {
			toast = new Toast(context);
		}
		if (view == null) {
			view = Toast.makeText(context, "", Toast.LENGTH_SHORT).getView();
		}
		toast.setView(view);
	}

	public static void shortToast(Context context, String content) {
		// Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
		showToast(context, content, Toast.LENGTH_SHORT);
	}

	public static void longToast(Context context, String content) {
		// Toast.makeText(context, content, Toast.LENGTH_LONG).show();
		showToast(context, content, Toast.LENGTH_LONG);
	}

	private static void showToast(Context context, CharSequence msg, int time) {
		try {
			getToast(context);
			toast.setText(msg);
			toast.setDuration(time);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
