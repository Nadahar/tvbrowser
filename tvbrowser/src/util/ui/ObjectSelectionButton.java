/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import tvbrowser.ui.mainframe.MainFrame;
import util.misc.StringPool;

import devplugin.Program;

/**
 * A Button with an attached dialog to
 * select Objects.
 * 
 * @author René Mach
 * @param <E> The class of the selection Object.
 * @since 3.2
 */
public class ObjectSelectionButton<E> extends JButton implements ActionListener {
  private E[] mObjectArr;
  private ListCellRenderer mListCellRenderer;
  private ArrayList<ObjectSelectionListener<E>> mListenerList;
  private long mLastClosedDialogTime;
  
  public ObjectSelectionButton(E[] objects, ImageIcon icon) {
    mListenerList = new ArrayList<ObjectSelectionListener<E>>();
    mLastClosedDialogTime = 0;
    mListCellRenderer = new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if(value instanceof Program) {
          c.setText(StringPool.getString(((Program)value).getTimeString()) + " " + ((Program)value).getTitle());
          
          if(!isSelected && !cellHasFocus && ((Program)value).isExpired()) {
            c.setForeground(Color.lightGray);
          }
        }        
        
        return c;
      }
    };
    
    setIcon(new ImageIcon(icon.getImage()) {      
      public void paintIcon(Component c,Graphics g,int x,int y) {
        super.paintIcon(c, g, x, y);
        
        int x1 = x+super.getIconWidth()+2;
        int y1 = y+getIconHeight()/2-1;
        int[] xPoints = {x1,x1+3,x1+6};
        int[] yPoints = {y1,y1+4,y1};
        
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.gray);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(Color.gray);
        g.drawPolygon(xPoints, yPoints, 3);
      }
      
      public int getIconWidth() {
        return super.getIconWidth() + 11;
      }
    });
    addActionListener(this);
    mObjectArr = objects;
    setHorizontalTextPosition(RIGHT);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    showDialog();
  }
  
  private void showDialog() {
    if(mObjectArr != null && mLastClosedDialogTime + 800 < System.currentTimeMillis()) {
      final JList list = new JList(mObjectArr);
      final JDialog dialog = new JDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
      
      list.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e)) {
            objectSelected(list,dialog);
          }
        }
      });
      
      list.addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          int index = list.locationToIndex(e.getPoint());
          
          if(index != -1) {
            list.setSelectedIndex(index);
          }
        }
      });
      
      list.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            objectSelected(list,dialog);
          }
        }
      });
      
      list.setCellRenderer(mListCellRenderer);
      
      JScrollPane scrollPane = new JScrollPane(list);
            
      Toolkit t = Toolkit.getDefaultToolkit();
      
      scrollPane.setMaximumSize(new Dimension(t.getScreenSize().width/2, t.getScreenSize().height/2));
      
      dialog.setModal(false);
      dialog.setUndecorated(true);
      
      dialog.getContentPane().setLayout(new BorderLayout());
      
      dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
      dialog.pack();
      
      Point p = getLocationOnScreen();
  
      int x = p.x;
      int y = p.y + getHeight();
      
      if(x + dialog.getWidth() > t.getScreenSize().width) {
        x = p.x + getWidth() - dialog.getWidth();
      }
      
      if(x < 0) {
        x = 0;
      }
      
      if(y + dialog.getHeight() > t.getScreenSize().height) {
        y = p.y - dialog.getHeight();
      }
      
      if(y < 0) {
        y = 0;
      }
      
      dialog.setLocation(x,y);    
      
      UiUtilities.registerForClosing(new WindowClosingIf() {
        public void close() {
          dialog.removeWindowListener(dialog.getWindowListeners()[0]);
          dialog.dispose();
        }

        public JRootPane getRootPane() {
          return dialog.getRootPane();
        }
      });
  
      dialog.addWindowListener(new WindowAdapter() {
        public void windowDeactivated(WindowEvent e) {
          ((JDialog)e.getSource()).dispose();
        }
        
        public void windowClosed(WindowEvent e) {
          mLastClosedDialogTime = System.currentTimeMillis();
        }
      });
        
        
      dialog.setVisible(true);    
    }    
  }
  
  private void objectSelected(JList list, JDialog dialog) {
    if(list.getSelectedIndex() >= 0) {
      @SuppressWarnings("unchecked")
      E object = (E)list.getSelectedValue();
      
      for(ObjectSelectionListener<E> listener : mListenerList) {
        listener.objectSeleted(object);
      }
    
      dialog.dispose();
    }
  }
  
  public void setObjectArr(E[] objectArr) {
    mObjectArr = objectArr;
  }
  
  public void setListCellRenderer(ListCellRenderer renderer) {
    mListCellRenderer = renderer;
  }
  
  public void addProgramSelectionListener(ObjectSelectionListener<E> listener) {
    mListenerList.add(listener);
  }
  public void removeProgramSelectionListener(ObjectSelectionListener<E> listener) {
    mListenerList.remove(listener);
  }
}
