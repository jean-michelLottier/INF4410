/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.server;

import ca.polymtl.inf4410.tp2.commontools.Commands;
import ca.polymtl.inf4410.tp2.request.Request;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class ServerInfo {

    private long serverId;
    private String serverName;
    private InetAddress addressServer;
    private int port;
    private int resources;
    private ArrayList<String> tasks;
    private HashMap<Commands.commands, ArrayList<Request>> requests;
    private HashMap<Commands.commands, ArrayList<Request>> responses;

    public ServerInfo() {
        super();
    }

    public ServerInfo(long serverId, String serverName, InetAddress addressServer, int port, int resource) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.addressServer = addressServer;
        this.port = port;
        this.resources = resource;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public InetAddress getAddressServer() {
        return addressServer;
    }

    public void setAddressServer(InetAddress addressServer) {
        this.addressServer = addressServer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HashMap<Commands.commands, ArrayList<Request>> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<Commands.commands, ArrayList<Request>> requests) {
        this.requests = requests;
    }

    public HashMap<Commands.commands, ArrayList<Request>> getResponses() {
        return responses;
    }

    public void setResponses(HashMap<Commands.commands, ArrayList<Request>> responses) {
        this.responses = responses;
    }

    public ArrayList<String> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<String> tasks) {
        this.tasks = tasks;
    }

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }
}
