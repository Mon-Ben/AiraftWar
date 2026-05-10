package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;
import edu.hitsz.network.BattleMessage;
import edu.hitsz.network.BattleSession;
import edu.hitsz.network.BattleSocketClient;

public class BattleRoomActivity extends AppCompatActivity {
    private TextView tvStatus;
    private EditText etRoomId;
    private BattleSocketClient socketClient;
    private String currentRoomId;
    private String playerId;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_room);

        tvStatus = findViewById(R.id.tv_battle_status);
        etRoomId = findViewById(R.id.et_room_id);
        Button btnCreateRoom = findViewById(R.id.btn_create_room);
        Button btnJoinRoom = findViewById(R.id.btn_join_room);
        Button btnBack = findViewById(R.id.btn_battle_back);

        socketClient = new BattleSocketClient(new BattleSocketClient.Listener() {
            @Override
            public void onConnected() {
                tvStatus.setText("已连接服务器，可创建或加入房间");
            }

            @Override
            public void onMessage(BattleMessage message) {
                handleBattleMessage(message);
            }

            @Override
            public void onError(String message) {
                tvStatus.setText("连接失败: " + message);
            }

            @Override
            public void onClosed() {
                if (!started) {
                    tvStatus.setText("连接已关闭");
                }
            }
        });
        socketClient.connect();

        btnCreateRoom.setOnClickListener(v -> {
            tvStatus.setText("正在创建房间...");
            socketClient.send(BattleMessage.create("CREATE_ROOM"));
        });

        btnJoinRoom.setOnClickListener(v -> {
            String roomId = etRoomId.getText().toString().trim();
            if (roomId.isEmpty()) {
                tvStatus.setText("请输入房间号");
                return;
            }
            BattleMessage join = BattleMessage.create("JOIN_ROOM");
            join.roomId = roomId;
            tvStatus.setText("正在加入房间 " + roomId + "...");
            socketClient.send(join);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void handleBattleMessage(BattleMessage message) {
        if (message == null || message.type == null) {
            return;
        }
        switch (message.type) {
            case "CONNECTED":
                tvStatus.setText("服务器连接成功");
                break;
            case "ROOM_CREATED":
                currentRoomId = message.roomId;
                playerId = message.playerId;
                etRoomId.setText(currentRoomId);
                tvStatus.setText("房间创建成功，房间号: " + currentRoomId + "，正在等待玩家加入...");
                break;
            case "JOINED_ROOM":
                currentRoomId = message.roomId;
                playerId = message.playerId;
                tvStatus.setText("已加入房间 " + currentRoomId + "，等待开始");
                break;
            case "BATTLE_READY":
                tvStatus.setText("双方已就绪，进入对战");
                startBattle();
                break;
            case "ERROR":
                tvStatus.setText("服务器错误: " + message.message);
                break;
            default:
                break;
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
        BattleSession.setSocketClient(socketClient);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!started && socketClient != null) {
            socketClient.close();
        }
    }
}
