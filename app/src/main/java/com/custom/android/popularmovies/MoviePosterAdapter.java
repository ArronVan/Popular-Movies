package com.custom.android.popularmovies;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by duele on 10/7/2016.
 */

public class MoviePosterAdapter extends ArrayAdapter<MoviePoster> {

    // default constructor
    public MoviePosterAdapter(Activity context, List<MoviePoster> posters){
        // initialize adapter storage
        super(context, 0, posters);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoviePoster moviePoster = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.poster_item, parent, false);
        }

        ImageView posterView = (ImageView)convertView.findViewById(R.id.poster_item_image);
        Picasso.with(convertView.getContext()).load("http://image.tmdb.org/t/p/w500/" + moviePoster.urlString).into(posterView);

        return convertView;
    }
}
