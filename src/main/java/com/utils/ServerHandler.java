package com.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import com.AggregationServer.AggregationServer;
import com.AggregationServer.HttpClient;
import com.models.WeatherData;

public class ServerHandler {
    private int port;
    private ExecutorService threadPool;
    private final LamportClock lamportClock;
    private Timer timer;
    private TimerTask timerTask;
    private volatile boolean isRunning;
    private ServerSocket serverSocket;
    private static long timerInterval = 30 * 1000; // Thirty seconds

    public ServerHandler(int port, ReentrantLock lock, LamportClock lamportClock,
            HashMap<String, WeatherData> weatherDataMap) {
        this.port = port;
        this.lamportClock = lamportClock;
        this.threadPool = Executors.newFixedThreadPool(10);

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Purging expired entries..");
                AggregationServer.removeOldEntries();
            }
        };
    }

    public void start() {
        isRunning = true;

        System.out.println("Running aggregation server on port " + port + "..");
        timer = new Timer();
        timer.schedule(timerTask, timerInterval, timerInterval);
        startServer();
        startDataCleanupTask();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new HttpClient(clientSocket, lamportClock)::handle);
            }
        } catch (IOException e) {
            if (isRunning) {
                System.out.println("Failed to start HTTP server");
            } else {
                System.out.println("Server socket was closed, server is shutting down");
            }
        }
    }

    public void stop() {
        isRunning = false;
        if (timer != null) {
            timer.cancel();
        }
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void startDataCleanupTask() {
        System.out.println("Starting data cleanup task...");
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(timerInterval);
                    AggregationServer.removeOldEntries();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }
}
