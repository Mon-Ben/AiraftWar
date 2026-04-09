package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.FirePlus;

public class FirePlusPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY,
                                   int speedX, int speedY) {
        return new FirePlus(locationX, locationY, speedX, speedY);
    }
}