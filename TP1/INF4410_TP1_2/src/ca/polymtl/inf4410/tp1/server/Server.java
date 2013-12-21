/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp1.server;

import ca.polymtl.inf4410.tp1.shared.FileObj;
import ca.polymtl.inf4410.tp1.shared.IServer;
import ca.polymtl.inf4410.tp1.shared.NotFoundFileException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Server implements IServer {

    private static final Logger logger = Logger.getLogger("Server");
    /**
     * Map servant de base de données temporaire pour le serveur
     */
    private Map<String, FileObj> filesIntoServer;

    public Server() {
        super();
    }

    public Map<String, FileObj> getFilesIntoServer() {
        return filesIntoServer;
    }

    public void setFilesIntoServer(Map<String, FileObj> filesIntoServer) {
        this.filesIntoServer = filesIntoServer;
    }

    @Override
    public long create(String fileName) throws RemoteException {
        if (fileName == null || fileName.isEmpty()) {
            logger.info("You must specify a file name.");
            return -1;
        }
        Map<String, FileObj> filesMap = getFilesIntoServer();
        if (filesMap == null || filesMap.isEmpty()) {
            filesMap = new HashMap<>();
        }
        if (filesMap.containsKey(fileName)) {
            logger.info("File already exists.");
            return -100;
        }
        byte[] fileContent = new byte[0];
        long checkSum = generateCheckSum();
        FileObj fileObj = new FileObj(checkSum, fileContent);
        filesMap.put(fileName, fileObj);
        setFilesIntoServer(filesMap);
        logger.log(Level.INFO, "{0} added, checkSum={1}", new Object[]{fileName, checkSum});

        return checkSum;
    }

    @Override
    public ArrayList<String> list() throws RemoteException {
        Map<String, FileObj> fileMap = getFilesIntoServer();
        ArrayList<String> filesList = new ArrayList<>();
        if (fileMap == null || fileMap.isEmpty()) {
            return filesList;
        }
        for (String current : fileMap.keySet()) {
            filesList.add(current);
        }
        return filesList;
    }

    @Override
    public long push(String fileName, byte[] contents, long checkSum) throws RemoteException {
        if (fileName == null || fileName.isEmpty() || checkSum <= 0) {
            logger.info("Error(s) found in parameters.");
            return -1;
        }

        Map<String, FileObj> filesMap = getFilesIntoServer();
        if (filesMap == null || filesMap.isEmpty()) {
            logger.info("No files found in the server.");
            return -10;
        }
        if (!filesMap.containsKey(fileName)) {
            logger.info("File does not exist on the server.");
            return -100;
        }
        FileObj fileObj = filesMap.get(fileName);
        if (fileObj.getCheckSum() != checkSum) {
            logger.info("File has been modified by an other client recently.");
            return -fileObj.getCheckSum();
        }

        long newCheckSum = generateCheckSum();
        System.out.println("holdCheckSum = " + checkSum + ", newCheckSum = " + newCheckSum);
        fileObj.setCheckSum(newCheckSum);
        fileObj.setFileContent(contents);
        filesMap.put(fileName, fileObj);
        this.setFilesIntoServer(filesMap);
        return newCheckSum;
    }

    /**
     * <p>Méthode pour récupérer la dernière version d'un fichier spécdifié.</p>
     *
     * @param fileName
     * @param checkSum
     * @return FileObj or null if file not found on the server or checkSum is
     * recent
     * @throws RemoteException
     */
    @Override
    public FileObj sync(String fileName, long checkSum) throws RemoteException, NotFoundFileException {
        if (fileName == null || fileName.isEmpty() || checkSum < -1 || checkSum == 0) {
            logger.info("Error(s) found in parameters.");
            return null;
        }
        Map<String, FileObj> fileMap = getFilesIntoServer();
        if (fileMap == null || fileMap.isEmpty()) {
            logger.info("No files found in the server.");
            throw new NotFoundFileException();
        }
        if (!fileMap.containsKey(fileName)) {
            logger.info("File does not exist on the server.");
            throw new NotFoundFileException();
        }
        FileObj fileObj = fileMap.get(fileName);
        System.out.println("checkSum(parameter) = " + checkSum + ", checkSum(file) = " + fileObj.getCheckSum());
        if (fileObj.getCheckSum() == checkSum) {
            logger.log(Level.INFO, "File ''{0}'' is up to date.", fileName);
            return null;
        }
        return fileObj;
    }

    /**
     * <p>
     * Création des checkSum en fonction de la date de l'appel à la seconde
     * près.
     * <br/>
     * <strong>Remarque : </strong> il serait préférable, pour une utilisation
     * "industrie", d'aller au moins jusqu'à la milliseconde.
     * </p>
     *
     * @return
     */
    private long generateCheckSum() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return Long.valueOf(dateFormat.format(calendar.getTime()).toString());
    }

    private void run() throws Exception {
        if (System.getSecurityManager() == null) {
            try {
                System.setSecurityManager(new SecurityManager());
            } catch (SecurityException e) {
                System.err.println("the security manager has already been set\n"
                        + "and its checkPermission method doesn't allow it to be replaced.");
            }
        }

        try {
            IServer iServer = (IServer) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
            registry.bind("server", iServer);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err
                    .println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.run();
    }
}
