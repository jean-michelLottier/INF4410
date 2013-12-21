/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.request;

import ca.polymtl.inf4410.tp2.commontools.Commands;
import ca.polymtl.inf4410.tp2.exceptions.SendRequestException;
import ca.polymtl.inf4410.tp2.server.ServerInfo;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class SendRequest implements ISendRequest {

    @Override
    public Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId) throws SendRequestException {
        return sendRequest(socket, oos, command, serverId, null, 0, 0, null, 0);
    }

    @Override
    public Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, byte[] content, int resource) throws SendRequestException {
        return sendRequest(socket, oos, command, serverId, content, 0, 0, null, resource);
    }

    @Override
    public Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, int firstLineNumber, int lastLineNumber) throws SendRequestException {
        return sendRequest(socket, oos, command, serverId, null, firstLineNumber, lastLineNumber, null, 0);
    }

    @Override
    public Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, HashMap<String, Integer> result) throws SendRequestException {
        return sendRequest(socket, oos, command, serverId, null, 0, 0, result, 0);
    }

    @Override
    public Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, byte[] content, int firstLineNumber, int lastLineNumber, HashMap<String, Integer> result, int resource) throws SendRequestException {
        if (serverId <= 0) {
            throw new SendRequestException("Bad server ID.");
        }
        if (command == null) {
            throw new SendRequestException("A command must be notified.");
        }

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        long taskId = (String.valueOf(serverId) + dateFormat.format(calendar.getTime()).toString()).hashCode();

        Request request = new Request();
        request.setTaskId(taskId);
        request.setCommand(command);
        request.setContent(content);
        request.setFirstLineNumber(firstLineNumber);
        request.setLastLineNumber(lastLineNumber);
        request.setResult(result);
        request.setResource(resource);

        try {
            oos.writeObject(request);
            oos.flush();
        } catch (IOException ex) {
            throw new SendRequestException("Failed to send request.");
        }

        return request;
    }

    @Override
    public HashMap<Long, ServerInfo> addRequestIntoServerInfo(HashMap<Long, ServerInfo> serverInfos, Request request, long serverId) {
        if (serverInfos == null || request == null) {
            return serverInfos;
        }

        ServerInfo serverInfo = serverInfos.get(serverId);
        HashMap<Commands.commands, ArrayList<Request>> requests = serverInfo.getRequests();
        ArrayList<Request> requestsList;
        if (requests == null || requests.isEmpty()) {
            requests = new HashMap<>();
            requestsList = new ArrayList<>();
        } else if (requests.containsKey(request.getCommand())) {
            requestsList = requests.get(request.getCommand());
        } else {
            requestsList = new ArrayList<>();
        }

        requestsList.add(request);
        requests.put(request.getCommand(), requestsList);
        serverInfo.setRequests(requests);
        serverInfos.put(serverId, serverInfo);

        return serverInfos;
    }

    @Override
    public HashMap<Long, ServerInfo> addResponseIntoServerInfo(HashMap<Long, ServerInfo> serverInfos, Request response, long serverId) {
        if (serverInfos == null || response == null) {
            return serverInfos;
        }

        ServerInfo serverInfo = serverInfos.get(serverId);
        HashMap<Commands.commands, ArrayList<Request>> responses = serverInfo.getResponses();
        ArrayList<Request> responsesList;
        if (responses == null || responses.isEmpty()) {
            responses = new HashMap<>();
            responsesList = new ArrayList<>();
        } else if (responses.containsKey(response.getCommand())) {
            responsesList = responses.get(response.getCommand());
        } else {
            responsesList = new ArrayList<>();
        }

        responsesList.add(response);
        responses.put(response.getCommand(), responsesList);
        serverInfo.setResponses(responses);
        serverInfos.put(serverId, serverInfo);

        return serverInfos;
    }
}
