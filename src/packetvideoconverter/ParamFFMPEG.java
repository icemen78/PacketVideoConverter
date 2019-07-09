/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Icemen78
 */
public class ParamFFMPEG implements Serializable, Cloneable{
    private static final long serialVersionUID = 2149644430609735448L;
    public static final double maxLenTrashFile=(double)5;
    public static final double minCompression=0.1D;
    public static final double minCompressionDelayPercent=0.05D;                     // 1.0D is 100 percent by duration
    public static final boolean DELETE_TRASH_FILE_DEFAULT=true;
    public static final boolean SKIP_BAD_COMPRESSION_DEFAULT=true;
    public static final boolean TRY_TO_FULL_WIDTH_DEFAULT=true;
    public static final Resolution resDefault = Resolution.res1280x720;
    public static final Frequency freqDefault = Frequency.freq48000;
    public static final Quality qualityDefault = Quality.qualityMid;                //range 1(maxQuality)..51(minQuality), ref: 18..26
    public static final Bitrate bitrateDefault = Bitrate.bitrate192;
    public static final Range rangeDefault = Range.rangeOriginal;                   //get from origin
    public static final FormatOUT formatDefault = FormatOUT.avi;                    //preset
    public static final Codecname codecnameDefault = Codecname.codecnamelib264high;
    
    public static final String qualityOriginalStab="'original'";
    
    private String vcod="";
    private String vres="";
    private String vqua="";
    private String vran="";
    private String vfor="";
    private String afre="";
    private String abit="";
    
    private int vkey=-1;
    private int akey=-1;
    
    private boolean valDeleteTrashFile=DELETE_TRASH_FILE_DEFAULT;
    private boolean valSkipBadCompression = SKIP_BAD_COMPRESSION_DEFAULT;
    private boolean valTryToFullWidth=TRY_TO_FULL_WIDTH_DEFAULT;
    
    private FileInfo fileinfo=null;
    private boolean isReady=false;
    
    public ParamFFMPEG() {
        //Конструктор с пустыми параметрами,
        //<editor-fold defaultstate="collapsed" desc="CONSTRUCTOR ParamFFMPEG with EmptyValues">
        //указывающими на необходимость дальнейшего заполнения (напр. считать в оригинальном файле)
    }//</editor-fold>
    public ParamFFMPEG(String codecname, String vresolution, String vquality, String vrange, String vformat, String afrequency, String abitrate) {
        //Конструктор пользовательского объекта
        //<editor-fold defaultstate="collapsed" desc="CONSTRUCTOR ParamFFMPEG with UserValues">
        vcod=codecname;
        vres=vresolution;
        vqua=vquality;
        vran=vrange;
        vfor=vformat;
        afre=afrequency;
        abit=abitrate;
    }//</editor-fold>
    public ParamFFMPEG(boolean blnDefault) {
        //Конструктор с параметрами по умолчанию
        //<editor-fold defaultstate="collapsed" desc="CONSTRUCTOR ParamFFMPEG with DefaultValues">
        if (blnDefault) {
            vcod=codecnameDefault.getValue();
            vres=resDefault.getValue();
            vqua=qualityDefault.getValue();
            vran=rangeDefault.getValue();
            vfor=formatDefault.getValue();
            afre=freqDefault.getValue();
            abit=bitrateDefault.getValue();
        }
    }//</editor-fold>
    
    @Override
    public ParamFFMPEG clone() throws CloneNotSupportedException {
        ParamFFMPEG retval = (ParamFFMPEG)super.clone();
//        System.out.println("checkpoint");
        ObjectOutputStream oos=null;
        ObjectInputStream ois=null;
        try {
            ByteArrayOutputStream object = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(object);
            oos.writeObject(this); oos.flush(); oos.close();
            ByteArrayInputStream robject = new ByteArrayInputStream(object.toByteArray());
            ois = new ObjectInputStream(robject);

            retval = (ParamFFMPEG)ois.readObject(); ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            throw new CloneNotSupportedException();
        } finally {
            try {
                if (oos!=null) {oos.close();}
                if (ois!=null) {ois.close();}
            } catch (IOException ex) {
            }
        }
        return retval;
    }
    
