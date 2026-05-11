package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;
import edu.hitsz.network.BattleMessage;
import edu.hitsz.network.BattleSession;
import edu.hitsz.network.BattleSocketClient;

public class BattleRoomActivity extends AppCompatActivity {
    private static final String TAG = "BattleRoomActivity";
    private enum RoomState {
        IDLE,
        CREATING,
        WAITING_FOR_PLAYER,
        JOINING,
        READY
    }

    private TextView tvStatus;
    private EditText etRoomId;
    private Button btnCreateRoom;
    private Button btnJoinRoom;
    private BattleSocketClient socketClient;
    private String currentRoomId;
    private String playerId;
    private boolean started;
    private boolean leavingRoom;
    private RoomState roomState = RoomState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_battle_room);

        tvStatus = findViewById(R.id.tv_battle_status);
        etRoomId = findViewById(R.id.et_room_id);
        btnCreateRoom = findViewById(R.id.btn_create_room);
        btnJoinRoom = findViewById(R.id.btn_join_room);
        Button btnBack = findViewById(R.id.btn_battle_back);

        updateActionButtons(false);
        socketClient = new BattleSocketClient(new BattleSocketClient.Listener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected, state=" + roomState);
                roomState = RoomState.IDLE;
                tvStatus.setText("已连接服务器，可创建或加入房间");
                updateActionButtons(true);
            }

            @Override
            public void onMessage(BattleMessage message) {
                handleBattleMessage(message);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError, message=" + message + ", started=" + started + ", state=" + roomState);
                if (!started) {
                    roomState = RoomState.IDLE;
                    tvStatus.setText("连接失败: " + message);
                    updateActionButtons(false);
                }
            }

            @Override
            public void onClosed() {
                Log.d(TAG, "onClosed, started=" + started + ", state=" + roomState);
                if (!started) {
                    roomState = RoomState.IDLE;
                    tvStatus.setText("连接已关闭");
                    updateActionButtons(false);
                }
            }
        });
        socketClient.connect();

        btnCreateRoom.setOnClickListener(v -> {
            if (roomState != RoomState.IDLE) {
                tvStatus.setText("当前正在处理房间操作，请勿重复点击");
                return;
            }
            Log.d(TAG, "create room clicked, state=" + roomState + ", socketClient=" + socketClient);
            currentRoomId = null;
            playerId = null;
            roomState = RoomState.CREATING;
            updateActionButtons(false);
            tvStatus.setText("正在创建房间...");
            socketClient.send(BattleMessage.create("CREATE_ROOM"));
        });

        btnJoinRoom.setOnClickListener(v -> {
            if (roomState != RoomState.IDLE) {
                tvStatus.setText("当前正在处理房间操作，请勿重复点击");
                return;
            }
            String roomId = etRoomId.getText().toString().trim();
            if (roomId.isEmpty()) {
                tvStatus.setText("请输入房间号");
                return;
            }
            currentRoomId = roomId;
            playerId = null;
            roomState = RoomState.JOINING;
            updateActionButtons(false);
            BattleMessage join = BattleMessage.create("JOIN_ROOM");
            join.roomId = roomId;
            tvStatus.setText("正在加入房间 " + roomId + "...");
            socketClient.send(join);
        });

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "back clicked, close socket");
            leavingRoom = true;
            if (socketClient != null) {
                socketClient.close();
            }
            finish();
        });
    }

    private void handleBattleMessage(BattleMessage message) {
        if (message == null || message.type == null) {
            return;
        }
        Log.d(TAG, "handleBattleMessage type=" + message.type
                + ", roomId=" + message.roomId
                + ", playerId=" + message.playerId
                + ", currentRoomId=" + currentRoomId
                + ", currentPlayerId=" + playerId
                + ", state=" + roomState);
        switch (message.type) {
            case "CONNECTED":
                tvStatus.setText("服务器连接成功，可创建或加入房间");
                updateActionButtons(true);
                break;
            case "ROOM_CREATED":
                handleRoomCreated(message);
                break;
            case "JOINED_ROOM":
                handleJoinedRoom(message);
                break;
            case "BATTLE_READY":
                handleBattleReady(message);
                break;
            case "ERROR":
                roomState = RoomState.IDLE;
                tvStatus.setText("服务器错误: " + message.message);
                updateActionButtons(true);
                break;
            default:
                break;
        }
    }

    private void handleRoomCreated(BattleMessage message) {
        if (message.roomId == null || message.playerId == null) {
            roomState = RoomState.IDLE;
            tvStatus.setText("服务器返回的房间信息不完整");
            updateActionButtons(true);
            return;
        }
        Log.d(TAG, "handleRoomCreated success, roomId=" + message.roomId + ", playerId=" + message.playerId);
        currentRoomId = message.roomId;
        playerId = message.playerId;
        roomState = RoomState.WAITING_FOR_PLAYER;
        etRoomId.setText(currentRoomId);
        tvStatus.setText("房间创建成功，房间号: " + currentRoomId + "，正在等待玩家加入...");
        updateActionButtons(false);
    }

    private void handleJoinedRoom(BattleMessage message) {
        if (message.roomId == null || message.playerId == null) {
            roomState = RoomState.IDLE;
            tvStatus.setText("服务器返回的加入房间信息不完整");
            updateActionButtons(true);
            return;
        }
        currentRoomId = message.roomId;
        playerId = message.playerId;
        roomState = RoomState.JOINING;
        tvStatus.setText("已加入房间 " + currentRoomId + "，等待双方就绪...");
        updateActionButtons(false);
    }

    private void handleBattleReady(BattleMessage message) {
        if (currentRoomId == null || playerId == null) {
            tvStatus.setText("收到开始消息，但本机尚未加入房间，已忽略");
            return;
        }
        if (message.roomId != null && !currentRoomId.equals(message.roomId)) {
            tvStatus.setText("收到其他房间的开始消息，已忽略");
            return;
        }
        roomState = RoomState.READY;
        tvStatus.setText("双方已就绪，进入对战");
        startBattle();
    }

    private void updateActionButtons(boolean enabled) {
        if (btnCreateRoom != null) {
            btnCreateRoom.setEnabled(enabled);
        }
        if (btnJoinRoom != null) {
            btnJoinRoom.setEnabled(enabled);
        }
    }

    private void startBattle() {
        if (started) {
            return;
        }
        started = true;
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("difficulty", "MEDIUM");
        intent.putExtra("multiplayer", true);
        intent.putExtra("roomId", currentRoomId);
        intent.putExtra("playerId", playerId);
        Log.d(TAG, "startBattle, roomId=" + currentRoomId + ", playerId=" + playerId + ", socketClient=" + socketClient);
        BattleSession.setSocketClient(socketClient);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop, started=" + started + ", leavingRoom=" + leavingRoom + ", state=" + roomState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy, started=" + started + ", leavingRoom=" + leavingRoom + ", state=" + roomState);
        super.onDestroy();
        if (leavingRoom && !started && socketClient != null) {
            socketClient.close();
        }
    }
}
