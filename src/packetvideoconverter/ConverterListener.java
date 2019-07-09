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
public interface ConverterListener {
    public void started(Converter converter);
    public void progressed(ParamFFMPEG.Frame frame);
    public void finished(Converter converter, Exception e);
}
