package com.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.day.BigModelNew;
// import com.day.BigModelNew.IsOverChangeListener;
import com.qwen.qwen;

public class Server {
    // static BufferedWriter bw;
    public static ArrayList<Thread> threads = new ArrayList<Thread>();
    static int port = 11451;
    static ServerSocket server;
    static Socket socketAtServer;
    static Set<Socket> clientSockets = Collections.synchronizedSet(new HashSet<>());

    public static void main(String args[]) throws IOException {
        try {
            System.out.println("Server is running...");
            server = new ServerSocket(port);
            while (true) {
                Socket clientSocket = server.accept();
                clientSockets.add(clientSocket);
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                Thread thread = new Thread(new StartChat(clientSocket, clientSocket.getInetAddress().toString()));
                threads.add(thread);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void noteLog(String msg) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./log.txt", true))) {
            writer.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class StartChat implements Runnable {
    Socket clientSocket;
    static int op;
    qwen qw;
    BigModelNew spark;
    String chatId;

    Object lock = new Object();

    public StartChat(Socket clientSocket, String chatId) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        op = Integer.parseInt(in.readLine());
        this.clientSocket = clientSocket;
        this.chatId = chatId;
    }

    class OpenChat implements Runnable { // 打开聊天服务
        @Override
        public void run() {
            try {
                switch (StartChat.op) {
                    case 0 -> {
                        spark = new BigModelNew(chatId, false, lock); // 实例化
                        spark.getChat();
                    }
                    case 1 -> {
                        qw = new qwen(); // 实例化
                        qw.getChat();
                    }
                    default -> {
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void run() { // 接收消息
        try {
            new Thread(new OpenChat()).start();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // 通信流
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Received: " + msg);
                String rec = ReceiveChat(msg);
                out.println(rec);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String ReceiveChat(String str) throws Exception { // 接收服务器消息
        String rec = "";
        switch (StartChat.op) {
            case 0 -> {
                Server.noteLog((LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) + "\n"
                        + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " aksed Spark: " + str + "\n"); // 记录日志
                synchronized (lock) {
                    spark.text = str;
                    spark.flag = true;
                    lock.wait(); // 等待回答
                    rec = spark.getAns(); // 获取回答
                    spark.text = "";
                }
                Server.noteLog("Spark answered: " + rec + "\n\n"); // 记录日志
            }
            case 1 -> {
                Server.noteLog((LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) + "\n"
                        + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " aksed Qwen: " + str + "\n"); // 记录日志
                qw.text = str;
                qw.flag = true;
                qw.setFlag(true);
                Thread.sleep(200);
                while (!qw.isOver) { // 等待回答
                    Thread.sleep(200);
                }
                rec = qw.returnText; // 获取回答
                qw.text = "";
                Server.noteLog("Qwen answered: " + rec + "\n\n"); // 记录日志
            }
            default -> {
            }
        }
        System.out.println(clientSocket.getInetAddress() + " received: " + rec);
        return rec;
    }
}
