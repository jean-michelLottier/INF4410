/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.server;

import ca.polymtl.inf4410.tp2.commontools.Commands;
import ca.polymtl.inf4410.tp2.commontools.PropUtils;
import ca.polymtl.inf4410.tp2.exceptions.SendRequestException;
import ca.polymtl.inf4410.tp2.request.ISendRequest;
import ca.polymtl.inf4410.tp2.request.Request;
import ca.polymtl.inf4410.tp2.request.SendRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Server extends Thread {

    private static final Logger logger = Logger.getLogger("Server");
    private static final String pathConfig = "../config";
    private static Properties properties;
    private Socket socket;
    private static byte[] text;
    private PropUtils utils;
    private ISendRequest sendRequest;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public PropUtils getUtils() {
        return utils;
    }

    public void setUtils(PropUtils utils) {
        this.utils = utils;
    }

    @Override
    public void run() {
        logger.info("\n++++++++++++++++++++++++++++++++++++++"
                + "\n++++++++++ Start connection ++++++++++"
                + "\n++++++++++++++++++++++++++++++++++++++");

        boolean isSocketOpen = true;
        sendRequest = new SendRequest();
        utils = new PropUtils();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            while (isSocketOpen) {
                Request request = (Request) ois.readObject();

                if (request == null || request.isEmpty()) {
                    sendRequest.sendRequest(socket, oos, Commands.commands.retry, Long.valueOf(properties.getProperty("ipaddress")));
                    continue;
                }

                if (request.getCommand().equals(Commands.commands.close)) {
                    logger.info("---------- Recept command : close ----------");
                    isSocketOpen = false;
                    continue;
                }

                if (request.getCommand().equals(Commands.commands.init)) {
                    logger.info("---------- Recept command : init ----------");
                    System.out.println("taskId : " + request.getTaskId()
                            + " command : " + request.getCommand().toString()
                            + " resource : " + request.getResource());
                    Commands.commands command = initTask(request);
                    //Thread.sleep(3000);
                    sendRequest.sendRequest(socket, oos, command, Long.valueOf(properties.getProperty("ipaddress")));
                    continue;
                }

                if (request.getCommand().equals(Commands.commands.task)) {
                    logger.info("---------- Recept command : task ----------");
                    System.out.println("taskId : " + request.getTaskId()
                            + " command : " + request.getCommand().toString()
                            + " task : " + request.getFirstLineNumber() + " to " + request.getLastLineNumber());
                    String textToAnalyze = utils.extractPeaceOfTextByLinesSelected(new String(text, "UTF-8"), request.getFirstLineNumber(), request.getLastLineNumber());
                    int u = textToAnalyze.getBytes().length;
                    double q = Double.valueOf(properties.getProperty(Commands.configs.q.toString()));
                    System.out.println("ressource utilisée : " + u + " ressource dispoible : " + q);
                    //Thread.sleep(3000);
                    if (isServerCanProcessTask(q, u)) {
                        System.out.println("Send command 'commit' for task : " + request.getTaskId());
                        textToAnalyze = utils.cleanText(textToAnalyze);
                        HashMap<String, Integer> result = countInstances(textToAnalyze);
                        sendRequest.sendRequest(socket, oos, Commands.commands.commit, Long.valueOf(properties.getProperty("ipaddress")), result);
                    } else {
                        System.out.println("Send command 'abort' for task : " + request.getTaskId());
                        sendRequest.sendRequest(socket, oos, Commands.commands.abort, Long.valueOf(properties.getProperty("ipaddress")), request.getFirstLineNumber(), request.getLastLineNumber());
                    }
                }
            }
            logger.info("\n++++++++++++++++++++++++++++++++++++"
                    + "\n++++++++++ End connection ++++++++++"
                    + "\n++++++++++++++++++++++++++++++++++++");
            oos.close();
            ois.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            logger.log(Level.WARNING, "Error : {0}", ex);
        } /*catch (InterruptedException ex) {
            logger.log(Level.WARNING, "Error : {0}", ex);
        } */catch (SendRequestException ex) {
            logger.log(Level.WARNING, "Error : {0}", ex);
        }
    }

    private Commands.commands initTask(Request task) {
        if (task.getContent() == null) {
            return Commands.commands.retry;
        }

        text = task.getContent();

        //if (!properties.containsKey(Commands.configs.q.toString())) {
            properties.setProperty(Commands.configs.q.toString(), String.valueOf(task.getResource()));
        //}
        return Commands.commands.task;
    }

    private static String initServer() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Veuillez saisir l'hôte sur lequel le serveur tourne :");
        String serverName = scanner.nextLine();
        FileInputStream fis;

        try {
            File file = new File(pathConfig);
            fis = new FileInputStream(file);
            properties = new Properties();
            properties.load(fis);
            fis.close();
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, "Error : {0}", ex);
        } catch (IOException ex) {
            logger.log(Level.INFO, "Error : {0}", ex);
        }
        return serverName;
    }

    private static void initConnection(int port, String serverName) throws IOException {
        InetAddress address = InetAddress.getByName(serverName);
        properties.put("ipaddress", address.getHostAddress().replaceAll("\\.", ""));
        ServerSocket serverSocket = new ServerSocket(port, 10, address);
        while (true) {
            Server server = new Server(serverSocket.accept());
            server.start();
        }
    }

    /**
     * <p>
     * Compte les instances se trouvant dans un objet String.<br/>
     * Les instances trouvés sont intégrés dans une map :
     * </p>
     * <ul>
     * <li>key : instance</li>
     * <li>value : nombre d'intances trouvés pour la clé donnée</li>
     * </ul>
     *
     * @param content
     * @return
     */
    private HashMap<String, Integer> countInstances(String content) {
        String[] words = content.split(" ");
        HashMap<String, Integer> instances = new HashMap<>();
        for (String word : words) {
            word = word.trim().replace("\n", "");
            if (instances.containsKey(word)) {
                int cpt = instances.get(word);
                instances.put(word, ++cpt);
            } else {
                instances.put(word, 1);
            }
        }

        if (properties.get(Commands.configs.mode.toString()).equals("nosecure")) {
            instances = applyMaliciousMethod(instances);
        }

        return instances;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String serverName = initServer();

        try {
            initConnection(Integer.valueOf(properties.getProperty("port")), serverName);
        } catch (IOException e) {
            logger.log(Level.INFO, "Error : {0}", e);
        }
    }

    private boolean isServerCanProcessTask(double q, int u) {
        if (u <= q) {
            return true;
        }

        long failureRate = (long) (((u - q) / (9 * q)) * 100);
        int rand = (int) (Math.random() * 100);
        if (rand <= failureRate) {
            return false;
        } else {
            return true;
        }
    }

    private HashMap<String, Integer> applyMaliciousMethod(HashMap<String, Integer> instances) {
        int frequency = Integer.valueOf(properties.getProperty(Commands.configs.frequencyFalseResults.toString()));
        int rand = (int) (Math.random() * 100);
        if (rand <= frequency) {
            rand = (int) (Math.random() * 5);
            for (String word : instances.keySet()) {
                int value = instances.get(word) + rand;
                instances.put(word, value);
            }
        }
        return instances;
    }
}
