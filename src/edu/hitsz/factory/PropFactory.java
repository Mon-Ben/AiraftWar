package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;

public interface PropFactory {
    /**
     * 创建道具实例
     * @param locationX 初始 X 坐标
     * @param locationY 初始 Y 坐标
     * @param speedX 水平速度
     * @param speedY 垂直速度
     */
    AbstractProp createProp(int locationX, int locationY,
                            int speedX, int speedY);
}