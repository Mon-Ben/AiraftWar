package edu.hitsz.difficulty;

import edu.hitsz.application.Game;

public class MediumDifficulty extends GameDifficultyTemplate {

    private int upgradeCount = 0;
    private double eliteProb = 0.3;
    private int enemyCycle = 600;
    private double multiplier = 1.0;

    @Override
    protected boolean shouldUpgrade(int time, int currentScore) {

        return time / 10000 > upgradeCount;
    }

    @Override
    protected void upgradeFactors() {
        upgradeCount++;
        eliteProb = Math.min(0.5, eliteProb + 0.02);
        enemyCycle = Math.max(400, enemyCycle - 20);
        multiplier = Math.min(2.0, multiplier + 0.08);
    }

    @Override
    protected void applyCurrentParameters(Game game, int currentScore) {
        game.setEnemyMaxNumber(getEnemyMaxNumber() + upgradeCount);
        game.setEnemyCycle(enemyCycle);
        game.setHeroShootCycle(getHeroShootCycle() - upgradeCount * 3);
        game.setEnemyShootCycle(getEnemyShootCycle() - upgradeCount * 2);
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
    public double getBossHpMultiplier(int bossCount) {
        return 1.0;
    }
}