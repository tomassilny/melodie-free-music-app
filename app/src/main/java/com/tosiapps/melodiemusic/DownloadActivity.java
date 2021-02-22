package com.tosiapps.melodiemusic;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.tosiapps.melodiemusic.YouTubeDownloader.YouTubeFragmentedVideo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-May-15
 */

public class DownloadActivity extends Activity
{
    private static final String TAG = DownloadActivity.class.getSimpleName();

    private static String youtubeLink;
    private InterstitialAd mInterstitialAd;
    private boolean no_ads = false;
    private LinearLayout mainLayout;
    private ProgressBar mainProgressBar;
    private List<YouTubeFragmentedVideo> formatsToShowList;

    Context context_main;

    public DownloadActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download);
        mainLayout = findViewById(R.id.main_layout);
        mainProgressBar = findViewById(R.id.progress_bar);
        Intent intent = getIntent();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String ytLink = intent.getStringExtra("link");

        // Check how it was started and if we can get the youtube link
        if (savedInstanceState == null && ytLink != null && !ytLink.trim().isEmpty()) {
            if (ytLink != null) {
                youtubeLink = ytLink;
                // We have a valid link
                getYoutubeDownloadUrl(context_main, youtubeLink);
            } else {
               // Toast.makeText(this, "No Link", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (savedInstanceState != null && youtubeLink != null) {
            getYoutubeDownloadUrl(context_main, youtubeLink);
        } else {
            finish();
        }
    }

    private Context getActivityContext() { return DownloadActivity.this; }

    public void getYoutubeDownloadUrl(Context context, String youtubeLink)
    {
        Toast.makeText(context, R.string.download_started_moment, Toast.LENGTH_LONG).show();
        TinyDB tinyDB = new TinyDB(context);
        no_ads = tinyDB.getBoolean("no_ads");

        context_main = context;
        mInterstitialAd = new InterstitialAd(context_main);
        mInterstitialAd.setAdUnitId("ca-app-pub-4006620366079806/3448181847");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        new YouTubeExtractor(context_main)
        {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta)
            {
//                mainProgressBar.setVisibility(View.GONE);
                if (ytFiles == null) {
                    TextView tv = new TextView(context_main);
                    tv.setText("Could't extract URL.");
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    //mainLayout.addView(tv);
                    return;
                }
                formatsToShowList = new ArrayList<>();
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    YtFile ytFile = ytFiles.get(itag);

                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        addFormatToList(ytFile, ytFiles);
                    }
                }
                Collections.sort(formatsToShowList, new Comparator<YouTubeFragmentedVideo>()
                {
                    @Override
                    public int compare(YouTubeFragmentedVideo lhs, YouTubeFragmentedVideo rhs)
                    {
                        return lhs.height - rhs.height;
                    }
                });
                    startDownload(vMeta.getTitle(), formatsToShowList.get(0));

            }
        }.extract(youtubeLink, true, false);
    }

    private void addFormatToList(YtFile ytFile, SparseArray<YtFile> ytFiles)
    {
        int height = ytFile.getFormat().getHeight();
        if (height != -1) {
            for (YouTubeFragmentedVideo frVideo : formatsToShowList) {
                if (frVideo.height == height && (frVideo.videoFile == null ||
                        frVideo.videoFile.getFormat().getFps() == ytFile.getFormat().getFps())) {
                    return;
                }
            }
        }
        YouTubeFragmentedVideo frVideo = new YouTubeFragmentedVideo();
        frVideo.height = height;
        if (ytFile.getFormat().isDashContainer()) {
            if (height > 0) {
                frVideo.videoFile = ytFile;
                frVideo.audioFile = ytFiles.get(Config.YT_ITAG_FOR_AUDIO);
            } else {
                frVideo.audioFile = ytFile;
            }
        } else {
            frVideo.videoFile = ytFile;
        }
        formatsToShowList.add(frVideo);
    }

    private void addButtonToMainLayout(final String videoTitle, final YouTubeFragmentedVideo ytFragmentedVideo)
    {
        // Display some buttons and let the user choose the formatViewCount
        String btnText;
        if (ytFragmentedVideo.height == -1) {
            btnText = "Audio " + ytFragmentedVideo.audioFile.getFormat().getAudioBitrate() + " kbit/s";
        } else {
            btnText = (ytFragmentedVideo.videoFile.getFormat().getFps() == 60) ?
                    ytFragmentedVideo.height + "p60" :
                    ytFragmentedVideo.height + "p";
        }


    }
    public void startDownload(final String videoTitle, final YouTubeFragmentedVideo ytFragmentedVideo)
    {
        if (mInterstitialAd.isLoaded()) {
            if (!no_ads) {
                mInterstitialAd.show();
            }
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

        String filename;
        if (videoTitle.length() > 55) {
            filename = videoTitle.substring(0, 55);
        } else {
            filename = videoTitle;
        }
        filename = filename.replaceAll("\\\\|>|<|\"|\\||\\*|\\?|%|:|#|/", "");
        filename += (ytFragmentedVideo.height == -1) ? "" : "-" + ytFragmentedVideo.height + "p";
        String downloadIds = "";
        boolean hideAudioDownloadNotification = false;
        downloadFromUrl(ytFragmentedVideo.audioFile.getUrl(), videoTitle, filename + "." + ytFragmentedVideo.audioFile.getFormat().getExt(), false);
              /*  if (ytFragmentedVideo.videoFile != null) {
                    downloadIds += downloadFromUrl(ytFragmentedVideo.videoFile.getUrl(), videoTitle,
                            filename + "." + ytFragmentedVideo.videoFile.getFormat().getExt(), false);
                    downloadIds += "-";
                    hideAudioDownloadNotification = true;
                }
                if (ytFragmentedVideo.audioFile != null) {
                    downloadIds += downloadFromUrl(ytFragmentedVideo.audioFile.getUrl(), videoTitle,
                            filename + "." + ytFragmentedVideo.audioFile.getFormat().getExt(), hideAudioDownloadNotification);
                }*/
        //    if (ytFragmentedVideo.audioFile != null)
        //cacheDownloadIds(downloadIds);
        finish();
    }



    private long downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName, boolean hide) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

           if (hide) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);
        } else
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/Melodie Music/", fileName);
        DownloadManager manager = (DownloadManager) context_main.getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);


    }

    private void cacheDownloadIds(String downloadIds)
    {
        File dlCacheFile = new File(this.getCacheDir().getAbsolutePath() + "/" + downloadIds);
        try {
            dlCacheFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* this.progressDialog = new ProgressDialog(DownloadActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();*/
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());

                //Append timestamp to file name
                fileName = timestamp + "_" + fileName;

                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + File.separator + "androiddeft/";

                //Create androiddeft folder if it does not exist
                File directory = new File(folder);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Downloaded at: " + folder + fileName;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
//            this.progressDialog.dismiss();

            // Display File path after downloading
          /*  Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();*/
        }
    }

}
