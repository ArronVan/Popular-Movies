package com.custom.android.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // get string arraylist from extra
        ArrayList<CharSequence> movieDetails = getIntent().getCharSequenceArrayListExtra(Intent.EXTRA_TEXT);

        // set movie title
        TextView textView = (TextView)findViewById(R.id.movie_detail_title);
        textView.setText(movieDetails.get(0));

        // get movie poster
        ImageView posterView = (ImageView)findViewById(R.id.movie_detail_poster);
        Picasso.with(this).load("http://image.tmdb.org/t/p/w780/" + movieDetails.get(1)).into(posterView);

        // get movie synopsis
        textView = (TextView)findViewById(R.id.movie_detail_synopsis);
        textView.setText(getResources().getString(R.string.movie_synopsis) + ":\n" + movieDetails.get(2));

        // get user ratings
        textView = (TextView)findViewById(R.id.movie_detail_rating);
        textView.setText(getResources().getString(R.string.movie_rating) + ": " + movieDetails.get(3));

        // get release date
        textView = (TextView)findViewById(R.id.movie_detail_release_date);
        textView.setText(getResources().getString(R.string.movie_release_date) + ": " + movieDetails.get(4));
    }
}
