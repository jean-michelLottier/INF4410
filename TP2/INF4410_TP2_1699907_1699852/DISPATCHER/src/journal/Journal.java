/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package journal;

import ca.polymtl.inf4410.tp2.server.ServerInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Journal {

    private byte[] text;
    private HashMap<Long, ServerInfo> serverInfos;
    private ArrayList<String> tasksTotal;
    private Semaphore semaphore;

    public Journal() {
        semaphore = new Semaphore(1);
    }

    public ArrayList<String> getTasksTotal() {
        return tasksTotal;
    }

    public void setTasksTotal(ArrayList<String> tasksTotal) {
        this.tasksTotal = tasksTotal;
    }

    public byte[] getText() {
        return text;
    }

    public void setText(byte[] text) {
        this.text = text;
    }

    public HashMap<Long, ServerInfo> getServerInfos() throws InterruptedException {
        semaphore.acquire();
        return serverInfos;
    }

    public void setServerInfos(HashMap<Long, ServerInfo> serverInfos) {
        this.serverInfos = serverInfos;
        semaphore.release();
    }

    public void freeSemaphore() {
        semaphore.release();
    }
}
