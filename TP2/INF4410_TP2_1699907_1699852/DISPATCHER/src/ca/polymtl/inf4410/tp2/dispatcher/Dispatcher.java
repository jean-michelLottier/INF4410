/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.dispatcher;

import ca.polymtl.inf4410.tp2.commontools.Commands;
import ca.polymtl.inf4410.tp2.commontools.PropUtils;
import ca.polymtl.inf4410.tp2.exceptions.InitConnectionException;
import ca.polymtl.inf4410.tp2.exceptions.SendRequestException;
import ca.polymtl.inf4410.tp2.request.ISendRequest;
import ca.polymtl.inf4410.tp2.request.Request;
import ca.polymtl.inf4410.tp2.request.SendRequest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import journal.Journal;
import ca.polymtl.inf4410.tp2.server.ServerInfo;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import javax.smartcardio.CommandAPDU;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Dispatcher extends Thread {

    private static Properties properties;
    private static final String pathConfig = "../config";
    private static Journal journal;
    private static PropUtils utils;
    private Socket socket;
    private long serverIdConnected;
    private long taskId;
    private ISendRequest sendRequest;

    public Dispatcher(Socket socket, long serverId) {
        this.socket = socket;
        this.serverIdConnected = serverId;
    }

    private static void runDispatcher(long startTime) {
        System.out.println("running...");
        boolean isAServerRun = true;
        int totalTasks;


        ArrayList<String> failedTasks;
        HashMap<Long, ServerInfo> serverInfos;
        while (isAServerRun) {
            totalTasks = journal.getTasksTotal().size();
            int successfulTasks = 0;
            failedTasks = new ArrayList<>();
            try {
                Thread.sleep(20);
                serverInfos = journal.getServerInfos();


                for (long serverId : serverInfos.keySet()) {//Pour chaque serveur...
                    ServerInfo serverInfo = serverInfos.get(serverId);
                    HashMap<Commands.commands, ArrayList<Request>> responses = serverInfo.getResponses();
                    HashMap<Commands.commands, ArrayList<Request>> requests = serverInfo.getRequests();
                    if (responses == null) {
                        journal.freeSemaphore();
                        continue;
                    }

                    if (responses.containsKey(Commands.commands.commit) && requests.containsKey(Commands.commands.close)) {//...on verifie si la/les tache(s) a/ont commité...
                        successfulTasks += responses.get(Commands.commands.commit).size();
                    }

                    if (responses.containsKey(Commands.commands.abort)) {//...on vérifie aussi si la/les tache(s) a/ont planté
                        for (Request request : responses.get(Commands.commands.abort)) {//on récupère la tache
                            String task = request.getFirstLineNumber() + "-" + request.getLastLineNumber();
                            failedTasks.add(task);
                            ArrayList<String> tasksList = serverInfo.getTasks();
                            tasksList.remove(task);
                            serverInfo.setTasks(tasksList);
                            tasksList = journal.getTasksTotal();
                            tasksList.remove(task);
                            journal.setTasksTotal(tasksList);
                        }
                        responses.remove(Commands.commands.abort);
                        serverInfo.setResponses(responses);
                        serverInfos.put(serverId, serverInfo);
                    }

                    if (responses.containsKey(Commands.commands.fail) && !responses.get(Commands.commands.fail).isEmpty()) {//si le serveur est tombé en panne
                        for (Request request : responses.get(Commands.commands.fail)) {//on récupère la tache
                            String task = request.getFirstLineNumber() + "-" + request.getLastLineNumber();
                            failedTasks.add(task);
                            ArrayList<String> tasksList = serverInfo.getTasks();
                            tasksList.remove(task);
                            serverInfo.setTasks(tasksList);
                            tasksList = journal.getTasksTotal();
                            tasksList.remove(task);
                            journal.setTasksTotal(tasksList);
                        }
                        responses.put(Commands.commands.fail, new ArrayList<Request>());
                        serverInfo.setResponses(responses);
                        serverInfos.put(serverId, serverInfo);
                    }
                }

                if (successfulTasks == totalTasks) {
                    System.out.println(successfulTasks + "/" + totalTasks);
                    isAServerRun = false;
                } else if (!failedTasks.isEmpty()) {//si des taches non taitées 
                    for (String task : failedTasks) {
                        String[] lines = task.trim().split("-");

                        ArrayList<String> splitTask = splitTask(Integer.valueOf(lines[0]), Integer.valueOf(lines[1]), 2);

                        for (String current : splitTask) {
                            ServerInfo serverInfo = findServerToExecNewTask(serverInfos);
                            ArrayList<String> tasksList = serverInfo.getTasks();
                            tasksList.add(current);
                            System.out.println("serverInfoSelected : " + serverInfo.getServerName() + " newNbtasks : " + serverInfo.getTasks().size());
                            serverInfo.setTasks(tasksList);
                            serverInfos.put(serverInfo.getServerId(), serverInfo);
                            tasksList = journal.getTasksTotal();
                            tasksList.add(current);
                            journal.setTasksTotal(tasksList);
                            initConnection(serverInfo.getAddressServer().getHostAddress(), serverInfo.getPort(), serverInfo.getServerId());
                        }
                    }
                }
                journal.setServerInfos(serverInfos);
            } catch (InterruptedException ex) {
                Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InitConnectionException ex) {
                System.err.println("Bad parameters for initConnection method" + ex);
            } catch (IOException ex) {
                System.err.println("Try to connect with a crashed server => Error design method!!!" + ex);
            }
        }
        fillOutputFile();
        long endTime = System.currentTimeMillis();
        System.out.println("!!!!!!!!!!!!!!! FIN !!!!!!!!!!!!!!!");
        System.out.println("Temps d'execution (ms): " + (endTime - startTime));
    }

    public static PropUtils getUtils() {
        return utils;
    }

    public static void setUtils(PropUtils utils) {
        Dispatcher.utils = utils;
    }

    public ISendRequest getSendRequest() {
        return sendRequest;
    }

    public void setSendRequest(ISendRequest sendRequest) {
        this.sendRequest = sendRequest;
    }

    public static Journal getJournal() {
        return journal;
    }

    public static void setJournal(Journal journal) {
        Dispatcher.journal = journal;
    }

    private static void initDispatcher() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(pathConfig);
            properties = new Properties();
            properties.load(fis);
            fis.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error : " + ex);
        } catch (IOException ex) {
            System.err.println("Error : " + ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        initDispatcher();
        byte[] text = extractText(properties.getProperty(Commands.configs.inputPathFile.toString()));
        journal = new Journal();
        journal.setText(text);
        long startTime = System.currentTimeMillis();
        try {
            int port = Integer.valueOf(properties.getProperty(Commands.configs.port.toString()));
            String[] hosts = properties.getProperty(Commands.configs.hosts.toString()).split(",");
            ArrayList<String> tasks = generateTasks(text, hosts.length);
            journal.setTasksTotal(tasks);
            int i = 0;
            for (String host : hosts) {
                long serverId;
                int resource = generateResource(Integer.valueOf(properties.getProperty(Commands.configs.q.toString())), i);
                if (properties.getProperty(Commands.configs.mode.toString()).equals("secure")) {
                    if (tasks.get(i).equals("undefined")) {
                        continue;
                    }
                    serverId = addServerInfoIntoJournal(host, port, tasks.get(i), resource);
                    initConnection(host.trim(), port, serverId);
                } else {
                    serverId = addServerInfoIntoJournal(host, port, tasks, resource);
                    int j = tasks.size();
                    while (j != 0) {
                        initConnection(host.trim(), port, serverId);
                        Thread.sleep(0, 100);
                        j--;
                    }
                }
                i++;
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("Error : " + ex);
        } catch (InitConnectionException ex) {
            System.err.println("Error : " + ex);
        } catch (InterruptedException ex) {
            System.err.println("Error : " + ex);
        }

        runDispatcher(startTime);
    }

    private static byte[] extractText(String pathFile) {
        if (pathFile == null || pathFile.isEmpty()) {
            System.err.println("Aucun chemin vers le fichier à traiter renseigné.");
            return null;
        }

        byte[] text;
        try {
            text = Files.readAllBytes(Paths.get(pathFile));
        } catch (IOException ex) {
            System.err.println("Le fichier à traiter n'a pas été trouvé.");
            return null;
        }

        return text;
    }

    private static ArrayList<String> generateTasks(byte[] content, int nbServers) {
        String task;
        ArrayList<String> tasks = new ArrayList<>();
        String[] linesOfText = new String(content).trim().split("\n");
        int nbLines = linesOfText.length;
        if (nbLines > 10) {
            int nbLinesByServer = nbLines / nbServers;
            int start = 0, end;
            for (int i = 0; i < nbServers; i++) {
                end = start + nbLinesByServer;
                if (i + 1 >= nbServers) {
                    end = nbLines;
                }
                task = start + "-" + end;
                tasks.add(task);
                start += nbLinesByServer;
            }
        } else {
            tasks.add("0-" + nbLines);
            for (int i = 0; i < nbServers - 1; i++) {
                tasks.add("undefined");
            }
        }
        return tasks;
    }

    private static long addServerInfoIntoJournal(String host, int port, String task, int resource) throws UnknownHostException, InitConnectionException {
        ArrayList<String> tasks = new ArrayList<>(1);
        tasks.add(task);
        return addServerInfoIntoJournal(host, port, tasks, resource);
    }

    private static long addServerInfoIntoJournal(String host, int port, ArrayList<String> tasks, int resource) throws InitConnectionException, UnknownHostException {
        if (host == null || host.isEmpty() || port < 5000 || port > 5020) {
            throw new InitConnectionException("Failed to init connection");
        }
        InetAddress address = InetAddress.getByName(host);
        String ipAddress = address.getHostAddress().trim().replaceAll("\\.", "");
        long serverId = Long.valueOf(ipAddress);


        ServerInfo serverInfo = new ServerInfo(serverId, host, address, port, resource);
        serverInfo.setTasks(tasks);
        HashMap<Long, ServerInfo> serverInfos = new HashMap<>();
        try {
            serverInfos = journal.getServerInfos();
        } catch (InterruptedException ex) {
            System.out.println("ACCES BLOQUÉ (InterruptedException)");
        }
        if (serverInfos == null || serverInfos.isEmpty()) {
            serverInfos = new HashMap<>();
        }
        serverInfos.put(serverId, serverInfo);
        journal.setServerInfos(serverInfos);

        return serverId;
    }

    private static void initConnection(String host, int port, long serverId) throws InitConnectionException, UnknownHostException, IOException {
        if (host == null || host.isEmpty() || port < 5000 || port > 5020) {
            throw new InitConnectionException("Failed to init connection");
        }

        Socket socket = new Socket(host, port);
        Dispatcher dispatcher = new Dispatcher(socket, serverId);
        dispatcher.start();
    }

    private static int generateResource(int q, int i) {
        if (q <= 0) {
            q = 50;
        }
        int val = (int) Math.pow(2, i);

        return val * q;
    }

    @Override
    public void run() {
        Request request;
        Request response;
        sendRequest = new SendRequest();
        HashMap<Long, ServerInfo> serverInfos;
        ServerInfo serverInfo;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            /*
             * Requête 1 : Envoyer le text
             */
            serverInfos = journal.getServerInfos();
            serverInfo = serverInfos.get(serverIdConnected);
            if (serverInfo.getRequests() == null || serverInfo.getRequests().isEmpty()) {
                request = sendRequest.sendRequest(socket, oos, Commands.commands.init, serverIdConnected, journal.getText(), serverInfo.getResources());
                serverInfos = sendRequest.addRequestIntoServerInfo(serverInfos, request, serverIdConnected);
                journal.setServerInfos(serverInfos);
                /**
                 * attendre réponse du serveur
                 */
                response = (Request) ois.readObject();
                serverInfos = journal.getServerInfos();
                serverInfos = sendRequest.addResponseIntoServerInfo(serverInfos, response, serverIdConnected);
                journal.setServerInfos(serverInfos);
            } else {
                journal.freeSemaphore();
            }
            /*
             * Requête 2 : Envoyer la tache à executer
             */
            serverInfos = journal.getServerInfos();
            serverInfo = serverInfos.get(serverIdConnected);
            String tasks = findTaskToExecute(serverInfo);
            String[] lines = tasks.trim().split("-");
            request = sendRequest.sendRequest(socket, oos, Commands.commands.task, serverIdConnected, Integer.valueOf(lines[0]), Integer.valueOf(lines[1]));
            taskId = request.getTaskId();
            serverInfos = sendRequest.addRequestIntoServerInfo(serverInfos, request, serverIdConnected);
            journal.setServerInfos(serverInfos);
            /**
             * attendre réponse du serveur
             */
            response = (Request) ois.readObject();
            System.out.println("server : " + serverIdConnected + " task : " + response.getTaskId() + "-->" + response.getCommand().toString());
            //HashMap<String, Integer> result = response.getResult();
            serverInfos = journal.getServerInfos();
            serverInfos = sendRequest.addResponseIntoServerInfo(serverInfos, response, serverIdConnected);
            journal.setServerInfos(serverInfos);
            /**
             * cloturer la connexion
             */
            request = sendRequest.sendRequest(socket, oos, Commands.commands.close, serverIdConnected);
            serverInfos = journal.getServerInfos();
            serverInfos = sendRequest.addRequestIntoServerInfo(serverInfos, request, serverIdConnected);
            journal.setServerInfos(serverInfos);
        } catch (IOException ex) {
            System.out.println("server '" + serverIdConnected + "' failed!!!!");
            try {
                serverInfos = journal.getServerInfos();

                serverInfo = serverInfos.get(serverIdConnected);
                String tasks = findTaskToExecute(serverInfo);
                String[] lines = tasks.trim().split("-");
                response = new Request();
                response.setCommand(Commands.commands.fail);
                response.setFirstLineNumber(Integer.valueOf(lines[0]));
                response.setLastLineNumber(Integer.valueOf(lines[1]));
                serverInfos = sendRequest.addResponseIntoServerInfo(serverInfos, response, serverIdConnected);
                journal.setServerInfos(serverInfos);
            } catch (InterruptedException ex1) {
                Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (SendRequestException ex) {
            System.err.println("Impossible d'envoyer une requête vers le serveur '" + serverIdConnected + "'\n" + ex);
        } catch (ClassNotFoundException ex) {
            System.err.println("Impossible d'envoyer une requête vers le serveur '" + serverIdConnected + "'");
        } catch (InterruptedException ex) {
            System.out.println("ACCES BLOQUÉ (InterruptedException)");
        }
    }

    /**
     * Etape 1 : Récupère les résultats de tous les serveurs (requêtes 'commit')
     * Etape 2 : Mettre en correlation les résultats pour obtenir une seule map
     * (mots-occurences) Etape 3 : Le résultat doit être insérer dans le fichier
     * dont le pathname se trouve dans le properties (outputPathFile)
     */
    private static void fillOutputFile() {
        System.out.println("Extract results of each server");
        HashMap<String, Integer> result;
        if (properties.getProperty(Commands.configs.mode.toString()).equals("secure")) {
            result = concatResultTasksModeSecure();
        } else {
            HashMap<String, ArrayList<String>> resultesByTask = extractResultsByTask();
            result = concatResultTasksModeNoSecure(resultesByTask);
        }

        utils = new PropUtils();
        result = utils.sortMapByKey(result);
        StringBuilder sb = new StringBuilder();
        for (String word : result.keySet()) {
            sb.append(word).append(" = ").append(result.get(word)).append("\n");
        }
        System.out.println("Start write into file.");
        FileWriter fileWriter;
        try {
            File file = new File(properties.getProperty(Commands.configs.outputPathFile.toString()));
            fileWriter = new FileWriter(file);
            BufferedWriter buff = new BufferedWriter(fileWriter);
            buff.write(sb.toString());
            buff.flush();
            buff.close();
        } catch (IOException ex) {
            System.out.println("Error : " + ex);
        }
        System.out.println("End write into file.");
    }

    private String findTaskToExecute(ServerInfo serverInfo) {
        HashMap<Commands.commands, ArrayList<Request>> requestsByCommand = serverInfo.getRequests();
        if (requestsByCommand == null || requestsByCommand.isEmpty() || !requestsByCommand.containsKey(Commands.commands.task)) {
            return serverInfo.getTasks().get(0);
        }

        ArrayList<String> tasks = serverInfo.getTasks();
        for (Request request : requestsByCommand.get(Commands.commands.task)) {
            tasks.remove(request.getFirstLineNumber() + "-" + request.getLastLineNumber());
            if (request.getTaskId() == taskId) {
                tasks.add(request.getFirstLineNumber() + "-" + request.getLastLineNumber());
            }
        }
        return tasks.get(0);
    }

    private static ArrayList<String> splitTask(int startLine, int endLine, int nbTasks) {
        ArrayList<String> splitTask = new ArrayList<>();
        if (nbTasks <= 0) {
            splitTask.add(startLine + "-" + endLine);
            return splitTask;
        }

        int partLine = (int) (startLine + endLine) / nbTasks;
        splitTask.add(startLine + "-" + partLine);
        nbTasks--;
        for (int i = 0; i < nbTasks; i++) {
            if (i + 1 > (nbTasks - 1)) {
                splitTask.add(partLine + "-" + endLine);
            } else {
                int tmp = 2 * partLine;
                splitTask.add(partLine + "-" + tmp);
                partLine = tmp;
            }
        }

        return splitTask;
    }

    private static ServerInfo findServerToExecNewTask(HashMap<Long, ServerInfo> serverInfos) {
        long nbTasks = 100000;
        ServerInfo serverInfoSelected = new ServerInfo();
        for (long serverId : serverInfos.keySet()) {
            ServerInfo si = serverInfos.get(serverId);
            System.out.println("server : " + si.getServerName() + " nbTasks : " + si.getTasks().size());
            if (si.getTasks().size() <= nbTasks
                    && (si.getResponses() == null || si.getResponses().isEmpty() || !si.getResponses().containsKey(Commands.commands.fail))) {
                serverInfoSelected = si;
                nbTasks = si.getTasks().size();
            }
        }
        System.out.println("serverInfoSelected : " + serverInfoSelected.getServerName() + " nbtasks : " + serverInfoSelected.getTasks().size());
        return serverInfoSelected;
    }

    private static HashMap<String, Integer> concatResultTasksModeSecure() {
        HashMap<String, Integer> result = new HashMap<>();
        try {
            HashMap<Long, ServerInfo> serverInfos = journal.getServerInfos();
            journal.freeSemaphore();

            for (long serverId : serverInfos.keySet()) {
                ServerInfo serverInfo = serverInfos.get(serverId);
                ArrayList<Request> responsesCommit = serverInfo.getResponses().get(Commands.commands.commit);
                if (responsesCommit == null || responsesCommit.isEmpty()) {
                    continue;
                }
                for (Request response : responsesCommit) {
                    if (result == null || result.isEmpty()) {
                        result = response.getResult();
                    } else {
                        HashMap<String, Integer> resultRequest = response.getResult();
                        for (String word : resultRequest.keySet()) {
                            if (result.containsKey(word)) {
                                int cpt = result.get(word);
                                result.put(word, (cpt + resultRequest.get(word)));
                            } else {
                                result.put(word, resultRequest.get(word));
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("Error : " + ex);
        }
        return result;
    }

    private static HashMap<String, Integer> concatResultTasksModeNoSecure(HashMap<String, ArrayList<String>> resultsByTask) {
        HashMap<String, Integer> result = new HashMap<>();
        for (String current : resultsByTask.keySet()) {
            if (result.isEmpty()) {
                result = findGoodTask(resultsByTask.get(current));
            } else {
                HashMap<String, Integer> tmp = findGoodTask(resultsByTask.get(current));
                for (String word : tmp.keySet()) {
                    if (result.containsKey(word)) {
                        int cpt = result.get(word);
                        result.put(word, (cpt + tmp.get(word)));
                    } else {
                        result.put(word, tmp.get(word));
                    }
                }
            }
        }
        return result;
    }

    private static HashMap<String, Integer> findGoodTask(ArrayList<String> results) {
        int nbResults = 0;
        HashMap<String, Integer> tmp = new HashMap<>();
        for (String result : results) {
            if (tmp.isEmpty() || !tmp.containsKey(result)) {
                tmp.put(result, 1);
            } else {
                int value = tmp.get(result);
                tmp.put(result, (++value));
            }
        }
        String goodTask = null;
        for (String result : tmp.keySet()) {
            if (tmp.get(result) > nbResults) {
                goodTask = result;
            }
        }

        String[] lines = goodTask.split("\n");
        tmp = new HashMap<>();
        for (String line : lines) {
            String[] value = line.split("=");
            tmp.put(value[0].trim(), Integer.valueOf(value[1].trim()));
        }
        return tmp;
    }

    private static HashMap<String, ArrayList<String>> extractResultsByTask() {
        HashMap<String, ArrayList<String>> resultsByTask = new HashMap<>();

        HashMap<Long, ServerInfo> serverInfos;
        try {
            serverInfos = journal.getServerInfos();
            journal.freeSemaphore();
            for (long serverId : serverInfos.keySet()) {
                ServerInfo serverInfo = serverInfos.get(serverId);
                ArrayList<Request> responses = serverInfo.getResponses().get(Commands.commands.commit);
                for (Request response : responses) {
                    String task = response.getFirstLineNumber() + "-" + response.getLastLineNumber();
                    HashMap<String, Integer> results = response.getResult();
                    StringBuilder result = new StringBuilder();
                    for (String word : results.keySet()) {
                        result.append(word).append("=").append(results.get(word)).append("\n");
                    }
                    ArrayList<String> resultsTask = new ArrayList<>();
                    if (!resultsByTask.isEmpty() && resultsByTask.containsKey(serverId)) {
                        resultsTask = resultsByTask.get(task);
                    }
                    resultsTask.add(result.toString());
                    resultsByTask.put(task, resultsTask);
                }
            }
        } catch (InterruptedException ex) {
            System.err.println("Error : " + ex);
        }

        return resultsByTask;
    }
}
