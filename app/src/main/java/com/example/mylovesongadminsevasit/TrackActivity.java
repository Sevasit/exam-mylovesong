package com.example.mylovesongadminsevasit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylovesongadminsevasit.model.TrackClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class TrackActivity extends AppCompatActivity {
    String artistname, artistid;
    float Rating;
    public Uri downloadUrl;

    public StorageReference audiostorageReference;
    String result = null;
    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference rootDbRef;
    public DatabaseReference trackRef;
    public ProgressDialog progressDialog;

    EditText InputTrack;
    RatingBar Rate;
    TextView ShowRate;
    TextView ShowArtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        InputTrack = findViewById(R.id.edtTrack);
        Rate = findViewById(R.id.ratingBar);
        ShowRate = findViewById(R.id.showRating);
        ShowArtName = findViewById(R.id.txtArtistname);

        audiostorageReference = FirebaseStorage.getInstance().getReference();
//
        Intent intent = getIntent();
        artistname = intent.getStringExtra("artName");
        artistid = intent.getStringExtra("artId");
        ShowArtName.setText("ชื่อศิลปิน : " + artistname);
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootDbRef = firebaseDatabase.getReference();
        trackRef = rootDbRef.child("tracks").child(artistid);

        Rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ShowRate.setText(String.valueOf(v));
                Rating = v;
            }
        });
    }

    public void showAudioFile(View view) {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 101);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null &&
                data.getData() != null) {
            downloadUrl = data.getData();
            String filename = getFileName(downloadUrl);
            InputTrack.setText(filename);
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public String getFileName(Uri uri) {
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }//end try
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    public void choose(View view) {
        showAudioFile(view);
    }

    public void addtrack(View view) {
        if (InputTrack.getText().toString().isEmpty() || downloadUrl == null || Rating == 0.0) {
            Toast.makeText(getApplicationContext(), "Please enter track name, select an audio file, and rate the track", Toast.LENGTH_LONG).show();
        } else {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            final StorageReference storageReference = audiostorageReference.child("audio/" + System.currentTimeMillis() + "." + getFileExtension(downloadUrl));
            storageReference.putFile(downloadUrl)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri;
                                    String uriNew = downloadUrl.toString();
                                    //dismissing the progress dialog
                                    progressDialog.dismiss();
                                    String trackName = InputTrack.getText().toString().trim();
                                    float rating = Rating;


                                    String id = trackRef.push().getKey();
                                    TrackClass track = new TrackClass(id, trackName, rating, uriNew);
                                    trackRef.child(id).setValue(track);
                                    Toast.makeText(getApplicationContext(), "Track saved", Toast.LENGTH_LONG).show();
                                    ShowArtName.setText("");
                                    InputTrack.setText("");
                                    ShowRate.setText("");
                                    Intent openMain = new Intent(TrackActivity.this, ShowArtist.class);
                                    startActivity(openMain);


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
    }


    public void viewtrack(View view) {
        //creating an intent
        Intent intent = new Intent(getApplicationContext(), Showtracks.class);
        //putting artist name and id to intent
        intent.putExtra("ARTIST_ID", artistid);
        intent.putExtra("ARTIST_NAME", artistname);
        //starting the activity with intent
        startActivity(intent);
    }


}