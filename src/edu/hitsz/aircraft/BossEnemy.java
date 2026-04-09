package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.strategy.RingFireStrategy;
import edu.hitsz.strategy.ScatterFireStrategy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Boss 敌机
 * 环射弹道：20 发环形，冷却 1000 ms
 */
public class BossEnemy extends AbstractAircraft {

    private static final int SHOOT_NUM = 20;
    private static final int POWER = 30;
    private static final int DIRECTION = 1;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.fireStrategy = new RingFireStrategy();
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT * 0.25) {
            speedY = 0; // 悬浮
        }
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }


}