package com.tosiapps.melodiemusic;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.PlayerUIController;
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Calendar;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import okhttp3.internal.Version;

import static com.google.api.client.util.IOUtils.copy;

public class FloatingViewService extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private long startClickTime;
    String CHANNEL_ID = "my_channel_01";// The id of the channel.
    RemoteViews views, bigViews;
    boolean trackEnd = false;
    Button cover_button;

    private static final int MAX_CLICK_DURATION = 200;
    boolean pause = false, changeVideo = false, seeker = false, icon_to_play = false, play_next_track = false, play_prev_track = false;
    RelativeLayout img;
    String id, title, title_sec = null, playlist, thumb = null;
    Handler handler;
    ImageView closebtn;
    WindowManager.LayoutParams params_initial, params;
    private YouTubePlayerView youtubePlayerView, youTubePlayerView;
    int videoDuration = 0, currentSecond = 0, loop = 0, seekto = -1;

    Notification status;
    private final String LOG_TAG = "NotificationService";

    public void showNotification(String title, String thumb, boolean play) {
        //TOTO
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {


// Using RemoteViews to bind custom layouts into Notification
            views = new RemoteViews(getPackageName(),
                    R.layout.status_bar);
            bigViews = new RemoteViews(getPackageName(),
                    R.layout.status_bar_expanded);

            if (play) {
                views.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_pause);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_pause);
            } else {
                views.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_play);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_play);
            }

// showing default album image
            // views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
            views.setViewVisibility(R.id.status_bar_album_art, View.VISIBLE);


            Picasso.get().load(thumb).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    bigViews.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
                    views.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });

            Intent notificationIntent = new Intent(this, Player.class);
            notificationIntent.putExtra("title", title);
            notificationIntent.putExtra("id", id);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Intent previousIntent = new Intent(this, FloatingViewService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0);

            Intent playIntent = new Intent(this, FloatingViewService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

            Intent nextIntent = new Intent(this, FloatingViewService.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0);

            Intent closeIntent = new Intent(this, FloatingViewService.class);
            closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                    closeIntent, 0);

            views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
            bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

            views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
            bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

            views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
            bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

            views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
            bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);


            views.setTextViewText(R.id.status_bar_track_name, title);
            bigViews.setTextViewText(R.id.status_bar_track_name, title);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int notifyID = 1;
                String CHANNEL_ID = "my_channel_01";// The id of the channel.
                CharSequence name = "Melodie";// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.setSound(null, null);
                mChannel.enableVibration(false);

