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

package tvbrowser.ui.programtable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;

import util.exc.TvBrowserException;
import util.ui.ProgramPanel;

import tvbrowser.core.*;
import tvbrowser.ui.SkinPanel;
import tvbrowser.ui.ContextMenu;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
class ProgramDayTime extends JPanel {

  private JPanel centerPanel;
  private JLabel statusLabel;
  private MouseMotionListener mouseMotionListener;
  private ProgramColumn[] cols;


  public ProgramDayTime(int numOfCols) {
    setLayout(new BorderLayout(0,0));

    centerPanel=new JPanel(new GridLayout(1,0,0,0));

	add(centerPanel,BorderLayout.WEST);

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

  public void addProgram(Program prog) {
    int col = ChannelList.getPos(prog.getChannel());

    if (col<0) {
      return; // cannot add program (channel is not subscribed)
    }
    ProgramPanel panel = new ProgramPanel(prog);
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

public class ProgramTablePanel extends JComponent implements MouseInputListener, ScrollableTablePanel, ActionListener, KeyListener {

  private static int NUM_OF_DAYTIMES=6;

  private ProgramDayTime part[];
  private SkinPanel centerPanel;
  private JScrollPane scrollPane=null;

  private boolean isDragging;
  private Point draggingPoint, viewPoint;
  private ContextMenu contextMenu;

  private ChannelPanel channelPanel;
  private ChannelChooser channelChooser;
  private JPanel content;
  private JLabel statusLabel;
  private DayProgram dayProgram;
  private Frame parent;

  private javax.swing.Timer timer;

  private JViewport headerPanel;

  /**
   * Constructs a new ProgramTablePanel with the specified parent frame.
   */
  public ProgramTablePanel(Frame parent) {

	this.parent=parent;
    setLayout(new BorderLayout());
    final ScrollableTablePanel tablePanel=this;
    part=new ProgramDayTime[NUM_OF_DAYTIMES];


	contextMenu=DataService.getInstance().createPluginContextMenu(parent);


    timer=new javax.swing.Timer(10000,this);
    channelChooser=new ChannelChooser(parent,this);
    channelPanel=new ChannelPanel();
    
	addKeyListener(this);
    
	setFocusable(true);
	
    
  }

  public void setPluginContextMenu(ContextMenu menu) {
	 contextMenu=menu;
   }

   public void subscribedChannelsChanged() {
   	if (channelPanel!=null && headerPanel!=null) {
   		headerPanel.remove(channelPanel);
   	}
	channelPanel=new ChannelPanel();


	channelChooser=new ChannelChooser(parent,this);

   }

  /**
   * Changes the dayprogram to prog and repaints the program table.
   */
  public void setDayProgram(DayProgram prog) throws TvBrowserException {

    if (timer.isRunning()) {
      timer.stop();
    }

    dayProgram = prog;

    // Remember the scroll position for a later restore
    Point oldViewPosition = null;
    if (scrollPane != null) {
      oldViewPosition = scrollPane.getViewport().getViewPosition();
    }

    if (scrollPane!=null) remove(scrollPane);
    if (centerPanel!=null) remove(centerPanel);
    if (content!=null) remove(content);

    content=new JPanel(new BorderLayout());

    centerPanel=new SkinPanel(Settings.getTableSkin(),Settings.getTableBGMode());
    centerPanel.setColDiff(Settings.getColumnWidth());

    centerPanel.setLayout(new BorderLayout());
    centerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

    scrollPane=new JScrollPane(centerPanel);
    scrollPane.addMouseMotionListener(this);
    scrollPane.addMouseListener(this);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(30);
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);
    
	
    

    /*JViewport*/ headerPanel=new JViewport();

    headerPanel.add(channelPanel);
    scrollPane.setColumnHeader(headerPanel);

    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER,channelChooser);

    for (int i=0;i<part.length;i++) {
      part[i]=new ProgramDayTime(ChannelList.getNumberOfSubscribedChannels());
    }

    ProgramPanelFactory.reset();

    if (dayProgram != null) {
      Iterator iterator=dayProgram.iterator();
      ChannelDayProgram curDayProgram;

      Iterator progs;
      Program curProgram;
      int h;

      while (iterator.hasNext()) {
        curDayProgram = (ChannelDayProgram) iterator.next();

        progs = curDayProgram.getPrograms();

        while (progs.hasNext()) {
          curProgram = (Program) progs.next();
          h=curProgram.getHours();
          int inx=h/((24/part.length));
          part[inx].addProgram(curProgram);
        }
      }
    }

    centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.Y_AXIS));
    for (int i=0;i<part.length;i++) {
		centerPanel.add(part[i]);
    }


    content.add(scrollPane,BorderLayout.CENTER);

    add(content);

    if (dayProgram != null) {
      if (new devplugin.Date().equals(dayProgram.getDate())) {
        dayProgram.markProgramsOnAir();
        timer.start();
      }
    }

    this.validate();

