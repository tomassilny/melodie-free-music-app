package com.tosiapps.melodiemusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;

import java.util.ArrayList;

/**
 * Created by Tomas on 6. 12. 2018.
 */

public class Config {

    public static final int YT_ITAG_FOR_AUDIO = 140;

    public void splitArray(ArrayList<String> arrayList){
    }

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void displayNetworkErrorBox(Context context){
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.alert))
                .setMessage(context.getString(R.string.no_internet_connection))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(context.getString(R.string.refresh), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity)context).finish();
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(context.getString(R.string.close_app), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                })
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
