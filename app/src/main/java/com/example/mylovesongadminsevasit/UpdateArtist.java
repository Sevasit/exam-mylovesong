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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

public class UpdateArtist extends AppCompatActivity {

    EditText artistEditText;
    EditText historyEditText;
    TextView typeEditText;
    Spinner genreSpinner;
    ImageView imageView;
    String idR, nameR, historyR, genreR, urlR;

    private int GALLERY = 1;
    private int CAMERA = 2;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private String pictureFilePath;
    private Uri filePath;
    //uri to keep firebase image address to firebase database
    private Uri downloadUrl;
    private ProgressDialog progressDialog;
    String artistName, artistId, artistType , artHistory;


    //1.ประกาศตัวแปรเพื่อเชื่อมต่อ Project Firebase
    public FirebaseDatabase firebaseDatabase;
    //2.ประกาศตัวแปรเพื่อเชื่อมต่อ Root Node (node พ่อ)
    public DatabaseReference rootDbRef;
    //3.ประกาศตัวแปรเพื่อเชื่อมต่อ Child Node (node ลูก)
    public DatabaseReference subrootDbRef_user;
    //4. ประกาศเพื่อเชื่อมต่อที่เก็บ รูป Storage
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_artist);

        typeEditText = findViewById(R.id.textType);
        artistEditText = findViewById(R.id.artiest);
        historyEditText = findViewById(R.id.hisOfArtiest);
        genreSpinner = findViewById(R.id.genre);
        imageView = findViewById(R.id.selectedImage);

        //Receive data click to Show old data
        Intent intent = getIntent();
        idR = intent.getStringExtra("artId");
        nameR = intent.getStringExtra("artName");
        genreR = intent.getStringExtra("artGenre");
        historyR = intent.getStringExtra("artHistory");
        urlR = intent.getStringExtra("url");

        //Show data receive from listviewclick
        artistEditText.setText(nameR);
        historyEditText.setText(historyR);
        typeEditText.setText("*ประเภทศิลปิน : " + genreR + "*");

        Glide.with(this)
                .load(urlR)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView);

        //เชื่อมต่อ Project Firebase โดยใช้ตัวแปรที่ประกาศไว้
        firebaseDatabase = FirebaseDatabase.getInstance();
        //เชื่อมต่อ root node โดยใช้ตัวที่ประกาศไว้
        rootDbRef = firebaseDatabase.getReference();
        //เชื่อมต่อ child node โดยใช้ตัวที่ประกาศไว้
        subrootDbRef_user = rootDbRef.child("artists");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ShowArtist.class);
        startActivity(intent);
        finish();
    }

    private void uploadFile() {
        //checking if file is available
        if (filePath != null) { //มีรูป
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            //getting the storage reference
            final StorageReference sRef = storageReference.child("images/" + +System.currentTimeMillis() + "." + getFileExtension(filePath));
//adding the file to reference
            sRef.putFile(filePath)
//เก็บไฟล์รูปสำเร็จเก็บข้อมูลลงฐานข้อมูล
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
                                    Toast.makeText(getApplicationContext(), "แก้ไขเรียบร้อย ", Toast.LENGTH_LONG).show();

                                    artistName = artistEditText.getText().toString();
                                    artistType = genreSpinner.getSelectedItem().toString();
                                    artHistory = historyEditText.getText().toString();
                                    //หน้าแก้้ไขที่สำคัญไม่สร้าง Primary Key ใช้ตัวเดิมที่รับค่ามา
                                    artistId = idR; // สร้างรหัส
                                    if (artistName.isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "enter author name", Toast.LENGTH_LONG).show();
                                    } else {
                                        //กรอกครบถ้วนบันทึกลงฐานข้อมูล
                                        //creating Book Object


                                        Artist addartist = new Artist(artistId, artistName,artHistory, artistType, urinew);
                                        subrootDbRef_user.child(artistId).setValue(addartist); //เก็บลงฐานข้อมูล


                                        Toast.makeText(getApplicationContext(), "แก้ไขเรียบร้อย", Toast.LENGTH_LONG).show();


                                        artistEditText.setText(null);
                                        imageView.setImageResource(R.drawable.upload);
                                        Intent intent = new Intent(getApplicationContext(), ShowArtist.class);
                                        startActivity(intent);


                                    }//ปิด if


                                }
                            });
                        }
                    })
//เก็บรูปไม่สำเร็จ
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
//ระหว่างบันทึกรูป
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        } else { // เขาไม่ได้เลือกรูป
            if (urlR.isEmpty()) {
                Toast.makeText(getApplicationContext(), "กรุณาเลือกรูปภาพ", Toast.LENGTH_LONG).show();
            } else {
                artistName = artistEditText.getText().toString();
                artistType = genreSpinner.getSelectedItem().toString();
                artHistory = historyEditText.getText().toString();
                //หน้าแก้้ไขที่สำคัญไม่สร้าง Primary Key ใช้ตัวเดิมที่รับค่ามา
                artistId = idR; // สร้างรหัส
                if (artistName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "enter author name", Toast.LENGTH_LONG).show();
                } else {
                    //กรอกครบถ้วนบันทึกลงฐานข้อมูล
                    //creating Book Object


                    Artist addartist = new Artist(artistId, artistName,artHistory, artistType, urlR);
                    subrootDbRef_user.child(artistId).setValue(addartist); //เก็บลงฐานข้อมูล


                    Toast.makeText(getApplicationContext(), "แก้ไขเรียบร้อย", Toast.LENGTH_LONG).show();


                    artistEditText.setText(null);
                    imageView.setImageResource(R.drawable.upload);
                    Intent intent = new Intent(getApplicationContext(), ShowArtist.class);
                    startActivity(intent);


                }//ปิด if
            }

        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String filename;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                filePath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == CAMERA) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                filePath = Uri.fromFile(imgFile);
                imageView.setImageURI(Uri.fromFile(imgFile));
            }
        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "picture_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    private void sendTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
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
                Uri photoURI = FileProvider.getUriForFile(this, AUTHORITY, pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA);
            }
        }
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    public void showPictureDialog(View view) {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("เลือกรูป");
        String[] pictureDialogItems = {
                "เลือกรูปจากแกลอรี่",
                "ถ่ายรูป"};
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


    public void Addartist(View view) {
        uploadFile();
    }

    public void Cancelartist(View view) {
        artistEditText.setText(null);
        imageView.setImageResource(R.drawable.upload);
    }


}