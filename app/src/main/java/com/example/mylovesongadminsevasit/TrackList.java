package com.example.mylovesongadminsevasit;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.mylovesongadminsevasit.model.TrackClass;

import java.io.IOException;
import java.util.List;

public class TrackList extends ArrayAdapter<TrackClass> {
    private Activity context;
    List<TrackClass> tracks ;
    MediaPlayer mediaPlayer;
    public TrackList(Activity context, List<TrackClass> tracks) {
        super(context, R.layout.layout_track_list, tracks);
        this.context = context;
        this.tracks = tracks;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_track_list, null, true);


        TextView textViewTrackName = (TextView) listViewItem.findViewById(R.id.txttrackname);
        Button buttonPlay = (Button) listViewItem.findViewById(R.id.btnPlaySound);
        Button buttonPause = (Button) listViewItem.findViewById(R.id.btnPauseSound);

        TrackClass track = tracks.get(position);


        textViewTrackName.setText("ชื่อไฟล์เพลง : " + track.getName());


        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(position);
            }
        });
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
        return listViewItem;
    }
    public void play(int position){


        TrackClass track = tracks.get(position);


        mediaPlayer = new MediaPlayer();
        try {


            mediaPlayer.setDataSource(track.getUrl());


            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void pause()
    {
        mediaPlayer.pause();
    }

}
