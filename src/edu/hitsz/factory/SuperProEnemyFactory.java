package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.SuperProEnemy;

public class SuperProEnemyFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY,
                                        int speedX, int speedY,
                                        int hp, int power) {
        return new SuperProEnemy(locationX, locationY, speedX, speedY, hp);
    }
}