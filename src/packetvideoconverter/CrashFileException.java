/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import java.io.File;

/**
 *
 * @author User
 */
public class CrashFileException extends Exception{
    private static final long serialVersionUID = -5683185501143660560L;
    private final File crashfile;
    public CrashFileException(String message, File file){
        super(message);  
        crashfile=file;
    }
    public File getCrashFile() {
        return crashfile;
    }
}
