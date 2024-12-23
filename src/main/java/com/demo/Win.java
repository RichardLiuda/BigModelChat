package com.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

public class Win extends JFrame {
    JTabbedPane p;
    String models[] = { "讯飞星火大模型", "阿里通义千问大模型" };
    ArrayList<Chat> tabs = new ArrayList<Chat>();
    JButton addChat;

    public static void main(String[] args) {
        Win win = new Win();
    }

    public Win() {
        setTitle("2023193004-刘俊熙-Java课程作业");
        setSize(1080, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        validate();
        p = new JTabbedPane(JTabbedPane.LEFT);
        addChat = new JButton("添加新聊天");
        addChat.addActionListener((ActionEvent e) -> {
            addTab();
        });

        add(addChat, BorderLayout.NORTH);
        addTab();

        add(p, BorderLayout.CENTER);
        p.validate();
    }

    final public void addTab() {
        int modelIndex = JOptionPane.showOptionDialog( // 弹出选择框
                null,
                "请选择聊天模型",
                "选择聊天模型",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                models,
                models[0]);
        tabs.add(new Chat("第" + String.valueOf(tabs.size() + 1) + "个聊天 " + models[modelIndex], modelIndex,
                tabs.size() + 1)); // 创建新的聊天窗格
        Chat chat = tabs.get(tabs.size() - 1);
        p.add(tabs.get(tabs.size() - 1).title, tabs.get(tabs.size() - 1).page); // 将新的聊天窗格添加到主面板中
        flush();
    }

    public void flush() { // 刷新
        p.validate();
        p.repaint();
    }

    public class Chat {
        public String title;
        public int chatIndex;
        public int modelIndex;
        public ChatPage page;

        public Chat(String title, int modelIndex, int chatIndex) {
            this.title = title;
            this.modelIndex = modelIndex;
            this.chatIndex = chatIndex;
            page = new ChatPage(modelIndex, chatIndex);
        }
    }

    public class ChatPage extends JPanel {
        int chatIndex;
        public Client client;
        public int modelIndex;
        public JPanel chatPanel;
        public JPanel inputPanel;
        public JScrollPane scrollPane;

        public ChatPage(int modelIndex, int chatIndex) {
            this.modelIndex = modelIndex;
            client = new Client(modelIndex, this);
            this.chatIndex = chatIndex;
            setLayout(new BorderLayout());

            JPanel titlePanel = new JPanel(); // 创建标题面板
            JLabel title = new JLabel("第" + chatIndex + "个聊天 " + models[modelIndex]);
            titlePanel.setLayout(new BorderLayout());
            titlePanel.add(title, BorderLayout.NORTH);
            add(titlePanel, BorderLayout.NORTH);

            inputPanel = new JPanel(); // 创建输入面板
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
            JButton sendButton = new JButton("发送");
            JTextField inputField = new JTextField(80);
            inputField.addKeyListener(new KeyListener() { // 监听回车键
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            String str = inputField.getText();
                            addChatBubble(str, true);
                            inputField.setText("");
                            client.sentChat(str);
                        } catch (Exception e1) {
                            System.err.println(e1);
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            sendButton.addActionListener((ActionEvent e) -> { // 监听发送按钮
                String str = inputField.getText();
                if (str.length() > 0) {
                    try {
                        addChatBubble(str, true);
                        inputField.setText("");
                        client.sentChat(str);
                    } catch (Exception e1) {
                        System.err.println(e1);
                    }
                }
            });
            inputPanel.add(inputField);
            inputPanel.add(sendButton);
            add(inputPanel, BorderLayout.SOUTH);

            chatPanel = new JPanel(); // 创建聊天面板
            chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(chatPanel);
            scrollPane.setPreferredSize(new Dimension(400, 500));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            chatPanel.add(createChatBubble("你好！有什么可以帮忙的？", false));

            add(scrollPane, BorderLayout.CENTER);
        }

        public static Dimension calculatePreferredSize(JTextArea textArea, int maxWidth) { // 计算文本区域的首选大小
            FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
            String text = textArea.getText();

            int lineHeight = metrics.getHeight(); // 每行高度
            int lines = 0;
            int width = 0;

            // 按最大宽度计算每行字符数
            int maxCharsPerLine = (maxWidth - textArea.getInsets().left - textArea.getInsets().right)
                    / metrics.charWidth('W'); // 假设 'W' 为宽字符
            for (String line : text.split("\n")) {
                int lineChars = line.length();
                lines += Math.ceil((double) lineChars / maxCharsPerLine);
                width = Math.max(width, Math.min(maxWidth, metrics.stringWidth(line)));
            }

            // 考虑内边距
            Insets insets = textArea.getInsets();
            int height = lines * lineHeight + insets.top + insets.bottom;
            width += insets.left + insets.right + 10;

            return new Dimension(width, height);
        }

        class WaitingPanel extends JPanel {
            WaitingPanel() {
                super();
            }
        }

        final WaitingPanel createWaitingBubble(String text, boolean isUser) { // 创建聊天气泡
            WaitingPanel bubble = new WaitingPanel();
            bubble.setLayout(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
            JTextArea texts = new JTextArea(text);
            texts.setEditable(false);
            texts.setOpaque(true);
            texts.setLineWrap(true); // 启用自动换行
            texts.setWrapStyleWord(true);
            texts.setBackground(isUser ? Color.BLUE : Color.GRAY);
            texts.setForeground(Color.WHITE);
            texts.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            texts.setPreferredSize(calculatePreferredSize(texts, 600)); // 限制宽度
            bubble.add(texts);
            return bubble;
        }

        final JPanel createChatBubble(String text, boolean isUser) { // 创建聊天气泡
            JPanel bubble = new JPanel();
            bubble.setLayout(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
            JTextArea texts = new JTextArea(text);
            texts.setEditable(false);
            texts.setOpaque(true);
            texts.setLineWrap(true); // 启用自动换行
            texts.setWrapStyleWord(true);
            texts.setBackground(isUser ? Color.BLUE : Color.GRAY);
            texts.setForeground(Color.WHITE);
            texts.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            texts.setPreferredSize(calculatePreferredSize(texts, 600)); // 限制宽度
            bubble.add(texts);
            return bubble;
        }

        public void addChatBubble(String text, boolean isUser) { // 添加聊天气泡
            chatPanel.add(createChatBubble(text, isUser));
            chatPanel.revalidate();
            chatPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }

        public void addWaitingBubble(String text, boolean isUser) { // 添加聊天气泡
            chatPanel.add(createWaitingBubble(text, isUser));
            chatPanel.revalidate();
            chatPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }

        synchronized public void changeTextByIndex(String text) { // 根据气泡索引更改气泡文本
            JPanel newBubble;
            int index = chatPanel.getComponentCount() - 1;
            chatPanel.remove(index);
            newBubble = createWaitingBubble(text, false);
            chatPanel.add(newBubble);
            chatPanel.revalidate();
            chatPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }

        synchronized public void removeWaiting() {
            ArrayList<Component> components = new ArrayList<>(Arrays.asList(chatPanel.getComponents()));
            for (int i = 0; i < components.size(); i++) {
                Component panel = components.get(i);
                if (components.get(i) instanceof WaitingPanel) {
                    chatPanel.remove(i);
                    System.out.println(i);
                    chatPanel.revalidate();
                    chatPanel.repaint();
                }
            }
        }
    }
}
