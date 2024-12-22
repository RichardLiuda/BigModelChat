package com.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JPanel;

class Client {
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 11451;
    int modelIndex;
    Socket socket;
    PrintWriter out;
    Win.ChatPage chatPage; // 绑定聊天页面
    Waiting waiting;
    Thread waitingThread; // 等待线程
    Boolean waitingFlag = false; // waitingThread是否在运行

    public Client(int modelIndex, Win.ChatPage chatPage) {
        this.modelIndex = modelIndex;
        this.chatPage = chatPage;

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to the server.");
            new Thread(new ReceiveChat(socket)).start();
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(modelIndex);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void sentChat(String str) throws Exception { // 发送消息
        out.println(str);
        if (!waitingFlag) {
            waiting = new Waiting();
            waitingThread = new Thread(waiting);
            waitingFlag = true;
            waitingThread.start();
        }
    }

    private class ReceiveChat implements Runnable { // 接收消息
        private Socket socket;

        public ReceiveChat(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                Boolean flag = true;
                while ((message = in.readLine()) != null) { // 监听消息
                    if (flag) {
                        chatPage.chatPanel.remove(chatPage.chatPanel.getComponentCount() - 1); // 移除等待消息
                        flag = false;
                    }
                    if (waitingThread.isAlive()) { // 如果等待线程在运行，则停止等待线程
                        waitingFlag = false;
                        waitingThread.interrupt();
                    }
                    if (!message.equals("")) { // 如果消息不为空，则显示消息
                        System.out.println("Received: " + message);
                        chatPage.addChatBubble(message, false);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    class Waiting implements Runnable {
        @Override
        public void run() {
            String waitingLabel = "正在等待对方回复";
            JPanel waitingPanel = chatPage.createChatBubble(waitingLabel, false);
            System.out.println("Waiting...");
            chatPage.addChatBubble(waitingLabel, false);
            chatPage.revalidate();
            chatPage.repaint();
            int count = chatPage.chatPanel.getComponentCount();

            while (waitingFlag) {
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复";
                    chatPage.changeTextByIndex(waitingLabel, count - 1);
                }
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复.";
                    chatPage.changeTextByIndex(waitingLabel, count - 1);
                }
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复..";
                    chatPage.changeTextByIndex(waitingLabel, count - 1);
                }
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复...";
                    chatPage.changeTextByIndex(waitingLabel, count - 1);
                }
            }

        }
    }
}
