/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Icemen78
 */
public class Controller implements Runnable{
    public static final int CLR_UNKNOWN=0;
    public static final int CLR_READY=1;
    public static final int CLR_BUSY=2;
    private int state=CLR_UNKNOWN;
    
    private final List<ControllerListener> listeners = new ArrayList<>();
    private final File module;
    private final TreeMap<File,Long> files;
    private TreeMap<File,Long> filesConv = new TreeMap<>();
    private TreeMap<File,Long> filesError = new TreeMap<>();
    private volatile Converter converter=null;
    private final long totalsize;
    private final ParamFFMPEG userParam;
    final Object lock = new Object();
    private boolean interrupt=false;
    private final boolean useCurrentDirForTMP;
    
    public Controller (TreeMap<File,Long> files, File moduleFile, ParamFFMPEG par) {
        module=moduleFile;
        userParam = par;
        this.files=files;
        totalsize=CalcTotalSize(files);
        useCurrentDirForTMP=false;
    }
    public Controller (TreeMap<File,Long> files, File moduleFile, ParamFFMPEG par, boolean useCurrentDir) {
        module=moduleFile;
        userParam = par;
        this.files=files;
        totalsize=CalcTotalSize(files);
        useCurrentDirForTMP=useCurrentDir;
    }
    private long CalcTotalSize(TreeMap<File,Long> files){
        long retval=0;
        if (!files.isEmpty()) {
            TreeSet<File> key = new TreeSet<>(files.keySet());
            for (File file:key) {
                if (file.exists()) {retval += file.length();}
            }
        }
        return retval;
    }
    public void addListener(ControllerListener obj) {
        listeners.add(obj);
    }
    public void removeListener(ControllerListener obj) {
        listeners.remove(obj);
    }
    @Override
    public void run() {
        state=CLR_BUSY;
        if (files.size()>0) {
            TreeSet<File> key = new TreeSet<>(files.keySet());
            for (final File f : key) {
                if (interrupt) {break;}
                converter = new Converter(f,module,files.get(f),useCurrentDirForTMP,userParam.getFormat());
                converter.addListener(new ConverterListener() {
                    @Override
                    public void started(Converter converter) {
//                        toLogging(converter.getParamFFMPEG().getStringFileInfo(true),false);
                        toLogging(converter.getParamFFMPEG().getTitle().toString(),false);
                        toLogging(converter.getParamFFMPEG().getDutarion().toString(),false);
                        toLogging("source | "+converter.getParamFFMPEG().toString(true),false);
                        toLogging("target | "+converter.getParamFFMPEG().toString(),false);
                    }
                    @Override
                    public void progressed(ParamFFMPEG.Frame current) {
                        hasProgressed(current);
                    }
                    @Override
                    public void finished(Converter converter, Exception e) {
                        hasProgressTotal(converter,e);
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
                
                try {
                    if (converter.convert(userParam.clone())) {
                        synchronized (lock) {
                            try {
                                while (converter.isBusy()) {
                                    lock.wait();
                                }
                            } catch (InterruptedException ex) {
                                //Добавляем инфу
                                toLogging(ex.getMessage(),true);
                                break;
                            }                    
                        }
                    }
                }catch (CloneNotSupportedException ex){
                    //Добавляем инфу
//                    ex.printStackTrace();
                    hasProgressTotal(converter,ex);
                    //continue;
                }
                
            }
            toFinished();
        }
        state=CLR_READY;
    }
    public long getTotalFSize(){
        return totalsize;
    }
    public int getState() {
        return state;
    }
    public void start() {
        Thread t = new Thread(this, "packetconverter");
        t.start();
    }
    
    private void toFinished() {
        for (ControllerListener cl:listeners) {cl.finished();} 
    }
    private void toLogging(String message, boolean important) {
        for (ControllerListener cl:listeners) {cl.logging(message, important);}
    }
    private void hasProgressed(ParamFFMPEG.Frame current){
        for (ControllerListener cl:listeners) {cl.progressed(current);}
    }
    private void hasProgressTotal(Converter converter, Exception e) {
        if (e!=null) {
            filesError.put(converter.returnedFile(), converter.getFileSise());
//           Добавляем инфу
            toLogging(e.getMessage(),true);
        }else {
            filesConv.put(converter.returnedFile(), converter.returnedFile().length());
        }
        for (ControllerListener cl:listeners) {cl.progressedTotal(converter);}        
    }
    public TreeMap<File,Long> getFiles(){
        return files;
    }
    public TreeMap<File,Long> getConverdedFiles(){
        return filesConv;
    }
    public TreeMap<File,Long> getErrorFiles(){
        return filesError;
    }
    void interrutp() {
        interrupt = true;
        converter.toSkip();
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
