package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeroAircraftTest {

    private HeroAircraft hero;

    @BeforeEach
    void setUp() {
        // 每次测试前重新拿单例，避免状态污染
        HeroAircraft.instance = null;
        hero = HeroAircraft.getInstance(256, 700, 0, 0, 100);
    }

    @Test
    @DisplayName("shoot")
    void shootBlackBox() {
        List<BaseBullet> bullets = hero.shoot();
        assertNotNull(bullets);
        assertEquals(2, bullets.size(), "默认应发射 2 发");
        bullets.forEach(b -> assertTrue(b.getSpeedY() < 0, "子弹应向上"));
    }

    @Test
    @DisplayName("decreaseHp")
    void decreaseHpNormal() {
        hero.decreaseHp(30);
        assertEquals(70, hero.getHp());
    }

    @Test
    @DisplayName("decreaseHp")
    void decreaseHpToZero() {
        hero.decreaseHp(100);
        assertEquals(0, hero.getHp());
        assertTrue(hero.notValid(), "血量为 0 时应标记失效");
    }

    @Test
    @DisplayName("decreaseHp")
    void decreaseHpOver() {
        hero.decreaseHp(200);
        assertEquals(0, hero.getHp(), "不能扣成负数");
        assertTrue(hero.notValid());
    }


    @ParameterizedTest
    @CsvSource({"0,100", "50,50", "99,1", "100,0"})
    @DisplayName("getHp")
    void getHpBlackBox(int damage, int expect) {
        hero.decreaseHp(damage);
        assertEquals(expect, hero.getHp());
    }

    @Test
    @DisplayName("单例")
    void singletonIdentity() {
        HeroAircraft h2 = HeroAircraft.getInstance(0, 0, 0, 0, 0);
        assertSame(hero, h2, "必须全局唯一");
    }
}