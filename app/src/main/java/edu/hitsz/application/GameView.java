package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import edu.hitsz.aircraft.*;
import edu.hitsz.audio.AudioManager;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.dao.GameConfig;
import edu.hitsz.difficulty.*;
import edu.hitsz.factory.*;
import edu.hitsz.observer.BombClearObserver;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.Bomb;

import java.util.LinkedList;
import java.util.List;
// 导入正确的 Handler
import android.os.Handler;
import android.os.Message;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder holder;
    private Thread gameThread;
    private boolean isRunning;
    private Canvas canvas;
    private Paint paint;
    private AudioManager audioManager;   // 音频管理器

    // 游戏逻辑成员（与原Game保持一致）
    private int backGroundTop = 0;
    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;
    private final BombClearObserver bombObserver = new BombClearObserver();

    // 敌机工厂
    private final EnemyFactory mobFactory = new MobEnemyFactory();
    private final EnemyFactory proFactory = new ProEnemyFactory();
    private final EnemyFactory superFactory = new SuperProEnemyFactory();
    private final EnemyFactory bossFactory = new BossEnemyFactory();

    // 道具工厂数组
    private final PropFactory[] propFactories = {
            new HealPropFactory(),
            new FirePropFactory(),
            new BombPropFactory(),
            new FirePlusPropFactory()
    };

    private int score = 0;
    private int time = 0;
    private int timeInterval = 16;// 帧间隔(ms)
    private int cycleDuration = 600;
    private int cycleTime = 0;
    private int shootCounter = 0;
    private int enemyShootCounter = 0;
    private int heroShootCycle = 6;
    private int enemyShootCycle = 12;
    private int enemyMaxNumber = 10;
    private double currentEliteProb = 0.3;
    private double currentAttributeMultiplier = 1.0;

    private final int bossThreshold = 500;
    private int bossTriggerCount = 0;

    private boolean gameOverFlag = false;
    private final GameDifficultyTemplate difficultyTemplate;
    private int screenWidth, screenHeight;

    private Handler uiHandler;  // 用于与主线程通信
    private int finalScore;     // 记录最终得分

    public GameView(Context context, Handler uiHandler) {
        super(context);
        this.uiHandler = uiHandler;
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);

        // 初始化音频管理器
        audioManager = AudioManager.getInstance(context);
        // 播放普通背景音乐（如果音效开启）
        audioManager.playBgm();

        // 初始化游戏对象
        HeroAircraft.reset(0, 0, 0, 0, 100);   // 确保全新实例
        heroAircraft = HeroAircraft.getInstance(
                0, 0, 0, 0, 100); // 初始位置稍后根据屏幕设置
        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        // 难度模板初始化（根据GameConfig）
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

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);

        // 设置触摸监听
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE ||
                    event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                // 边界限制
                if (x < 0 || x > screenWidth || y < 0 || y > screenHeight) {
                    return true;
                }
                heroAircraft.setLocation((int) x, (int) y);
            }
            return true;
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        // 更新全局屏幕尺寸，供 AbstractFlyingObject 使用
        AppSettings.setScreenSize(width, height);

        // 设置英雄机初始位置
        heroAircraft.setLocation(screenWidth / 2, screenHeight - 150);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 释放音频资源
        audioManager.release();
    }

    @Override
    public void run() {
        while (isRunning) {
            long startTime = System.currentTimeMillis();

            // 游戏逻辑更新
            updateGame();

            // 绘制
            drawGame();

            // 控制帧率
            long endTime = System.currentTimeMillis();
            long diff = endTime - startTime;
            if (diff < timeInterval) {
                try {
                    Thread.sleep(timeInterval - diff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateGame() {
        time += timeInterval;

        // 周期性判断（产生敌机、射击等）
        if (timeCountAndNewCycleJudge()) {
            // Boss生成
            if (shouldSpawnBoss()) {
                int bossX = (int) (Math.random() * (screenWidth - 200));
                int bossY = (int) (Math.random() * screenHeight * 0.05);
                double bossHpMultiplier = difficultyTemplate.getBossHpMultiplier(bossTriggerCount);
                int bossHp = (int)(600 * bossHpMultiplier);
                enemyAircrafts.add(bossFactory.createEnemy(bossX, bossY, 2, 2, bossHp, 30));
                // 切换为 Boss 背景音乐
                audioManager.playBossBgm();
            }
            // 产生敌机
            spawnEnemy();
            // 射击
            shootAction();
        }

        // 移动
        bulletsMoveAction();
        aircraftsMoveAction();
        propsMoveAction();

        // 难度参数更新
        difficultyTemplate.applyDifficulty(this, score, time);

        // 碰撞检测
        crashCheckAction();

        // 后处理（移除无效对象）
        postProcessAction();

        // 游戏结束判断
        if (heroAircraft.getHp() <= 0 && !gameOverFlag) {
            gameOverFlag = true;
            isRunning = false;
            // 播放游戏结束音效并停止背景音乐
            audioManager.playSound("game_over");
            audioManager.stopBgm();
            //日志调试
            finalScore = score;
            Log.d("GameView", "Game Over, finalScore=" + finalScore);  // 添加日志

            // 通过 Handler 通知主线程游戏结束
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = finalScore;
            uiHandler.sendMessage(msg);
        }
    }

    private void drawGame() {
        canvas = holder.lockCanvas();
        if (canvas == null) return;
        try {
            // 清屏
            canvas.drawColor(Color.BLACK);

            // 绘制背景（平铺滚动，无间隔）
// 绘制背景（平铺滚动，使用 Rect 拉伸确保覆盖全屏）
            Bitmap bg = ImageManager.getBackground();
            if (bg != null) {
                int bgHeight = bg.getHeight();
                int screenH = screenHeight > 0 ? screenHeight : AppSettings.WINDOW_HEIGHT;

                // 滚动偏移量，不断增加
                backGroundTop += 2;
                // 取模，让偏移量在 0~bgHeight 之间循环
                int offset = (int) (backGroundTop % bgHeight);

                // 需要绘制的张数：至少 ceil(screenH / bgHeight) + 1
                int drawCount = (screenH + bgHeight - 1) / bgHeight + 1;

                for (int i = 0; i < drawCount; i++) {
                    int y = offset + i * bgHeight - bgHeight; // 让第一张图从负数开始，保证屏幕上方有图
                    Rect dstRect = new Rect(0, y, screenWidth, y + bgHeight);
                    canvas.drawBitmap(bg, null, dstRect, null);
                }
            }

            // 绘制子弹、道具、敌机
            paintObjects(canvas, enemyBullets);
            paintObjects(canvas, heroBullets);
            paintObjects(canvas, props);
            paintObjects(canvas, enemyAircrafts);

            // 绘制英雄机
            Bitmap heroImg = ImageManager.get(HeroAircraft.class.getName());
            if (heroImg != null) {
                canvas.drawBitmap(heroImg,
                        heroAircraft.getLocationX() - heroImg.getWidth() / 2f,
                        heroAircraft.getLocationY() - heroImg.getHeight() / 2f,
                        null);
            }

            // 绘制得分和生命值
            paint.setTextSize(50);
            paint.setColor(Color.WHITE);
            canvas.drawText("Score: " + score, 20, 80, paint);
            canvas.drawText("Life: " + heroAircraft.getHp(), 20, 140, paint);

            if (gameOverFlag) {
                paint.setTextSize(100);
                canvas.drawText("GAME OVER", screenWidth/2 - 200, screenHeight/2, paint);
            }

        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void paintObjects(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject obj : objects) {
            Bitmap img = ImageManager.get(obj.getClass().getName());
            if (img != null && !obj.notValid()) {
                canvas.drawBitmap(img,
                        obj.getLocationX() - img.getWidth() / 2f,
                        obj.getLocationY() - img.getHeight() / 2f,
                        null);
            }
        }
    }

    // ---------- 以下为原有Game中的逻辑方法，基本不变（仅移除了Swing相关和音频）----------
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
            int initX = (int) (Math.random() * (screenWidth - 100));
            int initY = (int) (Math.random() * screenHeight * 0.05);

            if (rand < (1 - currentEliteProb)) {
                int baseHp = (int)(30 * currentAttributeMultiplier);
                int speed = (int)(8 * currentAttributeMultiplier);
                enemyAircrafts.add(mobFactory.createEnemy(initX, initY, 0, speed, baseHp, 0));
            } else if (rand < (1 - currentEliteProb * 0.4)) {
                int baseHp = (int)(60 * currentAttributeMultiplier);
                int speed = (int)(5 * currentAttributeMultiplier);
                enemyAircrafts.add(proFactory.createEnemy(initX, initY, Math.random() > 0.5 ? 2 : -2, speed, baseHp, 20));
            } else {
                int baseHp = (int)(80 * currentAttributeMultiplier);
                int speed = (int)(4 * currentAttributeMultiplier);
                enemyAircrafts.add(superFactory.createEnemy(initX, initY, Math.random() > 0.5 ? 3 : -3, speed, baseHp, 25));
            }
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        }
        return false;
    }

    private void shootAction() {
        if (shootCounter >= heroShootCycle) {
            heroBullets.addAll(heroAircraft.shoot());
            shootCounter = 0;
        } else {
            shootCounter++;
        }

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
        for (BaseBullet bullet : heroBullets) bullet.forward();
        for (BaseBullet bullet : enemyBullets) bullet.forward();
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemy : enemyAircrafts) enemy.forward();
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) prop.forward();
    }

    private void crashCheckAction() {
        // 敌机子弹击中英雄
        for (BaseBullet ebullet : enemyBullets) {
            if (ebullet.notValid()) continue;
            if (heroAircraft.crash(ebullet)) {
                heroAircraft.decreaseHp(ebullet.getPower());
                ebullet.vanish();
            }
        }

        // 英雄子弹击中敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) continue;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy.notValid()) continue;
                if (enemy.crash(bullet)) {
                    enemy.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    // 播放击中音效
                    audioManager.playSound("bullet_hit");
                    if (enemy.notValid()) {
                        if (enemy instanceof ProEnemy) {
                            if (Math.random() < 0.5) randomProp(enemy.getLocationX(), enemy.getLocationY());
                            score += 20;
                        } else if (enemy instanceof SuperProEnemy) {
                            if (Math.random() < 0.3) {
                                randomProp(enemy.getLocationX()+50, enemy.getLocationY()-50);
                                randomProp(enemy.getLocationX()-50, enemy.getLocationY()+50);
                            }
                            score += 25;
                        } else if (enemy instanceof BossEnemy) {
                            randomProp(enemy.getLocationX(), enemy.getLocationY());
                            randomProp(enemy.getLocationX()+50, enemy.getLocationY()+50);
                            randomProp(enemy.getLocationX()-50, enemy.getLocationY()-50);
                            score += 30;
                            // Boss 死亡，恢复普通背景音乐
                            audioManager.resumeNormalBgm();
                        } else {
                            score += 10;
                        }
                    }
                    // 英雄与敌机相撞
                    if (enemy.crash(heroAircraft) || heroAircraft.crash(enemy)) {
                        enemy.vanish();
                        heroAircraft.decreaseHp(Integer.MAX_VALUE);
                    }
                }
            }
        }

        // 英雄获得道具
        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop)) {
                prop.active(heroAircraft);
                // 播放获取道具音效
                audioManager.playSound("get_supply");
                if (prop instanceof Bomb) {
                    score = bombObserver.onBombClear(enemyAircrafts, enemyBullets, heroAircraft, score);
                    // 炸弹清屏音效
                    audioManager.playSound("bomb_explode");
                }
                prop.vanish();
            }
        }
    }

    private void randomProp(int x, int y) {
        double r = Math.random();
        PropFactory factory = propFactories[r < 0.1 ? 2 : r < 0.6 ? 1 : r < 0.8 ? 0 : 3];
        props.add(factory.createProp(x, y, 0, 4));
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }

    // 供难度模板调用的setter
    public void setEnemyMaxNumber(int max) { this.enemyMaxNumber = max; }
    public void setEnemyCycle(int cycleMs) { this.cycleDuration = cycleMs; }
    public void setHeroShootCycle(int cycleMs) { this.heroShootCycle = cycleMs / timeInterval; }
    public void setEnemyShootCycle(int cycleMs) { this.enemyShootCycle = cycleMs / timeInterval; }
    public void setEliteProb(double prob) { this.currentEliteProb = prob; }
    public void setAttributeMultiplier(double multiplier) { this.currentAttributeMultiplier = multiplier; }

    // 供 Activity 调用的生命周期方法
    public void pauseGame() {
        audioManager.pauseBgm();
    }

    public void resumeGame() {
        audioManager.resumeBgm();
    }


}