package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.ProEnemy;

public class ProEnemyFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY,
                                        int speedX, int speedY,
                                        int hp, int power) {
        int shootNum = 3;
        int direction = 1;
        return new ProEnemy(locationX, locationY, speedX, speedY,
                hp, power, shootNum, direction);
    }
}