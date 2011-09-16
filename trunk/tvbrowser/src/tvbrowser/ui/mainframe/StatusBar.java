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

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import util.ui.persona.Persona;

import devplugin.ProgressMonitor;

/**
 * Statusbar
 * @author bodum
 */
public class StatusBar extends JPanel {
  /** Progressbar for Download-Status etc */
  private JProgressBar mProgressBar;
  /** Info-Text */
  private JLabel mInfoLabel;

  /**
   * Create the Statusbar
   * @param keyListener The key listener for FAYT.
   */
  public StatusBar(KeyListener keyListener) {
    setOpaque(false);
    setLayout(new BorderLayout(1, 0));
    
    if(!UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel") &&
        !UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
      setBorder(BorderFactory.createEmptyBorder(2,0,0,0));          
    }

    setPreferredSize(new Dimension(0, 22));
    
    mInfoLabel = new JLabel() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
          int baseLine = getBaseline(0,getHeight())-1;          
        
          if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) { 
            g.setColor(Persona.getInstance().getShadowColor());
            g.drawString(getText(),getIconTextGap()+1,baseLine+1);
            g.drawString(getText(),getIconTextGap()+2,baseLine+2);
          }
          
          g.setColor(Persona.getInstance().getTextColor());
          g.drawString(getText(),getIconTextGap(),baseLine);
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    
    mInfoLabel.addKeyListener(keyListener);
    
    mProgressBar = new JProgressBar();
    mProgressBar.setVisible(false);
    mProgressBar.addKeyListener(keyListener);
    mProgressBar.setPreferredSize(new Dimension(150, 10));
    
updatePersona();
    
    

    add(mInfoLabel, BorderLayout.CENTER);
    add(mProgressBar, BorderLayout.EAST);
  }

  /**
   * Gets the ProgressBar
   * @return ProgressBar
   */
  public JProgressBar getProgressBar() {
    return mProgressBar;
  }

  /**
   * Gets the Label
   * @return Label
   */
  public JLabel getLabel() {
    return mInfoLabel;
  }

  public ProgressMonitor createProgressMonitor() {
    return new ProgressMonitor(){
      public void setMaximum(int maximum) {
        mProgressBar.setMaximum(maximum);
      }

      public void setValue(int value) {
        mProgressBar.setValue(value);
      }

      public void setMessage(String msg) {
        mInfoLabel.setText(msg);
      }
    };
  }
  
  /**
   * Updates the search field on Persona change.
   */
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() == null) {
      mInfoLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,1,0,0,mProgressBar.getBackground().darker().darker()),BorderFactory.createMatteBorder(0,0,1,1,mProgressBar.getBackground().brighter())),BorderFactory.createEmptyBorder(0,3,0,0)));
      mProgressBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,1,0,0,mProgressBar.getBackground().darker().darker()),BorderFactory.createMatteBorder(0,0,1,1,mProgressBar.getBackground().brighter())),BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2),BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,1,0,0,mProgressBar.getBackground().darker().darker()),BorderFactory.createMatteBorder(0,0,1,1,mProgressBar.getBackground().brighter().brighter())))));  
    }
    else {
      mInfoLabel.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
      mProgressBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2),BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,1,0,0,mProgressBar.getBackground().darker().darker()),BorderFactory.createMatteBorder(0,0,1,1,mProgressBar.getBackground().brighter().brighter()))));
    }
  }
}