package edu.hitsz.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BattleSocketClient {
    private static final String TAG = "BattleSocketClient";
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();
    private volatile Listener listener;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter writer;
    private volatile boolean running;
    private volatile boolean connected;
    private volatile boolean closing;

    public interface Listener {
        void onConnected();
        void onMessage(BattleMessage message);
        void onError(String message);
        void onClosed();
    }

    public BattleSocketClient(Listener listener) {
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private Listener getListener() {
        return listener;
    }

    public void connect() {
        connectExecutor.execute(() -> {
            try {
                closing = false;
                socket = new Socket();
                socket.connect(new InetSocketAddress(NetworkConfig.SERVER_HOST, NetworkConfig.SOCKET_PORT), 5000);
                Log.d(TAG, "connected to " + NetworkConfig.SERVER_HOST + ":" + NetworkConfig.SOCKET_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                running = true;
                connected = true;
                Listener currentListener = getListener();
                if (currentListener != null) {
                    mainHandler.post(currentListener::onConnected);
                }
                String fromServer;
                while (running && (fromServer = in.readLine()) != null) {
                    Log.d(TAG, "receive raw: " + fromServer);
                    BattleMessage message;
                    try {
                        message = gson.fromJson(fromServer, BattleMessage.class);
                    } catch (Exception parseError) {
                        Log.e(TAG, "message parse failed: " + fromServer, parseError);
                        Listener errorListener = getListener();
                        if (errorListener != null && !closing) {
                            mainHandler.post(() -> errorListener.onError("服务器消息解析失败"));
                        }
                        continue;
                    }
                    Log.d(TAG, "receive message type=" + (message == null ? "null" : message.type)
                            + ", roomId=" + (message == null ? "null" : message.roomId)
                            + ", playerId=" + (message == null ? "null" : message.playerId));
                    Listener messageListener = getListener();
                    if (messageListener != null) {
                        mainHandler.post(() -> messageListener.onMessage(message));
                    }
                }
                Log.d(TAG, "read loop ended, running=" + running + ", closing=" + closing
                        + ", socketClosed=" + (socket == null || socket.isClosed()));
            } catch (Exception e) {
                Log.e(TAG, "socket error", e);
                if (!closing) {
                    Listener currentListener = getListener();
                    if (currentListener != null) {
                        mainHandler.post(() -> currentListener.onError(e.getMessage()));
                    }
                }
            } finally {
                running = false;
                connected = false;
                closeInternal();
                if (!closing) {
                    Listener currentListener = getListener();
                    if (currentListener != null) {
                        mainHandler.post(currentListener::onClosed);
                    }
                }
            }
        });
    }

    public void send(BattleMessage message) {
        sendExecutor.execute(() -> {
            if (!connected || writer == null) {
                Listener currentListener = getListener();
                if (currentListener != null && !closing) {
                    mainHandler.post(() -> currentListener.onError("尚未连接服务器，消息发送失败"));
                }
                return;
            }
            message.timestamp = System.currentTimeMillis();
            String json = gson.toJson(message);
            Log.d(TAG, "send: " + json);
            writer.println(json);
            if (writer.checkError()) {
                connected = false;
                Listener currentListener = getListener();
                if (currentListener != null && !closing) {
                    mainHandler.post(() -> currentListener.onError("消息发送失败，连接可能已断开"));
                }
            }
        });
    }

    public void close() {
        Log.d(TAG, "close called", new Throwable("close caller"));
        closing = true;
        running = false;
        connected = false;
        sendExecutor.execute(this::closeInternal);
    }

    private void closeInternal() {
        try {
            if (socket != null && !socket.isClosed()) {
                Log.d(TAG, "closeInternal: closing socket");
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "closeInternal failed", e);
        }
    }
}
