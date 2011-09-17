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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import util.ui.GridFlowLayout;
import util.ui.TimeFormatter;
import util.ui.persona.Persona;
import devplugin.SettingsItem;


public class TimeChooserPanel extends JPanel implements ChangeListener, MouseListener {
    
    /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(TimeChooserPanel.class);

    
    private MainFrame mParent;
    
    private JPanel mGridPn;
    private KeyListener mKeyListener;
    private JButton mNowBt;
    private Border mDefaultButtonBorder;
    private final static int mBorderWidth = 9;
    private final static int mBorderHeight = 4;
    private boolean mRollOverEnabled;
    
    public TimeChooserPanel(MainFrame parent,KeyListener keyListener) {
      addKeyListener(keyListener);
      mParent=parent;
      setLayout(new BorderLayout(0,2));
      setBorder(BorderFactory.createEmptyBorder(5,3,5,3));
      
      mGridPn = new JPanel(new GridFlowLayout(5,5,GridFlowLayout.TOP, GridFlowLayout.CENTER));
      updatePersona();
      mGridPn.addKeyListener(keyListener);
      mGridPn.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          mGridPn.requestFocus();
        }
      });
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          requestFocus();
        }
      });
      
      add(mGridPn,BorderLayout.CENTER);
      
      mKeyListener = keyListener;
      
      String msg;
      msg = mLocalizer.msg("button.now", "Now");
      mNowBt=new JButton(msg) {
        protected void paintComponent(Graphics g) {
          if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
            Color c = Persona.getInstance().getAccentColor();
            
            double test = (0.2126 * Persona.getInstance().getTextColor().getRed()) + (0.7152 * Persona.getInstance().getTextColor().getGreen()) + (0.0722 * Persona.getInstance().getTextColor().getBlue());
            int alpha = 100;
            
            if(test <= 30) {
              c = Color.white;
              alpha = 200;
            }
            else if(test <= 40) {
              c = c.brighter().brighter().brighter().brighter().brighter().brighter();
              alpha = 200;
            }
            else if(test <= 60) {
              c = c.brighter().brighter().brighter();
              alpha = 160;
            }
            else if(test <= 100) {
              c = c.brighter().brighter();
              alpha = 140;
            }
            else if(test <= 145) {
              alpha = 120;
            }
            else if(test <= 170) {
              c = c.darker();
              alpha = 120;
            }
            else if(test <= 205) {
              c = c.darker().darker();
              alpha = 120;
            }
            else if(test <= 220){
              c = c.darker().darker().darker();
              alpha = 100;
            }
            else if(test <= 235){
              c = c.darker().darker().darker().darker();
              alpha = 100;
            }
            else {
              c = Color.black;
              alpha = 100;
            }
            
            Color textColor = Persona.getInstance().getTextColor();
            
            if(getModel().isArmed() || getModel().isRollover() || isFocusOwner()) {
              c = UIManager.getColor("List.selectionBackground");
              
              double test1 = (0.2126 * c.getRed()) + (0.7152 * c.getGreen()) + (0.0722 * c.getBlue());
              double test2 = (0.2126 * textColor.getRed()) + (0.7152 * textColor.getGreen()) + (0.0722 * textColor.getBlue());
              
              if(Math.abs(test2-test1) <= 40) {
                textColor = UIManager.getColor("List.selectionForeground");
              }
            }
            
            if(getModel().isPressed()) {
              alpha -= 50;
            }
            else if(isFocusOwner()) {
              alpha -= 100;
            }
            
            g.setColor(Persona.getInstance().getTextColor());
            g.draw3DRect(0,0,getWidth()-1,getHeight()-1,!getModel().isPressed());
            g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
            g.fillRect(1,1,getWidth()-2,getHeight()-2);
            
            FontMetrics metrics = g.getFontMetrics(getFont());
            int textWidth = metrics.stringWidth(getText());
            int baseLine =  getHeight()/2+ metrics.getMaxDescent()+1;
            
            if(!Persona.getInstance().getShadowColor().equals(textColor) && Persona.getInstance().getTextColor().equals(textColor)) {
              g.setColor(Persona.getInstance().getShadowColor());
              
              g.drawString(getText(),getWidth()/2-textWidth/2+1,baseLine+1);
             // g.drawString(getText(),getWidth()/2-textWidth/2+2,baseLine+2);
            }
            
            g.setColor(textColor);
            g.drawString(getText(),getWidth()/2-textWidth/2,baseLine);
          }
          else {
            super.paintComponent(g);
          }
        }
      };
      
      mDefaultButtonBorder = mNowBt.getBorder();
      mRollOverEnabled = mNowBt.isRolloverEnabled();
      
      if(mNowBt != null && Persona.getInstance().getHeaderImage() != null) {
        mNowBt.setBorder(BorderFactory.createEmptyBorder(mBorderHeight,mBorderWidth,mBorderHeight,mBorderWidth));
        mNowBt.setRolloverEnabled(true);
      }
      
      mNowBt.addKeyListener(keyListener);
      mNowBt.setOpaque(false);
      mNowBt.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
            mParent.scrollToNow();
        }});
      add(mNowBt, BorderLayout.SOUTH);
      mNowBt.addMouseListener(this);
      
      createContent();
      
      addMouseListener(this);
    }
    
    public void stateChanged(ChangeEvent e) {
        createContent();
    }

    public void updateButtons() {
      createContent();
      mGridPn.updateUI();
    }
    
    private void createContent() {try {
      mGridPn.removeAll();
      
      TimeFormatter formatter = new TimeFormatter();
      
      int[] times = Settings.propTimeButtons.getIntArray();
      
      for (final int time : times) {
        int h = time/60;
        int m = time%60;
        JButton btn = new JButton(formatter.formatTime(h, m)) {
          protected void paintComponent(Graphics g) {
            if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
              Color c = Persona.getInstance().getAccentColor();
              
              double test = (0.2126 * Persona.getInstance().getTextColor().getRed()) + (0.7152 * Persona.getInstance().getTextColor().getGreen()) + (0.0722 * Persona.getInstance().getTextColor().getBlue());
              int alpha = 100;
              
              if(test <= 30) {
                c = Color.white;
                alpha = 200;
              }
              else if(test <= 40) {
                c = c.brighter().brighter().brighter().brighter().brighter().brighter();
                alpha = 200;
              }
              else if(test <= 60) {
                c = c.brighter().brighter().brighter();
                alpha = 160;
              }
              else if(test <= 100) {
                c = c.brighter().brighter();
                alpha = 140;
              }
              else if(test <= 145) {
                alpha = 120;
              }
              else if(test <= 170) {
                c = c.darker();
                alpha = 120;
              }
              else if(test <= 205) {
                c = c.darker().darker();
                alpha = 120;
              }
              else if(test <= 220){
                c = c.darker().darker().darker();
                alpha = 100;
              }
              else if(test <= 235){
                c = c.darker().darker().darker().darker();
                alpha = 100;
              }
              else {
                c = Color.black;
                alpha = 100;
              }
              
              Color textColor = Persona.getInstance().getTextColor();
              
              if(getModel().isArmed() || getModel().isRollover() || isFocusOwner()) {
                c = UIManager.getColor("List.selectionBackground");
                
                double test1 = (0.2126 * c.getRed()) + (0.7152 * c.getGreen()) + (0.0722 * c.getBlue());
                double test2 = (0.2126 * textColor.getRed()) + (0.7152 * textColor.getGreen()) + (0.0722 * textColor.getBlue());
                
                if(Math.abs(test2-test1) <= 40) {
                  textColor = UIManager.getColor("List.selectionForeground");
                }
              }
              
              if(getModel().isPressed()) {
                alpha -= 50;
              }
              else if(isFocusOwner()) {
                alpha -= 100;
              }
              
              g.setColor(Persona.getInstance().getTextColor());
              g.draw3DRect(0,0,getWidth()-1,getHeight()-1,!getModel().isPressed());
              g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
              g.fillRect(1,1,getWidth()-2,getHeight()-2);
              
              FontMetrics metrics = g.getFontMetrics(getFont());
              int textWidth = metrics.stringWidth(getText());
              int baseLine =  getHeight()/2+ metrics.getMaxDescent()+1;
              
              if(!Persona.getInstance().getShadowColor().equals(textColor) && Persona.getInstance().getTextColor().equals(textColor)) {
                g.setColor(Persona.getInstance().getShadowColor());
                
                g.drawString(getText(),getWidth()/2-textWidth/2+1,baseLine+1);
               // g.drawString(getText(),getWidth()/2-textWidth/2+2,baseLine+2);
              }
              
              g.setColor(textColor);
              g.drawString(getText(),getWidth()/2-textWidth/2,baseLine);
            }
            else {
              super.paintComponent(g);
            }
          }
        };
        btn.setOpaque(false);
        btn.addKeyListener(mKeyListener);
        
        if(Persona.getInstance().getHeaderImage() != null) {
          btn.setBorder(BorderFactory.createEmptyBorder(mBorderHeight,mBorderWidth,mBorderHeight,mBorderWidth));
          btn.setRolloverEnabled(true);
        }
        else {
          btn.setBorder(mDefaultButtonBorder);
          btn.setRolloverEnabled(mRollOverEnabled);
        }
        
        mGridPn.add(btn);
        btn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent arg0) {
            mParent.scrollToTime(time);
          }
        });
        btn.addMouseListener(this);
      }}catch(Throwable t  ){t.printStackTrace();}
    }
    
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) {
        showPopup(e);
      }
    }
      
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        showPopup(e);
      }
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    private void showPopup(MouseEvent e) {
      JPopupMenu menu = new JPopupMenu();
      
      JMenuItem configure = new JMenuItem(mLocalizer.msg("configure","Configure"));
      configure.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().showSettingsDialog(SettingsItem.TIMEBUTTONS);
        }
      });
      
      menu.add(configure);
      
      int x = e.getX();
      int y = e.getY();
      
      if (e.getSource() != this) {
        x += ((JButton)e.getSource()).getX();
        y += ((JButton)e.getSource()).getY();
      }
      
      menu.show(this, x, y);
    }
    
    /**
     * Updates the search field on Persona change.
     */
    public void updatePersona() {
      if(Persona.getInstance().getHeaderImage() != null) {
        setOpaque(false);
        mGridPn.setOpaque(false);
        
        if(mNowBt != null) {
          mNowBt.setBorder(BorderFactory.createEmptyBorder(mBorderHeight,mBorderWidth,mBorderHeight,mBorderWidth));
          mNowBt.setRolloverEnabled(true);
          updateButtons();
        }
      }
      else {
        setOpaque(true);
        mGridPn.setOpaque(true);
        
        if(mNowBt != null) {
          mNowBt.setRolloverEnabled(mRollOverEnabled);
          mNowBt.setBorder(mDefaultButtonBorder);
          updateButtons();
        }
      }
    }
}