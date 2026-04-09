package edu.hitsz.application;


import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.dao.GameConfig;
import edu.hitsz.prop.Bomb;
import edu.hitsz.prop.Fire;
import edu.hitsz.prop.FirePlus;
import edu.hitsz.prop.Heal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 综合管理图片的加载，访问
 * 提供图片的静态访问方法
 *
 * @author hitsz
 */
public class ImageManager {

    /**
     * 类名-图片 映射，存储各基类的图片 <br>
     * 可使用 CLASSNAME_IMAGE_MAP.get( obj.getClass().getName() ) 获得 obj 所属基类对应的图片
     */
    private static final Map<String, BufferedImage> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static BufferedImage BACKGROUND_IMAGE;
    public static BufferedImage HERO_IMAGE;
    public static BufferedImage HERO_IMAGE1;
    public static BufferedImage HERO_IMAGE2;
    public static BufferedImage HERO_IMAGE3;
    public static BufferedImage HERO_BULLET_IMAGE;
    public static BufferedImage ENEMY_BULLET_IMAGE;
    public static BufferedImage MOB_ENEMY_IMAGE;
    public static BufferedImage PRO_ENEMY_IMAGE;
    public static BufferedImage SUPER_ENEMY_IMAGE;
    public static BufferedImage BOSS_ENEMY_IMAGE;
    public static BufferedImage HEAL_IMAGE;
    public static BufferedImage BOMB_IMAGE;
    public static BufferedImage FIRE_IMAGE;
    public static BufferedImage FPLUS_IMAGE;


    static {
        try {
            if(Objects.equals(GameConfig.difficulty, "MEDIUM")) {
                BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg3.jpg"));
            }
            else if(Objects.equals(GameConfig.difficulty, "EASY")) {
                BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg1.jpg"));
            }
            else if(Objects.equals(GameConfig.difficulty, "HARD")) {
                BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg5.jpg"));
            }

            // 加载所有英雄机图片
            HERO_IMAGE1 = ImageIO.read(new FileInputStream("src/images/hero.png"));
            HERO_IMAGE2 = ImageIO.read(new FileInputStream("src/images/hero2.png"));
            HERO_IMAGE3 = ImageIO.read(new FileInputStream("src/images/hero3.png"));
            
            // 根据配置选择默认英雄机图片
            if (Objects.equals(GameConfig.heroImage, "hero2")) {
                HERO_IMAGE = HERO_IMAGE2;
            } else if (Objects.equals(GameConfig.heroImage, "hero3")) {
                HERO_IMAGE = HERO_IMAGE3;
            } else {
                HERO_IMAGE = HERO_IMAGE1;
            }
            MOB_ENEMY_IMAGE = ImageIO.read(new FileInputStream("src/images/mob.png"));
            PRO_ENEMY_IMAGE = ImageIO.read(new FileInputStream("src/images/elite.png"));
            SUPER_ENEMY_IMAGE = ImageIO.read(new FileInputStream("src/images/elitePlus.png"));
            BOSS_ENEMY_IMAGE = ImageIO.read(new FileInputStream("src/images/boss.png"));

            HERO_BULLET_IMAGE = ImageIO.read(new FileInputStream("src/images/bullet_hero.png"));
            ENEMY_BULLET_IMAGE = ImageIO.read(new FileInputStream("src/images/bullet_enemy.png"));

            HEAL_IMAGE = ImageIO.read(new FileInputStream("src/images/prop_blood.png"));
            BOMB_IMAGE = ImageIO.read(new FileInputStream("src/images/prop_bomb.png"));
            FIRE_IMAGE = ImageIO.read(new FileInputStream("src/images/prop_bullet.png"));
            FPLUS_IMAGE = ImageIO.read(new FileInputStream("src/images/prop_bulletPlus.png"));

            CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
            CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(ProEnemy.class.getName(), PRO_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(SuperProEnemy.class.getName(), SUPER_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(Heal.class.getName(), HEAL_IMAGE);
            CLASSNAME_IMAGE_MAP.put(Bomb.class.getName(), BOMB_IMAGE);
            CLASSNAME_IMAGE_MAP.put(Fire.class.getName(), FIRE_IMAGE);
            CLASSNAME_IMAGE_MAP.put(FirePlus.class.getName(), FPLUS_IMAGE);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static BufferedImage get(String className){
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static BufferedImage get(Object obj){
        if (obj == null){
            return null;
        }
        return get(obj.getClass().getName());
    }

}
