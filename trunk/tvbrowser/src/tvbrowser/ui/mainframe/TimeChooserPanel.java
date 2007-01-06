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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import util.ui.GridFlowLayout;
import util.ui.TimeFormatter;
import devplugin.SettingsItem;


public class TimeChooserPanel extends JPanel implements ChangeListener, AncestorListener, MouseListener {
    
    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(TimeChooserPanel.class);

    
    private MainFrame mParent;
    private JPanel mTimeBtnPanel;
    
    private JPanel mGridPn;
    
    public TimeChooserPanel(MainFrame parent) {
      setOpaque(false);
      mParent=parent;
      setLayout(new BorderLayout(0,2));
      setBorder(BorderFactory.createEmptyBorder(5,3,5,3));
      
      mGridPn = new JPanel(new GridFlowLayout(5,5,GridFlowLayout.TOP, GridFlowLayout.CENTER));
      add(mGridPn,BorderLayout.CENTER);

      addAncestorListener(this);
      
      createContent();
      
      String msg;
      msg = mLocalizer.msg("button.now", "Now");
      JButton nowBt=new JButton(msg);
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
      updateUI();
    }
    
    private void createContent() {
      mGridPn.removeAll();
      
      TimeFormatter formatter = new TimeFormatter();
      
      int[] times = Settings.propTimeButtons.getIntArray();
      
      for (int i=0; i<times.length; i++) {
        final int time = times[i];
        int h = time/60;
        int m = time%60;
        JButton btn = new JButton(formatter.formatTime(h, m));
        mGridPn.add(btn);
        btn.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent arg0) {
            mParent.scrollToTime(time);
          }
        });
        btn.addMouseListener(this);
      }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
     */
    public void ancestorAdded(AncestorEvent event) {
        Settings.propTimeButtons.addChangeListener(this);        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent)
     */
    public void ancestorMoved(AncestorEvent event) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent)
     */
    public void ancestorRemoved(AncestorEvent event) {
        Settings.propTimeButtons.removeChangeListener(this);        
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

}