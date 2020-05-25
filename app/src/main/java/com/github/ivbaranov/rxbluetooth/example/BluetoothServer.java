package com.github.ivbaranov.rxbluetooth.example;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by chm on 2020/5/11.
 */
public class BluetoothServer {

    private static BluetoothServer bluetoothServer;
    private final String TAG = "BluetoothServer";
    private RxBluetooth rxBluetooth;
    public static final String BluetoothServiceName = "ChaseSmartPOSBluetoothService";//蓝牙服务名
    public static final String BluetoothServiceUUID = "6d5ac25d-f83f-4cf7-b98e-0881d0076306";//蓝牙服务UUID
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private BluetoothServer() {
    }

    public static BluetoothServer getInstance() {
        if (bluetoothServer == null) {
            bluetoothServer = new BluetoothServer();
        }
        return bluetoothServer;
    }

    @SuppressLint("CheckResult")
    public void startServer() {
        if (rxBluetooth == null)
            rxBluetooth = new RxBluetooth(App.getApp());

        if (!rxBluetooth.isBluetoothAvailable()) {//处理缺乏蓝牙支持的问题
            // handle the lack of bluetooth support
            Log.d(TAG, "Bluetooth is not supported!");
        } else {
            //            initEventListeners();
            // check if bluetooth is currently enabled and ready for use
            if (!rxBluetooth.isBluetoothEnabled()) {
                // to enable bluetooth via startActivityForResult()
                Log.d(TAG, "Enabling Bluetooth");
                rxBluetooth.enableBluetooth(null, 1);
            } else {
                // you are ready
                //-------------------------------------
                if (firstTask == null) {
                    firstTask = new ThreadUtils.SimpleTask<Object>() {
                        @Nullable
                        @Override
                        public Object doInBackground() {
                            //                            compositeDisposable.add(rxBluetooth.observeConnectionState()
                            //                                    .observeOn(AndroidSchedulers.mainThread())
                            //                                    .subscribeOn(Schedulers.computation())
                            //                                    .subscribe(new Consumer<ConnectionStateEvent>() {//此方法监听不到客户端连接
                            //                                        @Override
                            //                                        public void accept(ConnectionStateEvent event) throws Exception {
                            //                                            switch (event.getState()) {
                            //                                                case BluetoothAdapter.STATE_DISCONNECTED:
                            //                                                    // device disconnected
                            //                                                    System.out.println("disconnected");
                            //                                                    break;
                            //                                                case BluetoothAdapter.STATE_CONNECTING:
                            //                                                    // device connecting
                            //                                                    break;
                            //                                                case BluetoothAdapter.STATE_CONNECTED:
                            //                                                    // device connected
                            //                                                    break;
                            //                                                case BluetoothAdapter.STATE_DISCONNECTING:
                            //                                                    // device disconnecting
                            //                                                    System.out.println("STATE_DISCONNECTING");
                            //                                                    break;
                            //                                            }
                            //                                        }
                            //                                    }));

                            compositeDisposable.add(rxBluetooth.connectAsServer(BluetoothServiceName, UUID.fromString(BluetoothServiceUUID)).subscribe(
                                    new Consumer<BluetoothSocket>() {
                                        @Override
                                        public void accept(BluetoothSocket bluetoothSocket) throws Exception {
                                            // Client connected, do anything with the socket
                                            //                                        System.out.println(Thread.currentThread() == Looper.getMainLooper().getThread());// chm false
                                            //                                        System.out.println(bluetoothSocket);
                                            final BluetoothConnection bluetoothConnection = new BluetoothConnection(bluetoothSocket);
                                            //                                        bluetoothConnection.send("haha");

                                            // Observe every byte received
                                            //                                        compositeDisposable.add(bluetoothConnection.observeStringStream()
                                            //                                                .observeOn(AndroidSchedulers.mainThread())
                                            //                                                .subscribeOn(Schedulers.io())
                                            //                                                .subscribe(new Consumer<String>() {
                                            //                                                    @Override
                                            //                                                    public void accept(String aByte) throws Exception {
                                            //                                                        // This will be called every single byte received
                                            ////                                                        System.out.println(Thread.currentThread() == Looper.getMainLooper().getThread());//chm true
                                            //                                                        System.out.println(aByte);
                                            //
                                            //                                                    }
                                            //                                                }, new Consumer<Throwable>() {
                                            //                                                    @Override
                                            //                                                    public void accept(Throwable throwable) throws Exception {
                                            //                                                        // Error occured
                                            ////                                                        System.out.println(Thread.currentThread() == Looper.getMainLooper().getThread());//chm true
                                            //                                                        System.out.println(throwable.getMessage());
                                            //                                                    }
                                            //                                                }));
                                            //-------------------------------------
                                            final int[] bodyLen = {0};
                                            final int[] len = {0};
                                            final int[] count = {1};
                                            final byte[] buffer_Head = new byte[11];
                                            final byte[][] body = new byte[1][1];
                                            compositeDisposable.add(bluetoothConnection.observeByteStream()
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribe(new Consumer<Byte>() {
                                                        @Override
                                                        public void accept(Byte aByte) throws Exception {
                                                            // This will be called every single byte received
                                                            //                                                        System.out.println(Thread.currentThread() == Looper.getMainLooper().getThread());//chm true
                                                            if (len[0] < 10) {//读取报文头
                                                                buffer_Head[len[0]] = aByte;
                                                                len[0]++;
                                                            } else {//读取报文体
                                                                if (bodyLen[0] == 0) {//第11次读取
                                                                    buffer_Head[10] = '\0';
                                                                    String str_Head = new String(buffer_Head);
                                                                    System.out.println("read buffer_Head: " + str_Head);

                                                                    bodyLen[0] = Integer.parseInt(str_Head.trim());
                                                                    body[0] = new byte[bodyLen[0]];
                                                                    body[0][0] = aByte;
                                                                    //                                                                } catch (Exception e) {
                                                                    //                                                                    CustomLogTools.writeLog("Integer.parseInt Exception: %s", e.getMessage());
                                                                    //                                                                    return -1;
                                                                    //                                                                }
                                                                } else {//11次往后的读取
                                                                    if (count[0] < bodyLen[0] - 1) {
                                                                        body[0][count[0]] = aByte;
                                                                        count[0]++;
                                                                    } else {//读取完毕
                                                                        body[0][count[0]] = aByte;
                                                                        System.out.println(new String(body[0]));
                                                                        JSONObject object = new JSONObject();
                                                                        object.put("respCode", "FN");
                                                                        object.put("respMsg", "FN");
                                                                        String str_RspContent = object.toString();
                                                                        int nRspContentByteLen = str_RspContent.getBytes().length;
                                                                        String strLen = String.format(Locale.ENGLISH, "%010d", nRspContentByteLen);
                                                                        if (bluetoothConnection.send(strLen + str_RspContent)) {
                                                                            System.out.println("send success");
                                                                        }
                                                                    }
                                                                }

                                                            }
                                                            //                                                        System.out.println(aByte);

                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(Throwable throwable) throws Exception {//正常接收完连接断开或异常断开都会回调
                                                            // Error occured
                                                            System.out.println(throwable.getMessage());
                                                            if (compositeDisposable != null) {
                                                                //                                                                compositeDisposable.dispose();//此处取消订阅会导致重启服务后,无法触发接受数据回调,要用clear()
                                                                compositeDisposable.clear();
                                                            }
                                                            startServer();
                                                        }
                                                    }));

                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            // On error
                                        }
                                    }));
                            return null;
                        }

                        @Override
                        public void onSuccess(@Nullable Object result) {

                        }
                    };
                }
                ThreadUtils.executeBySingle(firstTask);

            }
        }
    }

    private ThreadUtils.SimpleTask<Object> firstTask;

    public void destroy() {
        if (rxBluetooth != null) {
            rxBluetooth.cancelDiscovery();
        }
        if (compositeDisposable != null)
            compositeDisposable.dispose();
    }

}
