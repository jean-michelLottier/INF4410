/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.request;

import ca.polymtl.inf4410.tp2.commontools.Commands;
import ca.polymtl.inf4410.tp2.exceptions.SendRequestException;
import ca.polymtl.inf4410.tp2.server.ServerInfo;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public interface ISendRequest {

    Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId) throws SendRequestException;

    Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, byte[] content, int resource) throws SendRequestException;

    Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, int firstLineNumber, int lastLineNumber) throws SendRequestException;

    Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, HashMap<String, Integer> result) throws SendRequestException;

    /**
     * <p>Envoi une requête au serveur ciblé</p>
     * @param socket
     * @param oos
     * @param command
     * @param serverId
     * @param content
     * @param firstLineNumber
     * @param lastLineNumber
     * @param result
     * @param resource
     * @return
     * @throws SendRequestException 
     */
    Request sendRequest(Socket socket, ObjectOutputStream oos, Commands.commands command, long serverId, byte[] content, int firstLineNumber, int lastLineNumber, HashMap<String, Integer> result, int resource) throws SendRequestException;

    /**
     * <p>Ajoute une requête dans l'objet ServerInfo</p>
     * @param serverInfos
     * @param request
     * @param serverId
     * @return 
     */
    HashMap<Long, ServerInfo> addRequestIntoServerInfo(HashMap<Long, ServerInfo> serverInfos, Request request, long serverId);
    
    /**
     * <p>Ajoute une réponse (Request) dans l'objet ServerInfo</p>
     * @param serverInfos
     * @param response
     * @param serverId
     * @return 
     */
    HashMap<Long, ServerInfo> addResponseIntoServerInfo(HashMap<Long, ServerInfo> serverInfos, Request response, long serverId);
}
