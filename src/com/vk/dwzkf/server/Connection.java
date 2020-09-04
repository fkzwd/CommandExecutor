package com.vk.dwzkf.server;

import com.vk.dwzkf.thread.AbstractThread;
import com.vk.dwzkf.utils.ReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class Connection extends AbstractThread {
    private Socket socket;
    private String userName;
    private Server owner;
    private ReaderUtil readerUtil;
    private Process process;
    private ReaderUtil processReader;
    private ReaderUtil processExceptionReader;
    private static Logger logger = LogManager.getLogger(Connection.class);

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
            readerUtil.sendMessage("Please log in.");
            readerUtil.sendMessage("USERNAME: ");
            String userName = readerUtil.getMessage();
            readerUtil.sendMessage("PASSWORD: ");
            String password = readerUtil.getMessage();
            if (!owner.checkUser(userName, password)) {
                this.setStopped(true);
                readerUtil.sendMessage("[ERROR]: No such user with such password.");
                readerUtil.sendMessage("[ERROR]: Or admin already connected.");
                return;
            }
            setUserName(userName);
            setName(getName()+" :"+userName);
            readerUtil.sendMessage("[SUCCESS] You are logged in.");

            process = Runtime.getRuntime().exec("cmd");
            processReader = new ReaderUtil(process.getInputStream(), process.getOutputStream()) {
                @Override
                public void mainActions() {
                    try {
                        String s;
                        while ((s=getMessage()) != null) {
                            if (!s.isBlank()) readerUtil.sendMessage(s);
                        }
                        setStopped(true);
                    }
                    catch (Exception e) {
                        setStopped(true);
                    }
                }

                @Override
                public void closeActions() {

                }
            };
            processReader.setUp();
            processReader.start();
            processReader.sendMessage("chcp 65001");

            processExceptionReader = new ReaderUtil(process.getErrorStream(), null) {
                @Override
                public void mainActions() {
                    try {
                        String s;
                        while ((s=getMessage()) != null) {
                            if (!s.isBlank()) readerUtil.sendMessage(s);
                        }
                        setStopped(true);
                    }
                    catch (Exception e) {
                        setStopped(true);
                    }
                }

                @Override
                public void closeActions() {

                }
            };
            processExceptionReader.setUp();
            processExceptionReader.start();
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
        readerUtil.shutdown();
        if (processReader!=null) {
            processReader.setStopped(true);
        }
        if (processExceptionReader!=null) {
            processExceptionReader.setStopped(true);
        }
        try {
            socket.close();
            if (process!=null) {
                process.children().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
        }
        catch (Exception e) {
            logger.error("Socket not exists or already closed.",e);
        }
    }

    private void processMessage(String message) {
        logger.info("Server accept message:"+message);
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
