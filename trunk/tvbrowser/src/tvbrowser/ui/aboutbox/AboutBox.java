/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
 
package tvbrowser.ui.aboutbox;

import javax.swing.*;

import util.ui.html.*;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

import tvbrowser.TVBrowser;

/**
 * 
 * @author Martin Oberhauser (darras@users.sourceforge.net)
 *
 */
public class AboutBox extends JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(AboutBox.class);
  
  
  
   
  
  private static Font smallFont=new Font("Dialog", Font.PLAIN, 10);
  private static Font bigFont=new Font("Dialog", Font.PLAIN, 24);
  private static Font normalFont=new Font("Dialog", Font.PLAIN, 12);
  private static Font boldFont=new Font("Dialog",Font.BOLD, 12);
  
  
  
  public AboutBox(Frame parent) {
    super(parent,true);
    
    setTitle(mLocalizer.msg("about", "About"));
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    final JDialog parentFrame=this;
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        parentFrame.hide();
      }
    });
    getRootPane().setDefaultButton(closeBtn);
    btnPanel.add(closeBtn,BorderLayout.EAST);
    btnPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    contentPane.add(btnPanel,BorderLayout.SOUTH);
    
    
    JEditorPane infoEP = new JEditorPane();
    infoEP.setOpaque(false);
    infoEP.setEditorKit(new ExtendedHTMLEditorKit());
    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) infoEP.getDocument();
    String text = createAboutText(doc);
    infoEP.setText(text);
    infoEP.setEditable(false);
    
    contentPane.add(infoEP,BorderLayout.CENTER);
  
  }
  
  private StringBuffer createInfoEntry(StringBuffer buf, String key, String value) {
    
    buf.append("<tr><td width=\"35%\">");
    buf.append("<div id=\"key\">");
    
    buf.append(key);
    buf.append("</div>");
    return buf.append("</td><td>").append(value).append("</td></tr>");
    
    
  }
  
  private String createAboutText(ExtendedHTMLDocument doc) {
  
    StringBuffer buf=new StringBuffer();
    buf.append("<html>" +
               "  <head>" +
               "<style type=\"text/css\" media=\"screen\">" +
               "<!--" +
               "#title { font-size:18px; font-family:Dialog; text-align:center; font-weight:bold;}" +
               "#key { font-size:12px; font-famile:Dialog; font-weight:bold; }" +
               "" +
               "" +
               "" +
               "#small { font-size:9px; font-family:Dialog; }" +
               "-->" +
               "  </head>" +
               "  <body>" +
               "    <div id=\"title\">"+mLocalizer.msg("about", "About")+"</div>" +
               "<p>" +
               "    <table width=\"100%\"");
               
    createInfoEntry(buf, mLocalizer.msg("version", "Version"),
       TVBrowser.VERSION.toString());
       
    createInfoEntry(buf, mLocalizer.msg("platform", "Platform"),
       System.getProperty("os.name") + " " + System.getProperty("os.version"));
       
    createInfoEntry(buf, mLocalizer.msg("system", "System"),
       System.getProperty("os.arch"));
    
    createInfoEntry(buf, mLocalizer.msg("javaVersion", "Java Version"),
       System.getProperty("java.version"));
    
    createInfoEntry(buf, mLocalizer.msg("javaVM", "Java VM"),
       System.getProperty("java.vm.name"));
    
    createInfoEntry(buf, mLocalizer.msg("javaVendor", "Java vendor"),
      System.getProperty("java.vendor"));
    
    createInfoEntry(buf, mLocalizer.msg("javaHome", "Java home"),
      System.getProperty("java.home"));
    
    createInfoEntry(buf, mLocalizer.msg("location", "Location"),
      System.getProperty("user.country") + "," + System.getProperty("user.language"));
               
    java.util.TimeZone timezone = java.util.TimeZone.getDefault();
    int tzOffset = timezone.getRawOffset() / 1000 / 60 / 60;
    String tzOffsetAsString = mLocalizer.msg("hours", "({0,number,+#;#} hours)",
    new Integer(tzOffset));           
               
    createInfoEntry(buf, mLocalizer.msg("timezone", "Timezone"),
       timezone.getDisplayName() + " " + tzOffsetAsString);         
                
               
    buf.append("</p>    </table>");
    buf.append("<p valign=\"10px\"");
    buf.append("<div id=\"small\">");
    
    buf.append(mLocalizer.msg("copyrightText",
          "Copyright (c) 04/2003 by Martin Oberhauser, Til Schneider under the"
          + "GNU General Public License"));
          
    buf.append("</div>");
    
    buf.append("<div id=\"small\">");
    buf.append("This product includes software developed " +
                       "by L2FProd.com (http://www.L2FProd.com/).");
    
    buf.append("</div>");
    buf.append("</p>");
    buf.append("  </body>" +
               "</html>");
    return buf.toString();
  }
}
