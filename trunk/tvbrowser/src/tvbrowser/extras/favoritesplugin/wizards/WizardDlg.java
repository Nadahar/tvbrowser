package tvbrowser.extras.favoritesplugin.wizards;

import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.Borders;


public class WizardDlg extends JDialog implements WindowClosingIf {

  public static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(WizardDlg.class);


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

  public WizardDlg(Dialog parent, WizardHandler handler, WizardStep step) {
    super(parent, true);
    init(step, handler);
  }

  public WizardDlg(Frame parent, WizardHandler handler, WizardStep step) {
    super(parent, true);
    init(step, handler);
  }

  private void init(WizardStep step, WizardHandler handler) {
    setSize(500,200);
    UiUtilities.registerForClosing(this);
    mResult = CANCEL;
    mHandler = handler;

    getContentPane().setLayout(new BorderLayout());



    switchToStep(step);

  }

  private JPanel createButtonPanel(int[] btns) {

    mDoneBtn = new JButton(mLocalizer.msg("done","Done"));
    mCancelBtn = new JButton(mLocalizer.msg("cancel","Cancel"));
    mNextBtn = new JButton(mLocalizer.msg("next","Next")+" >>");
    mBackBtn = new JButton("<< " + mLocalizer.msg("back","Back"));

    FormLayout layout = new FormLayout("fill:pref:grow, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "pref");
    layout.setColumnGroups(new int[][] { { 2, 4, 6, 8} });
    JPanel buttonPanel = new JPanel(layout);

    buttonPanel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    for (int i=0; i<btns.length; i++) {
      int p = 10 - 2*btns.length  + i*2;

      if (btns[i] == WizardStep.BUTTON_DONE) {
        buttonPanel.add(mDoneBtn, cc.xy(p, 1));
      }
      else if (btns[i] == WizardStep.BUTTON_CANCEL) {
        buttonPanel.add(mCancelBtn, cc.xy(p, 1));
      }
      else if (btns[i] == WizardStep.BUTTON_BACK) {
        buttonPanel.add(mBackBtn, cc.xy(p, 1));
      }
      else if (btns[i] == WizardStep.BUTTON_NEXT) {
        buttonPanel.add(mNextBtn, cc.xy(p, 1));
      }
    }

    mDoneBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if (mStep.isValid()) {
          mDataObject = mStep.createDataObject(mDataObject);
          close(FINISH);
        }
      }
    });

    mCancelBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });


    mNextBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if (mStep.isValid()) {
          mDataObject = mStep.createDataObject(mDataObject);
          switchToStep(mStep.next());
        }
      }
    });

    mBackBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        switchToStep(mStep.back());
      }
    });

    return buttonPanel;

  }


  private void switchToStep(WizardStep step) {
    mStep = step;
    setTitle(step.getTitle());
    if (mCurrentContentPanel != null) {
      getContentPane().remove(mCurrentContentPanel);
    }
    if (mButtonPanel != null) {
      getContentPane().remove(mButtonPanel);
    }
    mCurrentContentPanel = mStep.getContent(mHandler);
    mCurrentContentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    getContentPane().add(mCurrentContentPanel, BorderLayout.CENTER);
    mCurrentContentPanel.validate();
    mCurrentContentPanel.updateUI();

    mButtonPanel = createButtonPanel(step.getButtons());
    getContentPane().add(BorderLayout.SOUTH, mButtonPanel);
    getContentPane().validate();
  }

  public int getResult() {
    return mResult;
  }

  public Object getDataObject() {
    return mDataObject;
  }

  public void close() {
    hide();
  }

  public void close(int val) {
    mResult = val;
    hide();
  }

  public void allowNext(boolean allow) {
    mNextBtn.setEnabled(allow);
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

}
