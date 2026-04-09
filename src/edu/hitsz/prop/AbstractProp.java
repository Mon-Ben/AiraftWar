package edu.hitsz.prop;


import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;

import java.awt.image.BufferedImage;

/**
 * 所有道具的抽象父类
 */
public abstract class AbstractProp extends AbstractFlyingObject {

    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    /** 道具生效逻辑，由子类实现 */
    public abstract void active(HeroAircraft hero);

}