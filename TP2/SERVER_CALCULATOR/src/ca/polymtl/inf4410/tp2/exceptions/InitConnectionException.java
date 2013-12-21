/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.exceptions;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class InitConnectionException extends Exception {

    /**
     * Creates a new instance of
     * <code>InitConnectionException</code> without detail message.
     */
    public InitConnectionException() {
    }

    /**
     * Constructs an instance of
     * <code>InitConnectionException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InitConnectionException(String msg) {
        super(msg);
    }
}
