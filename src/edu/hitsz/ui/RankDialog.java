package edu.hitsz.ui;

import edu.hitsz.dao.GameConfig;
import edu.hitsz.dao.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RankDialog extends JDialog {
    private final ScoreDao dao = new FileScoreDao();
    private final String difficulty = GameConfig.difficulty;
    private JTable table;
    private final int currentScore;

    public RankDialog(JFrame owner, int currentScore) {
        super(owner, "排行榜 - " + GameConfig.difficulty, true);
        this.currentScore = currentScore;
        setSize(600, 450);
        setLocationRelativeTo(owner);

        // 直接显示排行榜（不包含当前成绩）
        showRankingTable(false);

        // 使用 Timer 延迟弹出输入对话框，确保排行榜先显示
        Timer timer = new Timer(100, e -> {
            showNameInputDialog();
            ((Timer)e.getSource()).stop();
        });
        timer.start();
    }

    /**
     * 显示排行榜
     * @param includeCurrentScore 是否包含当前成绩
     */
    private void showRankingTable(boolean includeCurrentScore) {
        // 如果需要包含当前成绩，先保存
        if (includeCurrentScore) {
        }

        getContentPane().removeAll();

        JPanel rankPanel = new JPanel(new BorderLayout());
        JLabel topLabel = new JLabel("游戏难度：" + difficulty, SwingConstants.LEFT);
        rankPanel.add(topLabel, BorderLayout.NORTH);

        // 表模型
        String[] col = {"名次", "玩家名", "得分", "记录时间"};
        List<ScoreItem> list = dao.listByDifficulty(difficulty);
        String[][] data = new String[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            ScoreItem it = list.get(i);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = it.getPlayerName();
            data[i][2] = String.valueOf(it.getScore());
            data[i][3] = it.getRankString();
        }

        table = new JTable(data, col);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel();
        JButton delBtn = new JButton("删除选中记录");
        delBtn.addActionListener(this::deleteSelected);
        south.add(delBtn);

        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(e -> dispose());
        south.add(okBtn);

        rankPanel.add(south, BorderLayout.SOUTH);

        add(rankPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * 显示输入玩家名的小弹窗
     */
    private void showNameInputDialog() {
        JDialog inputDialog = new JDialog(this, "记录成绩", true);
        inputDialog.setLayout(new BorderLayout());
        inputDialog.setSize(400, 200);
        inputDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 信息标签
        JLabel msg = new JLabel("游戏结束，您的得分为 " + currentScore + "，请输入您的游戏名：");
        contentPanel.add(msg, BorderLayout.NORTH);

        // 输入框
        JTextField nameField = new JTextField();
        contentPanel.add(nameField, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton okBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");

        // 确认按钮事件
        okBtn.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                playerName = "Player-" + System.currentTimeMillis() % 10000;
            }
            // 保存玩家成绩
            dao.addScore(playerName, currentScore);
            inputDialog.dispose();
            // 刷新并显示更新后的排行榜
            showRankingTable(true);
        });

        // 取消按钮事件
        cancelBtn.addActionListener(e -> {
            inputDialog.dispose();
            // 取消时不更新，保持当前排行榜显示
        });

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        inputDialog.add(contentPanel);
        inputDialog.getRootPane().setDefaultButton(okBtn);
        inputDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 自动弹出并聚焦到输入框
        inputDialog.setVisible(true);
        nameField.requestFocusInWindow();
    }

    private void deleteSelected(ActionEvent actionEvent) {
        if (table == null) return;

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行！");
            return;
        }
        int result = JOptionPane.showConfirmDialog(
                this,
                "是否确定删除选中的玩家？",
                "确认删除",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            dao.deleteByRank(difficulty, row + 1);
            // 删除后重新刷新表格
            showRankingTable(false);
        }
    }
}