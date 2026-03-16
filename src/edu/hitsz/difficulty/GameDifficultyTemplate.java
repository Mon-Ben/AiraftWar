package edu.hitsz.difficulty;

import edu.hitsz.application.Game;

/**
 * 游戏难度模板：定义算法骨架，子类重写具体步骤
 */
public abstract class GameDifficultyTemplate {

    /* ========== 模板方法：算法骨架（final，不可重写） ========== */
    public final void applyDifficulty(Game game, int currentScore, int time) {
        // 1. 判断是否该提升难度
        if (shouldUpgrade(time, currentScore)) {
            upgradeFactors();
            printUpgradeInfo();
        }
        // 2. 应用当前难度参数
        applyCurrentParameters(game, currentScore);
    }

    /* ========== 抽象步骤：子类必须实现 ========== */
    protected abstract boolean shouldUpgrade(int time, int currentScore);
    protected abstract void upgradeFactors();
    protected abstract void applyCurrentParameters(Game game, int currentScore);

    /* ========== 工具方法：子类可复用 ========== */
    protected void printUpgradeInfo() {
        // 调用子类的具体实现来获取当前值
        System.out.printf("提高难度！精英机概率：%.2f，敌机周期：%d，敌机属性提升倍率：%.2f%n",
                getCurrentEliteProb(), getCurrentEnemyCycle(), getCurrentAttributeMultiplier());
    }

    /* ========== 新增：获取当前值的抽象方法 ========== */
    protected abstract double getCurrentEliteProb();
    protected abstract int getCurrentEnemyCycle();
    protected abstract double getCurrentAttributeMultiplier();

    /* ========== 可调因子：子类可覆盖 ========== */
    public int getEnemyMaxNumber() { return 5; }
    public int getEnemyCycle() { return 600; }
    public int getHeroShootCycle() { return 30; }
    public int getEnemyShootCycle() { return 120; }
    public double getEliteProb() { return 0.3; }
    public double getAttributeMultiplier() { return 1.0; }

    /* ========== Boss 规则：子类覆盖 ========== */
    public boolean hasBoss() { return false; }
    public boolean bossHpIncreases() { return false; }

    public double getBossHpMultiplier(int bossCount) {
        return bossHpIncreases() ? 1.0 + 0.2 * bossCount : 1.0;
    }
}