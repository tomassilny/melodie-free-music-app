package com.tosiapps.melodiemusic;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.ArrayList;

import static com.tosiapps.melodiemusic.StandOutWindow.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFrag extends Fragment implements BillingProcessor.IBillingHandler {
    public SettingsFrag() {
        // Required empty public constructor
    }

    ArrayList<String> playlists = new ArrayList<String>();
    ArrayList<String> playlists_songs = new ArrayList<String>();
    BillingProcessor bp;
    TinyDB tinydb;
    boolean no_ads = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_settings, container, false);

        bp = new BillingProcessor(getContext(), "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqGpI/rOnk2yk1D/z1ZVIAIr5d8MoJ7NJYuriYHgxI6RlU89vSq+HE054M6NXTT29PzRM+H2C72xZovc/8oCntrcrnzyBXDcjfwwr0SOZ+dJyyfw4/sM7oSee0yABNZrqu8T+eplXNQX+7jL6PUeHYwqFWwebcv8gf95n5zMY+wom1CUbhfbrYLVQbhw2wtD7sEYr0H9dKowTwchA72WC4lO/IylsCugcWalwaSef/xaB6q0akFaQQpmPFbEZ/S/aAFQqTdqLcjetvIqRHRl0cRLT/eOCmmB/hd0I+G6UyS3kft2x/pg0LpF+GN2QU+y8sIWrFqopvuScM+vysrtCJQIDAQAB", (BillingProcessor.IBillingHandler) this);
        bp.initialize();

        tinydb = new TinyDB(getContext());
        no_ads = tinydb.getBoolean("no_ads");


        RelativeLayout delete_playlists = rootview.findViewById(R.id.delete_playlists);
        RelativeLayout share = rootview.findViewById(R.id.share);
        RelativeLayout remove_ads = rootview.findViewById(R.id.remove_ads);
        RelativeLayout rate_us = rootview.findViewById(R.id.rate_us);
        RelativeLayout follow_us = rootview.findViewById(R.id.instagram);

        remove_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!no_ads) {
                    bp.purchase((Activity) getContext(), "remove_ads");
                }else{
                    Toast.makeText(getContext(), "You already bought this.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rate_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
                }
            }
        });

        follow_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://instagram.com/_u/tomasko878");
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                likeIng.setPackage("com.instagram.android");

                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/tomasko878")));
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=com.tosiapps.melodymusic";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.download_melodie));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
            }
        });

        delete_playlists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TinyDB tinyDB = new TinyDB(getContext());
                playlists = tinyDB.getListString("playlists");
                playlists_songs = tinyDB.getListString("playlists_songs");
                for (int k = 0; k < playlists.size();){
                    playlists.remove(k);
                }
                for (int k = 0; k < playlists_songs.size();){
                    playlists_songs.remove(k);
                }
                tinyDB.putListString("playlists", playlists);
                tinyDB.putListString("playlists_songs", playlists_songs);
                Toast.makeText(getActivity(), getString(R.string.playlists_successfully_deleted), Toast.LENGTH_SHORT).show();
            }
        });

        return rootview;
    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        tinydb.putBoolean("no_ads", true);
        Toast.makeText(getContext(), getString(R.string.transaction_successful), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
        Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
