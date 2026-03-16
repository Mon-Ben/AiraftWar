package edu.hitsz.observer;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BombClearObserver implements GameObserver {

    @Override
    public int onBombClear(List<AbstractAircraft> enemyAircrafts,
                                              List<BaseBullet> enemyBullets,
                                              HeroAircraft hero,int score) {
        List<AbstractAircraft> cleared = new ArrayList<>();

        // 1. 清除敌机（区分类型）
        Iterator<AbstractAircraft> it = enemyAircrafts.iterator();
        while (it.hasNext()) {
            AbstractAircraft enemy = it.next();
            if (enemy.notValid()) continue;

            if (enemy instanceof BossEnemy) {
                // Boss：不受影响，跳过
                continue;
            } else if (enemy instanceof SuperProEnemy) {
                // 超级精英：血量减少 50%，若 ≤0 则清除
                enemy.decreaseHp(enemy.getHp() / 2);
                if (enemy.notValid()) {
                    cleared.add(enemy);
                    it.remove();
                    score += 25;
                }
            }
            else if(enemy instanceof ProEnemy){
                score += 20;
                enemy.vanish();
                cleared.add(enemy);
                it.remove();
            }
            else {
                score += 10;
                enemy.vanish();
                cleared.add(enemy);
                it.remove();
            }
        }

        // 2. 清除敌机子弹
        Iterator<BaseBullet> bulletIt = enemyBullets.iterator();
        while (bulletIt.hasNext()) {
            BaseBullet b = bulletIt.next();
            if (b instanceof EnemyBullet) {
                b.vanish();
                bulletIt.remove();
            }
        }
        return score;
    }
}