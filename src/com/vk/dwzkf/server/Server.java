package com.vk.dwzkf.server;

import com.vk.dwzkf.admin.Admin;
import com.vk.dwzkf.thread.AbstractThread;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Server extends AbstractThread {
    private ServerSocket serverSocket;
    private int port;
    private List<Connection> connections = new LinkedList<>();
    private Properties serverProperties = new Properties();
    private Admin admin;
    private InetAddress inetAddress;
    private int maxConnections;

    public Server() {
        this(6689);
    }

    public Server(int port) {
        this.port = port;
        serverProperties.put("ip","localhost");
        serverProperties.put("port","6689");
        serverProperties.put("username","fkzwd");
        serverProperties.put("password","password");
        serverProperties.put("maxconnections","10");
        admin = new Admin(serverProperties.getProperty("username"), serverProperties.getProperty("password"));
    }

    @Override
    public boolean setUpActions() {
        try {
            inetAddress = InetAddress.getByName("localhost");
            try {
                serverProperties.load(Files.newInputStream(Paths.get("cfg/server.properties")));
                maxConnections = Integer.parseInt(serverProperties.getProperty("maxconnections"));
                inetAddress = InetAddress.getByName(serverProperties.getProperty("ip"));
                admin.setUsername(serverProperties.getProperty("username"));
                admin.setPassword(serverProperties.getProperty("password"));
                port = Integer.parseInt(serverProperties.getProperty("port"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            serverSocket = new ServerSocket(port, maxConnections, inetAddress);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public void preMain() {
        System.out.println("Server started.");
        System.out.println("Server ip: "+serverSocket.getInetAddress().toString());
        System.out.println("Server port: "+serverSocket.getLocalPort());
        System.out.println("ADMIN username: "+admin.getUsername());
        System.out.println("ADMIN password: "+admin.getPassword());
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
        if (admin.isConnected()) return false;
        if (admin.getUsername().equals(userName) && admin.getPassword().equals(password)) {
            System.out.println("[User connected]: "+userName);
            System.out.println("[Current users count]: "+connections.size());
            admin.setConnected(true);
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
        admin.setConnected(false);
    }
}
