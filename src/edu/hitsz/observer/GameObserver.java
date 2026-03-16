package edu.hitsz.observer;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import java.util.List;

/**
 * 游戏事件观察者：炸弹清除时回调
 */
public interface GameObserver {
    /**
     * 炸弹激活，要求清除敌机和敌机子弹
     * @param enemyAircrafts 当前敌机列表（可修改）
     * @param enemyBullets   当前敌机子弹列表（可修改）
     * @param hero           英雄机（用于加分）
     *
     */
    int onBombClear(List<AbstractAircraft> enemyAircrafts,
                                       List<BaseBullet> enemyBullets,
                                       HeroAircraft hero,int score);
}