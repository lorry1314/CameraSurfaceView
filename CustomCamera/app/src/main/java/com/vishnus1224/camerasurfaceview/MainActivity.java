package com.vishnus1224.camerasurfaceview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vishnus1224.camerasurfaceview.CustomView.PreviewSurfaceView;
import com.vishnus1224.camerasurfaceview.Task.ImageDecodeTask;
import com.vishnus1224.camerasurfaceview.Task.SaveImageTask;
import com.vishnus1224.camerasurfaceview.Utility.Constant;

import static com.vishnus1224.camerasurfaceview.Utility.Constant.*;


public class MainActivity extends Activity {

    private Camera camera;
    private PreviewSurfaceView previewSurfaceView;
    private FrameLayout previewFrame;
    private Button captureButton;
    private Button switchCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewFrame = (FrameLayout)findViewById(R.id.camera_preview);
        captureButton = (Button)findViewById(R.id.button_capture);
        switchCameraButton = (Button)findViewById(R.id.button_switch);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });

        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constant.BACK_CAMERA_IN_USE) {
                    showFrontCamera();
                } else {
                    showBackCamera();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
          showBackCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removePreview();
        releaseCamera();
    }

    private void showBackCamera() {
        releaseCamera();
        camera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (camera != null) {
            if (previewSurfaceView != null) {
                removePreview();
            }
            Constant.BACK_CAMERA_IN_USE = true;
            attachCameraToPreview();
        }
    }

    private void showFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();

        if (numberOfCameras > 1) {
            releaseCamera();
            camera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Constant.BACK_CAMERA_IN_USE = false;
            removePreview();
            attachCameraToPreview();
        } else {
            Toast.makeText(MainActivity.this, "Front camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachCameraToPreview() {
        previewSurfaceView = new PreviewSurfaceView(this, camera);
        previewFrame.addView(previewSurfaceView);
    }

    private void removePreview() {
        previewFrame.removeView(previewSurfaceView);
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    public Camera getCameraInstance(int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            captureButton.setEnabled(false);

            camera.stopPreview();

            //decodes and returns the bitmap if this flag is set in constants.
            if(Constant.FLAG_DECODE_BITMAP){
                new ImageDecodeTask(MainActivity.this, data, previewFrame.getHeight(), previewSurfaceView.getHeight()).execute();
            }else{
                //the method decodeBitmapComplete will not be called if the task is not started. So enable the button
                captureButton.setEnabled(true);
            }

            camera.startPreview();
        }
    };

    public void decodeBitmapComplete(Bitmap bitmap){
        captureButton.setEnabled(true);
        //The decoded bitmap is passed as a parameter. Use this for all further operations.
        if(bitmap != null){

            //Save the image to disk if this flag is set
            if(FLAG_SAVE_IMAGE){
                new SaveImageTask(this).execute(bitmap);
            }
        }
        //set the bitmap to null if it is no longer needed
        bitmap = null;

    }

    public void fileSaveComplete(FileSaveStatus fileSaveStatus){
        showToast(fileSaveStatus == FileSaveStatus.SUCCESS ? IMAGE_SAVE_SUCCESS_MESSAGE : IMAGE_SAVE_FAILURE_MESSAGE);
    }

    private void showToast(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}
