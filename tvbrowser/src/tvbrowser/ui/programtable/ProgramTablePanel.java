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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.programtable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;

import tvbrowser.core.*;
import tvbrowser.ui.SkinPanel;
import tvbrowser.ui.ContextMenu;
import tvbrowser.ui.channelpanel.ChannelPanel;
import tvbrowser.ui.channelpanel.ChannelChooser;


class ProgramDayTime extends JPanel {

  private JPanel centerPanel;
  private JLabel statusLabel;
  private MouseMotionListener mouseMotionListener;
  private ProgramColumn[] cols;


  public ProgramDayTime(int numOfCols) {
    setLayout(new BorderLayout(0,0));

    centerPanel=new JPanel(new GridLayout(1,0,0,0));

    add(centerPanel,BorderLayout.CENTER);

    cols=new ProgramColumn[numOfCols];
    for (int i=0;i<numOfCols;i++) {
      cols[i]=new ProgramColumn();
      centerPanel.add(cols[i]);
    }

    centerPanel.setOpaque(false);
    setOpaque(false);

  }

  public devplugin.Program getProgramAt(Point p) {

    Component c=getComponentAt(p);
    c=c.getComponentAt(p.x-c.getX(),p.y-c.getY());
    if (c instanceof ProgramColumn) {
      return ((ProgramColumn)c).getProgramAt(p);
    }
    return null;
  }

  public void addProgram(tvdataloader.AbstractProgram p) {
  //  int col=p.getChannel().getPos();
    int col=ChannelList.getPos(p.getChannel().getId());
    
    if (col<0) {
      throw new RuntimeException("cannot add program from channel "+p.getChannel());

    }
    ProgramPanel panel=ProgramPanelFactory.createProgramPanel(p);
    cols[col].add(panel);
  }
}



class ProgramColumn extends JPanel {

  private JPanel centerPanel;
  private Vector components;

  public ProgramColumn() {
    setLayout(new BorderLayout(0,0));
    centerPanel=new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.Y_AXIS));

    add(centerPanel,BorderLayout.CENTER);
    components=new Vector();

    centerPanel.setOpaque(false);
    setOpaque(false);
  }



  public devplugin.Program getProgramAt(Point p) {

    Point p1=new Point(p.x-getX(),p.y-getY());

    Component c=getComponentAt(p1);
    c=c.getComponentAt(p1.x-c.getX(),p1.y-c.getY());
    if (c instanceof ProgramPanel) {
      return ((ProgramPanel)c).getProgram();
    }
    return null;
  }

  public Component add(Component comp) {
    components.add(comp);
    return centerPanel.add(comp);
  }

  public int getNumOfPrograms() {
    return components.size();
  }
}

public class ProgramTablePanel extends JPanel implements MouseInputListener, ChangeListener, ScrollableTablePanel, ActionListener {

  private static int NUM_OF_DAYTIMES=6;

  private ProgramDayTime part[];
  private SkinPanel centerPanel;
  private JScrollPane scrollPane=null;

  private boolean isDragging;
  private Point draggingPoint, viewPoint;
  private ContextMenu contextMenu;

  private ChannelPanel channelPanel;
  private JPanel content;
  private JLabel statusLabel;
  private DayProgram dayProgram;

  private javax.swing.Timer timer;

  /**
   * Constructs a new ProgramTablePanel with the specified parent frame.
   */
  public ProgramTablePanel(Frame parent) {

    setLayout(new BorderLayout());
    final ScrollableTablePanel tablePanel=this;
    part=new ProgramDayTime[NUM_OF_DAYTIMES];
    contextMenu=new ContextMenu(parent);
    Object[] plugins=PluginManager.getInstalledPlugins();
    for (int i=0;i<plugins.length;i++) {
      contextMenu.addPlugin((devplugin.Plugin)plugins[i]);
    }

    timer=new javax.swing.Timer(10000,this);
  }

  /**
   * Changes the dayprogram to prog and repaints the program table.
   */
  public void setDayProgram(DayProgram prog) throws java.io.IOException {

    if (timer.isRunning()) {
      timer.stop();
    }
    
    dayProgram = prog;

    if (scrollPane!=null) remove(scrollPane);
    if (centerPanel!=null) remove(centerPanel);
    if (content!=null) remove(content);

    content=new JPanel(new BorderLayout());

    centerPanel=new SkinPanel(Settings.getTableSkin(),Settings.getTableBGMode());

    centerPanel.setLayout(new BorderLayout());
    centerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

    scrollPane=new JScrollPane(centerPanel);
    scrollPane.addMouseMotionListener(this);
    scrollPane.addMouseListener(this);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(30);
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);

