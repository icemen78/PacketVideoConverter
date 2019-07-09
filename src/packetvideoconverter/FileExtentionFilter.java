/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

/**
 *
 * @author Icemen78
 */
public class FileExtentionFilter  extends javax.swing.filechooser.FileFilter implements java.io.FileFilter{
    private final String[] extentions;
    public FileExtentionFilter(String[] filter) {
        extentions=filter;
    }
    @Override
    public boolean accept(java.io.File file) {
        boolean retval = file.isDirectory();
        if (!retval) {
            for(String filter:extentions) {
               if(file.getName().toLowerCase().endsWith(filter.toLowerCase())) {
                   retval=true;
                   break;
               }
            }                    
        }
        return retval;
    }

    @Override
    public String getDescription() {
        String retval="";
        for (String value:extentions) {
            value="'"+value+"'";
            retval += (retval.length()>0)?(", "+value):value;
        }
        return retval;
    }
    
}
