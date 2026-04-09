package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;
import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.dao.GameConfig;
import edu.hitsz.prop.Bomb;
import edu.hitsz.prop.Fire;
import edu.hitsz.prop.FirePlus;
import edu.hitsz.prop.Heal;

public class ImageManager {
    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();
    private static Bitmap BACKGROUND_IMAGE;
    private static Bitmap HERO_IMAGE1, HERO_IMAGE2, HERO_IMAGE3, HERO_IMAGE;

    public static void init(Context context) {
        // 根据难度加载不同背景
        switch (GameConfig.difficulty) {
            case "EASY":
                BACKGROUND_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg1);
                break;
            case "HARD":
                BACKGROUND_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg5);
                break;
            default: // MEDIUM 或其他
                BACKGROUND_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg3);
                break;
        }

        // 加载英雄机图片
        HERO_IMAGE1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.hero);
        HERO_IMAGE2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.hero2);
        HERO_IMAGE3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.hero3);
        // 根据配置选择（简化：默认hero）
        HERO_IMAGE = HERO_IMAGE1;

        // 敌机
        Bitmap mob = BitmapFactory.decodeResource(context.getResources(), R.drawable.mob);
        Bitmap pro = BitmapFactory.decodeResource(context.getResources(), R.drawable.elite);
        Bitmap superPro = BitmapFactory.decodeResource(context.getResources(), R.drawable.eliteplus);
        Bitmap boss = BitmapFactory.decodeResource(context.getResources(), R.drawable.boss);

        // 子弹
        Bitmap heroBullet = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_hero);
        Bitmap enemyBullet = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_enemy);

        // 道具
        Bitmap heal = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_blood);
        Bitmap bomb = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bomb);
        Bitmap fire = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bullet);
        Bitmap firePlus = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bulletplus);

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), mob);
        CLASSNAME_IMAGE_MAP.put(ProEnemy.class.getName(), pro);
        CLASSNAME_IMAGE_MAP.put(SuperProEnemy.class.getName(), superPro);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), boss);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), heroBullet);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), enemyBullet);
        CLASSNAME_IMAGE_MAP.put(Heal.class.getName(), heal);
        CLASSNAME_IMAGE_MAP.put(Bomb.class.getName(), bomb);
        CLASSNAME_IMAGE_MAP.put(Fire.class.getName(), fire);
        CLASSNAME_IMAGE_MAP.put(FirePlus.class.getName(), firePlus);
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) return null;
        return get(obj.getClass().getName());
    }

    public static Bitmap getBackground() {
        return BACKGROUND_IMAGE;
    }
}