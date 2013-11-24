/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp1.shared;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class NotFoundFileException extends Exception {

    /**
     * Creates a new instance of
     * <code>NotFoundFileException</code> without detail message.
     */
    public NotFoundFileException() {
    }

    /**
     * Constructs an instance of
     * <code>NotFoundFileException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotFoundFileException(String msg) {
        super(msg);
    }
}
