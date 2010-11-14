/*
 * Created on 10.01.2005
 */
package util.ui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import util.browserlauncher.Launch;

/**
 * A Button for Web-Links
 *
 * @author bodum
 */
public class LinkButton extends JButton implements ActionListener {

  /** URL */
  private String mUrl;

  /**
   * Create a Link-Button for URL
   *
   * @param url
   *          Url to show
   */
  public LinkButton(String url) {
    this(url, url);
  }

  /**
   * Create a Link-Button for URL
   *
   * @param text
   *          Text to show
   * @param url
   *          URL to use
   */
  public LinkButton(String text, String url) {
    this(text, url, SwingConstants.CENTER);
  }

  /**
   * Create a Link-Button for URL
   *
   * @param text
   *          Text to show
   * @param url
   *          URL to use
   * @param halignment
   *          Horizontal Alignment (JLabel.LEFT, ...)
   * @since 2.2
   */
  public LinkButton(String text, String url, int halignment) {
    this(text, url, halignment, true);
  }

  /**
   * Create a Link-Button for URL
   *
   * @param text
   *          Text to show
   * @param url
   *          URL to use
   * @param halignment
   *          Horizontal Alignment (JLabel.LEFT, ...)
   * @param useLinkAction
   *          If true, the Button acts like a Link-Button and opens a Browser,
   *          if false no Action is added
   * @since 2.2
   */
  public LinkButton(String text, String url, int halignment, boolean useLinkAction) {
    super("<html><font color=\"blue\"><u>" + text + "</u></font></html>");
    mUrl = url;
    setHorizontalAlignment(halignment);
    createButton(useLinkAction);
  }

  /**
   * creates the Button
   *
   * @param useLinkAction
   */
  private void createButton(boolean useLinkAction) {
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    setContentAreaFilled(false);
    setBorderPainted(false);
    setRolloverEnabled(true);
    setToolTipText(mUrl);
    if (useLinkAction) {
      addActionListener(this);
    }
  }

  /**
   * Sets the Text in the LinkButton
   *
   * @param text
   *          new Text
   */
  public void setText(String text) {
    setText(text, "blue");
  }

  public void setText(String text, String color) {
    super.setText("<html><font color=\"" + color + "\"><u>" + text + "</u></font></html>");
  }

  /**
   * Set the URL in the LinkButton
   *
   * @param url
   *          new Url
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