package edu.hitsz.difficulty;

import edu.hitsz.application.Game;

public class EasyDifficulty extends GameDifficultyTemplate {

    @Override
    protected boolean shouldUpgrade(int time, int currentScore) {
        return false;
    }

    @Override
    protected void upgradeFactors() {
        // 无提升
    }

    @Override
    protected void applyCurrentParameters(Game game, int currentScore) {
        game.setEnemyMaxNumber(getEnemyMaxNumber());
        game.setEnemyCycle(getEnemyCycle());
        game.setHeroShootCycle(getHeroShootCycle());
        game.setEnemyShootCycle(getEnemyShootCycle());
        game.setEliteProb(getEliteProb());
        game.setAttributeMultiplier(getAttributeMultiplier());
    }

    // 新增：返回当前值的实现（简单难度始终返回初始值）
    @Override
    protected double getCurrentEliteProb() {
        return getEliteProb();
    }

    @Override
    protected int getCurrentEnemyCycle() {
        return getEnemyCycle();
    }

    @Override
    protected double getCurrentAttributeMultiplier() {
        return getAttributeMultiplier();
    }

    @Override
    public boolean hasBoss() { return false; }
}