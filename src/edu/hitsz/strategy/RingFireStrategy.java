package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class RingFireStrategy implements FireStrategy {
    @Override
    public List<BaseBullet> fire(AbstractAircraft plane) {
        List<BaseBullet> res = new LinkedList<>();
        if(plane instanceof HeroAircraft) {
            int x = plane.getLocationX();
            int y = plane.getLocationY();
            int shoot_num = 20;
            int power = 50;
            double angleStep = 2 * Math.PI / shoot_num;
            for (int i = 0; i < shoot_num; i++) {
                double angle = i * angleStep;
                int sx = (int) (Math.cos(angle) * 8);
                int sy = (int) (Math.sin(angle) * 8) ;
                res.add(new HeroBullet(x, y, sx, sy, power));
            }
        }
        else{
            int x = plane.getLocationX();
            int y = plane.getLocationY() +  2;
            int shoot_num = 20;
            int power = 30;
            double angleStep = 2 * Math.PI / shoot_num;
            for (int i = 0; i < shoot_num; i++) {
                double angle = i * angleStep;
                int sx = (int) (Math.cos(angle) * 10);
                int sy = (int) (Math.sin(angle) * 10);
                res.add(new EnemyBullet(x, y, sx, sy, power));
            }
        }
        return res;
    }
}