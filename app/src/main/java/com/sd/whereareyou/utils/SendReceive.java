package com.sd.whereareyou.utils;


import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static com.sd.whereareyou.utils.Constants.MESSAGE_READ;

public class SendReceive extends Thread {

    private static final String TAG = SendReceive.class.getName();
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler handler;

    public SendReceive(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG, "SendReceive(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (socket != null) {
            try {
                bytes = inputStream.read(buffer);

                if (bytes > 0)

                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
