/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import synchronizingdirectories.TmpFile;


/**
 *
 * @author Icemen78
 */
public class Converter implements Runnable{
    private static volatile Process static_p;
    private static final int CTR_READY=0;
    private static final int CTR_BUSY=1;
    private volatile int state = CTR_READY;
    
    
    private final List<ConverterListener> listeners = new ArrayList<>();
    private volatile boolean blnSkip=false;
    private volatile boolean interrupted=false;
    private final File module;
    private final File source;
    private File returnedFile=null;
    private synchronizingdirectories.TmpFile target;
    private ParamFFMPEG param;
    private FileInputStream filelock;
    private String commandFFMPEG="";
    private String compression="";
    private final long filesize;
    private static final int timeout=10000;
    private final boolean useCurrentDirForTMP;
    private boolean blnDeleteTrashFile;
    private boolean blsSkipBadCompression;
    
    public Converter(File file, File moduleFile, long filesize, boolean useCurrentDir) {
        state = CTR_BUSY;
        module=moduleFile;
        source=file;
        ParamFFMPEG.FormatOUT format = ParamFFMPEG.FormatOUT.avi;
        try {
            filelock = new FileInputStream(source);
            target = new TmpFile(new File(source.getAbsolutePath().substring(0,source.getAbsolutePath().lastIndexOf(".")+1)+format.getValue()),useCurrentDir);
        }catch (Exception e) {
        }
        this.filesize=filesize;
        useCurrentDirForTMP=useCurrentDir;
    }
    public Converter(File file, File moduleFile, long filesize, boolean useCurrentDir, String format) {
        state = CTR_BUSY;
        module=moduleFile;
        source=file;
        try {
            filelock = new FileInputStream(source);
            target = new TmpFile(new File(source.getAbsolutePath().substring(0,source.getAbsolutePath().lastIndexOf(".")+1)+format),useCurrentDir);
        }catch (Exception e) {
            
        }
        this.filesize=filesize;
        useCurrentDirForTMP=useCurrentDir;
    }
    
    private static synchronized Process getProcess(String command) throws IOException{
        boolean cancontinue=false;
        while (!cancontinue) {
            if (static_p!=null) {
                try {
                    int tryexitvalue = static_p.exitValue();
                    cancontinue = true;
                }catch (IllegalThreadStateException e) {
                    try {
                        Thread.sleep(1);
//                        System.out.println("... подождали освобождения потока 5 мсек");
                    } catch (InterruptedException ie) {
                        break;
                    }                    
                }
            }else {
                cancontinue = true;
            }
        }
        if (cancontinue) {
            static_p=Runtime.getRuntime().exec(command);
        }        
        return static_p;
    }
    
    private static synchronized int finProcess(){
        //exitValue:    -2 process has interrupted;
        //exitValue:    -1 process not exists;
        //exitValue:     0 normal finished;
        //exitValue:     1 process has inner error;
        int retval = -1;
        if (static_p!=null) {
            try {
                retval=static_p.exitValue();
            }catch(IllegalThreadStateException itse) {
                static_p.destroy();
                while (retval == -1) {
                    try {
                        Thread.sleep(1);
//                        System.out.println("... подождали завершения потока 1 мсек");
                        retval=static_p.exitValue();
                    } catch (Exception e) {
                        if (e instanceof InterruptedException) {
                            retval = -2;
                            break;
                        }else {
//                            System.out.println(e.toString());
                        }
                    }
                }
            }catch (Exception e) {
//              System.out.println(e.toString());
            }
            static_p=null;
        }
//        System.out.println(":**** exit value: "+retval);
        return retval;
    }
    
