package edu.hitsz.difficulty;

import edu.hitsz.application.Game;

public class HardDifficulty extends GameDifficultyTemplate {

    private int upgradeCount = 0;
    private double eliteProb = 0.3;
    private int enemyCycle = 600;
    private double multiplier = 1.0;

    @Override
    protected boolean shouldUpgrade(int time, int currentScore) {
        return time / 8000 > upgradeCount;
    }

    @Override
    protected void upgradeFactors() {
        upgradeCount++;
        eliteProb = Math.min(0.7, eliteProb + 0.03);
        enemyCycle = Math.max(300, enemyCycle - 25);
        multiplier = Math.min(3.0, multiplier + 0.12);
    }

    @Override
    protected void applyCurrentParameters(Game game, int currentScore) {
        game.setEnemyMaxNumber(getEnemyMaxNumber() + upgradeCount * 2);
        game.setEnemyCycle(enemyCycle);
        game.setHeroShootCycle(getHeroShootCycle() - upgradeCount * 5);
        game.setEnemyShootCycle(getEnemyShootCycle() - upgradeCount * 4);
        game.setEliteProb(eliteProb);
        game.setAttributeMultiplier(multiplier);
    }

    // 新增：返回当前值的实现
    @Override
    protected double getCurrentEliteProb() {
        return eliteProb;
    }

    @Override
    protected int getCurrentEnemyCycle() {
        return enemyCycle;
    }

    @Override
    protected double getCurrentAttributeMultiplier() {
        return multiplier;
    }

    @Override
    public boolean hasBoss() { return true; }

    @Override
    public boolean bossHpIncreases() { return true; }

    @Override
    public double getBossHpMultiplier(int bossCount) {
        return 1.0 + 0.3 * bossCount;
    }
}