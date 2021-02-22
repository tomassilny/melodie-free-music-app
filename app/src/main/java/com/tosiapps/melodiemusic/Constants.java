package com.tosiapps.melodiemusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.tosiapps.melodymusic.action.main";
        public static String INIT_ACTION = "com.tosiapps.melodymusic.action.init";
        public static String PREV_ACTION = "com.tosiapps.melodymusic.action.prev";
        public static String PLAY_ACTION = "com.tosiapps.melodymusic.action.play";
        public static String NEXT_ACTION = "com.tosiapps.melodymusic.action.next";
        public static String STARTFOREGROUND_ACTION = "com.tosiapps.melodymusic.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.tosiapps.melodymusic.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sk_square, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

}