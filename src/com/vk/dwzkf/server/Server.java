package com.vk.dwzkf.server;

import com.vk.dwzkf.thread.AbstractThread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server extends AbstractThread {
    private ServerSocket serverSocket;
    private int port;
    private List<Connection> connections = new LinkedList<>();

    public Server() {
        this(6689);
    }

    public Server(int port) {
        this.port = port;
    }

    @Override
    public boolean setUpActions() {
        try {
            serverSocket = new ServerSocket(port);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public void preMain() {

    }

    @Override
    public void mainActions() {
        try {
            Socket socket = serverSocket.accept();
            onNewConnection(socket);
        }
        catch (Exception e) {

        }
    }

    @Override
    public void postMain() {

    }

    @Override
    public void closeActions() {

    }

    public boolean checkUser(String userName, String password) {
        if (userName.equals("fkzwd") && password.equals("fkzwd")) {
            System.out.println("[User connected]: "+userName);
            System.out.println("[Current users count]: "+connections.size());
            return true;
        }
        else {
            return false;
        }
    }

    public void onNewConnection(Socket socket) {
        Connection connection = new Connection(this, socket);
        connection.setUp();
        connection.start();
        connections.add(connection);
    }

    public void closeConnection(Connection connection) {
        connections.remove(connection);
        System.out.println("[User removed]: "+connection.getUserName());
        System.out.println("[Current users size]: "+connections.size());
    }
}
