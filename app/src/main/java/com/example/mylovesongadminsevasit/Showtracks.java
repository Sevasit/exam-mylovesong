package com.example.mylovesongadminsevasit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mylovesongadminsevasit.model.TrackClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Showtracks extends AppCompatActivity {

    public ListView listViewTracks;
    public TextView ShowArtist;
    //a list to store all the artist from firebase database
    List<TrackClass> tracks;

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference rootRef, trackRef;
    String artistname,artistid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtracks);

        listViewTracks = (ListView) findViewById(R.id.Tracklist);
        ShowArtist = (TextView) findViewById(R.id.txtShowArtistName);
        tracks = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        trackRef = rootRef.child("tracks");

        //Receive ArtistName and id send from another activity
        Intent intent = getIntent();
        artistname = intent.getStringExtra("ARTIST_NAME");
        artistid = intent.getStringExtra("ARTIST_ID");
        ShowArtist.setText("Artist : "+ artistname);
        trackRef = rootRef.child("tracks").child(artistid);
    }
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        trackRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clearing the previous artist list
                tracks.clear();
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting artist
                    TrackClass track = postSnapshot.getValue( TrackClass.class);
                    //adding artist to the list
                    tracks.add(track);
                }
                //creating adapter
                TrackList trackAdapter = new TrackList(Showtracks. this,tracks);
                //attaching adapter to the listview
                listViewTracks.setAdapter(trackAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}