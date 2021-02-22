package com.tosiapps.melodiemusic;

import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.tosiapps.melodiemusic.constants.StandOutFlags;
import com.tosiapps.melodiemusic.ui.Window;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SimpleWindow extends StandOutWindow {
	private YouTubePlayerView youTubePlayerView;
	private String[] videoIds = {"6JYIGclVQdw", "LvetJ9U_tVY"};
	String id;

	@Override
	public String getAppName() {
		return "SimpleWindow";
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_close_clear_cancel;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		// create a new layout from body.xm
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.simple, frame, true);
		YouTubePlayerView youtubePlayerView = frame.findViewById(R.id.youtube_player_view);
        View customPlayerUI = youtubePlayerView.inflateCustomPlayerUI(R.layout.custom_player_ui);

		youtubePlayerView.initialize(youTubePlayer -> {
            CustomPlayerUIController customPlayerUIController = new CustomPlayerUIController(this, customPlayerUI, youTubePlayer, youTubePlayerView);
            youTubePlayer.addListener(customPlayerUIController);
            youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady() {
                    youTubePlayer.loadVideo(videoIds[0], 0);
                }
            });
		}, true);
	}

	// the window will be centered
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		return new StandOutLayoutParams(id, 500, 500,
				StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);
	}


	// move the window by dragging the view
	@Override
	public int getFlags(int id) {
		return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
				| StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return "Click to close the SimpleWindow";
	}

	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return StandOutWindow.getCloseIntent(this, SimpleWindow.class, id);
	}

	IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public SimpleWindow getServerInstance() {
			return SimpleWindow.this;
		}
	}

	public String getTime() {
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return mDateFormat.format(new Date());
	}
}
