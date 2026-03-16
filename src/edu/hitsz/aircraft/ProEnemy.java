package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.strategy.DirectFireStrategy;
import edu.hitsz.strategy.FireStrategy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 普通敌机
 * 不可射击
 *
 * @author hitsz
 */
public class ProEnemy extends AbstractAircraft {
    /**攻击方式 **/

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 3;

    /**
     * 子弹伤害
     */
    private int power = 20;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction = 1;


    public ProEnemy(int locationX, int locationY,int speedX, int speedY, int hp, int power, int shootNum, int direction) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = power;
        this.shootNum = shootNum;
        this.direction = direction;
        this.fireStrategy = new DirectFireStrategy();
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
