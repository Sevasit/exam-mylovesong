package com.example.mylovesongadminsevasit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mylovesongadminsevasit.model.Artist;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.collection.BuildConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    Logger logger = Logger.getLogger(MainActivity.class.getName());
    EditText artistEditText;
    EditText historyEditText;
    Spinner genreSpinner;

    private int GALLERY = 1;
    private int CAMERA = 2;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private String pictureFilePath;
    private Uri filePath;
    private StorageReference storageReference;
    //uri to keep firebase image address to firebase database
    private Uri downloadUrl;
    private ProgressDialog progressDialog;
    ImageView imageView;
    DatabaseReference artistsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find your EditText and Spinner views by their IDs

        artistEditText = findViewById(R.id.artiest);
        historyEditText = findViewById(R.id.hisOfArtiest);
        genreSpinner = findViewById(R.id.genre);
        imageView = findViewById(R.id.selectedImage);

        storageReference = FirebaseStorage.getInstance().getReference();

        // Find your buttons by their IDs
        Button saveButton = findViewById(R.id.submit);
        Button clearButton = findViewById(R.id.cancel);

        // Initialize DatabaseReference
        artistsRef = FirebaseDatabase.getInstance().getReference().child("artists");

        // Set click listeners for the buttons
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFields();
            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ShowArtist.class);
        startActivity(intent);
        finish();
    }



    public void showPictureDialog(View view) {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                                    sendTakePictureIntent();
                                }
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void sendTakePictureIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,AUTHORITY,pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA);
            }
        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "picture_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String filename;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;        }
        if (requestCode == GALLERY) {
            if (data != null) {
                filePath= data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            File imgFile = new  File(pictureFilePath);
            if(imgFile.exists())            {
                filePath = Uri.fromFile(imgFile);
                imageView.setImageURI(Uri.fromFile(imgFile));   }
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (filePath == null) {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_LONG).show();
            return;
        } else {
            String artistName = artistEditText.getText().toString().trim();
            String artistHistory = historyEditText.getText().toString().trim();
            String artistGenre = genreSpinner.getSelectedItem().toString().trim();
            String artistId = artistsRef.push().getKey();

            if (artistName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter artist name", Toast.LENGTH_LONG).show();
                return;
            } else if (artistHistory.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter artist history", Toast.LENGTH_LONG).show();
                return;
            } else if (artistGenre.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter artist genre", Toast.LENGTH_LONG).show();
                return;
            }

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            final StorageReference sRef = storageReference.child("images/" + +System.currentTimeMillis() + "." + getFileExtension(filePath));

            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri;
                                    String urinew = downloadUrl.toString();
                                    //dismissing the progress dialog
                                    progressDialog.dismiss();
                                    //displaying success toast
                                    Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                        Artist book = new Artist(artistId, artistName, artistHistory, artistGenre, urinew);
                                        //Saving the Book
                                        artistsRef.child(artistId).setValue(book);
                                        Toast.makeText(getApplicationContext(), "Data Inserted Successfully into Firebase Database", Toast.LENGTH_LONG).show();
                                        //clear fields
                                        clearFields();
                                        Intent intent = new Intent(getApplicationContext(), ShowArtist.class);
                                        startActivity(intent);
                                        finish();
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


    private void clearFields() {
        try {
            artistEditText.getText().clear();
            historyEditText.getText().clear();
            genreSpinner.setSelection(0);
            imageView.setImageResource(R.drawable.upload);
            Intent intent = new Intent(this, ShowArtist.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
}
