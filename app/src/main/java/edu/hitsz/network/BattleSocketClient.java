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
                socket = new Socket();
                socket.connect(new InetSocketAddress(NetworkConfig.SERVER_HOST, NetworkConfig.SOCKET_PORT), 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                running = true;
                Listener currentListener = getListener();
                if (currentListener != null) {
                    mainHandler.post(currentListener::onConnected);
                }
                String fromServer;
                while (running && (fromServer = in.readLine()) != null) {
                    BattleMessage message = gson.fromJson(fromServer, BattleMessage.class);
                    Listener messageListener = getListener();
                    if (messageListener != null) {
                        mainHandler.post(() -> messageListener.onMessage(message));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "socket error", e);
                Listener currentListener = getListener();
                if (currentListener != null) {
                    mainHandler.post(() -> currentListener.onError(e.getMessage()));
                }
            } finally {
                running = false;
                closeInternal();
                Listener currentListener = getListener();
                if (currentListener != null) {
                    mainHandler.post(currentListener::onClosed);
                }
            }
        });
    }

    public void send(BattleMessage message) {
        sendExecutor.execute(() -> {
            if (writer != null) {
                message.timestamp = System.currentTimeMillis();
                String json = gson.toJson(message);
                Log.d(TAG, "send: " + json);
                writer.println(json);
            }
        });
    }

    public void close() {
        running = false;
        sendExecutor.execute(this::closeInternal);
    }

    private void closeInternal() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
