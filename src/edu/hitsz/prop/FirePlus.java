package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.RingFireStrategy;

public class FirePlus extends AbstractProp {
    private static final long EFFECT_DURATION = 5000;
    public FirePlus(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active(HeroAircraft hero) {
        hero.setTemporaryFireStrategy(new RingFireStrategy(), EFFECT_DURATION);
        System.out.println("FirePlus active! 环形射击模式持续 " + (EFFECT_DURATION/1000) + " 秒");
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