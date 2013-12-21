/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.exceptions;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class SendRequestException extends Exception {

    /**
     * Creates a new instance of
     * <code>SendRequestException</code> without detail message.
     */
    public SendRequestException() {
    }

    /**
     * Constructs an instance of
     * <code>SendRequestException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public SendRequestException(String msg) {
        super(msg);
    }
}
