package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;
import edu.hitsz.dao.GameConfig;

public class DifficultyActivity extends AppCompatActivity {

    private CheckBox cbSound;
    private String selectedHero = "hero1"; // 默认选中 hero1
    private ImageView ivHero1, ivHero2, ivHero3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        cbSound = findViewById(R.id.cb_sound);

        Button btnEasy = findViewById(R.id.btn_easy);
        Button btnMedium = findViewById(R.id.btn_medium);
        Button btnHard = findViewById(R.id.btn_hard);

        ivHero1 = findViewById(R.id.iv_hero1);
        ivHero2 = findViewById(R.id.iv_hero2);
        ivHero3 = findViewById(R.id.iv_hero3);

        // 设置英雄机点击选择
        ivHero1.setOnClickListener(v -> selectHero("hero1", ivHero1));
        ivHero2.setOnClickListener(v -> selectHero("hero2", ivHero2));
        ivHero3.setOnClickListener(v -> selectHero("hero3", ivHero3));

        // 默认选中 hero1
        selectHero("hero1", ivHero1);

        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("EASY");
            }
        });

        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("MEDIUM");
            }
        });

        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("HARD");
            }
        });
    }
    private void selectHero(String heroTag, ImageView selectedView) {
        selectedHero = heroTag;
        // 更新所有 ImageView 的选中状态
        ivHero1.setSelected(heroTag.equals("hero1"));
        ivHero2.setSelected(heroTag.equals("hero2"));
        ivHero3.setSelected(heroTag.equals("hero3"));
    }
    private void startGame(String difficulty) {
        // 保存音效设置（可存到GameConfig）
        boolean soundOn = cbSound.isChecked();
        GameConfig.soundOn = soundOn;

        Intent intent = new Intent(DifficultyActivity.this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
        finish(); // 关闭难度选择界面，用户返回时不再看到它
    }
}