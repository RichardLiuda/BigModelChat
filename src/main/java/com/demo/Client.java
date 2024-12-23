package com.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
        Thread.sleep(500);
        if (!waitingFlag) {
            waitingFlag = true;
            waiting = new Waiting();
            waitingThread = new Thread(waiting);
            waitingThread.start();
        }
    }

    private class ReceiveChat implements Runnable { // 接收消息
        private Socket socket;
        // Boolean flag;

        public ReceiveChat(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                // flag = true;
                while ((message = in.readLine()) != null) { // 监听消息
                    if (waitingThread.isAlive()) { // 如果等待线程在运行，则停止等待线程
                        waitingFlag = false;
                        waitingThread.interrupt();
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                    chatPage.removeWaiting();
                    if (!message.equals("")) { // 如果消息不为空，则显示消息
                        System.out.println("Received: " + message);
                        chatPage.addChatBubble(message, false);
                    }
                }
                // flag = true;
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    class Waiting implements Runnable {
        @Override
        public void run() {
            String waitingLabel = "正在等待对方回复";
            System.out.println("Waiting...");
            chatPage.addWaitingBubble(waitingLabel, false);
            chatPage.revalidate();
            chatPage.repaint();

            while (waitingFlag) {
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复.";
                    chatPage.changeTextByIndex(waitingLabel);
                }
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复..";
                    chatPage.changeTextByIndex(waitingLabel);
                }
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复...";
                    chatPage.changeTextByIndex(waitingLabel);
                }
                try {
                    Thread.sleep(200); // 每200毫秒更新一次
                } catch (InterruptedException e) {
                }

                if (waitingFlag) {
                    waitingLabel = "正在等待对方回复";
                    chatPage.changeTextByIndex(waitingLabel);
                }
            }

        }
    }
}
