package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.MobEnemy;

public class MobEnemyFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY,
                                        int speedX, int speedY,
                                        int hp, int power) {
        return new MobEnemy(locationX, locationY, speedX, speedY, hp);
    }
}