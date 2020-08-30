package com.vk.dwzkf.thread;

import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractThread extends Thread {
    private boolean isStopped = true;

    public final void setUp() {
        if (!isStopped) return;
        isStopped = !setUpActions();
    }

    public void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    @Override
    public void run() {
        preMain();
        try {
            while (!isStopped) {
                mainActions();
            }
            postMain();
            closeActions();
        }
        catch (Exception e) {
            postMain();
            closeActions();
        }
    }

    @Override
    public synchronized void start() {
        if (isStopped) return;
        super.start();
    }

    public abstract boolean setUpActions();
    public abstract void preMain();
    public abstract void mainActions();
    public abstract void postMain();
    public abstract void closeActions();
}
