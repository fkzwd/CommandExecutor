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
                return;
            }
            setUserName(userName);
            readerUtil.sendMessage("[SUCCESS] You are logged in.");
        }
        catch (Exception e) {
            setStopped(true);
        }
    }

    @Override
    public void mainActions() {
        try {
            if (socket.isClosed()) {
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
        readerUtil.closeActions();
        try {
            socket.close();
        }
        catch (Exception e) {

        }
        owner.closeConnection(this);
    }

    private void processMessage(String message) {
        readerUtil.sendMessage("[SERVER] message received:"+message);
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName(){
        return userName;
    }
}