    for (int i=0;i<part.length;i++) {
      part[i]=new ProgramDayTime(ChannelList.getNumberOfSubscribedChannels());
    }

 
    ProgramPanelFactory.reset();

    if (dayProgram != null) {
      Iterator iterator=dayProgram.iterator();
      tvdataloader.AbstractChannelDayProgram curDayProgram;

      Iterator progs;
      tvdataloader.AbstractProgram curProgram;
      int h;

      while (iterator.hasNext()) {
        curDayProgram=(tvdataloader.AbstractChannelDayProgram)iterator.next();

        progs=curDayProgram.getPrograms();
        
        
        
        
        while (progs.hasNext()) {
          curProgram=(tvdataloader.AbstractProgram)progs.next();
          h=curProgram.getHours();
          int inx=h/((24/part.length));
          
          devplugin.Channel channel=curProgram.getChannel();
         
          part[inx].addProgram(curProgram);
        }
      }
    }



    centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.Y_AXIS));
    for (int i=0;i<part.length;i++) {
      centerPanel.add(part[i]);
    }


    scrollPane.getViewport().addChangeListener(this);
    content.add(scrollPane,BorderLayout.CENTER);
    channelPanel=new ChannelPanel();

    JPanel northPanel=new JPanel(new BorderLayout());
    northPanel.add(channelPanel,BorderLayout.CENTER);
    northPanel.add(new ChannelChooser(this),BorderLayout.EAST);
    northPanel.setBorder(BorderFactory.createLoweredBevelBorder());

    content.add(northPanel,BorderLayout.NORTH);

    add(content);

    if (dayProgram != null) {
      if (new devplugin.Date().equals(dayProgram.getDate())) {
        dayProgram.markProgramsOnAir();
        timer.start();
      }
    }

    this.validate();

  }

  public void updateBackground() {
    centerPanel.update(Settings.getTableSkin(),Settings.getTableBGMode());
  }


	public void paintComponent(Graphics g) {
		stateChanged(null);
	}

  private void drag(Point p)
  {
    isDragging=true;
    draggingPoint=p;
    viewPoint=scrollPane.getViewport().getViewPosition();
  }

  private ProgramDayTime getProgramDayTime(Point p) {
    /* ACHTUNG: Point p wird dabei so veraendert, dass p fuer den
        Aufruf in ProgramDayTime verwendet werden kann. */

    Component c=scrollPane.getComponentAt(p);
    if (c==null) return null;

    p.x-=c.getX();
    p.y-=c.getY();

    Component c2=c.getComponentAt(p);
    if (c2==null) return null;

    p.x=p.x-c2.getX();
    p.y=p.y-c2.getY();

    Component c3=c2.getComponentAt(p);
    if (c3==null) return null;

    p.x-=c3.getX();
    p.y-=c3.getY();

    return (ProgramDayTime)c3;
  }

  private devplugin.Program getProgram(Point p) {
    ProgramDayTime pdt=getProgramDayTime(p);
    if (pdt!=null) return pdt.getProgramAt(p);
    return null;
  }


  /**
   * interface ActionListener
   */
  public void actionPerformed(ActionEvent event) {
    if (dayProgram != null) {
      dayProgram.markProgramsOnAir();
    }
  }


  /**
   * interface ChangeListener
   */
  public void stateChanged(ChangeEvent e) {

    Point p=scrollPane.getViewport().getViewPosition();
    channelPanel.scroll(p.x);
  //  System.out.println(p.y);
  }

  /**
   * interface ScrollableTablePanel
   */
  public void scrollTo(int hour) {	
	int c=(int)(NUM_OF_DAYTIMES*hour/24.0);
 	
	int posY=0;
	int posX=(int)scrollPane.getViewport().getViewPosition().getX();
    for (int i=0;i<c;i++) {
    	posY+=part[i].getHeight();
    	System.out.print("*"+part[i].getHeight()+"*");
    }
    System.out.println();
    int s=hour%(24/NUM_OF_DAYTIMES);   
    
    posY+=part[c].getHeight()/NUM_OF_DAYTIMES*s - scrollPane.getHeight()/2;
	scrollPane.getViewport().setViewPosition(new Point(posX,posY));      
  
  }



  /**
   * interface ScrollableTablePanel
   */
  public void scrollTo(Channel ch) {
    //int pos=ch.getPos();
    int pos=ChannelList.getPos(ch.getId());
    
    int y=(int)scrollPane.getViewport().getViewPosition().getY();

    int x=centerPanel.getWidth()/ChannelList.getNumberOfSubscribedChannels()*pos;
    int maxX=centerPanel.getWidth()-scrollPane.getViewport().getWidth();
    if (x>centerPanel.getWidth()-scrollPane.getViewport().getWidth()) {
      x=maxX;
    }
    scrollPane.getViewport().setViewPosition(new Point(x,y));
  }

  /**
   * interface ScrollableTablePanel (currently unused)
   */
  public void up() {
    Point pos=scrollPane.getViewport().getViewPosition();
    int height=scrollPane.getHeight()*1/2;

    pos.y=pos.y-height;
    if (pos.y<0) pos.y=0;

    scrollPane.getViewport().setViewPosition(pos);

  }

  /**
   * interface ScrollableTablePanel (currently unused)
   */
  public void down() {
    Point pos=scrollPane.getViewport().getViewPosition();
    int height=scrollPane.getHeight()*1/2;
    int maxY=centerPanel.getHeight()-scrollPane.getViewport().getHeight();

    pos.y=pos.y+height;
    if (pos.y>maxY) pos.y=maxY;

    scrollPane.getViewport().setViewPosition(pos);
  }

  /**
   * interface ScrollableTablePanel (currently unused)
   */
  public void left() {
    Point pos=scrollPane.getViewport().getViewPosition();
    int width=Settings.getColumnWidth();
    pos.x=pos.x-width;
    if (pos.x<0) pos.x=0;

    scrollPane.getViewport().setViewPosition(pos);
  }

  /**
   * interface ScrollableTablePanel (currently unused)
   */
  public void right() {
    Point pos=scrollPane.getViewport().getViewPosition();
    int width=Settings.getColumnWidth();
    int maxX=centerPanel.getWidth()-scrollPane.getViewport().getWidth();
    pos.x=pos.x+width;
    if (pos.x>maxX) pos.x=maxX;

    scrollPane.getViewport().setViewPosition(pos);
  }


  /**
   * interface MouseInputListener
   */
  public void mouseDragged(MouseEvent e) {

    Point newPoint=e.getPoint();
    int dx=draggingPoint.x-newPoint.x+viewPoint.x;
    int dy=draggingPoint.y-newPoint.y+viewPoint.y;

    if (dx<0) { dx=0; }
    if (dy<0) { dy=0; }

    Dimension viewDim=scrollPane.getViewport().getSize();
    Dimension clientDim=centerPanel.getSize();

    int a=clientDim.width-viewDim.width;
    int b=clientDim.height-viewDim.height;

    if (dx>a) { dx=a; }
    if (dy>b) { dy=b; }

    scrollPane.getViewport().setViewPosition(new Point(dx,dy));

  }

  /**
   * interface MouseInputListener
   */
  public void mouseMoved(MouseEvent e) {}

  /**
   * interface MouseInputListener
   */
  public void mouseEntered(MouseEvent e) {}

  /**
   * interface MouseInputListener
   */
  public void mouseClicked(MouseEvent e) {
    Point pos=scrollPane.getViewport().getViewPosition();
    Point p=new Point(e.getX(),e.getY());
    if (SwingUtilities.isRightMouseButton(e)) {
      devplugin.Program prog=getProgram(p);
      if (prog==null) return;
      contextMenu.setProgram(prog);
      contextMenu.show(e.getComponent(),e.getX()-15,e.getY()-15);
    }
  }

  /**
   * interface MouseInputListener
   */
  public void mouseExited(MouseEvent e) {}

  /**
   * interface MouseInputListener
   */
  public void mousePressed(MouseEvent e) {
    drag(e.getPoint());
  }

  /**
   * interface MouseInputListener
   */
  public void mouseReleased(MouseEvent e) {
    if (!isDragging) {
      contextMenu.show(e.getComponent(),e.getX(),e.getY());
    }
    isDragging=false;
  }

}