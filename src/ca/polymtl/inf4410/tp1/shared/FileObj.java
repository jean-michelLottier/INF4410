/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp1.shared;

import java.io.Serializable;

/**
 *
 * @author Lottier Jean-Michel <jean-michel.lottier@polymtl.ca>
 */
public class FileObj implements Serializable {

    private long checkSum;
    private byte[] fileContent;

    public FileObj(long checkSum, byte[] fileContent) {
        this.checkSum = checkSum;
        this.fileContent = fileContent;
    }

    public long getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(long checkSum) {
        this.checkSum = checkSum;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }
}
