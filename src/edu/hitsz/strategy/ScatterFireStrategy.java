package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class ScatterFireStrategy implements FireStrategy {
    @Override
    public List<BaseBullet> fire(AbstractAircraft plane) {
        List<BaseBullet> res = new LinkedList<>();
        if(plane instanceof HeroAircraft) {
            int x = plane.getLocationX();
            int y = plane.getLocationY() - 2;
            int speedY = plane.getSpeedY() - 5;
            int shootNum = 3;
            int power = 40;
            // 扇形角度：-30° ~ +30°
            double angleStep = 60.0 / (shootNum - 1);
            for (int i = 0; i < shootNum; i++) {
                double angle = Math.toRadians(-30 + i * angleStep);
                int sx = (int) (Math.sin(angle) * 8);
                int sy = speedY - (int) (Math.cos(angle) * 8);
                res.add(new HeroBullet(x, y, sx, sy, power));
            }
        }
        else{
            int x = plane.getLocationX();
            int y = plane.getLocationY() + 2;
            int speedY = plane.getSpeedY() + 5;
            int shootNum = 3;
            int power = 25;
            // 扇形角度：-30° ~ +30°
            double angleStep = 60.0 / (shootNum - 1);
            for (int i = 0; i < shootNum; i++) {
                double angle = Math.toRadians(-30 + i * angleStep);
                int sx = (int) (Math.sin(angle) * 5);
                int sy = speedY + (int) (Math.cos(angle) * 5);
                res.add(new EnemyBullet(x + (i - shootNum / 2) * 10, y, sx, sy, power));
            }
        }

        return res;
    }
}