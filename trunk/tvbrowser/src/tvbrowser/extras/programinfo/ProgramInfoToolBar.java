package tvbrowser.extras.programinfo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

import util.ui.ProgramSelectionButton;
import util.ui.ProgramSelectionListener;
import util.ui.TVBrowserIcons;

public class ProgramInfoToolBar extends JPanel implements ProgramSelectionListener {
  private JButton mHistPrevious;
  private JButton mHistNext;
  private JButton mPrevious;
  private JButton mNext;
  private JLabel mCurrent;
  private ProgramSelectionButton mPreviousSelection;
  private ProgramSelectionButton mNextSelection;
  
  public ProgramInfoToolBar() {
    setLayout(new FormLayout("0dlu,default,3dlu,default,10dlu,default:grow,0dlu","default,3dlu"));
    
    mCurrent = new JLabel();
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
    
    mPreviousSelection = new ProgramSelectionButton(null, TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    mPreviousSelection.addProgramSelectionListener(this);
    mPreviousSelection.setEnabled(false);
    mPreviousSelection.setContentAreaFilled(false);
    mPreviousSelection.setBorder(BorderFactory.createEmptyBorder());
    mPreviousSelection.setOpaque(false);
    
    mNextSelection = new ProgramSelectionButton(null, TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    mNextSelection.addProgramSelectionListener(this);
    mNextSelection.setEnabled(false);
    mNextSelection.setContentAreaFilled(false);
    mNextSelection.setBorder(BorderFactory.createEmptyBorder());
    mNextSelection.setOpaque(false);
    
    mPrevious = new JButton();
    mPrevious.setForeground(UIManager.getColor("Label.foreground"));
    mPrevious.setContentAreaFilled(false);
    mPrevious.setBorder(BorderFactory.createEmptyBorder());
    mPrevious.setOpaque(false);
    mPrevious.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().previousProgram();
      }
    });
    
    mNext = new JButton();
    mNext.setForeground(UIManager.getColor("Label.foreground"));
    mNext.setContentAreaFilled(false);
    mNext.setBorder(BorderFactory.createEmptyBorder());
    mNext.setOpaque(false);
    mNext.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().nextProgram();
      }
    });
    
    CellConstraints cc = new CellConstraints();
    
    JPanel selection = new JPanel(new FormLayout("default,1dlu,100dlu,1dlu,100dlu:grow,1dlu,100dlu,1dlu,default","default"));
    selection.setBackground(UIManager.getColor("TextField.background"));
    selection.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")), BorderFactory.createEmptyBorder(3,3,3,3)));
    
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
  public void programSeleted(Program prog) {
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
    if(next != null && next.length > 0) {
      mNext.setText(next[0].getTitle());
      
      if(next[0].isExpired()) {
        mNext.setForeground(Color.lightGray);
      }
      else {
        mNext.setForeground(UIManager.getColor("Label.foreground"));
      }
    }
    
    mHistPrevious.setEnabled(ProgramInfo.getInstance().canNavigateBack());
    mHistPrevious.setToolTipText(ProgramInfo.getInstance().navigationBackwardText());
    mHistNext.setEnabled(ProgramInfo.getInstance().canNavigateForward());
    mHistNext.setToolTipText(ProgramInfo.getInstance().navigationForwardText());
    mPreviousSelection.setEnabled(previous != null);
    mPreviousSelection.setProgramArr(previous);
    mPrevious.setEnabled(previous != null);
    mNext.setEnabled(next != null);
    mNextSelection.setEnabled(next != null);
    mNextSelection.setProgramArr(next);
  }
}
