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

import com.AggregateServerHttpClient;
import com.AggregationServer;
import com.models.WeatherData;

public class ServerHandler {
    private int port;
    private ExecutorService threadPool;
    private final ReentrantLock lock;
    private final LamportClock lamportClock;
    private final HashMap<String, WeatherData> weatherDataMap;
    private Timer timer;
    private final long timerInterval;
    private TimerTask timerTask;
    private volatile boolean isRunning;
    private boolean isAggregator;
    private ServerSocket serverSocket;

    public ServerHandler(int port, ReentrantLock lock, LamportClock lamportClock,
            HashMap<String, WeatherData> weatherDataMap, boolean isAggregator) {
        this.port = port;
        this.lock = lock;
        this.lamportClock = lamportClock;
        this.weatherDataMap = weatherDataMap;
        this.isAggregator = isAggregator;
        this.threadPool = Executors.newFixedThreadPool(10);
        this.timerInterval = 30 * 1000; // Thirty seconds
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
        if (isAggregator) {
            System.out.println("Running aggregation server on port " + port + "..");
            timer = new Timer();
            timer.schedule(timerTask, timerInterval, timerInterval);
        }
        startServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new AggregateServerHttpClient(clientSocket, lamportClock)::handle);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start HTTP server.");
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
}