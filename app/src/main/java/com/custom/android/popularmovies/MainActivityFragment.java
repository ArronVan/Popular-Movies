package com.custom.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by duele on 10/7/2016.
 */

public class MainActivityFragment extends Fragment {

    private MoviePosterAdapter posterAdapter;
    private static JSONArray moviesArray = null;
    private static int pageNum = 0;
    private static String currentSort = null;

    public MainActivityFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        posterAdapter = new MoviePosterAdapter(getActivity(), new ArrayList<MoviePoster>());

        // to ensure that page does not reset to 1 when screen is rotated
        if (pageNum <= 0) {
            pageNum = 1;
        }

        // create onclick listeners to previous and next buttons
        rootView.findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterAdapter.clear();
                pageNum++;
                ((TextView)rootView.findViewById(R.id.page_header)).setText(getResources().getString(R.string.page_num) + " " + pageNum);
                new ApiCall().execute(pageNum);
            }
        });
        rootView.findViewById(R.id.previous_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageNum > 1) {
                    posterAdapter.clear();
                    pageNum--;
                    ((TextView)rootView.findViewById(R.id.page_header)).setText(getResources().getString(R.string.page_num) + " " + pageNum);
                    new ApiCall().execute(pageNum);
                }
            }
        });

        // show current page number to screen
        ((TextView)rootView.findViewById(R.id.page_header)).setText(getResources().getString(R.string.page_num) + " " + pageNum);

        // get reference to listview and attach adapter
        GridView gridView = (GridView) rootView.findViewById(R.id.listview_poster);
        gridView.setAdapter(posterAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // get information from jsonarray using position
                try {
                    JSONObject jsonObject = moviesArray.getJSONObject(i);
                    ArrayList<String> movieData = new ArrayList<>();
                    movieData.add(jsonObject.getString("original_title"));
                    movieData.add(jsonObject.getString("poster_path"));
                    movieData.add(jsonObject.getString("overview"));
                    movieData.add(jsonObject.getString("vote_average"));
                    movieData.add(jsonObject.getString("release_date"));

                    Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                    intent.putStringArrayListExtra(Intent.EXTRA_TEXT, movieData);
                    startActivity(intent);
                }
                catch (JSONException e){
                    Log.e("Error in json array", e.getMessage(), e);
                }
            }
        });

        currentSort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));

        reloadPosters();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // only reload and reset pages if user changes sort type
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular))
                .equals(currentSort)) {
            currentSort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
            pageNum = 1;
            reloadPosters();
        }
    }

    public void reloadPosters(){
        posterAdapter.clear();
        new ApiCall().execute(pageNum); // value passed in is what page number
    }

    public class ApiCall extends AsyncTask<Integer, Void, String>{

        @Override
        protected String doInBackground(Integer... ints) {

            // check if there is connection
            if (!checkConnection()){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            try {
                URL url = null;

                String order = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));

                // check preferences to see if setting wants sort by popular or top rated
                if (order.equals(getString(R.string.pref_sort_popular))) {
                    url = new URL("https://api.themoviedb.org/3/movie/popular?api_key=" + getResources().getString(R.string.api_key) + "&language=en-US&page=" + ints[0]);
                }
                else if (order.equals(getString(R.string.pref_sort_rate))){
                    url = new URL("https://api.themoviedb.org/3/movie/top_rated?api_key=" + getResources().getString(R.string.api_key) + "&language=en-US&page=" + ints[0]);
                }
                else{
                    Log.e("Incorrect sort option", "Error");
                    return null;
                }

                // create request to database and open connection
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // read input stream into a string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0){
                    return null;
                }

                return buffer.toString();
            }
            catch (MalformedURLException e){
                Log.e("Wrong URL Error", e.getMessage(), e);
            }
            catch (IOException e){
                Log.e("URL not found", e.getMessage(), e);
            }
            finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null){
                    try{
                        reader.close();
                    }
                    catch (IOException e){
                        Log.e("Error closing stream", e.getMessage(), e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null){
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.connect_error), duration);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
            else{
                // converts outputted string into json object
                JSONObject jsonObject = convertStringToJson(s);

                try {
                    // parse movies into a json array
                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    moviesArray = jsonArray;

                    for (int i=0; i<jsonArray.length(); i++){
                        posterAdapter.add(new MoviePoster(jsonArray.getJSONObject(i).getString("poster_path")));
                    }
                }
                catch (JSONException e){
                    Log.e("Error getting movies", e.getMessage(), e);
                }

            }

        }

        // this method extracts information from json string and stores them in a json object
        public JSONObject convertStringToJson(String jsonString){
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                return jsonObject;
            }
            catch (JSONException e){
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        public boolean checkConnection() {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

    }

}
