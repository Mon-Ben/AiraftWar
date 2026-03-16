package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class DirectFireStrategy implements FireStrategy {
    @Override
    public List<BaseBullet> fire(AbstractAircraft plane) {
        List<BaseBullet> res = new LinkedList<>();
        if(plane instanceof HeroAircraft){
            int x = plane.getLocationX();
            int y = plane.getLocationY() - 2;          // 稍微向前
            int speedY = plane.getSpeedY() - 5;        // 向上
            int power  = ((HeroAircraft) plane).getPower();
            int num    = ((HeroAircraft) plane).getShootNum();          // 默认 2
            for (int i = 0; i < num; i++) {
                res.add(new HeroBullet(
                        x + (i * 2 - num + 1) * 10,
                        y, 0, speedY, power));
            }
        }
        else {
            int x = plane.getLocationX();
            int y = plane.getLocationY() + 2;
            int speedY = plane.getSpeedY() + 5;
            int shootNum = 2;
            int power = 20;
            for (int i = 0; i < shootNum; i++) {
                res.add(new EnemyBullet(
                        x + (i * 2 - shootNum + 1) * 10,
                        y, 0, speedY, power));
            }
        }
        return res;
    }
}