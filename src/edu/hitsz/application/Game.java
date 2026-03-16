package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.dao.FileScoreDao;
import edu.hitsz.dao.GameConfig;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.difficulty.EasyDifficulty;
import edu.hitsz.difficulty.GameDifficultyTemplate;
import edu.hitsz.difficulty.HardDifficulty;
import edu.hitsz.difficulty.MediumDifficulty;
import edu.hitsz.factory.*;

import edu.hitsz.observer.BombClearObserver;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.Bomb;
import edu.hitsz.ui.RankDialog;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import videos.MusicThread;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;


/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;
    // 音效相关变量
    private MusicThread bgmThread;
    private MusicThread bossBgmThread;
    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    /** Boss 触发阈值与触发次数 */
    private final int bossThreshold = 500;   // 每 500 分触发 1 次
    private int bossTriggerCount = 0;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;

    private final BombClearObserver bombObserver = new BombClearObserver();
    /**
     * 屏幕中出现的敌机最大数量
     */
    private int enemyMaxNumber = 10;
    //敌机工厂
    final EnemyFactory mobFactory   = new MobEnemyFactory();
    final EnemyFactory proFactory   = new ProEnemyFactory();
    final EnemyFactory superFactory = new SuperProEnemyFactory();
    final EnemyFactory bossFactory  = new BossEnemyFactory();

    //道具工厂数组，用于掉落时随机选择
    final PropFactory[] propFactories = {
            new HealPropFactory(),
            new FirePropFactory(),
            new BombPropFactory()
    };
    /**
     * 当前得分
     */
    private int score = 0;
    /**
     * 当前时刻
     */
    private int time = 0;

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    private int cycleDuration = 600;
    private int cycleTime = 0;
    private int shootCounter = 0;        // 英雄机射击计数
    private int enemyShootCounter = 0;   // 敌机射击计数
    private int heroShootCycle = 6;      // 英雄机射击周期（ms → 计数次数）
    private int enemyShootCycle = 12;     // 敌机射击周期（ms → 计数次数）
    /**
     * 游戏结束标志
     */
    private boolean gameOverFlag = false;

    private final GameDifficultyTemplate difficultyTemplate;
    private double currentEliteProb = 0.3;
    private double currentAttributeMultiplier = 1.0;

    public Game() {
        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight() ,
                0, 0, 100);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props        = new LinkedList<>();
        /**
         * Scheduled 线程池，用于定时任务调度
         * 关于alibaba code guide：可命名的 ThreadFactory 一般需要第三方包
         * apache 第三方库： org.apache.commons.lang3.concurrent.BasicThreadFactory
         */
        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

        switch (GameConfig.difficulty) {
            case "EASY":
                difficultyTemplate = new EasyDifficulty();
                break;
            case "HARD":
                difficultyTemplate = new HardDifficulty();
                break;
            default:
                difficultyTemplate = new MediumDifficulty();
        }

        difficultyTemplate.applyDifficulty(this, 0, 0);
    }


    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {
        startBackgroundMusic();

        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {

            time += timeInterval;

            // 周期性执行（控制频率）
            if (timeCountAndNewCycleJudge()) {
                System.out.println(time);
                //BOSS机生成判断
                if (shouldSpawnBoss()) {
                    int bossX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth()));
                    int bossY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
                    double bossHpMultiplier = difficultyTemplate.getBossHpMultiplier(bossTriggerCount);
                    int bossHp = (int)(600 * bossHpMultiplier);
                    enemyAircrafts.add(bossFactory.createEnemy(bossX, bossY, 2, 2, bossHp, 30));
                    System.out.printf(">>> Boss Appears! Score=%d, HP=%.1f倍%n", score, bossHpMultiplier);
                    startBossMusic();
                }
                // 新敌机产生
                spawnEnemy();

                // 飞机射出子弹
                shootAction();
            }

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            //道具移动
            propsMoveAction();

            difficultyTemplate.applyDifficulty(this, score, time);

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            //每个时刻重绘界面
            repaint();

            // 游戏结束检查英雄机是否存活
            if (heroAircraft.getHp() <= 0) {
                stopAllMusic();
                /* =====  排行榜 DAO 写入 + Swing 界面  ===== */
                ScoreDao scoreDao = new FileScoreDao();
                // 弹出 Swing 排行榜（自动写入当前局）
                new RankDialog((JFrame) SwingUtilities.getWindowAncestor(Game.this), score).setVisible(true);
                //  & 设置结束标志
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
            }
        };
        /**
         * 以固定延迟时间进行执行
         * 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    //***********************
    //      Action 各部分
    //***********************
    private boolean shouldSpawnBoss() {
        int expectedTriggers = score / bossThreshold;
        if (expectedTriggers > bossTriggerCount && difficultyTemplate.hasBoss()) {
            bossTriggerCount = expectedTriggers;
            return true;
        }
        return false;
    }

    private void spawnEnemy() {
        if (enemyAircrafts.size() < enemyMaxNumber) {
            double rand = Math.random();
            EnemyFactory factory;
            int initX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth()));
            int initY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);

            // 使用当前难度设置的精英概率
            if (rand < (1 - currentEliteProb)) {
                // 普通敌机
                int baseHp = (int)(30 * currentAttributeMultiplier);
                int speed = (int)(8 * currentAttributeMultiplier);
                enemyAircrafts.add(mobFactory.createEnemy(initX, initY, 0, speed, baseHp, 0));
            } else if (rand < (1 - currentEliteProb * 0.4)) {
                // 精英机 (60%的精英概率部分)
                int baseHp = (int)(60 * currentAttributeMultiplier);
                int speed = (int)(5 * currentAttributeMultiplier);
                enemyAircrafts.add(proFactory.createEnemy(initX, initY, Math.random() > 0.5 ? 2 : -2, speed, baseHp, 20));
            } else {
                // 超级精英 (40%的精英概率部分)
                int baseHp = (int)(80 * currentAttributeMultiplier);
                int speed = (int)(4 * currentAttributeMultiplier);
                enemyAircrafts.add(superFactory.createEnemy(initX, initY, Math.random() > 0.5 ? 3 : -3, speed, baseHp, 25));
            }
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            // 跨越到新的周期
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    private void shootAction() {
        /* 1. 英雄射击：每 heroShootCycle 次循环射一次 */
        if (shootCounter >= heroShootCycle) {
            heroBullets.addAll(heroAircraft.shoot());
            shootCounter = 0;
        } else {
            shootCounter++;
        }

        /* 2. 敌机射击：每 enemyShootCycle 次循环射一次 */
        if (enemyShootCounter >= enemyShootCycle) {
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (!enemy.notValid()) {
                    enemyBullets.addAll(enemy.shoot());
                }
            }
            enemyShootCounter = 0;
        } else {
            enemyShootCounter++;
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }
    private void propsMoveAction() {
        for(AbstractProp  prop : props) {
            prop.forward();
        }
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        for (BaseBullet ebullet : enemyBullets) {
            if (ebullet.notValid()) {
                continue;
            }
                if (heroAircraft.crash(ebullet)) {
                    heroAircraft.decreaseHp(ebullet.getPower());
                    ebullet.vanish();

                }
        }
            // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    playSoundEffect("src/videos/bullet_hit.wav");
                    if (enemyAircraft.notValid()) {
                        if (enemyAircraft instanceof ProEnemy) {
                            if(Math.random() < 0.5) {
                                randomProp(enemyAircraft.getLocationX(),enemyAircraft.getLocationY());
                            }
                            score += 20;
                        }
                        if (enemyAircraft instanceof SuperProEnemy ) {
                            if(Math.random() < 0.3) {
                                randomProp(enemyAircraft.getLocationX()+50,enemyAircraft.getLocationY()-50);
                                randomProp(enemyAircraft.getLocationX()-50,enemyAircraft.getLocationY()+50);
                            }
                            score += 25;
                        }
                        if (enemyAircraft instanceof BossEnemy) {
                            randomProp(enemyAircraft.getLocationX(),enemyAircraft.getLocationY());
                            randomProp(enemyAircraft.getLocationX()+50,enemyAircraft.getLocationY()+50);
                            randomProp(enemyAircraft.getLocationX()-50,enemyAircraft.getLocationY()-50);
                            score += 30;
                            stopBossMusic();
                        }
                        else{score += 10;}
                    }
                    // 英雄机 与 敌机 相撞，均损毁
                    if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                        enemyAircraft.vanish();
                        heroAircraft.decreaseHp(Integer.MAX_VALUE);
                    }
                }
            }
        }
        // Todo: 我方获得道具，道具生效
        for(AbstractProp  prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if(heroAircraft.crash(prop)) {
                prop.active(heroAircraft);
                if(prop instanceof Bomb)
                    score = bombObserver.onBombClear(enemyAircrafts, enemyBullets, heroAircraft,score);
                prop.vanish();
                playSoundEffect("src/videos/get_supply.wav");
            }
        }
    }
    /* 辅助：随机生成一种道具 */
    private void randomProp(int x, int y){
        PropFactory[] propFactories = {
                new HealPropFactory(),
                new FirePropFactory(),
                new BombPropFactory(),
                new FirePlusPropFactory()
        };
        double r = Math.random();
        PropFactory factory = propFactories[r < 0.1 ? 2 : r < 0.6 ? 1 : r < 0.8 ? 0 : 3];
        props.add(factory.createProp(x, y, 0, 4));
    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * <p>
     * 无效的原因可能是撞击或者飞出边界
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractProp::notValid);
    }


    //***********************
    //      Paint 各部分
    //***********************

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param  g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, props);
        paintImageWithPositionRevised(g, enemyAircrafts);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        //绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }

    // 在游戏开始时播放背景音乐
    private void startBackgroundMusic() {
        MusicThread bgmThread = null;
        if (GameConfig.soundOn && bgmThread == null) {
            bgmThread = new MusicThread("src/videos/bgm.wav", true); // 循环播放
            bgmThread.start();
        }
    }
    // 在游戏结束时停止所有音乐
    private void stopAllMusic() {
        if (bgmThread != null) {
            bgmThread.stopMusic();
            bgmThread = null;
        }
        if (bossBgmThread != null) {
            bossBgmThread.stopMusic();
            bossBgmThread = null;
        }
        // 播放游戏结束音效
        if (GameConfig.soundOn) {
            new MusicThread("src/videos/game_over.wav").start();
        }
    }
    // Boss出场时播放Boss音乐
    private void startBossMusic() {
        if (GameConfig.soundOn) {
            // 停止普通背景音乐
            if (bgmThread != null) {
                bgmThread.stopMusic();
                bgmThread = null;
            }
            // 播放Boss音乐
            bossBgmThread = new MusicThread("src/videos/bgm_boss.wav", true);
            bossBgmThread.start();
        }
    }

    // Boss坠毁时停止Boss音乐，恢复普通背景音乐
    private void stopBossMusic() {
        if (bossBgmThread != null) {
            bossBgmThread.stopMusic();
            bossBgmThread = null;
        }
        // 恢复普通背景音乐
        startBackgroundMusic();
    }

    // 播放一次性音效的方法
    private void playSoundEffect(String soundFile) {
        if (GameConfig.soundOn) {
            new MusicThread(soundFile).start();
        }
    }

    /* ===== 供模板模式调用的 setter（零业务侵入） ===== */
    public void setEnemyMaxNumber(int max) {
        this.enemyMaxNumber = max;
    }

    public void setEnemyCycle(int cycleMs) {
        this.cycleDuration = cycleMs;
    }

    public void setHeroShootCycle(int cycleMs) {
        this.heroShootCycle = cycleMs / timeInterval;
    }

    public void setEnemyShootCycle(int cycleMs) {
        this.enemyShootCycle = cycleMs / timeInterval;
    }

    public void setEliteProb(double prob) {
        this.currentEliteProb = prob;
    }

    public void setAttributeMultiplier(double multiplier) {
        this.currentAttributeMultiplier = multiplier;
    }
}
