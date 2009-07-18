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

import util.browserlauncher.Launch;


/**
 * A Button for Web-Links
 * @author bodum
 */
public class LinkButton extends JButton implements ActionListener{

    /** URL */
    private String mUrl;
    
    /** Text of the Button */
    @SuppressWarnings("unused")
    private String mText;
    
    /**
     * Create a Link-Button for URL
     * @param url Url to show
     */
    public LinkButton(String url) {
        super("<html><FONT COLOR='BLUE'><u>"+url+"</u></FONT></html>");
        mText = url;
        mUrl = url;
        createButton(true);
    }
    
    /**
     * Create a Link-Button for URL
     * @param text Text to show
     * @param url URL to use
     */
    public LinkButton(String text, String url) {
        super("<html><FONT COLOR='BLUE'><u>"+text+"</u></FONT></html>");
        mText = text;
        mUrl = url;
        createButton(true);
    }

    /**
     * Create a Link-Button for URL
     * @param text Text to show
     * @param url URL to use
     * @param halignment Horizontal Alignment (JLabel.LEFT, ...)
     * @since 2.2
     */
    public LinkButton(String text, String url, int halignment) {
        super("<html><FONT COLOR='BLUE'><u>"+text+"</u></FONT></html>");
        mText = text;
        mUrl = url;
        setHorizontalAlignment(halignment);
        createButton(true);
    }
    
    /**
     * Create a Link-Button for URL
     * @param text Text to show
     * @param url URL to use
     * @param halignment Horizontal Alignment (JLabel.LEFT, ...)
     * @param useLinkAction If true, the Button acts like a Link-Button and opens a Browser, if false no Action is added
     * @since 2.2
     */
    public LinkButton(String text, String url, int halignment, boolean useLinkAction) {
        super("<html><FONT COLOR='BLUE'><u>"+text+"</u></FONT></html>");
        mText = text;
        mUrl = url;
        setHorizontalAlignment(halignment);
        createButton(useLinkAction);
    }
    
    /**
     * creates the Button 
     * @param useLinkAction 
     */
    private void createButton(boolean useLinkAction) {
        setBorder(BorderFactory.createEmptyBorder(2,0,0,0));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setRolloverEnabled(true);
        setUI(new MetalButtonUI());
        setToolTipText(mUrl);
        if (useLinkAction)
          addActionListener(this);
    }
    
    /**
     * Sets the Text in the LinkButton
     * @param text new Text
     */
    public void setText(String text) {
      setText(text, "BLUE");
    }
 
    public void setText(String text, String color) {
      super.setText("<html><FONT COLOR='"+color+"'><u>"+text+"</u></FONT></html>");
      mText = text;
    }
    
    /**
     * Set the URL in the LinkButton
     * @param url new Url
     */
    public void setUrl(String url) {
      mUrl = url;
    }
    
    public void actionPerformed(ActionEvent e) {
      if (mUrl != null) {
        Launch.openURL(mUrl);
      }
    }
   
}