// Create a notification and set the notification channel.
                status = new Notification.Builder(this)
                        .setContentTitle("New Message")
                        .setContentText("You've received new messages.")
                        .setSmallIcon(R.drawable.note)
                        .setChannelId(CHANNEL_ID)
                        .build();
            } else {

                status = new Notification.Builder(this)
                        .build();
            }

            status.contentView = views;
            status.bigContentView = bigViews;
            status.flags = Notification.FLAG_ONGOING_EVENT;
            status.icon = R.drawable.note;
            status.contentIntent = pendingIntent;


            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        }
    }

    IBinder mBinder = new FloatingViewService.LocalBinder();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TOTO
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                // showNotification();
                //  Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

            } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
                play_prev_track = true;
                Log.i(LOG_TAG, "Clicked Previous");
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                if (pause) {
                    playVideo();
                } else {
                    pauseVideo();
                }
            } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                play_next_track = true;
            } else if (intent.getAction().equals(
                    Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                // Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
                stopForeground(true);
                stopSelf();
            }
        }
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public FloatingViewService getServerInstance() {
            return FloatingViewService.this;
        }
    }

    public FloatingViewService() {
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();


        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        youtubePlayerView = mFloatingView.findViewById(R.id.youtube_player_view);
        View customPlayerUI = youtubePlayerView.inflateCustomPlayerUI(R.layout.custom_player_ui);

        ProgressBar progressBar = customPlayerUI.findViewById(R.id.progressbar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        youtubePlayerView.setClickable(false);
        youtubePlayerView.setFocusable(false);

        img = mFloatingView.findViewById(R.id.imageView10);
        cover_button = mFloatingView.findViewById(R.id.button3);
        closebtn = mFloatingView.findViewById(R.id.buttonClose);

        youtubePlayerView.initialize(youTubePlayer -> {
            YouTubePlayerTracker tracker = new YouTubePlayerTracker();
            youTubePlayer.addListener(tracker);

            CustomPlayerUIController customPlayerUIController = new CustomPlayerUIController(this, customPlayerUI, youTubePlayer, youTubePlayerView);
            youTubePlayer.addListener(customPlayerUIController);
            youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady() {
                    youTubePlayer.loadVideo(id, 0);
                }
            });



            handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
                    if( myKM.inKeyguardRestrictedInputMode()) {
                        pauseVideo();
                    } else {
                        if (!pause)
                            playVideo();
                    }

                    if (changeVideo){
                        youTubePlayer.loadVideo(id, 0);
                        changeVideo = false;
                    }
                    videoDuration = (int) tracker.getVideoDuration();
                    currentSecond = (int) tracker.getCurrentSecond();

                    if (pause){
                        youTubePlayer.pause();
                    }else{
                        youTubePlayer.play();
                    }

                    if (trackEnd && currentSecond == 0){
                       // pauseVideo();
                        trackEnd = false;
                    }

                    if (currentSecond > 5 && currentSecond < videoDuration - 1)
                        seeker = false;

                    if (loop == 1) {
                        if (pause)
                            youTubePlayer.pause();
                    }

                    //ak je nacitany
                    if (currentSecond > 0){
                        //ak je na konci
                        if (videoDuration == currentSecond){
                            if (!seeker) {
                                currentSecond = 0;
                                youTubePlayer.seekTo(0);
                                if (loop == 1) {
                                    trackEnd = false;
                                    playVideo();
                                } else {
                                    trackEnd = true;
                                    pauseVideo();
                                }
                                seeker = true;
                            }
                        }
                    }


                    // if track is paused
                  /*  if (pause)
                        youTubePlayer.pause();
                    // check if video ended if yes, then check loop
                    else {
                        if ((videoDuration != currentSecond) && (videoDuration > 0))
                            youTubePlayer.play();
                        else{
                            if (currentSecond > 0) {
                                if (loop == 1) {
                                    if (!seeker) {
                                        youTubePlayer.seekTo(1);
                                        youTubePlayer.play();
                                        seeker = true;
                                    }
                                } else {
                                    if (!seeker) {
                                        youTubePlayer.seekTo(0);
                                        youTubePlayer.pause();
                                        seeker = true;
                                    }
                                }
                            }
                        }
                    }*/
                    if (seekto != -1){
                        youTubePlayer.seekTo(seekto);
                        seekto = -1;
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.postDelayed(runnable, 100);

        }, true);

        //setting the layout parameters
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params_initial = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }else{
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params_initial = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(this);

        img.setActivated(false);

        //adding an touchlistener to make drag movement of the floating widget
        img.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (img.isActivated()) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            startClickTime = Calendar.getInstance().getTimeInMillis();
                            return true;

                        case MotionEvent.ACTION_UP:
                            long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                            if(clickDuration < MAX_CLICK_DURATION) {
                                Intent intent = new Intent(getApplicationContext(), Player.class);
                                intent.putExtra("id", id);
                                intent.putExtra("title", title);
                                intent.putExtra("playlist", playlist);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            startClickTime = 0;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            //this code is helping the widget to move around the screen with fingers
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mFloatingView, params);
                            return true;

                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
        stopSelf();
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonClose:
                handler.removeCallbacks(null);
                handler.removeCallbacksAndMessages(null);
                stopSelf();
                break;
        }
    }


    public void pauseVideo(){
        icon_to_play = true;
        showNotification(title, thumb, false    );
        pause = true;
    }
    public void playVideo(){
        icon_to_play = false;
       showNotification(title, thumb, true);
        pause = false;
    }
    public void seekTo(int i){
        seekto = i;
    }
    public boolean playNextTrack(){
        boolean return_value = false;
        if (play_next_track){
            return_value = true;
            play_next_track = false;
        }
        return return_value;
    }
    public boolean playPrevTrack(){
        boolean return_value = false;
        if (play_prev_track){
            return_value = true;
            play_prev_track = false;
        }
        return return_value;
    }
    public boolean trackEnd(){
        return trackEnd;
    }
    public int getDuration(){
        return videoDuration;
    }
    public int getCurrentSecond(){
        return currentSecond;
    }
    public boolean setIconToPlay(){
        return icon_to_play;
    }
    public void loop(int i){
        loop = i;
    }
    public void setFocus(boolean focus){
        img.setActivated(focus);
        if (focus){
            closebtn.setVisibility(View.VISIBLE);
            youtubePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(300, 200));
            cover_button.setLayoutParams(new RelativeLayout.LayoutParams(300, 200));
            cover_button.setVisibility(View.GONE);

        }
    }
    public void setVisible(boolean visible){
        if (visible){
            youtubePlayerView.setVisibility(View.VISIBLE);
            mFloatingView.setVisibility(View.VISIBLE);
        }else{
            youtubePlayerView.setVisibility(View.GONE);
            mFloatingView.setVisibility(View.GONE);
        }
    }
    public void center(){
        closebtn.setVisibility(View.INVISIBLE);
        youtubePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(500, 300));
        cover_button.setLayoutParams(new RelativeLayout.LayoutParams(500, 300));
        cover_button.setVisibility(View.VISIBLE);
        mWindowManager.updateViewLayout(mFloatingView, params_initial);
    }
    public void loadVideo(String id, String title, String playlist){
        if (!id.equals(this.id)){
            changeVideo = true;
        }
        this.id = id;
        this.title = title;
        this.playlist = playlist;
    }

    public void close(){
        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
        stopForeground(true);
        stopSelf();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            CharSequence name = "Melodie";
            String description = "Melodie Player";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.tosiapps.melodiemusic";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.note)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }
}