package com.example.decorateyourhome;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_stitching.Stitcher;

import yuku.ambilwarna.AmbilWarnaDialog;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.opencv_stitching.Stitcher.PANORAMA;

public class AddColor extends AppCompatActivity {
    ImageView myImage;
    Button button1,button2;
    private static final int PICK_IMAGE=100;
    final int REQUEST_EXTERNAL_STORAGE = 200;
    DragRectView view;
    boolean STITCHING_OK=false;
    File WorkingDirectory;
    File roomPath;
    String SroomPath;
    boolean coloringDone=false;

    File roi;
    int top_left_x,top_left_y,bottom_right_x,bottom_right_y;
    int rect_height,rect_width,img_height,img_width,layout_hight,layout_width;
    int DefaultColor;
    org.bytedeco.opencv.opencv_core.Rect rectCrop ;

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_add);

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
    /*
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        SavedImagesFolder = new File(root,"/DecorateYourHome/");
        SavedImagesFolder.mkdirs();
        Log.i("Images Directory : ",SavedImagesFolder.getAbsolutePath());
*/
        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rect_height>0&&rect_width>0){
                    openColorPicker();
                        //colorPicker.show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "please select any area in the image",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ImageView imageView = findViewById(R.id.image_view);
                if(rect_height>0&&rect_width>0){
                    ColorTheRoom(SroomPath);
                    if (roi.exists()) {
                //        Log.i("Exist", "img exists");

                        Bitmap myBitmap = BitmapFactory.decodeFile(roi.getAbsolutePath());
                        imageView.setImageBitmap(myBitmap);
                        coloringDone=true;
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "please select any area in the image",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        Button saveImg=findViewById(R.id.button3);
        saveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(coloringDone){
                    Bitmap myBitmap = BitmapFactory.decodeFile(roi.getAbsolutePath());
                    saveTempBitmap(myBitmap);
                    coloringDone=false;
                }
                else{
                    Toast.makeText(getApplicationContext(), "please select Image and add color",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(AddColor.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddColor.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
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
/*
                    top_left_x=(int)(((double)rect.left/(double)1090)*img_width);
                    top_left_y=(int)(((double)rect.top/(double)1000)*img_height);

*/
                    top_left_x=(int)(((double)rect.left/(double)layout_width)*img_width);
                    top_left_y=(int)(((double)rect.top/(double)layout_hight)*img_height);
                    bottom_right_x=rect.right;
                    bottom_right_y=rect.bottom;
                    //int h = Math.abs(img_height-)

                    rect_height = (int)((((double)rect.height()/(double)layout_hight)*img_height)+8);
                    rect_width =(int)(((double) rect.width()/(double)layout_width)*img_width);

                    //rect_height=(int)Math.abs(bottom_right_x-top_left_x);
                    //rect_height = (int) Math.abs(rect.height()-Math.abs(rect.height()-img_height));
                    //rect_width = (int) Math.abs(rect.width()-Math.abs(rect.width()-img_width));
                    //rect_width =(int)(((double) rect.width()/(double)1070)*img_width);

          //          Log.i("LAYOUT WIDTH","width : "+layout_width +" height : "+ layout_hight);

                    rectCrop= new org.bytedeco.opencv.opencv_core.Rect(top_left_x,top_left_y,rect_width,rect_height);

          //          Toast.makeText(getApplicationContext(), "Rect is (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom + "height :" +rect.height()+"width : "+rect.width()+ ")", Toast.LENGTH_LONG).show();

                }
            });
        //    Log.i("RECTANGLE", "Rect is ( : left " + top_left_x + ", top: " + top_left_y + ", right : " + bottom_right_x+ ",bottom: " + bottom_right_y + ")");
        }
    }
    public void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, DefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                DefaultColor = color;
                //relativeLayout.setBackgroundColor(mDefaultColor);
            }
        });
        colorPicker.show();
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
        //            Log.d("URI", imageUri.toString());
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

         //           Log.d("RESULT", "path os passed");
                    String spath = imageUri.toString() ;// "file:///mnt/sdcard/FileName.mp3"
                    String path = getPath(getApplicationContext(), imageUri);
                    img = imread(path);
         //           Log.d("READ", "message is read");
                    imgs.push_back(img);
                }

                roomPath = new File(WorkingDirectory,"roomPic.jpg");
                String result_name = roomPath.toString();
                SroomPath=roomPath.toString();
                Stitcher stitcher = Stitcher.create(PANORAMA);
                int status = stitcher.stitch(imgs, pano);
                if (status != Stitcher.OK) {
                    //System.out.println("Can't stitch images, error code = " + status);
         //           Log.i("TAG", "Can't stitch images, error code = " + status);
                } else {
                    // then stitching is ok

          //          Log.i("TAG", "OK");
                    imwrite(SroomPath, pano);
                    //File imgFile = new File(result_name);
                    //Log.i("imgfile", "img file is done");
                    if (roomPath.exists()) {
         //               Log.i("Exist", "img exists");
                        Bitmap myBitmap = BitmapFactory.decodeFile(roomPath.getAbsolutePath());
                        myImage.setImageBitmap(myBitmap);
                        STITCHING_OK=true;
                        img_width=myBitmap.getWidth();
                        img_height=myBitmap.getHeight();

         //               Log.i("IMAGE WIDTH","width : "+img_width +" height : "+ img_height);
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
         //           Log.i("First Path", SroomPath);
         //           Log.i("SECOND", spath);
                    img_width=bitmap.getWidth();
                    img_height=bitmap.getHeight();
                    // roomPath=new File(imageUri.get());
                    //roomPath=path.parse

          //          Log.i("Furn Path", "Not furn area");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

    }



    public static Bitmap changeBitmapColor(Bitmap sourceBitmap, int color)
    {
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(),true);
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, paint);
        return resultBitmap;
    }

    public void ColorTheRoom(String room_path){

        Mat roomImg= new Mat();
        roomImg=imread(room_path);
        Mat roomImg_cpy=roomImg.clone();
        Mat cropped=new Mat(roomImg_cpy,rectCrop);

        roi = new File(WorkingDirectory,"roi.png");
        String roi_p= roi.toString();
        imwrite(roi_p,cropped);

        Bitmap myBitmap = BitmapFactory.decodeFile(roi.getAbsolutePath());
        myBitmap = changeBitmapColor(myBitmap,DefaultColor);

        try (FileOutputStream out = new FileOutputStream(roi)) {
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat coloredPortion= imread(roi_p);
        coloredPortion.copyTo(roomImg_cpy.apply(rectCrop));
        imwrite(roi_p,roomImg_cpy);

    }
    public void saveTempBitmap(Bitmap bitmap) {
        if (isExternalStorageWritable()) {
            saveImage(bitmap);
        }else{
            Toast.makeText(AddColor.this,"Memory is not writable",Toast.LENGTH_LONG).show();
            //prompt the user or do something
        }
    }

    private void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/DecorateYourHome");
        if(!myDir.exists()){
            myDir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "ColoredImage_"+ timeStamp +".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(AddColor.this,"Image saved in : "+file.getAbsolutePath(),Toast.LENGTH_LONG).show();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /*
    public void saveToGallery(String room_path){
        final int min = 1;
        final int max = 1000000;
        final int image_name = new Random().nextInt((max - min) + 1) + min;
        SavedImages = new File(SavedImagesFolder,"roomPic"+image_name+".jpg");

        String savedImgPath = SavedImages.toString();
        Log.i("COLORED IMAGE PATH",savedImgPath);
        Mat roomImg= new Mat();
        roomImg=imread(room_path);
        imwrite(savedImgPath,roomImg);


        new File(savedImgPath).getParentFile().mkdir();

        try {
            OutputStream fileOutputStream = new FileOutputStream(savedImgPath);
            savedBitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        savedBitmap.recycle();

        File file = new File(resultPath);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Edited");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", resultPath);

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        MediaScannerConnection.scanFile(AddColor.this, new String[]{SavedImages.getPath()}, new String[]{"image/jpg"}, null);
    }

*/
    /*
    public static void addPicToGallery(Context context, String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
*/
    /*
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();

        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
// Tell the media scanner about the new file so that it is
// immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }*/
    /*
    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root,"DecorateYourHome");
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    /*
    public void storeInGallery(Bitmap bitmap){
        final int min = 1;
        final int max = 1000000;
        final int image_name = new Random().nextInt((max - min) + 1) + min;

        SavedImages = new File(SavedImagesFolder,"roomPic"+image_name+".png");

        String savedImgPath = SavedImages.toString();

        System.out.println(SavedImages.getAbsolutePath());
        if (SavedImages.exists()) SavedImages.delete();
        Log.i("LOAD", savedImgPath);
        try {
            FileOutputStream out = new FileOutputStream(SavedImages);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        /*
        bitmap.recycle();
        //File file = new File(resultPath);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Photo_"+image_name);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Edited");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, SavedImages.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, SavedImages.getName().toLowerCase(Locale.US));
        values.put("_data", savedImgPath);

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
*/
        //MediaScannerConnection.scanFile(AddColor.this, new String[]{SavedImages.getPath()}, new String[]{"image/png"}, null);
                /*
                    public static final String insertImage (ContentResolver cr, Bitmap source,
                    String title, String description)

                        Insert an image and create a thumbnail for it.

                    Parameters
                        cr : The content resolver to use
                        source : The stream to use for the image
                        title : The name of the image
                        description : The description of the image

                    Returns
                        The URL to the newly created image, or null if the image
                        failed to be stored for any reason.
                */
            /*generate random number for title*/
        /*
        final int min = 1;
        final int max = 1000000;
        final int random = new Random().nextInt((max - min) + 1) + min;
        // Save image to gallery
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "colored_image_"+random,
                "Image of colored room"
        );*/

        // Parse the gallery image url to uri
        //Uri savedImageURI = Uri.parse(savedImageURL);

        // Display the saved image to ImageView
       // iv_saved.setImageURI(savedImageURI);

        // Display saved image url to TextView
        //tv_saved.setText("Image saved to gallery.\n" + savedImageURL);

   // }


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




}
