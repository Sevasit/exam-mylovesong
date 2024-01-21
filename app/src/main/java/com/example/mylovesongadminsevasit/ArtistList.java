package com.example.mylovesongadminsevasit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mylovesongadminsevasit.model.Artist;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ArtistList extends ArrayAdapter<Artist> {
    private Activity context;
    List<Artist> artists;
    String id;
    public ArtistList(Activity context, List<Artist> artists) {
        super(context, R.layout.layout_artist_list, artists);
        this.context = context;
        this.artists = artists;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_artist_list, null, true);


        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView textViewGenre = (TextView) listViewItem.findViewById(R.id.textViewGenre);
        ImageView showimg = (ImageView) listViewItem.findViewById(R.id.imgShowArtist);


        final Artist artistshow = artists.get(position);
        id = artistshow.getArtistId(); //เก็บ PK ไว้ตัวแปร id
        textViewName.setText("ชื่อศิลปิน" + " " + artistshow.getName());
        textViewGenre.setText("ประเภท" + " " + artistshow.getGenre());

        Glide.with(context)
                .load(artistshow.getUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(showimg);


        listViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder pictureDialog = new AlertDialog.Builder(context);
                pictureDialog.setTitle("เลือกรายการ");
                String[] pictureDialogItems = {"เเก้ไขข้อมูล", "ลบข้อมูล", "เพลง"};
                        pictureDialog.setItems(pictureDialogItems,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent openUpdate = new Intent(context, UpdateArtist.class);
                                                openUpdate.putExtra("artId",artistshow.getArtistId());
                                                openUpdate.putExtra("artName",artistshow.getName());
                                                openUpdate.putExtra("artHistory",artistshow.getHistory());
                                                openUpdate.putExtra("artGenre",artistshow.getGenre());
                                                openUpdate.putExtra("url",artistshow.getUrl());
                                                context.startActivity(openUpdate);
                                                break;
                                            case 1:
                                                deleteArtistConfirmation(artistshow.getArtistId());
                                                break;
                                            case 2:
                                                break;
                                        }
                                    }
                                });
                pictureDialog.show();


            }
        });


        return listViewItem;
    }

    private boolean deleteArtist(String id) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("artists").child(id);
        dR.removeValue();
        Toast.makeText(getContext(), "ลบศิลปินเรียบร้อย", Toast.LENGTH_LONG).show();
        return true;
    }

    private void deleteArtistConfirmation(final String artistId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("ยืนยันการลบ");
        builder.setMessage("คุณต้องการลบศิลปินนี้ใช่หรือไม่?");

        // Add the buttons
        builder.setPositiveButton("ใช่", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked OK button
                deleteArtist(artistId);
            }
        });
        builder.setNegativeButton("ไม่", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Cancel button
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
