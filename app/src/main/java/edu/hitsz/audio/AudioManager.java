package edu.hitsz.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import edu.hitsz.R;
import edu.hitsz.dao.GameConfig;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private Context context;
    private MediaPlayer bgmPlayer;      // 普通背景音乐
    private MediaPlayer bossBgmPlayer;  // Boss 背景音乐
    private SoundPool soundPool;
    private Map<String, Integer> soundMap; // 存储音效资源 ID 与 SoundPool ID 的映射

    // 音效文件名（无扩展名）对应的资源 ID
    private static final String[] SOUND_NAMES = {
            "bullet_hit", "bomb_explode", "get_supply", "game_over"
    };

    private AudioManager(Context context) {
        this.context = context.getApplicationContext();
        soundMap = new HashMap<>();

        // 初始化 SoundPool（兼容 API 21+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)  // 最大并发流
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        // 加载音效文件
        for (String name : SOUND_NAMES) {
            int resId = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            if (resId != 0) {
                int soundId = soundPool.load(context, resId, 1);
                soundMap.put(name, soundId);
            }
        }
    }

    public static synchronized AudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new AudioManager(context);
        }
        return instance;
    }

    /**
     * 播放普通背景音乐（循环）
     */
    public void playBgm() {
        if (!GameConfig.soundOn) return;
        stopBgm(); // 停止当前正在播放的背景音乐
        bgmPlayer = MediaPlayer.create(context, R.raw.bgm);
        bgmPlayer.setLooping(true);
        bgmPlayer.start();
    }

    /**
     * 播放 Boss 背景音乐（循环）
     */
    public void playBossBgm() {
        if (!GameConfig.soundOn) return;
        stopBgm(); // 停止普通 bgm
        if (bossBgmPlayer == null) {
            bossBgmPlayer = MediaPlayer.create(context, R.raw.bgm_boss);
            bossBgmPlayer.setLooping(true);
        }
        bossBgmPlayer.start();
    }

    /**
     * 停止所有背景音乐
     */
    public void stopBgm() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.stop();
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) {
            bossBgmPlayer.stop();
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
    }

    /**
     * 恢复普通背景音乐（Boss 死亡后调用）
     */
    public void resumeNormalBgm() {
        if (!GameConfig.soundOn) return;
        stopBgm(); // 停止可能存在的 Boss 音乐
        playBgm(); // 重新播放普通音乐
    }

    /**
     * 播放短音效
     * @param soundName 音效名称，与 SOUND_NAMES 中的字符串一致
     */
    public void playSound(String soundName) {
        if (!GameConfig.soundOn) return;
        Integer soundId = soundMap.get(soundName);
        if (soundId != null) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    /**
     * 释放所有资源（在应用退出时调用）
     */
    public void release() {
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossBgmPlayer != null) {
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }

    public void pauseBgm() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
        if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) {
            bossBgmPlayer.pause();
        }
    }

    public void resumeBgm() {
        if (GameConfig.soundOn) {
            if (bgmPlayer != null && !bgmPlayer.isPlaying()) {
                bgmPlayer.start();
            }
            if (bossBgmPlayer != null && !bossBgmPlayer.isPlaying()) {
                bossBgmPlayer.start();
            }
        }
    }
}