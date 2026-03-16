package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.DirectFireStrategy;
import edu.hitsz.strategy.FireStrategy;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    private int shootNum = 2;

    private int power = 50;

    // 火力效果相关字段
    private FireStrategy originalFireStrategy; // 保存原始射击策略
    private ScheduledExecutorService effectExecutor; // 用于管理效果时间的线程池
    private ScheduledFuture<?> currentEffect; // 当前生效的效果
    /**
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */
    static HeroAircraft instance = null;

    HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setFireStrategy(new DirectFireStrategy());
        this.originalFireStrategy = new DirectFireStrategy(); // 保存原始策略
        this.effectExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
        }
        return instance;
    }
    @Override
    public void forward() {

    }

    public int getPower() {
        return power;
    }

    public int getShootNum() {
        return shootNum;
    }

    public void setFireStrategy(FireStrategy strategy) { this.fireStrategy = strategy; }

    public List<BaseBullet> shoot() {
        return super.shoot();
    }

    /**
     * 设置临时火力效果
     * @param temporaryStrategy 临时火力策略
     * @param duration 持续时间（毫秒）
     */
    public void setTemporaryFireStrategy(FireStrategy temporaryStrategy, long duration) {
        // 取消之前的效果（如果有）
        if (currentEffect != null && !currentEffect.isDone()) {
            currentEffect.cancel(true);
            System.out.println("取消之前的火力效果");
        }

        // 设置临时策略
        this.fireStrategy = temporaryStrategy;
        System.out.println("激活临时火力效果: " + temporaryStrategy.getClass().getSimpleName());

        // 创建定时任务恢复原始策略
        currentEffect = effectExecutor.schedule(new RestoreFireStrategyTask(), duration, TimeUnit.MILLISECONDS);
    }

    /**
     * 恢复原始射击策略的任务
     */
    private class RestoreFireStrategyTask implements Runnable {
        @Override
        public void run() {
            fireStrategy = originalFireStrategy;
            currentEffect = null;
            System.out.println("火力效果结束，恢复直射状态");
        }
    }

}
