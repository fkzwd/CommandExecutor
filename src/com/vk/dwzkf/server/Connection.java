package com.vk.dwzkf.server;

import com.vk.dwzkf.thread.AbstractThread;
import com.vk.dwzkf.utils.ReaderUtil;

import java.net.Socket;

public class Connection extends AbstractThread {
    private Socket socket;
    private String userName;
    private Server owner;
    private ReaderUtil readerUtil;

    public Connection(Server owner, Socket socket) {
        this.socket = socket;
        this.owner = owner;
    }

    @Override
    public boolean setUpActions() {
        try {
            readerUtil = new ReaderUtil(socket.getInputStream(), socket.getOutputStream());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    @Override
    public void preMain() {
        readerUtil.sendMessage("username: ");
        String userName = readerUtil.getMessage();
        readerUtil.sendMessage("password: ");
        String password = readerUtil.getMessage();
        if (!owner.checkUser(userName, password)) {
            this.setStopped(true);
            readerUtil.sendMessageLn("[ERROR]: No such user with such password.");
            return;
        }
        setUserName(userName);
    }

    @Override
    public void mainActions() {
        String s = readerUtil.getMessage();
        processMessage(s);
    }


    @Override
    public void postMain() {

    }

    @Override
    public void closeActions() {
        readerUtil.closeActions();
        try {
            socket.close();
        }
        catch (Exception e) {

        }
        owner.closeConnection(this);
    }

    private void processMessage(String message) {

    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName(){
        return userName;
    }
}
