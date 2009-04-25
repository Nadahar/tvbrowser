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
package tvbrowser.ui.licensebox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.html.HTMLTextHelper;

public class LicenseBox extends JDialog implements ActionListener,WindowClosingIf {
  
  private int mRemainingSecs;
  private JButton mAgreeBt, mDisagreeBt, mCloseBt;
  private Timer mTimer;
  private boolean mAgreed=false;
  private boolean mMustAgree;
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LicenseBox.class); 
  
  public LicenseBox(JFrame parent, String licenseTxt, boolean mustAgree) {
    
    super(parent, true);
    mMustAgree=mustAgree;
    setTitle(mLocalizer.msg("terms", "Terms of Use"));
    
    UiUtilities.registerForClosing(this);
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());

    if (!licenseTxt.startsWith("<html")) {
      licenseTxt = HTMLTextHelper.convertTextToHtml(licenseTxt, true);
    }

    final JEditorPane ta = new JEditorPane();

    ExtendedHTMLEditorKit kit = new ExtendedHTMLEditorKit();
    kit.setLinkCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    ta.setEditorKit(kit);

    Color c =  ta.getBackground();
    ta.setEditable(false);
    ta.setBackground(c);
    ta.setOpaque(true);
    ta.setFocusable(true);
    ta.setText(licenseTxt);

    ta.addHyperlinkListener(new HyperlinkListener() {
      private String mTooltip;
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          mTooltip = ta.getToolTipText();
          ta.setToolTipText(evt.getURL().toString());
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
          ta.setToolTipText(mTooltip);
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    });

    JPanel btnPanel=new JPanel();
    
    mAgreeBt=new JButton(mLocalizer.msg("agree","I agree"));
    mDisagreeBt=new JButton(mLocalizer.msg("disagree","I do not agree"));
       
    mCloseBt=new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    
    mAgreeBt.addActionListener(this);
    mDisagreeBt.addActionListener(this);
    
    mAgreeBt.setEnabled(false);
    if (mMustAgree) {
      btnPanel.add(mDisagreeBt);
      btnPanel.add(mAgreeBt);
      
      mRemainingSecs=5;    
      mTimer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mRemainingSecs--;
            if (mRemainingSecs==0) {
              mAgreeBt.setText(mLocalizer.msg("agree","I agree"));
              mAgreeBt.setEnabled(true);
              mTimer.stop();
            }
            else {
              mAgreeBt.setText(mLocalizer.msg("agree","I agree")+" ("+mRemainingSecs+") ");
            }
          }
        });
      mTimer.start();      
    }
    else {
      btnPanel.add(mCloseBt);
      mCloseBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
        }
      });
    }
    
    JPanel panel1=new JPanel(new BorderLayout());
    panel1.add(new JScrollPane(ta),BorderLayout.CENTER);
    panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    contentPane.add(panel1,BorderLayout.CENTER);
    contentPane.add(btnPanel,BorderLayout.SOUTH);
    
    
    
    setSize(460,520);
   
    
  }
  
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource()==mDisagreeBt) {
      setVisible(false);
    }
    else if (e.getSource()==mAgreeBt) {
      mAgreed=true;
      setVisible(false);
    }
  }
  
  public boolean agreed() {
    return mAgreed;    
  }


  public void close() {
   if(!mMustAgree || agreed() )
     dispose();
  }
  
}