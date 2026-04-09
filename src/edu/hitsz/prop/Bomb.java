package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;
import edu.hitsz.dao.GameConfig;
import videos.MusicThread;

public class Bomb extends AbstractProp {
    public Bomb(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }
    @Override
    public void active(HeroAircraft hero) {
        System.out.println("BombSupply active!");
        playSoundEffect("src/videos/bomb_explosion.wav");

    }
    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }
    // 播放一次性音效的方法
    private void playSoundEffect(String soundFile) {
        if (GameConfig.soundOn) {
            new MusicThread(soundFile).start();
        }
    }
}