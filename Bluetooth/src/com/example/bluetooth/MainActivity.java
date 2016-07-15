package com.example.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity implements View.OnClickListener, Camera.PictureCallback{
    Activity mAct = this;
    NsdHelper mNsdHelper = null;
    ChatConnection mConnection;
    private Handler mUpdateHandler;
    private CameraSurfacePreview mCameraSurPreview =null;
    private String TAG = "CarDebug";

    Button up;
    Button down;
    Button back;
    Button left;
    Button right;
    Button contact;
    Button online;
    Button offline;
    Button stop;
    Button s1;
    Button s2;
    Button s3;
    Button s4;
    Button connectButton;
    String command = null;
    private Button mCaptureButton = null;
    private PowerManager pm;
    private PowerManager.WakeLock mWakeLock;


    private boolean RegisterSuccess;
    private boolean ConnectSuccess;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mDevice = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "98:D3:31:20:16:C2";

    private String HostAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        command = "0";
        RegisterSuccess = false;
        ConnectSuccess =false;

     // 初始化布局元素
        initViewsss();
        

        //初始化NSD
        initNSD();

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        // in onResume() call

        mWakeLock.acquire();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "还是没有", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
        }

        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
            DisplayToast("注册成功" + mConnection.getLocalPort());
            RegisterSuccess = true;
        }
    }
    
    private void contact(){

        //初始化NSD
        initNSD();

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        // in onResume() call

        mWakeLock.acquire();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "还是没有", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
        }

        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
            DisplayToast("注册成功" + mConnection.getLocalPort());
            RegisterSuccess = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "不允许蓝牙开启", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    /**
     * 初始化视频界面
     */
    private void initCameraPreview(){

        //建立一个preview实例，并将它添加在layout的framework上
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraSurPreview = new CameraSurfacePreview(this, HostAddress);
        preview.addView(mCameraSurPreview);
    }

    /**
     * 初始化NSD
     */
    private void initNSD(){
        mUpdateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // 处理接受到的Message
                String data = (String) msg.obj;
                Log.d(TAG, "接受到消息：" + data + "，并成功处理");
                bluetoothSender(data);
            }
        };
        mConnection = new ChatConnection(mUpdateHandler, mAct);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
    }
    /**
     * 蓝牙传送指令
     */
     public void bluetoothSender(String command){
            byte[] msgBuffer;
            try {
                outStream = btSocket.getOutputStream();

            } catch (IOException e) {
                ;
            }
            msgBuffer = command.getBytes();
            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                ;
            }
    }

    /**
     * 在这里获取到每个需要用到的控件的实例，并给它们设置好必要的点击事件。
     */
    private void initViews() {
        up = (Button) findViewById(R.id.button1);
        down = (Button) findViewById(R.id.button2);
        left = (Button) findViewById(R.id.button3);
        back = (Button) findViewById(R.id.back);
        right = (Button) findViewById(R.id.button4);
        stop = (Button) findViewById(R.id.button5);
        s1 = (Button) findViewById(R.id.button6);
        s2 = (Button) findViewById(R.id.button7);
        s3 = (Button) findViewById(R.id.button8);
        s4 = (Button) findViewById(R.id.button9);
        //Button registerButton = (Button) findViewById(R.id.button_register);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        stop.setOnClickListener(this);
        back.setOnClickListener(this);
        s1.setOnClickListener(this);
        s2.setOnClickListener(this);
        s3.setOnClickListener(this);
        s4.setOnClickListener(this);

        /*
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!RegisterSuccess){
                    if (mConnection.getLocalPort() > -1) {
                        mNsdHelper.registerService(mConnection.getLocalPort());
                        DisplayToast("注册成功" + mConnection.getLocalPort());
                        RegisterSuccess = true;
                    }
                }
            }
        });

        */



    }
    
    private void initViewss() {
        //Button registerButton = (Button) findViewById(R.id.button_register);
        connectButton = (Button) findViewById(R.id.button_Connect);
        mCaptureButton = (Button) findViewById(R.id.button_capture);
        back = (Button) findViewById(R.id.back);

        mCaptureButton.setOnClickListener(this);
        back.setOnClickListener(this);

        /*
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!RegisterSuccess){
                    if (mConnection.getLocalPort() > -1) {
                        mNsdHelper.registerService(mConnection.getLocalPort());
                        DisplayToast("注册成功" + mConnection.getLocalPort());
                        RegisterSuccess = true;
                    }
                }
            }
        });

        */
        connectButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (!ConnectSuccess){
                    mNsdHelper.discoverServices();
                    //service是discover到的一个
                    NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
                    if (service != null) {
                        mConnection.connectToServer(service.getHost(),
                                service.getPort());
                        DisplayToast("连接" + service.getHost().toString());
                        HostAddress = service.getHost().toString();
                        ConnectSuccess = true;
                        //初始化视频界面
                        initCameraPreview();
                    }
                }
            }});



    }
    
    private void initViewsss() {
        //Button registerButton = (Button) findViewById(R.id.button_register);


        offline = (Button) findViewById(R.id.offline);
        online = (Button) findViewById(R.id.online);
        contact = (Button) findViewById(R.id.getonline);
        
        online.setOnClickListener(this);
        offline.setOnClickListener(this);
        contact.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button1:
                bluetoothSender("1");
                Log.d("Click","Press button 1");
                break;

            case R.id.button2:
                bluetoothSender("2");
                break;

            case R.id.button3:
                bluetoothSender("3");
                break;

            case R.id.button4:
                bluetoothSender("4");
                break;

            case R.id.button5:
                bluetoothSender("0");
                break;
                
            case R.id.button6:
                bluetoothSender("5");
                break;
                
            case R.id.button7:
                bluetoothSender("6");
                break;
               
            case R.id.button8:
                bluetoothSender("7");
                break;
                
            case R.id.button9:
                bluetoothSender("8");
                break;
                
            case R.id.online:
            	setContentView(R.layout.activity_main);
            	initViewss();
                break;
                
            case R.id.getonline:
            	contact();
                break;
                
            case R.id.offline:
            	setContentView(R.layout.offline);
            	initViews();
                break;
                
            case R.id.back:
            	setContentView(R.layout.startup);
            	initViewsss();
                break;   
                
            case R.id.button_capture:
                //当点击了拍照按钮
                mCaptureButton.setEnabled(false);

                // get an image from the camera
                mCameraSurPreview.takePicture(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayToast("正在尝试连接智能小车，请稍后····");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice d : pairedDevices) {
            if (d.getName().equals("HC-06")) {
                mDevice = d;
                Log.e("App", d.getName());
                break;
            }
        }
        try {
            btSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            DisplayToast("套接字创建失败！");
        }
        DisplayToast("成功连接智能小车！可以开始操控了~~~");
        mBluetoothAdapter.cancelDiscovery();
        try {
            btSocket.connect();
            DisplayToast("连接成功建立，数据连接打开！");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                DisplayToast("连接没有建立，无法关闭套接字！");
            }
        }

    }

    @Override
    public void onPause() {

        super.onPause();
        command = "0";
        // in onPause() call
        mWakeLock.release();
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            ;
        }
        byte[] msgBuffer = command.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            ;
        }

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                ;
            }

        }

        try {
            btSocket.close();
        } catch (IOException e2) {

            DisplayToast("套接字关闭失败！");
        }

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        //save the picture to sdcard
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();

            Toast.makeText(this, "Image has been saved to " + pictureFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        // Restart the preview and re-enable the shutter button so that we can take another picture
        camera.startPreview();

        //See if need to enable or not
        mCaptureButton.setEnabled(true);
    }

    private File getOutputMediaFile(){
        //get the mobile Pictures directory
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(picDir.getPath() + File.separator + "IMAGE_"+ timeStamp + ".jpg");
    }

    public void DisplayToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 220);
        toast.show();
    }
}