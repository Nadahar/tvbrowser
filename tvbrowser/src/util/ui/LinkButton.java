/*
 * Created on 10.01.2005
 */
package util.ui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.metal.MetalButtonUI;


/**
 * @author bodum
 */
public class LinkButton extends JButton implements ActionListener{

    private String mUrl;
    
    public LinkButton(String text, String url) {
        super("<html><FONT COLOR='BLUE'><u>"+text+"</u></FONT></html>");
        mUrl = url;
        
        setBorder(BorderFactory.createEmptyBorder());
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setRolloverEnabled(true);
        setUI(new MetalButtonUI());
        addActionListener(this);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        BrowserLauncher.openURL(mUrl);        
    }
   
}