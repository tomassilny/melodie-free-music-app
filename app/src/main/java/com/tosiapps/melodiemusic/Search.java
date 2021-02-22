package com.tosiapps.melodiemusic;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Search extends AppCompatActivity implements SearchView.OnQueryTextListener {
    String TAG= "Search";
    ListView search_lv;
    android.widget.Filter filter;
    ArrayList<HashMap<String, String>> resultList;
    ArrayList<HashMap<String, String>> result2List;
    ArrayList<String> recent_queries = new ArrayList<>();
    ArrayList<String> list_help = new ArrayList<>();
    ArrayList<String> list_help_name = new ArrayList<>();
    ProgressDialog progress;
    TextView no_recent_tracks;
    SearchView searchView;
    ProgressBar progressBar;
    String film_serial, url = "";
    ArrayAdapter<String> arrayAdapter;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proxima.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        no_recent_tracks = (TextView) findViewById(R.id.no_recent_tracks);

        resultList = new ArrayList<>();
        result2List = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(Search.this, R.layout.lv_item, R.id.list_content);

        tinyDB = new TinyDB(this);
        recent_queries = tinyDB.getListString("recent_queries");
        if (!(recent_queries.size() > 0))
            no_recent_tracks.setVisibility(View.VISIBLE);
        Collections.reverse(recent_queries);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        search_lv = (ListView)findViewById(R.id.listView);
        search_lv.setDivider(null);
        search_lv.setDividerHeight(0);
        search_lv.setTextFilterEnabled(false);

        search_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String nazov = (String) parent.getItemAtPosition(position);
                recent_queries.add(nazov);
                tinyDB.putListString("recent_queries", recent_queries);
                Intent intent = new Intent(Search.this, List.class);
                intent.putExtra("search", nazov);
                startActivity(intent);

            }
        });

        search_lv.setAdapter(new SearchListViewAdapter(getApplicationContext(), recent_queries, true));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.searchmenu, menu);
        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchMenuItem.expandActionView();
        searchView.setQueryHint(getString(R.string.search));

        MenuItemCompat.expandActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        no_recent_tracks.setVisibility(View.INVISIBLE);
        url = "https://wawier.com/melodie/search.php?q=" + newText;
        if(arrayAdapter != null)
            arrayAdapter.clear();

        if (resultList != null)
            resultList.clear();
        new GetEpisodes().execute();
        //filter.filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        recent_queries.add(query);
        tinyDB.putListString("recent_queries", recent_queries);
        Intent intent = new Intent(Search.this, List.class);
        intent.putExtra("search", query);
        startActivity(intent);
        return true;
    }




    @SuppressLint("StaticFieldLeak")
    private class GetEpisodes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            // Showing progress dialog
           /* progress = ProgressDialog.show(getApplicationContext(), "dialog title",
                    "dialog message", true);*/

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
                    JSONArray contacts = jsonObj.getJSONArray("results"); //NAZOV JSON


                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String id = c.getString("query");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();


                        // adding each child node to HashMap key => value
                        contact.put("query", id);


                        // adding contact to contact list
                        resultList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");


            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


            for (int i = 0; i < resultList.size(); i++){
                arrayAdapter.add(resultList.get(i).get("query"));
                String n = resultList.get(i).get("query");
                list_help.add(n);
                list_help_name.add(resultList.get(i).get("query"));
            }
            search_lv.setAdapter(arrayAdapter);
            progressBar.setVisibility(View.INVISIBLE);

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return id == R.id.action_search || super.onOptionsItemSelected(item);

    }

    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}