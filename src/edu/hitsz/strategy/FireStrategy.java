package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import java.util.List;

public interface FireStrategy {
    List<BaseBullet> fire(AbstractAircraft plane);

}