    public void setResolution(String resolution){
        vres=resolution;
    }
    public String getResolution(){
        return vres;
    }
    public void setQuality(String quality){
        vqua=quality;
    }
    public String getQuality(){
        return vqua;
    }
    public void setRange(String range){
        vran=range;
    }
    public String getRange(){
        return vran;
    }
    public void setFormat(String format){
        vfor=format;
    }
    public String getFormat(){
        return vfor;
    }
    public void setDeletingTrashFile(boolean value) {
        valDeleteTrashFile = value;
    }
    public boolean getDeletingTrashFile() {
        return valDeleteTrashFile;
    }
    public void setSkipBadCompression (boolean value) {
        valSkipBadCompression = value;
    }
    public boolean getSkipBadCompression () {
        return valSkipBadCompression;
    }
    public void setTryToFullWidth(boolean value) {
        valTryToFullWidth=value;
    }
    public boolean getTryToFullWidth() {
        return valTryToFullWidth;
    }
    public void setFrequency(String frequency){
        afre=frequency;
    }
    public String getFrequency(){
        return afre;
    }
    public void setBitrate(String bitrate){
        abit=bitrate;
    }
    public String getBitrate(){
        return abit;
    }
    public void setCode(String codecname){
        vcod=codecname;
    }
    public String getCodec() {
        return vcod;
    }
    public String getVideoKey(){
        return String.valueOf(vkey);
    }
    public String getAudioKey(){
        return String.valueOf(akey);
    }
    
//    public String getCodecOrigin() {
//        return this.fileinfo.getCodecType();
//    }
    public boolean isReady() {
        return isReady;
//        return (vres.length()>0 && vqua.length()>0 && vran.length()>0 && vfor.length()>0 && afre.length()>0 && abit.length()>0);
    }
    public boolean attachFileInfo(FileInfo fileinfo){
        if (fileinfo.isReady()) {
            this.fileinfo=fileinfo;
            this.isReady=applyFileInfoParam(fileinfo);
        }
        return this.isReady;
    }
    public String getStringFileInfo(boolean original ) {
        return this.fileinfo.toString(original);
    }
    public Title getTitle() {
        return this.fileinfo.getTitle();
    }
    public Duration getDutarion() {
        return this.fileinfo.getDuration();
    }
    @Override
    public String toString() {
        String retval="";
        retval += "Codec: "+this.getCodec()+"; ";
        retval += "Resolution: "+this.getResolution()+"; ";
        retval += "Quality: "+this.getQuality()+"; ";
        retval += "Range: "+this.getRange()+"; ";
        retval += "Format: "+this.getFormat()+"; ";
        retval += "Frequency: "+this.getFrequency()+"; ";
        retval += "Bitrate: "+this.getBitrate();
        return retval;
    }
    public final String toString(boolean origin) {
        return this.fileinfo.createParamFFMPEG().toString();
    }
    private boolean applyFileInfoParam(FileInfo fileinfo) {
        boolean retval = true;
        try {
            if (vcod.length()==0) {vcod=codecnameDefault.getValue();}
            String r = fileinfo.vresorution;
            if (valTryToFullWidth) {
                r=Resolution.getValueByHeight(r);
//                System.out.println("!!!!!!!!!!!!!"+r);
            }
            if (vres.length()==0 || (calcResolution(vres) > calcResolution(r))) {vres=r;}
            if (Math.abs(fileinfo.rotate)==90L) {vres=invertAspectRatio(vres);}
            if (vqua.length()==0) {vqua=calcQuality();}
            if ((vran.length()==0) || (ParamFFMPEG.parseLong(vran)>ParamFFMPEG.parseLong(fileinfo.vrange))) {vran=fileinfo.vrange;}
            if (vfor.length()==0) {vfor=formatDefault.getValue();}
            if ((afre.length()==0) || (ParamFFMPEG.parseLong(afre)>ParamFFMPEG.parseLong(fileinfo.afrequency))) {afre=fileinfo.afrequency;}
            if ((abit.length()==0) || (ParamFFMPEG.parseLong(abit)>ParamFFMPEG.parseLong(fileinfo.abitrate))) {abit=fileinfo.abitrate;}
            if (vcod.length()==0 && vres.length()==0 && vqua.length()==0 && vran.length()==0 && vfor.length()==0 && afre.length()==0 && abit.length()==0) {
                throw new Exception("Не корректные параметры конвертации: "+this.toString());
            }
            if (fileinfo.videokey<0 || fileinfo.audiokey<0) {
                throw new Exception("Не удается сопоставить дорожки потоков: "+this.toString());
            }
            vkey=fileinfo.videokey;
            akey=fileinfo.audiokey;
        }catch (Exception e) {
            retval=false;
        }
        return retval;
    }
    private String calcQuality() {
        String retval;
        long targetRes=calcResolution(vres);
        if (targetRes<=calcResolution(Resolution.res640x360.getValue())) {
            retval="18";
        }else if (targetRes<=calcResolution(Resolution.res960x540.getValue())) {
            retval="22";
        }else if (targetRes<=calcResolution(Resolution.res1280x720.getValue())) {
            retval="26";
        }else if (targetRes<=calcResolution(Resolution.res1920x1080.getValue())) {
            retval="28";
        }else if (targetRes<=calcResolution(Resolution.res3840x2160.getValue())) {
            retval="30";
        }else {
            retval="32";
        }
        return retval;
    }
    private static long calcResolution(String resolution){
        String str1[]= resolution.split("x");
        return parseLong(str1[0])*parseLong(str1[1]);
    }
    private static String invertAspectRatio(String resolution){
        String[] str=resolution.split("x");
        return str[1].trim() + "x" + str[0].trim();
    }
    
