package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import java.util.List;

/**
 * 白盒测试需要：让子类可以访问父类 protected 成员
 */
public class TestableHeroAircraft extends HeroAircraft {
    public TestableHeroAircraft(int x, int y, int sx, int sy, int hp) {
        super(x, y, sx, sy, hp);
    }
    /* 暴露父类方法方便白盒调用 */
    public int hp() { return hp; }
    public int maxHp() { return maxHp; }
}