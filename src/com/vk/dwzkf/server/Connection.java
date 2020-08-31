package com.vk.dwzkf.server;

import com.vk.dwzkf.thread.AbstractThread;
import com.vk.dwzkf.utils.ReaderUtil;

import java.io.IOException;
import java.net.Socket;

public class Connection extends AbstractThread {
    private Socket socket;
    private String userName;
    private Server owner;
    private ReaderUtil readerUtil;
    private Process process;
    private ReaderUtil processReader;

    public Connection(Server owner, Socket socket) {
        this.socket = socket;
        this.owner = owner;
    }

    @Override
    public boolean setUpActions() {
        try {
            socket.setSoTimeout(300000);
            readerUtil = new ReaderUtil(socket.getInputStream(), socket.getOutputStream());
            readerUtil.setUpActions();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    @Override
    public void preMain() {
        try {
            readerUtil.sendMessage("username: ");
            String userName = readerUtil.getMessage();
            readerUtil.sendMessage("password: ");
            String password = readerUtil.getMessage();
            if (!owner.checkUser(userName, password)) {
                this.setStopped(true);
                readerUtil.sendMessage("[ERROR]: No such user with such password.");
                readerUtil.sendMessage("[ERROR]: Or admin already connected.");
                return;
            }
            setUserName(userName);
            readerUtil.sendMessage("[SUCCESS] You are logged in.");

            process = Runtime.getRuntime().exec("cmd");
            processReader = new ReaderUtil(process.getInputStream(), process.getOutputStream()) {
                @Override
                public void mainActions() {
                    try {
                        String s;
                        while ((s=processReader.getMessage()) != null) {
                            readerUtil.sendMessage("[Executor]: "+s);
                        }
                        setStopped(true);
                    }
                    catch (Exception e) {
                        setStopped(true);
                    }
                }
            };
            processReader.setUp();
            processReader.start();
        }
        catch (Exception e) {
            setStopped(true);
        }
    }

    @Override
    public void mainActions() {
        try {
            if (socket.isOutputShutdown()) {
                setStopped(true);
            }
            else {
                String s = readerUtil.getMessage();
                processMessage(s);
            }
        }
        catch (Exception e) {
            setStopped(true);
        }
    }


    @Override
    public void postMain() {
        readerUtil.sendMessage("[SERVER] Good bye!");
    }

    @Override
    public void closeActions() {
        owner.closeConnection(this);
        readerUtil.closeActions();
        if (processReader!=null) {
            processReader.closeActions();
        }
        try {
            socket.close();
            process.destroyForcibly();
        }
        catch (Exception e) {

        }
    }

    private void processMessage(String message) {
        if (message.equals("exit")) {
            this.setStopped(true);
            return;
        }
        processReader.sendMessage(message);
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName(){
        return userName;
    }
}
