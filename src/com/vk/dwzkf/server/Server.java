package com.vk.dwzkf.server;

import com.vk.dwzkf.admin.Admin;
import com.vk.dwzkf.thread.AbstractThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static Logger logger = LogManager.getLogger(Server.class);

    public Server() {
        this(6689);
    }

    public Server(int port) {
        this.port = port;
        setDefaultConfig();
        admin = new Admin(serverProperties.getProperty("username"), serverProperties.getProperty("password"));
    }

    @Override
    public boolean setUpActions() {
        try {
            inetAddress = InetAddress.getByName("localhost");
            try {
                logger.info("Loading properties from file.");
                loadProperties();
            }
            catch (Exception e) {
                logger.error("Loading properties crashed.", e);
            }
            serverSocket = new ServerSocket(port, maxConnections, inetAddress);
            logger.info("Server started with "+inetAddress.getHostAddress()+":"+port);
            return true;
        }
        catch (Exception e) {
            logger.error("Cannot start server.", e);
            System.out.println("Cannot start server. Probably port "+port+" not available.");
            System.out.println("Try to print \"reset config\".");
            return false;
        }
    }

    @Override
    public void preMain() {
        System.out.println("[Server started]");
        showInfo();
    }

    @Override
    public void mainActions() {
        try {
            Socket socket = serverSocket.accept();
            onNewConnection(socket);
        }
        catch (Exception e) {
            logger.error("Exception on mainActions() server.",e);
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
            logger.info("Connected user:"+userName);
            admin.setConnected(true);
            return true;
        }
        else {
            return false;
        }
    }

    public void onNewConnection(Socket socket) {
        Connection connection = new Connection(this, socket);
        connection.setName("Connection"+getId());
        connection.setUp();
        connection.start();
        connections.add(connection);
    }

    public void closeConnection(Connection connection) {
        if (!connections.remove(connection)) return;
        logger.info("User "+connection.getUserName()+" removed. Users count:"+connections.size());
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
            logger.error("Cant get host addr. Setted to localhost.");
        }
        maxConnections = Integer.parseInt(serverProperties.getProperty("maxconnections", "10"));
        try {
            inetAddress = InetAddress.getByName(serverProperties.getProperty("ip", hostAddress));
        }
        catch (Exception e) {
            logger.error("Unknown host. Server ip set to default", e);
        }
        admin.setUsername(serverProperties.getProperty("username", "admin"));
        admin.setPassword(serverProperties.getProperty("password", "admin"));
        port = Integer.parseInt(serverProperties.getProperty("port", "6689"));
        logger.info("Server settings: ip="+inetAddress.getHostAddress()+":"+port+
                " admin name="+admin.getUsername()+" admin password="+admin.getPassword());
    }

    public void showInfo() {
        System.out.println("Server ip: "+serverSocket.getInetAddress().getHostAddress());
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
            System.out.println("Property set: "+key+" = "+value);
            logger.info("Property set "+key+"="+value);
        }
        catch (Exception e) {
            logger.error("Error saving property.",e);
            e.printStackTrace();
        }
    }

    public void resetConfig() {
        setDefaultConfig();
        try {
            Path file = Paths.get("server.cfg");
            Files.deleteIfExists(file);
            Files.createFile(file);
            OutputStream out = Files.newOutputStream(file);
            serverProperties.store(out, "To change property enter \"set <key> <value>\" ");
            out.close();
            System.out.println("Configuration reset successfully.");
        }
        catch (Exception e) {
            logger.error("Reset config crashed.",e);
            e.printStackTrace();
        }
    }

    private void setDefaultConfig(){
        InetAddress inetAddress;
        String hostAddress = "localhost";
        try {
            inetAddress = InetAddress.getLocalHost();
            hostAddress = inetAddress.getHostAddress();
        }
        catch (Exception e) {
            logger.error("Cant get host ip. Setted to localhost.",e);
        }
        serverProperties.put("ip",hostAddress);
        serverProperties.put("port","6689");
        serverProperties.put("username","admin");
        serverProperties.put("password","admin");
        serverProperties.put("maxconnections","10");
    }
}
