/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.request;

import ca.polymtl.inf4410.tp2.commontools.Commands;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class Request implements Serializable {

    private long taskId;
    private int resource;
    private Commands.commands command;
    private byte[] content;
    private int firstLineNumber;
    private int lastLineNumber;
    private HashMap<String, Integer> result;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Commands.commands getCommand() {
        return command;
    }

    public void setCommand(Commands.commands command) {
        this.command = command;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    public void setFirstLineNumber(int firstLineNumber) {
        this.firstLineNumber = firstLineNumber;
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    public void setLastLineNumber(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    public boolean isEmpty() {
        if (taskId == 0
                && command == null
                && content == null
                && firstLineNumber == 0
                && lastLineNumber == 0
                && result == null
                && resource == 0) {
            return true;
        }
        return false;
    }

    public HashMap<String, Integer> getResult() {
        return result;
    }

    public void setResult(HashMap<String, Integer> result) {
        this.result = result;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}
