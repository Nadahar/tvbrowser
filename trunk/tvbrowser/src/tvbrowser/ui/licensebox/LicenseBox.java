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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LicenseBox extends JDialog implements ActionListener {
  
  int mRemainingSecs;
  private JButton mAgreeBt, mDisagreeBt, mCloseBt;
  private Timer mTimer;
  private boolean mAgreed=false;
  private boolean mMustAgree;
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LicenseBox.class); 
  
  public LicenseBox(JFrame parent, boolean mustAgree) {
    
    super(parent, true);
    mMustAgree=mustAgree;
    setTitle(mLocalizer.msg("terms", "Terms of Use"));
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
 
    String licenseTxt=mLocalizer.msg("license","license text");
    JTextArea ta=new JTextArea(licenseTxt);
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setEditable(false);
    
    Font f=new Font("Monospaced",Font.PLAIN,12);
    
    JPanel btnPanel=new JPanel();
    
    mAgreeBt=new JButton(mLocalizer.msg("agree","I agree"));
    mDisagreeBt=new JButton(mLocalizer.msg("disagree","I do not agree"));
       
    mCloseBt=new JButton(mLocalizer.msg("close","close"));
    
    mAgreeBt.addActionListener(this);
    mDisagreeBt.addActionListener(this);
    
    mAgreeBt.setEnabled(false);
    if (mMustAgree) {
      btnPanel.add(mDisagreeBt);
      btnPanel.add(mAgreeBt);
      
      mRemainingSecs=10;    
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
          hide();
        }
      });
    }
    
    contentPane.add(new JScrollPane(ta),BorderLayout.CENTER);
    contentPane.add(btnPanel,BorderLayout.SOUTH);
    
    
    
    setSize(300,300);
   
    
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource()==mDisagreeBt) {
      hide();
    }
    else if (e.getSource()==mAgreeBt) {
      mAgreed=true;
      hide();
    }
  }
  
  public boolean agreed() {
    return mAgreed;    
  }
  
}