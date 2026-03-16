package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.Bomb;

public class BombPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY,
                                   int speedX, int speedY) {
        return new Bomb(locationX, locationY, speedX, speedY);
    }
}