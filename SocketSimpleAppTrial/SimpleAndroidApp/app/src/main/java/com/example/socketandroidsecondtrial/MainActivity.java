package com.example.socketandroidsecondtrial;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private static final int PICK_IMAGE=100;
    final int REQUEST_EXTERNAL_STORAGE = 200;
    Uri imageUri;
    InputStream imageStream;
    String ipAddress="192.168.56.1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=(ImageView)findViewById(R.id.imageView);


    }
    public void shutdown(View v)  {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
//                    return;
        } else {
            openGallery();
        }
        Client client=new Client("shutdown",imageUri);
        client.execute();
/*

        try {
            Socket  client = new Socket(ipAddress,9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
            /*OutputStream toServer=client.getOutputStream();
            DataOutputStream out = new DataOutputStream(toServer);
            //final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            File file = new File(String.valueOf(imageUri));
            FileInputStream input = new FileInputStream(file);
            int size = input.available();
            byte[] data = new byte[size];
            input.read(data);
            out.writeInt(size);
            out.write(data);
            out.flush();
            out.close();
            //input.close();*/



        Log.i("TAG","shutdown button is pressed");
    }
    public void restart(View v){
      /*  Client client=new Client("restart",imageUri);
        client.execute();*/
    }
    public void music(View v){
       /* Client client=new Client("music",imageUri);
        client.execute();*/
    }
    private void openGallery(){

        @SuppressLint("IntentReset")
        Intent gallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery,PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK) {

            imageUri = data.getData();

            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageURI(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //imageView.setImageURI(imageUri);

        }

    }

}