    public boolean convert(ParamFFMPEG par){
        blnDeleteTrashFile=par.getDeletingTrashFile();
        blsSkipBadCompression=par.getSkipBadCompression();
        boolean retval = true;
        try {
            if (!source.exists()){
                throw new IOException("Ошибка. Исходный файл отсутстует: "+source.getAbsolutePath());
            }
            if (par!=null) {
                if (!par.isReady() && !par.attachFileInfo(createFileInfo())) {throw new Exception ("Не удалось применить параметры конвертации");}
            }else {
                throw new Exception ("Не заданы параметры конвертации");
            }
        }catch (Exception e) {
            //Сообщение об ошибке и применение параметров по умолчанию
            //Нужен либо диалог, либо вывод сообщения через hasLogging
            //^^^
//            System.out.println("***!!!*** " + e.getMessage());
//            System.out.println("***!!!*** " + "Использованы параметры по умолчанию");
//            par = new ParamFFMPEG();
            //^^^
            toFinished(false,e);
            retval = false;
        }
        if (retval) {
            this.param=par;
            Thread t = new Thread(this, "converting");
            t.start();            
        }
        return retval;
    }
    
    public ParamFFMPEG.FileInfo createFileInfo() throws Exception {
        Exception exception = null;
        final Timer t = new Timer(timeout, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    interrupted=true;
                    toFinished(false, new Exception("Процесс не отвечает... прерывание (createFileInfo())"));
                }
            });
        t.setRepeats(false);
        t.start();
        BufferedReader input = null;
        String modulename = "\""+module.getAbsolutePath()+"\"";
        String fsname = "\""+source.getAbsolutePath()+"\"";
        String pinput="-i "+fsname;
        String command = modulename + " " + pinput;
