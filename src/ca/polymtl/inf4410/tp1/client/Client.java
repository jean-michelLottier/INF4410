/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp1.client;

import ca.polymtl.inf4410.tp1.shared.FileObj;
import ca.polymtl.inf4410.tp1.shared.IServer;
import ca.polymtl.inf4410.tp1.shared.NotFoundFileException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Client {

    private static final Logger logger = Logger.getLogger("Client");
    private static ArrayList<String> COMMAND;
    private static final String CLIENT_METADATA = "client_metadata.properties";

    static {
        COMMAND = new ArrayList<>(4);
        COMMAND.add("list");
        COMMAND.add("create");
        COMMAND.add("sync");
        COMMAND.add("push");
    }
    private IServer distantServerStub = null;

    public Client(String distantServerHostname) {
        super();
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        if (distantServerHostname != null) {
            distantServerStub = loadServerStub(distantServerHostname);
        }
    }

    private IServer loadServerStub(String hostname) {
        IServer stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (IServer) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    /**
     * <p>Permet de vérifier si le nombre d'arguments fournis respect les
     * exigences
     * <br/>
     * de la commande.</p>
     *
     * @param args
     * @return
     */
    private boolean isCommandAccepted(String[] args) {
        String command = args[0];
        if (!COMMAND.contains(command)) {
            return false;
        }

        if ((command.equals("create") || command.equals("sync") || command.equals("push"))
                && args.length < 2) {
            return false;
        }

        return true;
    }

    private BufferedWriter initWrite(File file, boolean writeEndFile) throws IOException {
        FileWriter fileWriter = new FileWriter(file, writeEndFile);
        return new BufferedWriter(fileWriter);
    }

    /**
     * <p>Méthode pour écrire dans un fichier</p>
     *
     * @param file
     * @param content
     * @param writeEndFile : true = écriture en fin du fichier, false = écrase
     * le contenu du fichier
     */
    private void write(File file, String content, boolean writeEndFile) {
        try {
            BufferedWriter buff = initWrite(file, writeEndFile);

            buff.write(content);
            buff.flush();
            buff.close();
        } catch (IOException e) {
            System.err.println("Error occured : " + e);
        }
    }

    private BufferedReader initRead(File file) throws FileNotFoundException {
        InputStream ips = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(ips);
        return new BufferedReader(isr);
    }

    /**
     * <p>Lecture du fichier. Le contenu est retourné sous forme de String</p>
     *
     * @param file
     * @return
     */
    private String read(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = initRead(file);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error occured : " + e);
        }
        return sb.toString();
    }

    /**
     * <p>Recherche dans le fichier metadata le checkSum correspondant au
     * fichier 'filename'.</p>
     *
     * @param metadata
     * @param fileName
     * @return le checkSum sinon -1
     */
    private long findCheckSumByFileName(File metadata, String fileName) {
        try {
            BufferedReader br = initRead(metadata);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(fileName)) {
                    String result = line.split(":")[1];
                    return Long.valueOf(result.trim());
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error occured : " + e);
        }
        return -1;
    }

    /**
     * <p>
     * Permet d'insérer le checkSum dans le fichier metadata.
     * <br/>
     * Si le fichier contient déja un checkSum pour le fichier filename alors
     * celui-ci est supprimé.
     * </p>
     *
     * @param metadata
     * @param fileName
     * @param checkSum
     */
    private void replaceCheckSum(File metadata, String fileName, long checkSum) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = initRead(metadata);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains(fileName)) {
                    sb.append(line).append("\n");
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error occured : " + e);
        }
        sb.append(fileName).append(":").append(checkSum).append("\n");
        write(metadata, sb.toString(), false);
    }

    /**
     * <p>Lance la commande "list"</p>
     */
    private void executeCommandList() {
        ArrayList<String> fileList = new ArrayList<>();
        try {
            fileList = distantServerStub.list();
        } catch (RemoteException e) {
            System.err.println("Error: " + e);
        }
        if (fileList == null || fileList.isEmpty()) {
            System.out.println("0 file.");
            return;
        }
        System.out.println("File list:");
        for (String current : fileList) {
            System.out.println("* " + current);
        }
        System.out.println(fileList.size() + " file(s)");
    }

    private void executeCommandCreate(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.out.println("No file name specify!");
            return;
        }
        try {
            long result = distantServerStub.create(filename);
            if (result == -1) {
                System.out.println("You must specify a file name.");
            } else if (result == -100) {
                System.out.println("File already exists.");
            } else {
                System.out.println(filename + " added.");
            }
        } catch (RemoteException e) {
            System.err.println("Error: " + e);
        }
    }

    private void executeCommandSync(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.out.println("No file name specify!");
            return;
        }

        File metadata = new File(CLIENT_METADATA);

        long checkSum = findCheckSumByFileName(metadata, filename);

        try {
            FileObj fileObj = distantServerStub.sync(filename, checkSum);
            if (fileObj == null) {
                System.out.println("File '" + filename + "' is up to date.");
                return;
            }
            if (checkSum > 0) {//file synchronized previously
                replaceCheckSum(metadata, filename, fileObj.getCheckSum());
            } else {//first synchronization
                String property = filename + ":" + String.valueOf(fileObj.getCheckSum()) + "\n";
                write(metadata, property, true);
            }

            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileObj.getFileContent());
            fos.flush();
            fos.close();

            System.out.println(filename + " synchronized");
            System.out.println("checkSum : " + fileObj.getCheckSum());
        } catch (RemoteException e) {
            System.err.println("Error: " + e);
        } catch (NotFoundFileException e) {
            System.err.println("No files found in the server.\n" + e);
        } catch (IOException e) {
            System.err.println("Error occured :" + e);
        }
    }

    private void executeCommandPush(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.out.println("No file name specify!");
            return;
        }
        File metadata = new File(CLIENT_METADATA);
        long checkSum = findCheckSumByFileName(metadata, filename);
        if (checkSum == -1) {
            System.out.println("You must you synchonize with server before to try to push your file");
            return;
        }

        File file = new File(filename);
        try {
            byte[] bytes = new byte[(int) file.length()];
            String textContent = read(file);
            if (textContent != null) {
                bytes = textContent.getBytes();
            }

            long newCheckSum = distantServerStub.push(filename, bytes, checkSum);
            if (newCheckSum == -1) {
                System.out.println("Bad parameters: filename = " + filename
                        + ", checkSum = " + checkSum);
            } else if (newCheckSum == -10 || newCheckSum == -100) {
                System.out.println("File does not exist on the server. First, you must create file in the server.");
            } else if (newCheckSum < -100) {
                System.out.println("File '" + filename + "' is out of date");
                System.out.println("CheckSum customer : " + checkSum);
                System.out.println("ChechSum server : " + -newCheckSum);
            } else {
                System.out.println("File '" + filename + "' has been send on the server.");
                System.out.println("new checkSum : " + newCheckSum);
                replaceCheckSum(metadata, filename, newCheckSum);
            }
        } catch (RemoteException e) {
            System.err.println("Error (remote): " + e);
        }
    }

    public static void main(String[] args) throws RemoteException {
        if (args == null || args.length <= 0) {
            System.out.println("You must enter a command!");
            return;
        }

        Client client = new Client("127.0.0.1");

        boolean result = client.isCommandAccepted(args);
        if (!result) {
            System.out.println("Your command is not acceptable");
            System.out.println("recall : "
                    + "\n command  |  parameter"
                    + "\n list     |  -"
                    + "\n create   |  filename"
                    + "\n sync     |  filename"
                    + "\n push     |  filename");
            return;
        }

        String command = args[0];
        switch (command) {
            case "list":
                client.executeCommandList();
                break;
            case "create":
                client.executeCommandCreate(args[1]);
                break;
            case "sync":
                client.executeCommandSync(args[1]);
                break;
            case "push":
                client.executeCommandPush(args[1]);
                break;
        }
    }
}
