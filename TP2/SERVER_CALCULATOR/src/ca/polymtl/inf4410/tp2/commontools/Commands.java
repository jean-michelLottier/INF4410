/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.commontools;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Commands {

    public enum commands {

        init, task, abort, commit, retry, close, fail
    }

    public enum configs {

        mode, port, q, inputPathFile, outputPathFile, hosts, frequencyFalseResults
    }
}
