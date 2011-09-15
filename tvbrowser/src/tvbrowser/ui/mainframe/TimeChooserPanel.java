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
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
      
      createContent();
      
      String msg;
      msg = mLocalizer.msg("button.now", "Now");
      JButton nowBt=new JButton(msg) {
        protected void paintComponent(Graphics g) {
          if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
            Color c = Persona.getInstance().getAccentColor().darker().darker().darker();
            g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),100));
            g.fillRect(0,0,getWidth(),getHeight());
            FontMetrics metrics = g.getFontMetrics(getFont());
            int textWidth = metrics.stringWidth(getText());
            int baseLine =  getHeight()/2+ metrics.getMaxDescent()+1;
            
            if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
              g.setColor(Persona.getInstance().getShadowColor());
              
              g.drawString(getText(),getWidth()/2-textWidth/2+1,baseLine+1);
              g.drawString(getText(),getWidth()/2-textWidth/2+2,baseLine+2);
            }
            
            g.setColor(Persona.getInstance().getTextColor());
            g.drawString(getText(),getWidth()/2-textWidth/2,baseLine);
          }
          else {
            super.paintComponent(g);
          }
        }
      };
      nowBt.addKeyListener(keyListener);
      nowBt.setOpaque(false);
      nowBt.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
            mParent.scrollToNow();
        }});
      add(nowBt, BorderLayout.SOUTH);
      nowBt.addMouseListener(this);
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
              Color c = Persona.getInstance().getAccentColor().darker().darker().darker();
              g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),100));
              g.fillRect(0,0,getWidth(),getHeight());
              FontMetrics metrics = g.getFontMetrics(getFont());
              int textWidth = metrics.stringWidth(getText());
              int baseLine =  getHeight()/2+ metrics.getMaxDescent()+1;
              
              if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
                g.setColor(Persona.getInstance().getShadowColor());
                
                g.drawString(getText(),getWidth()/2-textWidth/2+1,baseLine+1);
                g.drawString(getText(),getWidth()/2-textWidth/2+2,baseLine+2);
              }
              
              g.setColor(Persona.getInstance().getTextColor());
              g.drawString(getText(),getWidth()/2-textWidth/2,baseLine);
            }
            else {
              super.paintComponent(g);
            }
          }
        };
        btn.setOpaque(false);
        btn.addKeyListener(mKeyListener);
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
      }
      else {
        setOpaque(true);
        mGridPn.setOpaque(true);
      }
    }
}