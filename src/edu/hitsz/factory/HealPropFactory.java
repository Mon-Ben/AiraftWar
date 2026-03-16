package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.Heal;

public class HealPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY,
                                   int speedX, int speedY) {
        return new Heal(locationX, locationY, speedX, speedY);
    }
}