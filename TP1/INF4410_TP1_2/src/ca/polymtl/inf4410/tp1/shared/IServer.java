/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public interface IServer extends Remote {

    /**
     * <p>Cette méthode permet de créer un fichier de nom filename vide sur le serveur</p>
     * @param fileName
     * @return un code erreur ou le checksum
     * @throws RemoteException 
     */
    long create(String fileName) throws RemoteException;

    /**
     * <p>Retourne la liste des fichiers se trouvant sur le serveur<p>
     * @return
     * @throws RemoteException 
     */
    ArrayList<String> list() throws RemoteException;

    /**
     * <p>
     * Méthode permettant au client de mettre sur serveur la version modifiée
     * de fichier filename. 
     * <br/>
     * Au préalable, vérification du checkSum du client par rapport au checkSum
     * du même fichier coté serveur, afin de savoir si le client a la dernière version
     * du fichier.
     * </p>
     * @param fileName
     * @param contents
     * @param checkSum
     * @return
     * @throws RemoteException 
     */
    long push(String fileName, byte[] contents, long checkSum) throws RemoteException;

    /**
     * <p>
     * Permet au client de récupérer la dernière version du fichier filename.
     * <br/>
     * Si checkSum client est similaire au checkSum du serveur alors la version du client 
     * et à jour. (=> pas besoin de lui fournir la dernière version)
     * </p>
     * @param fileName
     * @param checkSum
     * @return
     * @throws RemoteException
     * @throws NotFoundFileException 
     */
    FileObj sync(String fileName, long checkSum) throws RemoteException, NotFoundFileException;
}
