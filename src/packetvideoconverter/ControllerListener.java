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
public interface ControllerListener {
    public void progressed(ParamFFMPEG.Frame current);
    public void progressedTotal(Converter converter);
    public void logging(String msg, boolean impotrant);
    public void finished();
}