    // Restore the scroll position
    if (oldViewPosition != null) {
      scrollPane.getViewport().setViewPosition(oldViewPosition);
    }
  }

  public void updateBackground() {
    centerPanel.update(Settings.getTableSkin(),Settings.getTableBGMode());
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
   * interface ScrollableTablePanel
   */
  public void scrollTo(int hour) {
	int c=(int)(NUM_OF_DAYTIMES*hour/24.0);

	int posY=0;
	int posX=(int)scrollPane.getViewport().getViewPosition().getX();
    for (int i=0;i<c;i++) {
    	posY+=part[i].getHeight();
    }
    int s=hour%(24/NUM_OF_DAYTIMES);

    posY+=part[c].getHeight()/NUM_OF_DAYTIMES*s - scrollPane.getHeight()/2;
    if (posY<0) {
    	posY=0;
    }
	scrollPane.getViewport().setViewPosition(new Point(posX,posY));
  }



  /**
   * interface ScrollableTablePanel
   */
  public void scrollTo(Channel ch) {
    int pos=ChannelList.getPos(ch);

    int y=(int)scrollPane.getViewport().getViewPosition().getY();

    int x=centerPanel.getWidth()/ChannelList.getNumberOfSubscribedChannels()*pos;
    int maxX=centerPanel.getWidth()-scrollPane.getViewport().getWidth();
    if (x>centerPanel.getWidth()-scrollPane.getViewport().getWidth()) {
      x=maxX;
    }
    scrollPane.getViewport().setViewPosition(new Point(x,y));
  }

  /**
   * interface ScrollableTablePanel 
   */
  public void up(int step) {
    Point pos=scrollPane.getViewport().getViewPosition();
    if (step<0) {
    	step=scrollPane.getHeight()*3/4;
    }
    
    pos.y=pos.y-step;
    if (pos.y<0) pos.y=0;

    scrollPane.getViewport().setViewPosition(pos);

  }

  /**
   * interface ScrollableTablePanel 
   */
  public void down(int step) {
    Point pos=scrollPane.getViewport().getViewPosition();
    if (step<0) {
			step=scrollPane.getHeight()*3/4;
		}
    int maxY=centerPanel.getHeight()-scrollPane.getViewport().getHeight();

    pos.y=pos.y+step;
    if (pos.y>maxY) pos.y=maxY;

    scrollPane.getViewport().setViewPosition(pos);
  }

  /**
   * interface ScrollableTablePanel 
   */
  public void left(int step) {
    Point pos=scrollPane.getViewport().getViewPosition();
    if (step<0) {
			step=scrollPane.getWidth()*3/4;
		}
    pos.x=pos.x-step;
    if (pos.x<0) pos.x=0;

    scrollPane.getViewport().setViewPosition(pos);
  }

  /**
   * interface ScrollableTablePanel 
   */
  public void right(int step) {
    Point pos=scrollPane.getViewport().getViewPosition();
    if (step<0) {
		step=scrollPane.getWidth()*3/4;
	}
    int maxX=centerPanel.getWidth()-scrollPane.getViewport().getWidth();
    pos.x=pos.x+step;
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
  public void mouseClicked(MouseEvent evt) {
  	requestFocus();
	Point pos = scrollPane.getViewport().getViewPosition();
    Point pnt = new Point(evt.getX(), evt.getY());
    devplugin.Program program = getProgram(pnt);
    if (SwingUtilities.isRightMouseButton(evt)) {
      if (program != null) {
        // this is a right click -> Show the context menu
        contextMenu.setProgram(program);
        contextMenu.show(evt.getComponent(), evt.getX() - 15, evt.getY() - 15);
      }
    }
    else if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
      if (program != null) {
        // This is a left double click
        // -> Execute the program using the first plugin that is in the context menu
        devplugin.Plugin[] instPluginArr = PluginManager.getInstalledPlugins();
        for (int i = 0; i < instPluginArr.length; i++) {
          if (instPluginArr[i].getContextMenuItemText() != null) {
            instPluginArr[i].execute(program);
            break;
          }
        }
      }
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
  
  /**
   * interface KeyListener
   *
   */

  public void keyPressed(KeyEvent e) {
  	
  	if (e.getKeyCode()==KeyEvent.VK_UP) {
  		if (e.isShiftDown()) up(50); else up(10);
  	}
  	else if (e.getKeyCode()==KeyEvent.VK_DOWN) {
		if (e.isShiftDown()) down(50); else down(10);
  	}
  	else if (e.getKeyCode()==KeyEvent.VK_LEFT) {
		if (e.isShiftDown()) left(50); else left(10);
  	}
  	else if (e.getKeyCode()==KeyEvent.VK_RIGHT) {
		if (e.isShiftDown()) right(50); else right(10);
  	}
  	
  	
  }
		  
  public void keyReleased(KeyEvent e) {
  	
  }
		   

  public void keyTyped(KeyEvent e) {
  	
  }


}