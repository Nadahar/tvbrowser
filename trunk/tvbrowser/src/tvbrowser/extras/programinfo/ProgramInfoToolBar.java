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
package tvbrowser.extras.programinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

import tvbrowser.core.Settings;
import util.io.IOUtilities;
import util.ui.ObjectSelectionButton;
import util.ui.ObjectSelectionListener;
import util.ui.TVBrowserIcons;

/**
 * A tool bar panel for the program info dialog.
 * 
 * @author René Mach
 */
public class ProgramInfoToolBar extends JPanel implements ObjectSelectionListener<Program> {
  private JButton mHistPrevious;
  private JButton mHistNext;
  private JButton mPrevious;
  private JButton mNext;
  private JLabel mCurrent;
  private ObjectSelectionButton<Program> mPreviousSelection;
  private ObjectSelectionButton<Program> mNextSelection;
  
  public ProgramInfoToolBar() {
    setLayout(new FormLayout("0dlu,default,3dlu,default,10dlu,default:grow,0dlu","default,3dlu"));
    
    MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        if(e.getComponent() instanceof JButton) {
          ((JButton)e.getComponent()).setContentAreaFilled(false);
        }
      }
      
      @Override
      public void mouseEntered(MouseEvent e) {
        if(e.getComponent() instanceof JButton && e.getComponent().isEnabled()) {
          ((JButton)e.getComponent()).setContentAreaFilled(true);
        }
      }
    };
    
    mCurrent = new JLabel() {
      public void paintComponent(Graphics g) {
        paintProgress(ProgramInfoDialog.getCurrentProgram(), this, g);        
        super.paintComponent(g);
      }
    };
    mCurrent.setHorizontalTextPosition(JLabel.CENTER);
    mCurrent.setHorizontalAlignment(JLabel.CENTER);
    mCurrent.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, UIManager.getColor("Label.foreground")));
    
    mHistPrevious = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    mHistPrevious.setEnabled(false);
    mHistPrevious.addActionListener(new ActionListener() {  
      @Override
      public void actionPerformed(ActionEvent arg0) {
        ProgramInfo.getInstance().historyBack();
      }
    });
    
    mHistNext = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    mHistNext.setEnabled(false);
    mHistNext.addActionListener(new ActionListener() {      
      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().historyForward();
      }
    });
    
    mPreviousSelection = new ObjectSelectionButton<Program>(null, TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    mPreviousSelection.addProgramSelectionListener(this);
    mPreviousSelection.setEnabled(false);
    mPreviousSelection.setContentAreaFilled(false);
    mPreviousSelection.setBorder(BorderFactory.createEmptyBorder());
    mPreviousSelection.setOpaque(false);
    mPreviousSelection.addMouseListener(mouseAdapter);
    
    mNextSelection = new ObjectSelectionButton<Program>(null, TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    mNextSelection.addProgramSelectionListener(this);
    mNextSelection.setEnabled(false);
    mNextSelection.setContentAreaFilled(false);
    mNextSelection.setBorder(BorderFactory.createEmptyBorder());
    mNextSelection.setOpaque(false);
    mNextSelection.addMouseListener(mouseAdapter);
    
    mPrevious = new JButton() {
      public void paintComponent(Graphics g) {
        Program[] previous = ProgramInfo.getInstance().getPreviousPrograms();

        if(previous != null && previous.length > 0) {
          paintProgress(previous[0], this, g);
        }
        
        super.paintComponent(g);
      }
    };
    mPrevious.setForeground(UIManager.getColor("Label.foreground"));
    mPrevious.setContentAreaFilled(false);
    mPrevious.setBorder(BorderFactory.createEmptyBorder());
    mPrevious.setOpaque(false);
    mPrevious.addMouseListener(mouseAdapter);
    
    mPrevious.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().previousProgram();
      }
    });
    
    mNext = new JButton() {
      public void paintComponent(Graphics g) {
        Program[] next = ProgramInfo.getInstance().getNextPrograms();

        if(next != null && next.length > 0) {
          paintProgress(next[0], this, g);
        }
        
        super.paintComponent(g);
      }
    };
    
    mNext.setForeground(UIManager.getColor("Label.foreground"));
    mNext.setContentAreaFilled(false);
    mNext.setBorder(BorderFactory.createEmptyBorder());
    mNext.setOpaque(false);
    mNext.addMouseListener(mouseAdapter);
    mNext.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().nextProgram();
      }
    });
    
    CellConstraints cc = new CellConstraints();
    
    JPanel selection = new JPanel(new FormLayout("default,1dlu,100dlu,1dlu,100dlu:grow,1dlu,100dlu,1dlu,default","default"));
    selection.setBackground(UIManager.getColor("TextField.background"));
    selection.setBorder(BorderFactory.createCompoundBorder(UIManager.getBorder("TextField.border"), BorderFactory.createEmptyBorder(3,3,3,3)));
    
    selection.add(mPreviousSelection, cc.xy(1, 1));
    selection.add(mPrevious, cc.xy(3, 1));
    selection.add(mCurrent, cc.xy(5, 1));
    selection.add(mNext, cc.xy(7, 1));
    selection.add(mNextSelection, cc.xy(9, 1));
    
    add(mHistPrevious, cc.xy(2, 1));
    add(mHistNext, cc.xy(4, 1));
    add(selection, cc.xy(6, 1));
  }

  @Override
  public void objectSeleted(Program prog) {
    ProgramInfo.getInstance().showProgram(prog);
  }
  
  public void update() {
    Program current = ProgramInfoDialog.getCurrentProgram();
    Program[] previous = ProgramInfo.getInstance().getPreviousPrograms();
    Program[] next = ProgramInfo.getInstance().getNextPrograms();
    
    if(current != null) {
      mCurrent.setText(current.getTitle());
      
      if(current.isExpired()) {
        mCurrent.setForeground(Color.lightGray);
      }
      else {
        mCurrent.setForeground(UIManager.getColor("Label.foreground"));
      }
    }
    
    if(previous != null && previous.length > 0) {
      mPrevious.setText(previous[0].getTitle());
      
      if(previous[0].isExpired()) {
        mPrevious.setForeground(Color.lightGray);
      }
      else {
        mPrevious.setForeground(UIManager.getColor("Label.foreground"));
      }
    }
    else {
      mPrevious.setText("");;
    }
    
    if(next != null && next.length > 0) {
      mNext.setText(next[0].getTitle());
      
      if(next[0].isExpired()) {
        mNext.setForeground(Color.lightGray);
      }
      else {
        mNext.setForeground(UIManager.getColor("Label.foreground"));
      }
    }
    else {
      mNext.setText("");
    }
    
    mHistPrevious.setEnabled(ProgramInfo.getInstance().canNavigateBack());
    mHistPrevious.setToolTipText(ProgramInfo.getInstance().navigationBackwardText());
    mHistNext.setEnabled(ProgramInfo.getInstance().canNavigateForward());
    mHistNext.setToolTipText(ProgramInfo.getInstance().navigationForwardText());
    mPreviousSelection.setEnabled(previous != null && previous.length > 0);
    mPreviousSelection.setObjectArr(previous);
    mPrevious.setEnabled(previous != null && previous.length > 0);
    mNext.setEnabled(next != null && next.length > 0);
    mNextSelection.setEnabled(next != null && next.length > 0);
    mNextSelection.setObjectArr(next);
    
    updateUI();
  }
  
  private void paintProgress(Program program, JComponent component, Graphics g) {
    if(program.isOnAir()) {
      g.setColor(Color.white);
      g.fillRect(0, 1, component.getWidth(), component.getHeight()-2);
      

      int runTime = IOUtilities.getMinutesAfterMidnight() - program.getStartTime();
      if (runTime < 0) {
        runTime += 24 * 60;
      }
      int progressX = (int)((component.getWidth())/(double)program.getLength() * runTime);

      g.setColor(Settings.propProgramTableColorOnAirDark.getColor());
      g.fillRect(0,1,progressX,component.getHeight()-2);

      g.setColor(Settings.propProgramTableColorOnAirLight.getColor());
      g.fillRect(0 + progressX,1,component.getWidth()-progressX,component.getHeight()-2);
      
      component.setForeground(Color.black);
    }
  }
}
