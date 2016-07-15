package com.example.bluetooth;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String ipname;


    public CameraSurfacePreview(Context context, String HostAddress) {
        super(context);
        ipname = HostAddress.substring(1);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	
    	Log.d("Dennis", "surfaceCreated() is called");
    	
        try {
			// Open the Camera in preview mode
			mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
			mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(new StreamIt(ipname)); // 设置回调的类

        } catch (IOException e) {
            Log.d("Dennis", "Error setting camera preview: " + e.getMessage());
        }
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	
    	Log.d("Dennis", "surfaceChanged() is called");
    	
        try {
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Dennis", "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	if (mCamera != null) {
    		mCamera.stopPreview();
        	mCamera.release();
        	mCamera = null;
    	}
    	
    	Log.d("Dennis", "surfaceDestroyed() is called");
    }
    
    public void takePicture(PictureCallback imageCallback) {
		mCamera.takePicture(null, null, imageCallback);
	}
}

class StreamIt implements Camera.PreviewCallback {
    private String ipname;

    public StreamIt(String ipname) {
        this.ipname = ipname;
    }
    private int picfre = 0;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        try {
            picfre = picfre + 1;
            // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if (image != null && picfre == 2) {
                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outstream);
                outstream.flush();
                // 启用线程将图像数据发送出去
                Thread th = new MyThread(outstream, ipname);
                th.start();
                picfre = 0;

            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }

    class MyThread extends Thread {
        private byte byteBuffer[] = new byte[1024];
        private OutputStream outsocket;
        private ByteArrayOutputStream myoutputstream;
        private String ipname;

        public MyThread(ByteArrayOutputStream myoutputstream, String ipname) {
            this.myoutputstream = myoutputstream;
            this.ipname = ipname;
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                // 将图像数据通过Socket发送出去
                Socket tempSocket = new Socket(ipname, 6000);
                outsocket = tempSocket.getOutputStream();
                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int amount;
                while ((amount = inputstream.read(byteBuffer)) != -1) {
                    outsocket.write(byteBuffer, 0, amount);
                }
                myoutputstream.flush();
                myoutputstream.close();
                tempSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}