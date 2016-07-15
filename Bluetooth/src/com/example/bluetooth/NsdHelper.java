package com.example.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class NsdHelper {

    Context mContext;
    Activity mAct = (Activity) mContext;
    
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    boolean discoverying = false;
    boolean registered = false;

    public static final String TAG = "NsdHelper";
    public static final String newTag = "me";
    public String mServiceName = "CarNSD";

    //这个是discover到的service的info
    NsdServiceInfo mService;

    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    //这个接口重写了discoveryService的方法
    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //在discovery开始后就被迅速调用
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                discoverying = true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {

                //找到一个service
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {

                    //检查service的类型是否和本service相同
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {

                    //检查service的name
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("ClientNSD")){

                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                discoverying = false;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    //这个接口重写了获取service的info的方法
    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
            }
        };
    }

    //这个接口可以提示application注册成功或者是失败
    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                //保存service name，因为Android可能会因为冲突而更新这个name
                mServiceName = NsdServiceInfo.getServiceName();
                registered = true;
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                //注册失败用来debug的
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                //注销
                //只会在调用NsdManager.unregisterService() 并 传递给这个listener时才会发生
                registered = false;
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

            }
            
        };
    }

    public void registerService(int port) {
        //收集本台设备的信息，并发布出去，使得网络中的其他设备可以看到并决定是否连接到我的设备
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();

        //不要和其它application使用相同的port，否则会冲突
        serviceInfo.setPort(port);

        //ServiceName对网络中同样使用NSD的设备可见，必须独一无二才能区分
        serviceInfo.setServiceName(mServiceName);

        //决定协议和传输层
        serviceInfo.setServiceType(SERVICE_TYPE);


        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        
    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    // NsdHelper's tearDown method
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }
}
