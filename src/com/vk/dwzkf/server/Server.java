package com.vk.dwzkf.server;

import com.vk.dwzkf.admin.Admin;
import com.vk.dwzkf.thread.AbstractThread;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
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
        InetAddress inetAddress;
        String hostAddress = "localhost";
        try {
            inetAddress = InetAddress.getLocalHost();
            hostAddress = inetAddress.getHostAddress();
        }
        catch (Exception e) {
            System.out.println("[ERROR] cant get host addr. Setted to localhost.");
        }
        serverProperties.put("ip",hostAddress);
        serverProperties.put("port","6689");
        serverProperties.put("username","admin");
        serverProperties.put("password","admin");
        serverProperties.put("maxconnections","10");
        admin = new Admin(serverProperties.getProperty("username"), serverProperties.getProperty("password"));
    }

    @Override
    public boolean setUpActions() {
        try {
            inetAddress = InetAddress.getByName("localhost");
            try {
                loadProperties();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            serverSocket = new ServerSocket(port, maxConnections, inetAddress);
            return true;
        }
        catch (Exception e) {
            System.out.println("Cannot start server. Probably port "+port+" not available.");
            System.out.println("Delete server.cfg and try again. Probably its broken.");
            return false;
        }
    }

    @Override
    public void preMain() {
        System.out.println("Server started.");
        showInfo();
    }

    @Override
    public void mainActions() {
        try {
            Socket socket = serverSocket.accept();
            onNewConnection(socket);
        }
        catch (Exception e) {
            e.printStackTrace();
            setStopped(true);
        }
    }

    @Override
    public void postMain() {
        System.out.println("[Server stopped]");
    }

    @Override
    public void closeActions() {
        for (Connection c : connections) {
            c.shutdown();
        }
        try {
            if (serverSocket!=null) serverSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
        if (!connections.remove(connection)) return;
        System.out.println("[User removed]: "+connection.getUserName());
        System.out.println("[Current users size]: "+connections.size());
        admin.setConnected(false);
    }

    public void loadProperties() throws Exception {
        Path file = Paths.get("server.cfg");
        if (!Files.exists(file)) {
            Files.createFile(file);
            OutputStream outputStream = Files.newOutputStream(file);
            serverProperties.store(outputStream, "To change property enter \"set <key> <value>\" ");
            outputStream.close();
        }
        InputStream inputStream = Files.newInputStream(file);
        serverProperties.load(inputStream);
        inputStream.close();
        String hostAddress = "localhost";
        try {
            inetAddress = InetAddress.getLocalHost();
            hostAddress = inetAddress.getHostAddress();
        }
        catch (Exception e) {
            System.out.println("[ERROR] cant get host addr. Setted to localhost.");
        }
        maxConnections = Integer.parseInt(serverProperties.getProperty("maxconnections", "10"));
        inetAddress = InetAddress.getByName(serverProperties.getProperty("ip", hostAddress));
        admin.setUsername(serverProperties.getProperty("username", "admin"));
        admin.setPassword(serverProperties.getProperty("password", "admin"));
        port = Integer.parseInt(serverProperties.getProperty("port", "6689"));
    }

    public void showInfo() {
        System.out.println("Server ip: "+serverSocket.getInetAddress().toString());
        System.out.println("Server port: "+serverSocket.getLocalPort());
        System.out.println("ADMIN username: "+admin.getUsername());
        System.out.println("ADMIN password: "+admin.getPassword());
    }

    public void saveProperty(String key, String value) {
        if (serverProperties.getProperty(key)==null) {
            return;
        }
        serverProperties.setProperty(key,value);
        try {
            Path file = Paths.get("server.cfg");
            Files.deleteIfExists(file);
            Files.createFile(file);
            OutputStream out = Files.newOutputStream(file);
            serverProperties.store(out, "To change property enter \"set <key> <value>\" ");
            out.close();
            System.out.println("Property was setted.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
