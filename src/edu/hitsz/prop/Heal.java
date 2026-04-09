package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;

public class Heal extends AbstractProp {


    public Heal(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);

    }

    @Override
    public void active(HeroAircraft hero) {
        hero.hp = Math.min(hero.getHp() + 30, hero.maxHp);      // HeroAircraft 把 hp 设成 protected 即可
        System.out.println("HpSupply active!");
    }
    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }
}