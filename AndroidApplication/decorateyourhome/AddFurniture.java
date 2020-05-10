package com.example.decorateyourhome;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

import org.bytedeco.librealsense.context;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_cudaimgproc.CannyEdgeDetector;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
//import org.opencv.core.Mat;


import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_stitching.Stitcher;
import org.opencv.core.Range;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.TrainData;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.opencv_stitching.Stitcher.PANORAMA;

public class AddFurniture extends AppCompatActivity {
    ImageView myImage;
    Button button1;
    private static final int PICK_IMAGE=100;
    final int REQUEST_EXTERNAL_STORAGE = 200;
    DragRectView view;
    Uri imageUri;
    boolean STITCHING_OK=false;
    File WorkingDirectory;
    File roomPath;
    String SroomPath;
    String fur_black_background;
    String fur_mask;

    File roi,output_final;
    int top_left_x,top_left_y,bottom_right_x,bottom_right_y;
    int rect_height,rect_width,img_height,img_width,layout_hight,layout_width;
    org.bytedeco.opencv.opencv_core.Rect rectCrop ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.furniture_add);

        /*We are making a directory to save the result images in the cache*/
        File sd = getCacheDir();
        WorkingDirectory = new File(sd, "/DecorateYourHome/");
        if (!WorkingDirectory.exists()) {
            if (!WorkingDirectory.mkdir()) {
                Log.e("ERROR", "Cannot create a directory!");
            } else {
                WorkingDirectory.mkdirs();
            }
        }

        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(rect_width>0&&rect_width>0) {
                    if (ActivityCompat.checkSelfPermission(AddFurniture.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AddFurniture.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
//                    return;
                    } else {
                        openGallery();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "please select any area in the image",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(AddFurniture.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddFurniture.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
//                    return;
                } else {
                    launchGalleryIntent();
                }
            }
        });
        view = (DragRectView) findViewById(R.id.dragRect);
        myImage = (ImageView) findViewById(R.id.imageView);
        final RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.layout);
        ViewTreeObserver viewTreeObserver = relativeLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                relativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                layout_width  = relativeLayout.getMeasuredWidth();
                layout_hight = relativeLayout.getMeasuredHeight();

            }
        });
        if (null != view) {
            view.setOnUpCallback(new DragRectView.OnUpCallback() {
                @Override
                public void onRectFinished(final Rect rect) {
                    //rectPoints[0][0]=  rect.left;

                    top_left_x=(int)(((double)rect.left/(double)layout_width)*img_width);
                    top_left_y=(int)(((double)rect.top/(double)layout_hight)*img_height);
                    bottom_right_x=rect.right;
                    bottom_right_y=rect.bottom;
                    //int h = Math.abs(img_height-)

                    rect_height = (int)(((double)rect.height()/(double)layout_hight)*img_height);
                    rect_width =(int)(((double) rect.width()/(double)layout_width)*img_width);
/*
                    top_left_x=(int)(((double)rect.left/(double)1090)*img_width);
                    top_left_y=(int)(((double)rect.top/(double)700)*img_height);
                    bottom_right_x=rect.right;
                    bottom_right_y=rect.bottom;
                    //int h = Math.abs(img_height-)

                    rect_height = (int)(((double)rect.height()/(double)530)*img_height);
                    rect_width =(int)(((double) rect.width()/(double)1000)*img_width);
                    */


                    Log.i("LAYOUT WIDTH","width : "+layout_width +" height : "+ layout_hight);

                    rectCrop= new org.bytedeco.opencv.opencv_core.Rect(top_left_x,top_left_y,rect_width,rect_height);

                    Toast.makeText(getApplicationContext(), "Rect is (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom + ")",
                            Toast.LENGTH_LONG).show();

                }
            });
            Log.i("RECTANGLE", "Rect is ( : left " + top_left_x + ", top: " + top_left_y + ", right : " + bottom_right_x+ ",bottom: " + bottom_right_y + ")");
        }
        //myImage.setOnTouchListener(AddFurniture.this);
    }
    /*This function is to get one image from the gallery */
    private void openGallery(){
        Intent gallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery,PICK_IMAGE);
    }
    /*This function is to get one multiple images from the gallery */
    public void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    launchGalleryIntent();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //if the user pressed select room images
        if (requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK) {

            final List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();

            if (clipData != null) {

                MatVector imgs = new MatVector();
                Mat pano = new Mat();
                Mat img = null;
                //String result_name = "drawable://" + R.drawable.result;

                //multiple images selecetd
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    Log.d("URI", imageUri.toString());
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    Log.d("RESULT", "path os passed");
                    String spath = imageUri.toString() ;// "file:///mnt/sdcard/FileName.mp3"
                    String path = getPath(getApplicationContext(), imageUri);
                    //Log.i("First Path", path);
                    //Log.i("SECOND", spath);
                    //img = imread(imageUri.getPath());
                    img = imread(path);
                    Log.d("READ", "message is read");
                    imgs.push_back(img);
                }

                roomPath = new File(WorkingDirectory,"roomPic.jpg");
                String result_name = roomPath.toString();
                SroomPath=roomPath.toString();
                /*The following commented method is a method to get the path of an image in the drawable directory*/
                /*
                Log.i("Before", "Before is OK");
                Uri result = Uri.parse("drawable://" + R.drawable.result);
                String second = "result.jpg";
                File AbsPath= getAbsoluteFile(second,AddFurniture.this);
                String result_name = AbsPath.toString();
                Log.i("ABSOLUTE", result_name);
                */
                Stitcher stitcher = Stitcher.create(PANORAMA);
                int status = stitcher.stitch(imgs, pano);
                if (status != Stitcher.OK) {
                    //System.out.println("Can't stitch images, error code = " + status);
                    Log.i("TAG", "Can't stitch images, error code = " + status);
                } else {
                    // then stitching is ok

                    Log.i("TAG", "OK");
                    imwrite(SroomPath, pano);
                    //File imgFile = new File(result_name);
                    //Log.i("imgfile", "img file is done");
                    if (roomPath.exists()) {
                        Log.i("Exist", "img exists");
                        Bitmap myBitmap = BitmapFactory.decodeFile(roomPath.getAbsolutePath());
                        myImage.setImageBitmap(myBitmap);
                        STITCHING_OK=true;
                        img_width=myBitmap.getWidth();
                        img_height=myBitmap.getHeight();

                    Log.i("IMAGE WIDTH","width : "+img_width +" height : "+ img_height);
                        Toast.makeText(this, "select your area", Toast.LENGTH_LONG).show();
                        //final DragRectView view = (DragRectView) findViewById(R.id.dragRect);
                    }
                    else{
                        Toast.makeText(this, "no result picture", Toast.LENGTH_LONG).show();
                    }

                }

            }
            else{
                    //single image selected
                   // ImageView myImage = (ImageView) findViewById(R.id.imageView);
                    Uri imageUri = data.getData();
                    Log.d("URI", imageUri.toString());
                    try {

                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //bitmaps.add(bitmap);
                        myImage.setImageURI(imageUri);
                        STITCHING_OK=true;
                        SroomPath = getPath(getApplicationContext(), imageUri);
                        String spath = imageUri.toString() ;// "file:///mnt/sdcard/FileName.mp3"
                       // String path = getPath(getApplicationContext(), imageUri);
                        Log.i("First Path", SroomPath);
                        Log.i("SECOND", spath);
                        img_width=bitmap.getWidth();
                        img_height=bitmap.getHeight();
                        // roomPath=new File(imageUri.get());
                        //roomPath=path.parse

                        Log.i("Furn Path", "Not furn area");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

        }
        // to add furniture image
        else if ( resultCode == RESULT_OK) {
            Log.i("FURNITURE", "Furniture Area is entered ");
            final ImageView imageView = findViewById(R.id.image_view);
            imageUri = data.getData();
            final InputStream imageStream;
          /*   try {

                imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageURI(imageUri);
*/
                //String spath = imageUri.toString();// "file:///mnt/sdcard/FileName.mp3"
                String path = getPath(getApplicationContext(), imageUri);
                Log.i("Furn Path", path);
                //Log.i("Furn SECOND", spath);
                //furniture_Analysis(path);

                AddFurniture(SroomPath, path);
              /*  if (output_final.exists()) {
                    Log.i("Exist", "img exists");
                    Bitmap myBitmap = BitmapFactory.decodeFile(output_final.getAbsolutePath());
                    //Bitmap myBitmap = BitmapFactory.decodeFile(f_black_background.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    //STITCHING_OK = true;
                    Toast.makeText(this, "select your area", Toast.LENGTH_LONG).show();
                    //final DragRectView view = (DragRectView) findViewById(R.id.dragRect);
                }*/

                if (roi.exists()) {
                    Log.i("Exist", "img exists");
                    Bitmap myBitmap = BitmapFactory.decodeFile(roi.getAbsolutePath());
                    //Bitmap myBitmap = BitmapFactory.decodeFile(f_black_background.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    //STITCHING_OK = true;
                    Toast.makeText(this, "select your area", Toast.LENGTH_LONG).show();
                    //final DragRectView view = (DragRectView) findViewById(R.id.dragRect);
                }

           /* } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/

        }
    }



    public void AddFurniture(String room_path,String imPath){
        //reading the original furniture image given from the user
        Mat f_img = new Mat();
        f_img = imread(imPath);
        Mat f_img_cpy=f_img.clone();
        Mat gray= new Mat();
        opencv_imgproc.cvtColor(f_img_cpy,gray,Imgproc.COLOR_BGR2GRAY);
        Mat blackAndWhite=new Mat();
        opencv_imgproc.threshold(gray,blackAndWhite,200,255,opencv_imgproc.THRESH_BINARY);

        Mat canny=new Mat();
        opencv_imgproc.Canny(blackAndWhite, canny, 20, 170);
        Mat hierarchy=new Mat();
        MatVector contours=new MatVector();
        opencv_imgproc.findContours(canny,contours,hierarchy,opencv_imgproc.RETR_EXTERNAL,opencv_imgproc.CHAIN_APPROX_NONE);
        Random r = new Random();
        for (int i=0;i< contours.size();i++){
            Scalar black = Scalar.BLACK ;//there may be a problem he
            opencv_imgproc.drawContours(f_img_cpy,contours,opencv_imgproc.FILLED,black);

        }
        Log.i("BEFORE CONVERSION", "The Following area is dangerous");
        Mat gray_img=new Mat();
        opencv_imgproc.cvtColor(f_img_cpy,gray_img,opencv_imgproc.COLOR_BGR2GRAY);

        Mat furniture_mask=new Mat();
        opencv_imgproc.threshold(gray_img,furniture_mask,200,255,opencv_imgproc.THRESH_BINARY);
        Log.i("BEFORE CONVERSION", "The Following area is dangerous");
        Mat notBlackWhite=new Mat();

        opencv_core.bitwise_not(furniture_mask,notBlackWhite);
        Mat furniture_with_black_back = new Mat();
        opencv_core.bitwise_and(f_img,f_img,furniture_with_black_back,notBlackWhite);

        File f_mask = new File(WorkingDirectory,"furniture_mask.png");
        fur_mask = f_mask.toString();
        File f_black_background = new File(WorkingDirectory,"furniture_with_black_background.png");
        fur_black_background= f_black_background.toString();
        imwrite(fur_mask, furniture_mask);
        imwrite(fur_black_background,furniture_with_black_back);


        //furniture_Analysis(furniture_path);
        Mat roomImg= new Mat();
        roomImg=imread(room_path);
        Mat roomImg_cpy=roomImg.clone();
        //roomImg_cpy = roomImg.adjustROI(top_left_y,bottom_right_y,top_left_x,bottom_right_x);
       /* Log.i( "MATRIX WIDTH" , "width is  : "+roomImg_cpy.arrayWidth()+"height is"+roomImg_cpy.arrayHeight() )    ;
        Log.i("DOWN RECTANGLE", "Rect is ( : left " + top_left_x + ", top: " + top_left_y + ", right : " + bottom_right_x+ ",bottom: " + bottom_right_y + ")");
        Log.i("CROPPED", "height : "+ rectCrop.height()+"width :" + rectCrop.width());*/

        Mat cropped=new Mat(roomImg_cpy,rectCrop);
        /*
        Log.i("HABBBBD", "height : "+ cropped.arrayHeight()+"width :" + cropped.arrayWidth());
        Log.i("CROPPEDImg", "height : "+ cropped.arrayHeight()+"width :" + cropped.arrayWidth());*/
        //cropped.adjustROI(top_left_y,bottom_right_y,top_left_x,bottom_right_x);
        //cropped.apply(rectCrop);
        //cropped.locateROI(rectCrop.size(),rectCrop.tl());
        /****************************
        roi = new File(WorkingDirectory,"roi.jpg");
        String roi_p= roi.toString();
        imwrite(roi_p,cropped);
*****************************/
        //Size rectSize = new Size(rect_width,rect_height);
        //Size rectSize = new Size(cropped.size());
        Size rectSize= new Size(cropped.arrayWidth(),cropped.arrayHeight());
        //Log.i("SIZE", "height : "+ rectSize);
        Log.i("RESIZE", "mat is resized : height "+ rectSize.height()+"mat is resized : width "+ rectSize.width());
        Mat resized_f_mask =new Mat();
        opencv_imgproc.resize(furniture_mask,resized_f_mask,rectSize);
        Log.i("CROPPED SIZE", "mat is resized : height "+ cropped.arrayHeight()+"mat is resized : width "+ cropped.arrayWidth());


        Log.i("MASK SIZE", "mat is resized : height "+ resized_f_mask.arrayHeight()+"mat is resized : width "+ resized_f_mask.arrayWidth());

        Mat out = new Mat(rectSize);
       /* if(cropped.size()==resized_f_mask.size()){
            Log.i("RESIZE222", "mat is resized : height "+ cropped.arrayHeight()+"mat is resized : width "+ cropped.arrayWidth());
        }*/


        opencv_core.bitwise_and(cropped,cropped,out,resized_f_mask);
        opencv_imgproc.resize(furniture_with_black_back,furniture_with_black_back,rectSize);


        Mat out2=new Mat();
        opencv_core.bitwise_or(furniture_with_black_back,out,out2);
        //roomImg.put()
        //Mat output_image=new Mat()
        out2.copyTo(roomImg_cpy.apply(rectCrop));
        roi = new File(WorkingDirectory,"roi.jpg");
        String roi_p= roi.toString();
        imwrite(roi_p,roomImg_cpy);

        output_final = new File(WorkingDirectory,"output.jpg");
        String final_out= output_final.toString();
        imwrite(final_out,roomImg_cpy);
    }


    //this function is to mask the image of furniture
    public void furniture_Analysis(String imPath){

        //reading the original furniture image given from the user
        Mat f_img = new Mat();
        f_img = imread(imPath);
        Mat f_img_cpy=f_img.clone();
        Mat gray= new Mat();
        opencv_imgproc.cvtColor(f_img_cpy,gray,Imgproc.COLOR_BGR2GRAY);
        Mat blackAndWhite=new Mat();
        opencv_imgproc.threshold(gray,blackAndWhite,200,255,opencv_imgproc.THRESH_BINARY);

        Mat canny=new Mat();
        opencv_imgproc.Canny(blackAndWhite, canny, 20, 170);
        Mat hierarchy=new Mat();
        MatVector contours=new MatVector();
        opencv_imgproc.findContours(canny,contours,hierarchy,opencv_imgproc.RETR_EXTERNAL,opencv_imgproc.CHAIN_APPROX_NONE);
        Random r = new Random();
        for (int i=0;i< contours.size();i++){
            Scalar black = Scalar.BLACK ;//there may be a problem he
            opencv_imgproc.drawContours(f_img_cpy,contours,opencv_imgproc.FILLED,black);

        }
        Log.i("BEFORE CONVERSION", "The Following area is dangerous");
        Mat gray_img=new Mat();
        opencv_imgproc.cvtColor(f_img_cpy,gray_img,opencv_imgproc.COLOR_BGR2GRAY);

        Mat blackWhite=new Mat();
        opencv_imgproc.threshold(gray_img,blackWhite,200,255,opencv_imgproc.THRESH_BINARY);
        Log.i("BEFORE CONVERSION", "The Following area is dangerous");
        Mat notBlackWhite=new Mat();

        opencv_core.bitwise_not(blackWhite,notBlackWhite);
        Mat furniture_with_black_back = new Mat();
        opencv_core.bitwise_and(f_img,f_img,furniture_with_black_back,notBlackWhite);

        File f_mask = new File(WorkingDirectory,"furniture_mask.png");
        fur_mask = f_mask.toString();
/*
        Uri furniture_mask = Uri.parse("drawable://" + R.drawable.furniture_mask);
        String im1_name = "furniture_mask.png";
        File AbsPath1= getAbsoluteFile(im1_name,AddFurniture.this);
        String f_mask_path = AbsPath1.toString();
        Log.i("ABSOLUTE", f_mask_path);
*/
        File f_black_background = new File(WorkingDirectory,"furniture_with_black_background.png");
        fur_black_background= f_black_background.toString();
/*
        Uri furniture_with_background = Uri.parse("drawable://" + R.drawable.furniture_with_black_background);
        String im2_name = "furniture_with_black_background.png";
        File AbsPath2= getAbsoluteFile(im2_name,AddFurniture.this);
        String f_with_black_background_path = AbsPath1.toString();
        Log.i("ABSOLUTE", f_mask_path);
*/
        imwrite(fur_mask, blackWhite);
        imwrite(fur_black_background,furniture_with_black_back);
    }




    public File getAbsoluteFile(String relativePath, Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), relativePath);
        } else {
            return new File(context.getFilesDir(), relativePath);
        }
    }
    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*This function is a trash but i need it in future*/
    public void furnitureAnalysis(String imPath){

        //reading the original furniture image given from the user
        Mat f_img = new Mat();
        f_img = imread(imPath);
        Mat f_img_cpy=f_img.clone();
        Mat gray= new Mat();
        opencv_imgproc.cvtColor(f_img_cpy,gray,Imgproc.COLOR_BGR2GRAY);
        Mat blackAndWhite=new Mat();
        opencv_imgproc.threshold(gray,blackAndWhite,200,255,opencv_imgproc.THRESH_BINARY);
        Mat canny=new Mat();
        opencv_imgproc.Canny(blackAndWhite, canny, 20, 170);
        Mat hierarchy=new Mat();
        MatVector contours=new MatVector();
        opencv_imgproc.findContours(canny,contours,hierarchy,opencv_imgproc.RETR_EXTERNAL,opencv_imgproc.CHAIN_APPROX_NONE);
        Random r = new Random();
        for (int i=0;i< contours.size();i++){
            Scalar black = Scalar.BLACK ;//there may be a problem he
            //Scala black = Scalar.BLACK ;//there may be a problem here
            //Imgproc.drawContours(f_img_cpy, contours, opencv_imgproc.FILLED,black, -1);
            //opencv_imgproc.drawContours(f_img_cpy, contours,opencv_imgproc.FILLED,black, 3);
            //opencv_imgproc.drawContours();
            opencv_imgproc.drawContours(f_img_cpy,contours,opencv_imgproc.FILLED,black);

        }
        Log.i("BEFORE CONVERSION", "The Following area is dangerous");
        Mat gray_img=new Mat();
        opencv_imgproc.cvtColor(f_img_cpy,gray_img,opencv_imgproc.COLOR_BGR2GRAY);

        Mat blackWhite=new Mat();
        opencv_imgproc.threshold(gray_img,blackWhite,200,255,opencv_imgproc.THRESH_BINARY);
        Log.i("BEFORE CONVERSION", "The Following area is dangerous");
        // Mat notBlackWhite=new Mat();
       /* OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();

        org.opencv.core.Mat CVnotBlackWhite=new org.opencv.core.Mat();
        // org.opencv.core.Mat CVnotBlackWhite = converter2.convert(converter1.convert(notBlackWhite));
        org.opencv.core.Mat CVblackWhite = converter2.convert(converter1.convert(blackWhite));*/
/*
        //Mat mat2 = converter2.convert(converter1.convert());
        Core.bitwise_not(CVblackWhite,CVnotBlackWhite);

        //notBlackWhite=converter1.convert(converter2.convert(CVnotBlackWhite));
        org.opencv.core.Mat CVfurniture_with_black_back=new org.opencv.core.Mat();
        org.opencv.core.Mat CVoriginalImg = converter2.convert(converter1.convert(f_img));
        Core.bitwise_and(CVoriginalImg,CVoriginalImg,CVfurniture_with_black_back,CVnotBlackWhite);

        Mat furniture_with_black_back=converter1.convert(converter2.convert(CVfurniture_with_black_back));

        Uri furniture_mask = Uri.parse("drawable://" + R.drawable.furniture_mask);
        String im1_name = "furniture_mask.png";
        File AbsPath1= getAbsoluteFile(im1_name,AddFurniture.this);
        String f_mask_path = AbsPath1.toString();
        Log.i("ABSOLUTE", f_mask_path);

        Uri furniture_with_background = Uri.parse("drawable://" + R.drawable.furniture_with_black_background);
        String im2_name = "furniture_with_black_background.png";
        File AbsPath2= getAbsoluteFile(im2_name,AddFurniture.this);
        String f_with_black_background_path = AbsPath1.toString();
        Log.i("ABSOLUTE", f_mask_path);
        imwrite(f_mask_path,furniture_with_black_back);
        imwrite(f_with_black_background_path,blackWhite);
*/
    }


}
