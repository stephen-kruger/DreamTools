package com.madibasoft.dreamtools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Utils {
	public static void infoDialog(final Activity activity, CharSequence title, CharSequence description, final boolean closeParent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(description)
		.setCancelable(false)
		.setPositiveButton(activity.getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//dialog.cancel();
				dialog.dismiss();
				if (closeParent) {
					activity.finish();
				}
			}
		});
		builder.setTitle(title);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	 public static void toastImage(Context c, int imageResource) {
	        Toast ImageToast = new Toast(c);
	        LinearLayout toastLayout = new LinearLayout(c);
	        toastLayout.setOrientation(LinearLayout.HORIZONTAL);
	        ImageView image = new ImageView(c);
//	        TextView text = new TextView(getBaseContext());
	        image.setImageResource(imageResource);
//	        text.setText("Hello!");
	        toastLayout.addView(image);
//	        toastLayout.addView(text);
	        ImageToast.setView(toastLayout);
	        ImageToast.setDuration(Toast.LENGTH_LONG);
	        ImageToast.show();
	    }
}
