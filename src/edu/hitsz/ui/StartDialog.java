package edu.hitsz.ui;

import edu.hitsz.dao.GameConfig;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import javax.imageio.ImageIO;

public class StartDialog extends JDialog {
    private final JButton easyBtn   = new JButton("简单模式");
    private final JButton mediumBtn = new JButton("普通模式");
    private final JButton hardBtn   = new JButton("困难模式");
    private final JComboBox<String> soundCombo = new JComboBox<>(new String[]{"音效：开", "音效：关"});
    
    // 英雄机选择相关组件
    private final JRadioButton hero1Btn = new JRadioButton("战机1");
    private final JRadioButton hero2Btn = new JRadioButton("战机2");
    private final JRadioButton hero3Btn = new JRadioButton("战机3");
    private final JLabel hero1Label = new JLabel();
    private final JLabel hero2Label = new JLabel();
    private final JLabel hero3Label = new JLabel();
    private final ButtonGroup heroGroup = new ButtonGroup();

    public StartDialog(JFrame owner) {
        super(owner, "游戏设置", true);
        setSize(480, 854);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //背景图片
        try {
            ImageIcon bgIcon = new ImageIcon("src/images/start_bg.jpg");   // ① 背景图片路径
            JLabel bgLabel = new JLabel(bgIcon);
            bgLabel.setLayout(new BorderLayout()); // 允许叠加组件
            setContentPane(bgLabel);               // 替换整个内容面板
        } catch (Exception e) {
            e.printStackTrace();
            // 若无背景图，保持原样（不阻塞）
        }
        //标题
        JLabel title = new JLabel("全民飞机大战", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 48));      // ② 大字体
        title.setForeground(new Color(0, 245, 255));         // ③ 紫色（RGB: 138,43,226）
        title.setOpaque(false);
        
        // 初始化英雄机选择面板
        initHeroSelectionPanel();

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);                         // 透明，露出背景
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(title);                               // 标题放第一行
        centerPanel.add(Box.createVerticalStrut(30));         // 垂直间隔
        
        // 英雄机选择标签
        JLabel heroSelectLabel = new JLabel("选择你的战机：", SwingConstants.CENTER);
        heroSelectLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        heroSelectLabel.setForeground(Color.WHITE);
        heroSelectLabel.setOpaque(false);
        centerPanel.add(heroSelectLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        
        // 英雄机选择区域
        JPanel heroPanel = new JPanel();
        heroPanel.setOpaque(false);
        heroPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // 战机1选择区域
        JPanel hero1Panel = new JPanel();
        hero1Panel.setOpaque(false);
        hero1Panel.setLayout(new BoxLayout(hero1Panel, BoxLayout.Y_AXIS));
        hero1Panel.add(hero1Label);
        hero1Panel.add(Box.createVerticalStrut(5));
        hero1Panel.add(hero1Btn);
        
        // 战机2选择区域
        JPanel hero2Panel = new JPanel();
        hero2Panel.setOpaque(false);
        hero2Panel.setLayout(new BoxLayout(hero2Panel, BoxLayout.Y_AXIS));
        hero2Panel.add(hero2Label);
        hero2Panel.add(Box.createVerticalStrut(5));
        hero2Panel.add(hero2Btn);
        
        // 战机3选择区域
        JPanel hero3Panel = new JPanel();
        hero3Panel.setOpaque(false);
        hero3Panel.setLayout(new BoxLayout(hero3Panel, BoxLayout.Y_AXIS));
        hero3Panel.add(hero3Label);
        hero3Panel.add(Box.createVerticalStrut(5));
        hero3Panel.add(hero3Btn);
        
        heroPanel.add(hero1Panel);
        heroPanel.add(hero2Panel);
        heroPanel.add(hero3Panel);
        
        centerPanel.add(heroPanel);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // 难度选择面板
        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setOpaque(false);
        difficultyPanel.setLayout(new GridLayout(0, 1, 10, 10));
        difficultyPanel.add(easyBtn);
        difficultyPanel.add(mediumBtn);
        difficultyPanel.add(hardBtn);
        
        centerPanel.add(difficultyPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(soundCombo);

        // 按钮半透明+圆角（模拟艺术按钮）
        JButton[] btns = {easyBtn, mediumBtn, hardBtn};
        for (JButton b : btns) {
            b.setFont(new Font("微软雅黑", Font.BOLD, 22));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(138, 43, 226, 200)); // 紫色 + 半透明
            b.setOpaque(false);                      // 必须透明
            b.setContentAreaFilled(false);           // 去掉填充
            b.setBorderPainted(false);               // 去掉边框
            b.setFocusPainted(false);
            b.setRolloverEnabled(true);              // 悬停高亮
        }
        easyBtn.setBounds(120, 600, 240, 50);
        mediumBtn.setBounds(120, 670, 240, 50);
        hardBtn.setBounds(120, 740, 240, 50);

        soundCombo.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        soundCombo.setBounds(120, 820, 240, 40);
        soundCombo.setOpaque(false);
        soundCombo.setBackground(new Color(0, 0, 0, 0)); // 下拉列表背景半透明
        soundCombo.setForeground(Color.WHITE);


        // 初始化英雄机选择事件
        hero1Btn.addActionListener(e -> GameConfig.heroImage = "hero1");
        hero2Btn.addActionListener(e -> GameConfig.heroImage = "hero2");
        hero3Btn.addActionListener(e -> GameConfig.heroImage = "hero3");
        
        // 默认选择第一个英雄机
        hero1Btn.setSelected(true);
        
        // 点击即运行游戏
        ActionListener start = (ActionEvent e) -> {
            JButton src = (JButton) e.getSource();
            if (src == easyBtn)   GameConfig.difficulty = "EASY";
            if (src == mediumBtn) GameConfig.difficulty = "MEDIUM";
            if (src == hardBtn)   GameConfig.difficulty = "HARD";
            GameConfig.soundOn = soundCombo.getSelectedIndex() == 0;
            dispose();
        };
        easyBtn.addActionListener(start);
        mediumBtn.addActionListener(start);
        hardBtn.addActionListener(start);

        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * 初始化英雄机选择面板
     */
    private void initHeroSelectionPanel() {
        // 设置单选按钮组
        heroGroup.add(hero1Btn);
        heroGroup.add(hero2Btn);
        heroGroup.add(hero3Btn);
        
        // 设置按钮样式
        JRadioButton[] heroBtns = {hero1Btn, hero2Btn, hero3Btn};
        for (JRadioButton btn : heroBtns) {
            btn.setOpaque(false);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        }
        
        // 加载英雄机图片
        try {
            BufferedImage hero1Img = ImageIO.read(new FileInputStream("src/images/hero.png"));
            BufferedImage hero2Img = ImageIO.read(new FileInputStream("src/images/hero2.png"));
            BufferedImage hero3Img = ImageIO.read(new FileInputStream("src/images/hero3.png"));
            
            // 调整图片大小
            ImageIcon hero1Icon = new ImageIcon(hero1Img.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            ImageIcon hero2Icon = new ImageIcon(hero2Img.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            ImageIcon hero3Icon = new ImageIcon(hero3Img.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            
            // 设置标签图片
            hero1Label.setIcon(hero1Icon);
            hero2Label.setIcon(hero2Icon);
            hero3Label.setIcon(hero3Icon);
            
            // 居中显示
            hero1Label.setHorizontalAlignment(SwingConstants.CENTER);
            hero2Label.setHorizontalAlignment(SwingConstants.CENTER);
            hero3Label.setHorizontalAlignment(SwingConstants.CENTER);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}