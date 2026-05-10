package edu.hitsz.application;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.audio.AudioManager;
import edu.hitsz.dao.FileScoreDao;
import edu.hitsz.dao.GameConfig;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.network.LeaderboardApiClient;
import edu.hitsz.network.LeaderboardEntry;
import edu.hitsz.network.BattleSession;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private AudioManager audioManager;
    private ScoreDao scoreDao;
    private boolean multiplayerMode;

    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int finalScore = msg.arg1;
                if (multiplayerMode) {
                    showBattleResultDialog(msg.arg1, msg.arg2);
                } else {
                    showInputNameDialog(finalScore);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取难度参数
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty != null) {
            GameConfig.difficulty = difficulty;
        }

        // 初始化图片管理器
        ImageManager.init(this);

        // 初始化音频管理器
        audioManager = AudioManager.getInstance(this);
        // 初始化 DAO
        scoreDao = new FileScoreDao(this);

        multiplayerMode = getIntent().getBooleanExtra("multiplayer", false);
        String roomId = getIntent().getStringExtra("roomId");
        String playerId = getIntent().getStringExtra("playerId");

        // 创建游戏视图，传入 Handler
        if (multiplayerMode) {
            gameView = new GameView(this, uiHandler, true, roomId, playerId, BattleSession.getSocketClient());
            BattleSession.clear();
        } else {
            gameView = new GameView(this, uiHandler);
        }
        setContentView(gameView);
    }

    private void showInputNameDialog(int finalScore) {
        Log.d("GameActivity", "finalScore received=" + finalScore);
        EditText input = new EditText(this);
        input.setHint("请输入您的昵称");
        new AlertDialog.Builder(this)
                .setTitle("游戏结束")
                .setMessage("您的得分：" + finalScore)
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        name = "匿名";
                    }
                    // 保存得分
                    scoreDao.addScore(name, finalScore);
                    uploadScoreToServer(name, finalScore);
                    // 跳转到排行榜
                    Intent intent = new Intent(GameActivity.this, RankActivity.class);
                    startActivity(intent);
                    // 关闭当前游戏页面
                    finish();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 不保存直接退出
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void uploadScoreToServer(String name, int finalScore) {
        LeaderboardEntry entry = new LeaderboardEntry(
                name,
                finalScore,
                GameConfig.difficulty,
                System.currentTimeMillis());
        new LeaderboardApiClient().uploadScore(entry, new LeaderboardApiClient.UploadCallback() {
            @Override
            public void onSuccess() {
                Log.d("GameActivity", "score uploaded to HTTP server");
            }

            @Override
            public void onError(String message) {
                Log.e("GameActivity", "score upload failed: " + message);
            }
        });
    }

    private void showBattleResultDialog(int myScore, int opponentScore) {
        new AlertDialog.Builder(this)
                .setTitle("对战结束")
                .setMessage("我的分数：" + myScore + "\n对手分数：" + opponentScore)
                .setPositiveButton("返回主菜单", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, DifficultyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.releaseGame();
        }
        if (audioManager != null) {
            audioManager.release();
        }
    }
}