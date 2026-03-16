package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.ScatterFireStrategy;

public class Fire extends AbstractProp {
    private static final long EFFECT_DURATION = 6000;
    public Fire(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }
    @Override
    public void active(HeroAircraft hero) {
        // 使用临时火力策略，持续 EFFECT_DURATION 毫秒
        hero.setTemporaryFireStrategy(new ScatterFireStrategy(), EFFECT_DURATION);
        System.out.println("FireSupply active! 散射模式持续 " + (EFFECT_DURATION/1000) + " 秒");
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