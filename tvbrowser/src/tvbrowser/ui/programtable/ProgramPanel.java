/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


/**
 * TV-Browser
 * @author Martin Oberhauser
 */
package tvbrowser.ui.programtable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import devplugin.Plugin;

import tvbrowser.core.*;
import tvbrowser.ui.TextArea;

/**
 * A ProgramPanel is a JComponent representing a single program.
 */
public class ProgramPanel extends JComponent implements ChangeListener {

  private static Font bold=null, italic=null;
  private static final int WIDTH_LEFT=40;;
  private static final int WIDTH_RIGHT=Settings.getColumnWidth()-WIDTH_LEFT;
  private static final int WIDTH=WIDTH_LEFT+WIDTH_RIGHT;
  private int height=0;
  private TextArea titleArea;
  private TextArea descArea;
  private String timeStr;
  private devplugin.Program program;

  {
    Font f=new JTextArea().getFont();
    bold=new Font(f.getName(),Font.BOLD,f.getSize());
    italic=new Font(f.getName(),Font.PLAIN,f.getSize()-2);
  }


  /**
   * Sets the content of the program panel.
   */
  public void init(devplugin.Program prog) {
    this.program=prog;
    
    program.addChangeListener(this);
    
    titleArea=new TextArea(prog.getTitle().toCharArray(),bold,WIDTH_RIGHT-10);
    descArea=new TextArea(prog.getShortInfo().toCharArray(),italic,WIDTH_RIGHT);

    height=titleArea.getHeight()+10+descArea.getHeight();
    timeStr=prog.getTimeString();
    this.setPreferredSize(new Dimension(WIDTH,height));
  }

  public ProgramPanel() {

  }

  public ProgramPanel(devplugin.Program prog) {
    init(prog);
  }


  public void paintComponent(Graphics g) {
    g.setFont(bold);
    g.setColor(Color.black);
    g.drawString(timeStr,0,bold.getSize());
    titleArea.paint(g,WIDTH_LEFT,0);
    descArea.paint(g,WIDTH_LEFT,titleArea.getHeight());

    if (program.isOnAir()) {
      g.setColor(new Color(128,128,255,40));
      g.fill3DRect(0, 0, WIDTH, height, true);
    }

    // paint the icons of the plugins that have marked the program
    Iterator pluginIter = program.getMarkedByIterator();
    if (pluginIter.hasNext()) {
      g.setColor(new Color(255,0,0,40));
      g.fill3DRect(0,0,WIDTH,height,true);

      int x = WIDTH - 16;
      int y = height - 16;
      while (pluginIter.hasNext()) {
        Plugin plugin = (Plugin) pluginIter.next();
        Icon icon = plugin.getIcon();
        if (icon != null) {
          icon.paintIcon(this, g, x, y);
          x -= 16;
        }
      }
    }
  }



  /**
   * Returns the devplugin.Program object of this ProgramPanel
   */
  public devplugin.Program getProgram() {
    return program;
  }
  
  
  // implements ChangeListener
  
  
  public void stateChanged(ChangeEvent evt) {
    if (evt.getSource() == program) {
      repaint();
    }
  }

}