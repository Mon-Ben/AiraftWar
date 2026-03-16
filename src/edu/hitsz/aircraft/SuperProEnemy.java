package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ScatterFireStrategy;

import java.util.List;

/**
 * 超级精英敌机
 * 散射弹道：5 发扇形
 */
public class SuperProEnemy extends AbstractAircraft {

    private static final int SHOOT_NUM = 5;
    private static final int POWER = 25;
    private static final int DIRECTION = 1;

    public SuperProEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.fireStrategy = new ScatterFireStrategy();
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }
}