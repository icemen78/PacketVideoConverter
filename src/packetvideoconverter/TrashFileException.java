/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import java.io.File;

/**
 *
 * @author Icemen78
 */
public class TrashFileException extends Exception {
    private static final long serialVersionUID = 432192543152055556L;
    private final File trashfile;
    private final double fileduration;
    public TrashFileException(String message, double duration, File file){
        super(message);  
        trashfile=file;
        fileduration=duration;
    }
    public File getTrashFile() {
        return trashfile;
    }
    public double getTrashFileDuration() {
        return fileduration;
    }
    
}