//        String errMessage="";
        
        ArrayList<String> stream = new ArrayList<>();
        ParamFFMPEG.FileInfo fileinfo=null;
        try {
            Process p = getProcess(command);
            input = new BufferedReader( new InputStreamReader( p.getErrorStream()) );
            String line = input.readLine();
            while(line != null && !interrupted) {
//                System.out.println(line);
                t.restart();
                stream.add(line.trim());
                if (!interrupted) {
                    t.restart();
                }else {
                    //Killed process found..
                    throw new KilledProcessFound("Процесс прерван, отмена...");
                }
                line = input.readLine();
            }
            if (interrupted) {
                throw new KilledProcessFound("Процесс прерван, отмена...");
            }
            fileinfo = new ParamFFMPEG.FileInfo(stream,source,blnDeleteTrashFile);
        } catch (Exception ex) {
            exception=ex;
        }
        t.stop();
        if (input!=null) {
            try {input.close();}catch(IOException ex1) {input=null;}
        }
        if (exception != null) {throw exception;}
        toFinished(true,null);
        return fileinfo;
    }
        
    @Override
    public void run() {
        toStarted();
        BufferedReader input=null;
        Exception e=null;
        final Timer t = new Timer(timeout, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    interrupted=true;
                    toFinished(false, new Exception("Процесс не отвечает... прерывание (run())"));
                }
            });
        t.setRepeats(false);
        t.start();
        try {
            String modulename = "\""+module.getAbsolutePath()+"\"";
            String fsname = "\""+source.getAbsolutePath()+"\"";
            String ftname = "\""+target.getAbsolutePath()+"\"";
            String pquality=param.getQuality();         //"26";//range 1(maxQuality)..51(minQuality) ref 18..26
            String presolution=param.getResolution();   //"1280x720";
            String prange=param.getRange();             //"25";
            String pformat=param.getFormat();           //"avi";
            String pbitrate=param.getBitrate();
            String pfrequency=param.getFrequency();
            String pmodule=modulename;
            String pinput="-i "+fsname + " -map 0:" + param.getVideoKey() + " -map 0:" + param.getAudioKey() + " ";
            //String pvparam="-y -s "+presolution+" -sar 1:1 -aspect 16:9 -r ...
            String pvparam="-y -s "+presolution+" -r "+prange+" -g 25 -keyint_min 1 -f "+pformat+" -vcodec libx264 -profile:v high422 -me_method umh -subq 6 -me_range 32 -bf 2 -intra-refresh 1 -b-pyramid 1 -refs 1 -qdiff 1 -fast-pskip 0 -mbtree 0 -threads 2 -thread_type 2 -qp "+pquality;
            String paparam="-acodec aac -strict experimental -af aresample=async=1:min_hard_comp=0.100000:first_pts=0 -ac 2 -b:a "+pbitrate+" -ar "+pfrequency;
            String poutput=ftname;
            String command=pmodule+" "+pinput+" "+pvparam+" "+paparam+" "+poutput;
            //command=pmodule+" "+pinput+" "+"-vn -ar 44100 -ac 2 -ab 256K -f mp3 "+poutput;
            //String command=modulename+" -re -i "+fileSource+fname+" -c copy -f flv rtmp://localhost/myapp/mystream/";
            //String command=modulename+" -i "+fileSource+fname+" -y -s 1280x720 -sar 1:1 -aspect 16:9 -r 25 -g 25 -keyint_min 1 "+" -f avi "+" -vcodec libx264 -profile high -qp "+pquality+"26 -me_method umh -subq 6 -me_range 32 -bf 2 -intra-refresh 1 -b-pyramid 1 -refs 1 -qdiff 1 -fast-pskip 0 -mbtree 0 -threads 2 -thread_type 2 -acodec aac -strict experimental -ac 2 -b:a 192K -ar 48000  "+fileSource+fname2;

            //override param
            //command=modulename+" -i "+fileSource+fname;

            System.out.println("Command: "+command);
            
            this.commandFFMPEG=command;
            Process p = getProcess(command);
            ParamFFMPEG.Duration dur=param.getDutarion();
            ParamFFMPEG.Frame frm;
            
            input = new BufferedReader( new InputStreamReader( p.getErrorStream()) );
            String line = input.readLine();
            while(line != null && !blnSkip && !interrupted) {
//                System.out.println(line);
                t.restart();
                String sout=line.trim().toLowerCase();
                if (sout.contains("frame=")&&sout.contains("time=")&&sout.contains("bitrate=")&&sout.contains("speed=")){
                    frm= new ParamFFMPEG.Frame(sout.toLowerCase(),dur);
                    frm.setCompressed(frm.getBitrate()/param.getDutarion().getBitrate());
                    toProgressed(frm);
                    if (blsSkipBadCompression && frm.minCompressionBreaked()) {
                        throw new Exception("Результат сжатия недостаточен: "+frm.getCompressed(true)+" Максимум "+frm.getCompressed((1D-ParamFFMPEG.minCompression),true));
                    }
                }else {
                    System.out.println(sout);
                }
//                Thread.sleep(11000);
                line = input.readLine();
            }
            if (interrupted) {
                throw new KilledProcessFound("Процесс прерван, отмена...");
            }else if(blnSkip) {
                throw new Exception("Прервано пользователем, изменения не сохранены");
            }
        }catch (Exception ex) {
            e = ex;
        }
        t.stop();
        if (input!=null) {
            try {input.close();}catch(IOException ex1) {input=null;}
        }
        this.toFinished(false, e);
    }
    public String getSourceFilename() {
        return this.source.getAbsolutePath();
    }
    public ParamFFMPEG getParamFFMPEG() {
        return this.param;
    }
    public String getCompression() {
        return this.compression;
    }
    
    public void addListener(ConverterListener obj) {
        listeners.add(obj);
    }
    public void removeListener(ConverterListener obj) {
        listeners.remove(obj);
    }
    public boolean isBusy() {
        return (state==CTR_BUSY);
    }
    public File returnedFile(){
        return returnedFile;
    }
    public void toSkip(){
        blnSkip=true;
        int i=0;
        int interval=100;
        int timeout=10*interval;
        try {
            while (i<timeout && static_p!=null) {
                Thread.sleep(interval);
                i+=interval;
            }
        }catch (Exception e) {
            i=timeout;
        }
        if (i>=timeout) {
            interrupted=true;
            this.toFinished(false, new Exception("Ошибка прерывания текущего процесса"));
        }
    }
    
    private void toStarted() {
        for (ConverterListener cl:listeners) {cl.started(this);}
    }
    
    private void toProgressed(ParamFFMPEG.Frame frame) {
        if (frame.isReady()) {
            for (ConverterListener cl:listeners) {cl.progressed(frame);} 
        }
    }
    
    private void toFinished(boolean toContinue, Exception ex){
//        System.out.println(toContinue+" "+(ex!=null?ex.getMessage():"null"));
        Exception exception = ex;
        if (exception!=null && exception instanceof KilledProcessFound) {
            //Killed process finished
        }else {
            double comp=0D;
            int exitValue = finProcess();

            if (toContinue) {
                //end this method.
            }else {
                //building a chain of exceptions...
                unlockSource();

                DecimalFormat df = new DecimalFormat("#.##");
                if (exception==null && exitValue==1) {
                    exception = new Exception("Ошибка кодека. Изменения не сохранены ("+this.commandFFMPEG+")");    
                }
                if(exception!=null) {
                    if (ex instanceof TrashFileException) {
                        TrashFileException tfe =(TrashFileException)ex;
                        try {
                            if (!tfe.getTrashFile().exists()) {
                                exception = new Exception("Ошибка удаления видео, продолжительностью "+tfe.getTrashFileDuration()+" сек" + " - файл ОТСУТСТВУЕТ");
                            }else if (!tfe.getTrashFile().delete()){
                                exception = new Exception("Ошибка удаления видео, продолжительностью "+tfe.getTrashFileDuration()+" сек" + " - файл занят");
                            }else {
                                exception = new Exception("Файл удален, т.к. продолжительность видео "+tfe.getTrashFileDuration()+" сек");
                            }
                        }catch (Exception e) {
                            exception=e;
                        }
                    }else if (ex instanceof CrashFileException) {
                        CrashFileException cfe =(CrashFileException)ex;
                        try {
                            File file = new File(cfe.getCrashFile().getAbsolutePath()+".crash");
                            if (!cfe.getCrashFile().exists()) {
                                exception = new Exception("Ошибка переименования поврежденного или неподдерживаемого видео ("+cfe.getCrashFile().getAbsolutePath()+") " + " - файл ОТСУТСТВУЕТ");
                            }else if (file.exists() && !cfe.getCrashFile().delete()){
                                exception = new Exception("Ошибка переименования поврежденного или неподдерживаемого видео ("+file.getAbsolutePath()+") " + " - файл существует и занят");
                            }else if (!cfe.getCrashFile().renameTo(file)){
                                exception = new Exception("Ошибка переименования поврежденного или неподдерживаемого видео ("+cfe.getCrashFile().getAbsolutePath()+") " + " - исходный файл существует и занят");
                            }else {
                                exception = new Exception("Файл переименован, т.к. он поврежден или его формат не поддерживается ("+file.getAbsolutePath()+")");
                            }
                        }catch (Exception e) {
                            exception=e;
                        }
                    }
                    if (target!=null) {target.rollup();}
                }else {
                    try {
                        //Замена оригинального файла
                        comp=(double)this.target.length()/ (double)this.source.length();
                        this.target.commit(this.source);
                    } catch (Exception e) {
                        exception = e;
                    }
                    if (exception==null) {
                        this.compression = df.format(comp)+"x";
                    }
                }
                state = CTR_READY;
                if (exception !=null) {
                    returnedFile=this.source;
                }else {
                    returnedFile=this.target;
                }
                for (ConverterListener cl:listeners) {cl.finished(this, exception);}
            }
        }
    }
    public long getFileSise() {
        return filesize;
    }
    private void unlockSource() {
        try {
            filelock.close();
            Thread.sleep(5);
        } catch (Exception e) {
            //IOException | InterruptedException e
        }
    }
    public static File fixFilename(File source) {
        File tgt;
        int spaceCountMax=0;
        int spaceCount=0;
        String fname=source.getAbsolutePath();
        for (char c:fname.toCharArray()) {
            if (c==" ".toCharArray()[0]) {
                spaceCount++;
            }else if (spaceCount>0){
                spaceCountMax = spaceCount>spaceCountMax?spaceCount:spaceCountMax;
                spaceCount=0;
            }
        }
        if (spaceCountMax>1) {
            for (int i=spaceCountMax;i>1;i--) {
                String space="";
                for (int j=1;j<=i;j++) {
                    space+=" ";
                }
                fname=fname.replace(space, " ");
            }
            tgt=new File(fname);
            source.renameTo(tgt);
        }else {
            tgt=source;
        }
        return tgt;
    }
}
