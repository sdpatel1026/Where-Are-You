package com.sd.whereareyou.utils;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerClass extends Thread {

    private static final String TAG = ServerClass.class.getName();
    private Socket socket;
    private SendReceive sendReceive;
    private ServerSocket serverSocket;
    private Handler handler;
    private OnCreateSendReceiveListener onCreateSendReceiveListener;


    public ServerClass(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();
            sendReceive = new SendReceive(socket, handler);
            if (onCreateSendReceiveListener != null) {
                onCreateSendReceiveListener.onCreateSendReceive(sendReceive);
            }
            sendReceive.start();


        } catch (IOException e) {
            Log.d(TAG, "run(): ServerThreadStarted" + e.getMessage());
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
