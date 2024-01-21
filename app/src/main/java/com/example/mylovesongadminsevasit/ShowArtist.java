package com.example.mylovesongadminsevasit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.mylovesongadminsevasit.model.Artist;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowArtist extends AppCompatActivity {

    public ListView listViewArtists;
    List<Artist> artists ;

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference rootDbRef;
    public DatabaseReference subrootDbRef_artist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_artist);
        listViewArtists = (ListView) findViewById(R.id.listViewArtists);
        artists = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootDbRef = firebaseDatabase.getReference();
        subrootDbRef_artist = rootDbRef.child("artists");

    }

    @Override
    public void onBackPressed() {
        // Show a confirmation dialog for back press
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("คุณต้องการออกหรือไม่?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "ใช่",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If "Yes" is clicked, exit the app
                        finish();
                    }
                });

        builder.setNegativeButton(
                "ไม่",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If "No" is clicked, dismiss the dialog
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void onStart() {
        super.onStart();
        subrootDbRef_artist.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                artists.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String id = postSnapshot.getKey();
                    Artist artistshow = postSnapshot.getValue(Artist.class);
                    artists.add(artistshow);
                }
                Collections.sort(artists, new Comparator<Artist>() {
                    @Override
                    public int compare(Artist artist1, Artist artist2) {
                        // ในที่นี้เราเปรียบเทียบตามชื่อศิลปิน (สมมติว่าชื่ออยู่ใน property name)
                        return artist1.getName().compareTo(artist2.getName());
                    }
                });
                ArtistList placeAdapter = new ArtistList(ShowArtist.this, artists);
                listViewArtists.setAdapter(placeAdapter);
            }
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void openExit(View view) {
        // Show a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("คุณต้องการออกหรือไม่?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "ใช่",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If "Yes" is clicked, exit the app
                        finish();
                    }
                });

        builder.setNegativeButton(
                "ไม่",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If "No" is clicked, dismiss the dialog
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    public void openadd(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}