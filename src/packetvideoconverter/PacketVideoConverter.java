/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import java.io.File;
import java.util.TreeMap;
import javax.swing.JFrame;

/**
 *
 * @author Icemen78
 */
public class PacketVideoConverter extends JFrame{
    private static final long serialVersionUID = -7343547853348608609L;
    private final File module;
    private File dir;
    private String[] ext = null;
    TreeMap<File,Long> files = new TreeMap<>();
    private Controller controller;
    private long preparedFilesSize=0;
    
    public PacketVideoConverter(String title) {
        super(title);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        String osArch = System.getProperty("os.arch").toLowerCase();
        String module_string = osArch.contains("64") ? "ffmpeg\\win32\\bin\\ffmpeg.exe" : "ffmpeg\\win64\\bin\\ffmpeg.exe";
        module = new File(module_string);
        //Проектирование UI
        //<...>
        
        
        
//        String fileSource="\"C:\\Temp\\converter\\video\\";
        dir=new File("E:\\SHOS\\Video\\Test");
        ext = new String[]{".ts",".mov",".mp4"};
        
        prepareDir(dir, ext);
        
        try {
            convert();
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args) {
//        System.out.println(Character.getType("'".charAt(0)));
        
//        JFileChooser_locate();
        PacketVideoConverter mainform = new PacketVideoConverter("Пакетный видеоконвертер");
    }
    
    private void prepareDir(File dir, final String[] ext) {
        files = getFList(dir, new FileExtentionFilter(ext));
        //Обновление информации на форме
        //<...>
    }
    
    private void convert() throws Exception{
        if (files.isEmpty()) {throw new Exception("Нет файлов для обработки");}
        controller = new Controller(files, module, prepareParam());
        controller.addListener(new ControllerListener() {
            @Override
            public void progressed(ParamFFMPEG.Frame current) {
                hasProgressed(current);
            }
            @Override
            public void progressedTotal(Converter current) {
                hasProgressedTotal(current);
            }
            @Override
            public void finished() {
                hasFinished();
            }
            @Override
            public void logging(String msg, boolean important) {
                hasLogging(msg,important);
            }
        });
        
        controller.start();
    }
    private TreeMap<File,Long> getFList(File fobject, FileExtentionFilter ff) {
        TreeMap<File,Long> retval = new TreeMap<>();

        if (fobject.isDirectory()) {
            for (File f:fobject.listFiles(ff)) {
                retval.putAll(getFList(f,ff));
            }
        }else {
            retval.put(fobject, fobject.length());
        }            
        return retval;
    }

    private ParamFFMPEG prepareParam() {
        ParamFFMPEG param;
        param = new ParamFFMPEG();
//        param = new ParamFFMPEG(true);
        param.setResolution(ParamFFMPEG.Resolution.res1920x1080.getValue());
//        param.setQuality(ParamFFMPEG.quality42);
//        param.setQuality("51");
//        param.
        //Если умолчания, то param = new ParamFFMPEG(true);
        //Если все параметры собственные, то param = new ParamFFMPEG(p1,p2,p3,p4,p5,p6);
        //Если некоторые (или все) параметры из файла, то param = new ParamFFMPEG(); далее set p1,p3...
        //Для начала...
        return param;
    }
    private void hasProgressed(ParamFFMPEG.Frame current){
        //<...>
        
        double percent = current.getPercent()*10000;
        percent=Math.round(percent);
        percent=(percent/100);
        System.out.print(percent+ "%    ");
        System.out.println(current.toString());
    }
    private void hasProgressedTotal(Converter c){
        if (c.getCompression().length()>0){System.out.println("Сжатие файла: "+c.getCompression());}
        preparedFilesSize+=c.getFileSise();
        double current=(double)preparedFilesSize;
        double total=(double)controller.getTotalFSize();
        double percent=Math.round((current/total*10000));
        percent=(percent/100);
        System.out.println("% выполнения: "+percent);
    }
    private void hasFinished(){
        //<...>
    }
    private void hasLogging(String msg, boolean important) {
        //<...>
        String errorPrefix=(important?"************ hasError: ":"");
        
        System.out.println(errorPrefix+msg);
    }
}
