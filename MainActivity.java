package com.example.pidv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pid.BuildConfig;
import com.example.pid.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.threshold;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private ImageView img_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

    }


    ///////////////////////////////////////////////////////////////////     ABRINDO DA GALERIA
    public void open_from_gallery(View view) {
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        img_view = findViewById(R.id.img_view);
        // Result code is RESULT_OK only if the user selects an Image
       /* if (resultCode == Activity.RESULT_OK)
            switch (requestCode){


            }*/
        // Result code is RESULT_OK only if the user captures an Image
        if (resultCode == Activity.RESULT_OK)
            try{
                switch (requestCode){
                    case CAMERA_REQUEST_CODE:
                        //img_view.setImageURI(Uri.parse(cameraFilePath));
                        Bitmap d=BitmapFactory.decodeFile(cameraFilePath);
                        int newHeight = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
                        Bitmap putImage = Bitmap.createScaledBitmap(d, 512, newHeight, true);

                        Bitmap to_binary_img = toBinary(putImage);

                        img_view.setImageBitmap(to_binary_img);

                        break;
                    case GALLERY_REQUEST_CODE:
                        //data.getData returns the content URI for the selected Image
                        Uri selectedImage = data.getData();
                        Bitmap bitmapcamera = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        int newHeightcamera = (int) ( bitmapcamera.getHeight() * (512.0 / bitmapcamera.getWidth()) );
                        Bitmap putImagecamera = Bitmap.createScaledBitmap(bitmapcamera, 512, newHeightcamera, true);

                        Bitmap to_binary = toBinary(putImagecamera);

                        img_view.setImageBitmap(to_binary);

                        break;
                }

            }catch (Exception e){}

        //if (resultCode == Activity.RESULT_OK)
        //   switch (requestCode){

        //   }
    }

    ///////////////////////////////////////////////////////////////////     ABRINDO CÂMERA

    private String cameraFilePath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";

        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");

        File image = File.createTempFile(imageFileName,".jpg",storageDir );

        // Save a file: path for using again
        cameraFilePath = image.getAbsolutePath();




        return image;
    }

    public void captureFromCamera(View view) {

        if (checkPermission()) {
            //main logic or main code

            // . write your main code to execute, It will execute if the permission is already given.
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } else {
            requestPermission();
        }

    }



    ///////////////////////////////////////////////////////////////////////////////////////////////Request permission


    private static final int PERMISSION_REQUEST_CODE = 200;



    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }

        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////Using OpenCV

    public void invert_color(View view){
        if(is_inverted){
            Mat img_mat = new Mat();
            Utils.bitmapToMat(return_img, img_mat);

            Imgproc.cvtColor(img_mat, img_mat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(img_mat, img_mat, 127, 255, THRESH_BINARY);
            Utils.matToBitmap(img_mat, return_img);

            img_view.setImageBitmap(return_img);
        }else if(!is_inverted){

            Mat img_mat = new Mat();
            Utils.bitmapToMat(return_img, img_mat);

            Imgproc.cvtColor(img_mat, img_mat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(img_mat, img_mat, 127, 255, THRESH_BINARY_INV);
            Utils.matToBitmap(img_mat, return_img);

            img_view.setImageBitmap(return_img);

        }else{
            Log.w("myApp", "Choose image first");
        }
    }


    boolean is_inverted = false;

    Bitmap return_img;
    public Bitmap toBinary(Bitmap img_binary){

        return_img = img_binary;


        Mat img_mat = new Mat();
        Utils.bitmapToMat(return_img, img_mat);

        Imgproc.cvtColor(img_mat, img_mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(img_mat, img_mat, 127, 255, THRESH_BINARY);
        Utils.matToBitmap(img_mat, return_img);


        return return_img;

    }

}
