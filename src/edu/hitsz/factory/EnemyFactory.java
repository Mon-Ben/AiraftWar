package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;

public interface EnemyFactory {
    /**
     * 创建敌机实例
     * @param locationX 初始 X 坐标
     * @param locationY 初始 Y 坐标
     * @param speedX 水平速度
     * @param speedY 垂直速度
     * @param hp 生命值
     * @param power 子弹威力（部分机型用）
     */
    AbstractAircraft createEnemy(int locationX, int locationY,
                                 int speedX, int speedY,
                                 int hp, int power);
}