    public static final long parseLong(String pattern) {
        // <editor-fold defaultstate="collapsed" desc="parseLong">
        String retval="";
        boolean startFlag=false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (Character.isDigit(c)) {
                retval+=c;
                startFlag=true;
            }else if (startFlag) {
                break;
            }
        }
        return (retval.length()>0)?Long.parseLong(retval):0L;
    }//</editor-fold>
    public static final double parseDouble(String pattern) {
        //<editor-fold defaultstate="collapsed" desc="parseDouble"> 
        String retval="";
        boolean startFlag=false;
        boolean hasPoint=false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!startFlag && Character.isDigit(c)) {startFlag=true;}
            if(startFlag){
                if (Character.isDigit(c)) {
                    retval+=c;
                }else if (!hasPoint && c==".".charAt(0)) {
                    hasPoint=true;
                    retval+=c;
                }else {
                    break;
                }
            }
        }
        return (retval.length()>0)?Double.parseDouble(retval):0D;
    }//</editor-fold>
    
    public static class FileInfo {
        //считываем внутренние значения кодирования файла (приводим в соответствие с полями ParamFFMPRG)
        //<editor-fold defaultstate="collapsed" desc="CLASS FileInfo"> 
        private final Title title;
        private final Duration duration;
        private final String vcodecname;
        private final String vresorution;   //!!!!!
        private final String vquality;      //only the user specifies
        private final String vrange;        //!!!!!
        private final String vformat;       //must be preset
        private final String afrequency;    //!!!!!
        private final String abitrate;      //!!!!!
        private final ArrayList<String> input;
        private long rotate=0L;
        private final long filesize;
        private final int videokey;
        private final int audiokey;
        
        public FileInfo(ArrayList<String> stream, File source, boolean deleteTrashFile) throws Exception{
            //Парсируем входящую строку (toLowerCase)
            //#: Stream #0:0[0x1000]: Video: mpeg2video (Main) ([2][0][0][0] / 0x0002), yuv420p(tv, progressive), 1280x720 [SAR 1:1 DAR 16:9], 25 fps, 25 tbr, 90k tbn, 50 tbc
            //#: Stream #0:1[0x1001]: Audio: mp2 ([3][0][0][0] / 0x0003), 48000 Hz, stereo, fltp, 384 kb/s
            filesize=source.length();
            if (filesize==0) {throw new TrashFileException("Файл пустой ("+source.getAbsolutePath()+"). Удалить.",0,source);}
            Title tit=null;
            Duration dur=null;
            String vres="";
            String vqua="";
            String vran="";
            String vfor="";
            String afre="";
            String abit="";
            String tcodec="";
            boolean hastitle=false;
            boolean hasduration=false;
            boolean hasvideo=false;
            boolean hasaudio=false;
            input=stream;
            boolean videoInfoBegin=false;
            int vkey=-1;
            int akey=-1;
            boolean metaparsing = false;
            if (input!=null) {
                for (String line:stream) {
                    if(!(hastitle && hasduration && hasvideo && !videoInfoBegin  && hasaudio )) {
                        try {
                            String sout=line.toLowerCase().trim();
                            if (!hastitle && sout.startsWith("input") && sout.contains("from")) {
                                tit = new Title(sout);
                                hastitle=true;
                            }
                            if (!hasduration && sout.startsWith("duration:")) {
                                dur=new Duration(sout, source, deleteTrashFile);
                                hasduration=true;
                            }
                            if (!hasvideo && sout.startsWith("stream #0:") && sout.contains("video:")) {
                                videoInfoBegin=true;
                                String str1[];
                                str1=sout.split(",");
                                for (String str:str1) {
                                    if (str.contains("video")) {
                                        tcodec=FileInfo.getCodec(str);
                                    }else if (str.contains("x")) {
                                        vres=FileInfo.getResolution(str);
                                    }else if (str.contains("fps")) {
                                        vran=FileInfo.getRange(str);
                                    }
                                }
                                vkey=retrieveKey(sout); //Предпологаем что аудиопоток не нужно парсить в метаданных
                                hasvideo=true;
                            }
                            if (!hasaudio && sout.startsWith("stream #0:") && sout.contains("audio:") && sout.contains("(default)")) {
                                akey=retrieveKey(sout);
                                String str1[];
                                str1=sout.split(",");
                                for (String str:str1) {
                                    if (str.contains("hz")) {
                                        afre=FileInfo.getFrequency(str);
                                    }else if (str.contains("kb/s")) {
                                        abit=FileInfo.getBitrate(str).trim();
                                    }
                                }
                                if (abit.length()>0) {
                                    hasaudio=true;
                                }else {
                                    metaparsing=true;
                                }
                            }else if (!hasaudio && sout.startsWith("stream #0:") && sout.contains("audio:")) {
                                akey=retrieveKey(sout);
                                String str1[];
                                str1=sout.split(",");
                                for (String str:str1) {
                                    if (str.contains("hz")) {
                                        afre=FileInfo.getFrequency(str);
                                    }else if (str.contains("kb/s")) {
                                        abit=FileInfo.getBitrate(str).trim();
                                    }
                                }
                                if (abit.length()>0) {
                                    hasaudio=true;
                                }else {
                                    metaparsing=true;
                                }
                            }else if (!hasaudio && metaparsing) {
                                if (sout.contains("bps ")) {
                                    String list[]=sout.split(":");
                                    if (list.length>1) {
                                        double d = ParamFFMPEG.parseDouble(list[1])/1000;
                                        abit = String.valueOf(d)+"K";
                                    }
                                    hasaudio=true; 
                                    metaparsing=false;
                                }else if (!sout.startsWith("stream #0:") && !sout.startsWith("at least one output file must be specified")) {
                                    continue;
                                }else {
                                    hasaudio=true; 
                                    abit=ParamFFMPEG.bitrateDefault.getValue();
                                    metaparsing=false;
                                }
                            }
                            
                            if (videoInfoBegin && sout.contains("rotate")) {
                                int pos=sout.indexOf(":");
                                if (pos>0) {
                                    this.rotate=ParamFFMPEG.parseLong(sout.substring(sout.indexOf(":")).trim());
                                }
                                videoInfoBegin=false;
                            }
                        }catch (Exception e) {
                            throw new Exception("Ошибка считывания параметров файла");
                        }
                    }else {
                        break;
                    }
                }
            }
            if (dur==null || tit==null) {throw new CrashFileException("Файл поврежден или его формат не поддерживается. Переименовать в "+source.getAbsolutePath()+".crash",source);}
            if (!(dur.isReady()&& tit.isReady())) {throw new Exception("Ошибка: не удалось найти заголовок файла");}
            if (dur.isTrashFile()) {throw new TrashFileException("Файл, длительностью менее "+ParamFFMPEG.maxLenTrashFile+"сек... удаление! ("+dur.getlenFileWarning()+" сек)",dur.getlenFileWarning(),source);}
            this.title=tit;
            this.duration=dur;
            
            this.vcodecname=tcodec;
            this.vresorution=vres;
            this.vquality=ParamFFMPEG.qualityOriginalStab;
            this.vrange=vran;
            this.vformat=tit.getType();
            this.afrequency=afre;
            this.abitrate=abit;
            this.videokey=vkey;
            this.audiokey=akey;
        }
        
        public boolean isReady() {
            boolean fileinfoisready = vresorution.length()>0 && vquality.length()>0 && vrange.length()>0 && vformat.length()>0 && afrequency.length()>0 && abitrate.length()>0;
            return fileinfoisready && this.duration.isReady();
        }
        public ParamFFMPEG createParamFFMPEG(){
            ParamFFMPEG retval = null;
            if (this.isReady()) {
                retval=new ParamFFMPEG(vcodecname,vresorution,vquality,vrange,vformat,afrequency,abitrate);
            }
            return retval;
        }
        public Title getTitle() {
            return this.title;
        }
        public Duration getDuration() {
            return this.duration;
        }
        
        public String toString(boolean original) {
            String retval="";
            for(String str:input) {
                retval+=((retval.length()==0)?(str.trim()):("\n"+str.trim()));
            }
            return original?retval:this.toString();
        }
        @Override
        public String toString(){
            return "codecname: "+vcodecname+" resorution: "+vresorution+" quality: " + vquality +" range: " + vrange +" format: " + vformat +" frequency: " + afrequency +" bitrate: " + abitrate;
        }
        private static String getCodec(String pattern) {
            //Stream #0:0[0x1000]: Video: mpeg2video (Main) ([2][0][0][0] / 0x0002)
            String retval;
            try {
                retval=pattern.substring(pattern.indexOf("video:"));
                retval=retval.substring("video:".length()).trim();
                if (retval.contains(")")) {
                    retval=retval.substring(0,retval.indexOf(")")+1).trim();
                }else {
                    retval=retval.substring(0,retval.indexOf(" ")).trim();
                }
            }catch (Exception ex) {
                retval="";
            }
            return retval;
        }
        private static String getResolution(String pattern){
            String retval=resDefault.getValue();
            pattern=pattern.trim();
            String str1[];
            str1=pattern.split("x");
            try {
                int hor=(int)parseLong(str1[0]);
                int vert=(int)parseLong(str1[1]);
                if (hor>=320 && hor <=3840 && vert>=180 && vert<=2160) {
                    retval=String.valueOf(hor)+"x"+String.valueOf(vert);
                }
            }catch (Exception e) {
                //
            }
            
            return retval;
        }
        private static String getRange(String pattern){
            String retval = rangeDefault.getValue();
            pattern = pattern.trim();
            pattern=pattern.substring(0, pattern.indexOf("fps")).trim();
            try {
                if (ParamFFMPEG.parseDouble(pattern)>=8 && ParamFFMPEG.parseDouble(pattern)<=100) {
                    retval=pattern;
                }
            }catch(Exception e) {
                //
            }
            return retval;
        }
        private static String getFrequency(String pattern){
            String retval = freqDefault.getValue();
            pattern=pattern.trim();
            pattern=pattern.substring(0,pattern.indexOf("hz")).trim();
            try {
                if (Integer.parseInt(pattern)>=8000 && Integer.parseInt(pattern)<=48000) {
                    retval=pattern;
                }
            }catch(Exception e) {
                //
            }
            return retval;
        }
        private static String getBitrate(String pattern){
            String retval = bitrateDefault.getValue();
            pattern=pattern.trim();
            pattern=pattern.substring(0,pattern.indexOf("kb/s")).trim();
            try {
                if (Integer.parseInt(pattern)>=8 && Integer.parseInt(pattern)<=384) {
                    retval=pattern+"K";
                }
            }catch(Exception e) {
                //Duration dur = new Duration("",null);
            }
            return retval;
        }
        private static int retrieveKey(String pattern) {
//            System.out.println("??????"+pattern);
            String list[]=pattern.split(":");
            int retval=-1;
            if (list.length>1) {
                retval=(int)ParamFFMPEG.parseLong(list[1]);
            }
//            System.out.println("!!!!!!"+retval);
            return retval;
        }
    }//</editor-fold>
    
    public static class Title {
        //вычисляем заголовок файла: тип и имя файла
        //<editor-fold defaultstate="collapsed" desc="CLASS Title"> 
        private final String input;
        private final String name;
        private final String type;
        
        public Title(String input) {
            //Парсируем входящую строку (toLowerCase)
            //#: Input #0, mpegts, from 'C:\Temp\converter\video\20170219 - 19 февраля 2017 - 02-57-28 .ts':
            //#: Input #0, mov,mp4,m4a,3gp,3g2,mj2, from 'E:\SHOS\Video\Test\clip-1.mp4':
            this.input=input;
            String nm = "";
            String tp = "";
            String str1[];
            boolean inputflag=false;
            str1=input.split(",");
            for (String str:str1) {
                if (!inputflag && str.contains("input")) {
                    inputflag=true;
                    //continue;
                }else if (!str.contains("from") && inputflag) {
                    tp+=(((tp.length()>0)?",":"")+str.trim());
                }else if (str.contains("from")) {
                    String s = str.replace("from", "").trim();
                    //Type: MOV; Name: 'e:\shos\video\test\тамара шамраева.mp4':
                    String woQuote="";
                    if (s.startsWith("'")) {
                        woQuote=s.substring(1);
                        if (woQuote.indexOf("'")>0) {
                            woQuote=woQuote.substring(0, woQuote.indexOf("'"));
                            s=woQuote;
                        }
                    }
                    nm=s;
//                    inputflag=true;
                    break;
                }
            }
            this.name=nm;
            this.type=tp;
        }
        public String getName() {
            return this.name;
        }
        public String getType() {
            return this.type;
        }
        public boolean isReady() {
            return (this.name.length()>0) && (this.type.length()>0);
        }
        public String toString(boolean original){
            return original?input:this.toString();
        }
        @Override
        public String toString(){
            return "Type: "+this.getType().toUpperCase()+"; Name: \""+this.getName()+"\"";
        }
    }//</editor-fold>
    
    public static class Duration {
        //вычисляем продолжительность файла, start и средний битрэйт
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        private final String input;
        private final double duration;
        private final double start;
        private final double bitrate;
        private final double lenFileWarning;
        private final long filesize;
        
        public Duration(String input, File source, boolean deleteTrashFile) {
            //Парсируем входящую строку (toLowerCase)
            //#: Duration: 00:02:06.62, start: 0.816444, bitrate: 12650 kb/s
            this.input=input;
            filesize=source.length();
            double drtn=-1;
            double strt=-1;
            double btrt=-1;
            
            String str1[],str2[];
            str1=input.split(",");
            for(String i: str1) {
                if (drtn==-1 && i.contains("duration")) {
                    str2=i.trim().split(":");
                    int poscount=0;
                    drtn=0;
                    if (str2.length>=4) {
                        try {
                            for(String j:str2) {
                                if (j.contains("duration")) {
                                    poscount=3;
                                    continue;
                                }else if(poscount>=1) {
                                    poscount--;
                                    drtn+=(ParamFFMPEG.parseDouble(j.trim())*(Math.pow(60, poscount)));
                                } 
                            }
                        }catch (Exception ex){
                            drtn=-2;
                        }
                    }
                }else if(strt==-1 && i.contains("start")) {
                    str2=i.trim().split(":");
                    int poscount=0;
                    if (str2.length>=2) {
                        try{
                            for(String j:str2) {
                                if (j.contains("start")) {
                                    poscount=1;
                                    continue;
                                }else if(poscount>=1) {
                                    poscount--;
                                    strt=ParamFFMPEG.parseDouble(j.trim());
                                } 
                            }
                        }catch (Exception ex) {
                            strt=-2;
                        }
                    }
                }else if(btrt==-1 && i.contains("bitrate")) {
                    str2=i.trim().split(":");
                    int poscount=0;
                    if (str2.length>=2) {
                        try{
                            for(String j:str2) {
                                if (j.contains("bitrate")) {
                                    poscount=1;
                                    continue;
                                }else if(poscount>=1) {
                                    poscount--;
                                    if (j.contains("kb/s")) {
                                        btrt=ParamFFMPEG.parseDouble(j.replace("kb/s", "").trim());
                                    }else if(j.contains("n/a") && drtn>0) {
                                        //<Вычислить битрейт через размер файла>
                                        btrt=Math.floor((filesize/drtn)*8/1000);
                                    }
                                } 
                            }
                        }catch (Exception ex) {
                            btrt=-2;
                        }
                    }
                }
            }
            this.duration=drtn;
            this.start=strt;
            this.bitrate=btrt;
            this.lenFileWarning=(deleteTrashFile && drtn>0 && drtn<ParamFFMPEG.maxLenTrashFile)?drtn:(long)-1;
    //                    System.out.println("-------         : "+sout); 
    //                    System.out.println("------- duration: "+duration);
    //                    System.out.println("-------    start: "+start); 
    //                    System.out.println("-------  bitrate: "+totalbitrate); 
        }
        public double getDuration() {
            return duration;
        }
        public double getStart() {
            return start;
        }
        public double getBitrate() {
            return bitrate;
        }
        public String getDuration(boolean asString) {
            return  String.valueOf(duration)+" sec";
        }
        public String getStart(boolean asString) {
            return  String.valueOf(start);
        }
        public String getBitrate(boolean asString) {
            return  String.valueOf(bitrate)+" kbits/s";
        }
        public double getlenFileWarning(){
            return lenFileWarning;
        }
        public boolean isTrashFile(){
            return this.lenFileWarning>=0;
        }
        public boolean isReady() {
            return (this.duration>=0) && (this.start>=0) && (this.bitrate>=0);
        }
        public String toString(boolean original) {
            return original?input:this.toString();
        }
        @Override
        public String toString() {
            //<...>
            return "Duration: "+getDuration(true)+", Start: "+getStart(true)+", Bitrate: "+getBitrate(true);
        }
    }//</editor-fold>
    
    public static class Frame {
        //вычисляем процент выполнения, текущий битрэйт и относительную скорость
        //<editor-fold defaultstate="collapsed" desc="CLASS Frame"> 
        private double position=-1;
        private double bitrate=-1;
        private double speed=-1;
        private double currentcompress;
        private Duration duration=null;
        private final String input;
        private final DecimalFormat df1;
        private final DecimalFormat df2;
        private final DecimalFormat df3;
        public Frame(String input, Duration dur){
            DecimalFormat df;
            df=new DecimalFormat("#.#");
            df.setMinimumFractionDigits(1);
            df1=df;
            df=new DecimalFormat("#.##");
            df.setMinimumFractionDigits(2);
            df2=df;
            df=new DecimalFormat("#.###");
            df.setMinimumFractionDigits(3);
            df3=df;
            //Парсируем входящую строку (toLowerCase)
            //#: frame=  135 fps= 26 q=28.0 size=    1034kB time=00:00:05.20 bitrate=1628.6kbits/s speed=1.01x
            this.input=input;
            duration=dur;
            String str1[],str2[];
            str1=input.split("=");
            for(int i=0; i<str1.length; i++) {
                String str=str1[i];
                if (position==-1 && str.contains("time")) {
                    position=0;
                    try {
                        String positionSequence=str1[i+1].trim();
                        positionSequence = positionSequence.split(" ")[0].trim();
                        str2 = positionSequence.split(":");
                        if (str2.length==3) {
                            position=ParamFFMPEG.parseDouble(str2[0].trim())*3600+ParamFFMPEG.parseDouble(str2[1].trim())*60+ParamFFMPEG.parseDouble(str2[2].trim());
                        }
                    } catch(Exception ex) {
                        position=-1;
                    }
                }else if(bitrate==-1 && str.contains("bitrate")) {
                    bitrate=0;
                    try {
                        String bitrateSequence=str1[i+1].trim();
                        if (bitrateSequence.contains("kbits/s")) {
                            bitrateSequence=bitrateSequence.substring(0, bitrateSequence.indexOf("kbits/s")).trim();
                            bitrate = ParamFFMPEG.parseDouble(bitrateSequence);
                        }
                    }catch (Exception ex) {
                        bitrate=-1;
                    }
                }else if(speed==-1 && str.contains("speed")) {
                    speed=0;
                    try {
                        String speedSequence=str1[i+1].trim();
                        if (speedSequence.contains("x")) {
                            speedSequence = speedSequence.substring(0, speedSequence.indexOf("x")).trim();
                            speed=ParamFFMPEG.parseDouble(speedSequence);
                        }
                    } catch(Exception ex) {
                        speed=-1;
                    }
                }
            }
        }
        public double getPosition() {
            return position;
        }
        public double getBitrate() {
            return bitrate;
        }
        public double getSpeed() {
            return speed;
        }
        public double getCompressed() {
            return currentcompress;
        }
        public String getCompressed(boolean asString){
            return asString?df2.format(currentcompress)+"x":df2.format(currentcompress);
        }
        public String getCompressed(double override, boolean asString){
            return asString?df2.format(override)+"x":df2.format(override);
        }
        public void setCompressed(double compressed) {
            currentcompress=compressed;
        }
        public Duration getDuration() {
            return duration;
        }
        public double getPercent() {
            return position/duration.getDuration();
        }
        public String getPosition(boolean asString) {
            return asString?df2.format(position)+ " sec":df2.format(position);
        }
        public String getBitrate(boolean asString) {
            return asString?df1.format(bitrate)+" kbits/s":df1.format(bitrate);
        }
        public String getSpeed(boolean asString) {
            return asString?df3.format(speed)+"x":df3.format(speed);
        }
        public boolean isReady() {
            return position>=0 && duration!=null && position<=duration.getDuration() && bitrate>=0 && speed>=0;
        }
        public boolean minCompressionBreaked() {
            return (this.getPercent()>ParamFFMPEG.minCompressionDelayPercent) && this.getCompressed()>(1-ParamFFMPEG.minCompression);
        }
        public String toString(boolean original) {
            return original?input:this.toString();
        }
        @Override
        public String toString(){
            return "Position "+getPosition(true)+", Bitrate "+getBitrate(true)+", Speed "+getSpeed(true)+", Compressed "+getCompressed(true);
        }
    }//</editor-fold>
    public enum Resolution {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        resOriginal(""),
        res3840x2160("3840x2160"),
        res1920x1080("1920x1080"),
        res1280x720("1280x720"),
        res1024x576("1024x576"),
        res960x540("960x540"),
        res640x360("640x360");
        
        private final String resolution;
        
        Resolution(String resolution) {
            this.resolution = resolution;
        }
        public String getValue() {
            return resolution;
        }
        public static final String getValueByHeight(String resolution){
            String retval = resolution;
            String[] array = resolution.split("x");
            if (array.length==2) {
                int target = Integer.parseInt(array[1]);
                for (Resolution r : Resolution.values()) {
                    array = r.getValue().split("x");
                    if (array.length==2) {
                        int source = Integer.parseInt(array[1]);
                        if (target==source) {
                            retval=r.getValue();
                            break;
                        }
                    }
                }
            }
            return retval;
        }
        public static final String getValueByWidth(String resolution){
            String retval = resolution;
            String[] array = resolution.split("x");
            if (array.length==2) {
                int target = Integer.parseInt(array[0]);
                for (Resolution r : Resolution.values()) {
                    array = r.getValue().split("x");
                    if (array.length==2) {
                        int source = Integer.parseInt(array[0]);
                        if (target==source) {
                            retval=r.getValue();
                            break;
                        }
                    }
                }
            }
            return retval;
        }
    }//</editor-fold>
    public enum Quality {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        //range 1(maxQuality)..51(minQuality), ref: 18..26
        qualityOriginal(""),
        qualityMax("18"),
        qualityMid("26"),
        qualityMin("34");
        
        private final String quality;
        Quality(String quality) {
            this.quality=quality;
        }
        public String getValue() {
            return quality;
        }
    }//</editor-fold>
    public enum Frequency {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        freqOriginal(""),
        freq48000("48000"),
        freq44100("44100"),
        freq24000("24000"),
        freq22050("22050"),
        freq12000("12000"),
        freq11025("11025");
        
        private final String frequency;
        
        Frequency(String frequency) {
            this.frequency = frequency;
        }
        public String getValue() {
            return frequency;
        }
    }//</editor-fold>
    public enum Bitrate {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        bitrateOriginal(""),
        bitrate320("320K"),
        bitrate256("256K"),
        bitrate192("192K"),
        bitrate128("128K"),
        bitrate96("96K");
        
        private final String bitrate;
        
        Bitrate (String bitrate) {
            this.bitrate = bitrate;
        }
        public String getValue() {
            return bitrate;
        }
    }//</editor-fold>
    public enum FormatIN {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        ts(".ts"),
        mov(".mov"),
        mpg(".mpg"),
        mp4(".mp4"),
        mpeg(".mpeg");
        
        private final String format;
        
        FormatIN (String format) {
            this.format = format;
        }
        public String getValue() {
            return format;
        }
    }//</editor-fold>
    public enum FormatOUT {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        avi("avi"),
        mp4("mp4"),
        test("hlv");
        
        private final String format;
        
        FormatOUT(String format) {
            this.format = format;
        }
        public String getValue() {
            return format;
        }
    }//</editor-fold>
    public enum Range {
        rangeOriginal(""),
        range25("25");
        
        private final String range;
        
        Range(String range) {
            this.range=range;
        }
        public String getValue() {
            return range;
        }
    }//</editor-fold>
    public enum Codecname {
        //<editor-fold defaultstate="collapsed" desc="CLASS Duration"> 
        codecnamelib264high("lib264 (high)");
        
        private final String codecname;
        
        Codecname(String codecname) {
            this.codecname=codecname;
        }
        public String getValue() {
            return codecname;
        }
    }//</editor-fold>
}
