package com.tosiapps.melodiemusic;


import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFrag extends Fragment {
    private TextView mTextMessage;
    private RelativeLayout box, top_box, new_box, genres_box;
    LinearLayout ad_unit;
    private ArrayList<String> tracks = new ArrayList<String>();
    private ArrayList<String> tracks_new = new ArrayList<String>();
    private final String TAG = "HomeFrag";
    private NativeAd nativeAd;
    private NativeAdLayout nativeAdLayout;
    private LinearLayout adView;
    private GetSongs songs_task;
    private GetSongsNew newsongs_task;
    private String country;
    private boolean no_ads = false;
    TextView box_title_trending;
    TinyDB tinyDB;
    private ListView trendingListView;
    private ListView newListView;
    private ProgressBar progressBar, progressBarNew;

    RelativeLayout sk_square, cz_square, us_square, pl_square, sp_square, ca_square, de_square;

    String titles[], thumbs[], durations[], genres[], ids[];

    private ProgressDialog pDialog;

    private static String url = "https://wawier.com/melodie/top.php?country=sk&count=3";
    private static String url_new = "https://wawier.com/melodie/genres.php?genre=new";

    ArrayList<HashMap<String, String>> trendingList, newList;

    public HomeFrag() {
        // Required empty public constructor
    }


    View rootview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootview =  inflater.inflate(R.layout.fragment_home, container, false);
        box_title_trending = rootview.findViewById(R.id.box_title_trending);
        trendingList = new ArrayList<>();
        newList = new ArrayList<>();

        tinyDB = new TinyDB(getContext());
        no_ads = tinyDB.getBoolean("no_ads");

        TopFrag topFrag = new TopFrag();
        String[] countries_codes = topFrag.countries_codes;
        country = Locale.getDefault().getCountry().toLowerCase();
        if (Arrays.asList(countries_codes).contains(country)) {
            box_title_trending.setText(getString(R.string.trending));
        }
        else {
            country = "us";
            box_title_trending.setText(getString(R.string.trending_in_us));
        }
        url = "https://wawier.com/melodie/top.php?country=" + country + "&count=3";

        progressBar = (ProgressBar)rootview.findViewById(R.id.progressBar);
        progressBarNew = rootview.findViewById(R.id.progressBarNew);
        trendingListView = (ListView)rootview.findViewById(R.id.trendingListView);
        ListView savedListView = (ListView) rootview.findViewById(R.id.listViewSaved);
        newListView = rootview.findViewById(R.id.newListView);
        box = (RelativeLayout)rootview.findViewById(R.id.box);
        top_box = rootview.findViewById(R.id.top_box);
        new_box = rootview.findViewById(R.id.new_box);
        genres_box = rootview.findViewById(R.id.genre_box);
        ad_unit = rootview.findViewById(R.id.ad_unit);

        sk_square = rootview.findViewById(R.id.relativeLayout2);
        cz_square = rootview.findViewById(R.id.relativeLayout3);
        us_square = rootview.findViewById(R.id.us_square);
        pl_square = rootview.findViewById(R.id.pl_square);
        de_square = rootview.findViewById(R.id.de_square);
        ca_square = rootview.findViewById(R.id.ca_square);
        sp_square = rootview.findViewById(R.id.sp_square);

        loadNativeAd();

        if (no_ads){
            ad_unit.setVisibility(View.GONE);
        }

        //genres GridView
        String[] genres_headings;
        Resources resources = getResources();
        genres_headings = resources.getStringArray(R.array.genres_headings);
        GridView gridView = rootview.findViewById(R.id.genresGrid);
        GenresAdapter genresAdapter = new GenresAdapter(getContext(), genres_headings);
        gridView.setAdapter(genresAdapter);
        String[] genres_this = getResources().getStringArray(R.array.genres_headings);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString("genre", genres_this[i]);
                TopFrag fragobj = new TopFrag();
                fragobj.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                ft.replace(R.id.rl, fragobj, "detailFragment");
                ft.commit();
            }
        });

        sk_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,"sk");
            }
        });

        cz_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,"cz");
            }
        });

        us_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,"us");
            }
        });

        pl_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,"pl");
            }
        });

        de_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,"de");
            }
        });

        ca_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,"ca");
            }
        });

        sp_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class, "sp");
            }
        });

        trendingListView.setFooterDividersEnabled(false);
        // savedListView.setFooterDividersEnabled(false);

        songs_task = (GetSongs) new GetSongs().execute();
        newsongs_task = (GetSongsNew) new GetSongsNew().execute();

        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class, country);
            }
        });

        top_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runActivity(List.class,country);
            }
        });
        return rootview;
    }
    private class GetSongs extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("songs");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String title = c.getString("title");
                        String thumb = c.getString("thumb");
                        String genres = c.getString("genres");
                        String duration = c.getString("duration");
                        String id = c.getString("id");

                        // tmp hash map for single contact
                        HashMap<String, String> song = new HashMap<>();

                        // adding each child node to HashMap key => value
                        song.put("title", title);
                        song.put("thumb", thumb);
                        song.put("genres", genres);
                        song.put("duration", duration);
                        song.put("id", id);

                        // adding contact to contact list
                        trendingList.add(song);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 1: " + e.getMessage());


                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.INVISIBLE);
            /**
             * Updating parsed JSON data into ListView
             * */
            titles = new String[trendingList.size()];
            thumbs = new String[trendingList.size()];
            genres = new String[trendingList.size()];
            ids = new String[trendingList.size()];
            durations = new String[trendingList.size()];

            for (int i = 0; i < trendingList.size(); i++){
                titles[i] = String.valueOf(trendingList.get(i).get("title"));
                thumbs[i] = String.valueOf(trendingList.get(i).get("thumb"));
                genres[i] = String.valueOf(trendingList.get(i).get("genres"));
                durations[i] = String.valueOf(trendingList.get(i).get("duration"));
                ids[i] = String.valueOf(trendingList.get(i).get("id"));
                tracks.add(ids[i] + ";" + titles[i] + ";" + thumbs[i] + ";" + genres[i]);
            }
            trendingListView.setAdapter(new ListViewAdapter(getContext(), tracks, null));

        }
    }

    private class GetSongsNew extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressBarNew.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url_new);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts_new = jsonObj.getJSONArray("songs");

                    // looping through All Contacts
                    for (int i = 0; i < contacts_new.length(); i++) {
                        JSONObject c = contacts_new.getJSONObject(i);

                        String title = c.getString("title");
                        String thumb = c.getString("thumb");
                        String genres = c.getString("genres");
                        String duration = c.getString("duration");
                        String id = c.getString("id");

                        // tmp hash map for single contact
                        HashMap<String, String> song = new HashMap<>();

                        // adding each child node to HashMap key => value
                        song.put("title", title);
                        song.put("thumb", thumb);
                        song.put("genres", genres);
                        song.put("duration", duration);
                        song.put("id", id);

                        // adding contact to contact list
                        newList.add(song);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error 1: " + e.getMessage());


                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (progressBarNew.getVisibility() == View.VISIBLE)
                progressBarNew.setVisibility(View.INVISIBLE);
            /**
             * Updating parsed JSON data into ListView
             * */
            titles = new String[newList.size()];
            thumbs = new String[newList.size()];
            genres = new String[newList.size()];
            ids = new String[newList.size()];
            durations = new String[newList.size()];

            for (int i = 0; i < newList.size(); i++){
                titles[i] = String.valueOf(newList.get(i).get("title"));
                thumbs[i] = String.valueOf(newList.get(i).get("thumb"));
                genres[i] = String.valueOf(newList.get(i).get("genres"));
                durations[i] = String.valueOf(newList.get(i).get("duration"));
                ids[i] = String.valueOf(newList.get(i).get("id"));
                tracks_new.add(ids[i] + ";" + titles[i] + ";" + thumbs[i] + ";" + genres[i]);
            }
            newListView.setAdapter(new ListViewAdapter(getContext(), tracks_new, null));

            //Toast.makeText(getContext(), "A: " + tracks_new.get(0), Toast.LENGTH_SHORT).show();

        }
    }

    private void runActivity(Class c, String lang){
        Bundle bundle = new Bundle();
        bundle.putString("country", lang);
        TopFrag fragobj = new TopFrag();
        fragobj.setArguments(bundle);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
        ft.replace(R.id.rl, fragobj, "detailFragment");
        ft.commit();
    }

    private void loadNativeAd() {
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeAd = new NativeAd(Objects.requireNonNull(getActivity()), "331110057759065_333107154226022");

        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Race condition, load() called again before last ad was displayed
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAd);
            }
            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // Request an ad
        nativeAd.loadAd();
    }
    private void inflateAd(NativeAd nativeAd) {

        nativeAd.unregisterView();

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = rootview.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(Objects.requireNonNull(getContext()), nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = rootview.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = rootview.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = rootview.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = rootview.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = rootview.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = rootview.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = rootview.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        ArrayList<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                rootview,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }
}
