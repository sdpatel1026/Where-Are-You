package com.sd.whereareyou.utils;


import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ClientClass extends Thread {

    private static final String TAG = ClientClass.class.getName();
    private static int TIME_OUT = 500; // milisecond
    private Socket socket;
    private String hostAdd;
    private SendReceive sendReceive;
    private Handler handler;
    private OnCreateSendReceiveListener onCreateSendReceiveListener;


    public ClientClass(InetAddress hostAddress, Handler handler) {
        hostAdd = hostAddress.getHostAddress();
        socket = new Socket();
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAdd, 8888), TIME_OUT);
            sendReceive = new SendReceive(socket, handler);
            if (onCreateSendReceiveListener != null) {
                onCreateSendReceiveListener.onCreateSendReceive(sendReceive);
            }
            sendReceive.start();
            Log.d(TAG, "run(): ClientThreadStarted");

        } catch (IOException e) {
            Log.d(TAG, "run(): " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void setOnCreateSendReceiveListener(OnCreateSendReceiveListener listener) {
        onCreateSendReceiveListener = listener;
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.d(TAG, "closeSocket(): " + e.getMessage());
            e.printStackTrace();
        }

    }
}