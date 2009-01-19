/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

public class WizardDlg extends JDialog implements WindowClosingIf {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(WizardDlg.class);

  public static final int CANCEL = 0;

  public static final int NEXT = 1;

  public static final int FINISH = 2;

  private int mResult;

  private WizardStep mStep;

  private JButton mNextBtn;

  private JButton mBackBtn;

  private JButton mDoneBtn;

  private JButton mCancelBtn;

  private JPanel mCurrentContentPanel;

  private JPanel mButtonPanel;

  private WizardHandler mHandler;

  private Object mDataObject;

  public WizardDlg(Window parent, WizardHandler handler, WizardStep step) {
    super(parent);
    setModal(true);
    init(step, handler);
  }

  private void init(WizardStep step, WizardHandler handler) {
    setSize(Sizes.dialogUnitXAsPixel(330, this), Sizes.dialogUnitYAsPixel(190, this));
    UiUtilities.registerForClosing(this);
    mResult = CANCEL;
    mHandler = handler;

    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new FormLayout("fill:default:grow", "fill:pref:grow, 3dlu, bottom:pref"));
    panel.setBorder(Borders.DLU4_BORDER);

    switchToStep(step);
  }

  private JPanel createButtonPanel(int[] btns) {
    mDoneBtn = new JButton(mLocalizer.msg("done", "Done"));
    mDoneBtn.setEnabled(false);
    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.setEnabled(false);
    mNextBtn = new JButton(Localizer.getLocalization(Localizer.I18N_NEXT) + " >>");
    mNextBtn.setEnabled(false);
    mBackBtn = new JButton("<< " + Localizer.getLocalization(Localizer.I18N_BACK));
    mBackBtn.setEnabled(false);

    JPanel panel = new JPanel(new FormLayout("fill:pref:grow, pref, 3dlu, pref", "pref"));
    CellConstraints cc = new CellConstraints();
    
    if (!mStep.isSingleStep()) {
      FormLayout layout = new FormLayout("pref, 3dlu, pref", "pref");
      layout.setColumnGroups(new int[][] { { 1, 3 } });
      JPanel nextpanel = new JPanel(layout);
      nextpanel.add(mBackBtn, cc.xy(1,1));
      nextpanel.add(mNextBtn, cc.xy(3,1));
      panel.add(nextpanel, cc.xy(2, 1));
    }
    
    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGriddedGrowing(mDoneBtn);
    builder.addRelatedGap();
    builder.addFixed(mCancelBtn);
    panel.add(builder.getPanel(), cc.xy(4, 1));
    
    for (int i = 0; i < btns.length; i++) {
      if (btns[i] == WizardStep.BUTTON_DONE) {
        mDoneBtn.setEnabled(true);
      } else if (btns[i] == WizardStep.BUTTON_BACK) {
        mBackBtn.setEnabled(true);
      } else if (btns[i] == WizardStep.BUTTON_NEXT) {
        mNextBtn.setEnabled(true);
      } else if (btns[i] == WizardStep.BUTTON_CANCEL) {
        mCancelBtn.setEnabled(true);
      }
    }

    mDoneBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mStep.isValid()) {
          mDataObject = mStep.createDataObject(mDataObject);
          close(FINISH);
        }
      }
    });

    mCancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    mNextBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mStep.isValid()) {
          mDataObject = mStep.createDataObject(mDataObject);
          switchToStep(mStep.next());
        }
      }
    });

    mBackBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchToStep(mStep.back());
      }
    });

    if(mNextBtn.isEnabled() && !mStep.isSingleStep())
      getRootPane().setDefaultButton(mNextBtn);
    else
      getRootPane().setDefaultButton(mDoneBtn);
    
    AbstractAction a = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if(mBackBtn.isEnabled()) {
          mBackBtn.dispatchEvent(new KeyEvent(mBackBtn, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_SPACE, ' '));
          mBackBtn.dispatchEvent(new KeyEvent(mBackBtn, KeyEvent.KEY_RELEASED, 0, 0, KeyEvent.VK_SPACE, ' '));
        }
      }
    };
    
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
    getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(stroke,"BACK");
    getRootPane().getActionMap().put("BACK", a);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.ALT_MASK);
    getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(stroke,"BACK");
    getRootPane().getActionMap().put("BACK", a);
    
    return panel;
  }

  private void switchToStep(WizardStep step) {
    mStep = step;
    setTitle(step.getTitle());
    getContentPane().removeAll();

    CellConstraints cc = new CellConstraints();

    mCurrentContentPanel = mStep.getContent(mHandler);
    getContentPane().add(mCurrentContentPanel, cc.xy(1, 1));
    mCurrentContentPanel.validate();
    mCurrentContentPanel.repaint();

    mButtonPanel = createButtonPanel(step.getButtons());
    getContentPane().add(mButtonPanel, cc.xy(1, 3));
    getContentPane().validate();
    ((JPanel)getContentPane()).repaint();
    mCurrentContentPanel.requestFocusInWindow();
  }

  public int getResult() {
    return mResult;
  }

  public Object getDataObject() {
    return mDataObject;
  }

  public void close() {
    setVisible(false);
  }

  public void close(int val) {
    mResult = val;
    close();
  }

  public void allowNext(boolean allow) {
    mNextBtn.setEnabled(allow);
    
    if(allow && !mStep.isSingleStep())
      getRootPane().setDefaultButton(mNextBtn);
    else
      getRootPane().setDefaultButton(mDoneBtn);
  }

  public void allowFinish(boolean allow) {
    mDoneBtn.setEnabled(allow);
  }

  public void allowBack(boolean allow) {
    mBackBtn.setEnabled(allow);
  }

  public void allowCancel(boolean allow) {
    mCancelBtn.setEnabled(allow);
  }

  public void setDoneBtnText() {
    mDoneBtn.setText(mStep.getDoneBtnText());
  }

  public void focusFinish() {
    mDoneBtn.requestFocusInWindow();
  }
}
