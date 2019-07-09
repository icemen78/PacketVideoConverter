/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packetvideoconverter;

import javax.swing.text.Caret;

/**
 *
 * @author User
 */
public class MyJTextPane extends javax.swing.JTextPane{
    private static final long serialVersionUID = 3017953333242874383L;
//    private final static String EMPTY_TEXT_START="<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>";
//    private final static String EMPTY_TEXT_END="</body></html>";
    
    
    public MyJTextPane() {
        super();
        super.setContentType("text/html");
        super.setAutoscrolls(true);
//        setText(EMPTY_TEXT_START+EMPTY_TEXT_END);
    }
    @Override
    public void setText(String newText) {
        super.setText("");
        super.setText(newText);
//        super.setAutoscrolls(true);
//        this.;
//        System.out.println(getText());
    }
    public void addRow(String text) {
        String txt = getText();
        int pos = txt.lastIndexOf("</p>");
        if (pos>0) {
            String retval=txt.substring(0,pos) + "<SPAN>" + text.replace("\n", "<br>") + "</SPAN><br>" + txt.substring(pos);
            setText(retval);
        }
    }
    public void addRow(String text, boolean bold, boolean italic, boolean underline) {
        String txt = getText();
        int pos = txt.lastIndexOf("</p>");
        if (pos>0) {
            String formatIN="<div class='formatted'>";
            String formatOUT="</div>";
            if (underline) {
                formatIN +="<u>";
                formatOUT = "</u>"+formatOUT;
            }
            if (italic) {
                formatIN +="<i>";
                formatOUT = "</i>"+formatOUT;
            }
            if (bold) {
                formatIN +="<b>";
                formatOUT = "</b>"+formatOUT;
            }
            String retval=txt.substring(0,pos) + "<SPAN>" + formatIN + text.replace("\n", "<br>") + formatOUT + "</SPAN><br>" + txt.substring(pos);
            setText(retval);
        }
    }
    public void addRow(String text, java.awt.Color color) {
        try {
            String txt = getText();
            int pos = txt.lastIndexOf("</p>");
            if (pos>0) {
                String r=Long.toHexString((long)color.getRed());
                if (r.length()==1) {r="0"+r;}
                String g=Long.toHexString((long)color.getGreen());
                if (g.length()==1) {g="0"+g;}
                String b=Long.toHexString((long)color.getBlue());
                if (b.length()==1) {b="0"+b;}
                String formatIN="<font color=#"+r+g+b+">";
                String formatOUT="</font>";
                String retval=txt.substring(0,pos) + "<SPAN>" + formatIN + text.replace("\n", "<br>") + formatOUT + "</SPAN><br>" + txt.substring(pos);
                setText(retval);
            }
        }catch (Exception e) {
            //
        }
    }
    public void addRow(String text, boolean bold, boolean italic, boolean underline, java.awt.Color color) {
        try {
            String txt = getText();
            int pos = txt.lastIndexOf("</p>");
            if (pos>0) {
                String r=Long.toHexString((long)color.getRed());
                if (r.length()==1) {r="0"+r;}
                String g=Long.toHexString((long)color.getGreen());
                if (g.length()==1) {g="0"+g;}
                String b=Long.toHexString((long)color.getBlue());
                if (b.length()==1) {b="0"+b;}
                String formatIN="<font color=#"+r+g+b+">";
                String formatOUT="</font>";
                if (underline) {
                    formatIN +="<u>";
                    formatOUT = "</u>"+formatOUT;
                }
                if (italic) {
                    formatIN +="<i>";
                    formatOUT = "</i>"+formatOUT;
                }
                if (bold) {
                    formatIN +="<b>";
                    formatOUT = "</b>"+formatOUT;
                }
                String retval=txt.substring(0,pos) + "<SPAN>" + formatIN + text.replace("\n", "<br>") + formatOUT + "</SPAN><br>" + txt.substring(pos);
                setText(retval);
            }
        }catch (Exception e) {
            //
        }
    }
}
