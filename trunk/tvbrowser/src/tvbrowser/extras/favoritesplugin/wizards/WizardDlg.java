package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

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

  public WizardDlg(Dialog parent, WizardHandler handler, WizardStep step) {
    super(parent, true);
    init(step, handler);
  }

  public WizardDlg(Frame parent, WizardHandler handler, WizardStep step) {
    super(parent, true);
    init(step, handler);
  }

  private void init(WizardStep step, WizardHandler handler) {
    setSize(Sizes.dialogUnitXAsPixel(300, this), Sizes.dialogUnitYAsPixel(200, this));
    UiUtilities.registerForClosing(this);
    mResult = CANCEL;
    mHandler = handler;

    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new FormLayout("fill:pref:grow", "fill:pref:grow, 3dlu, bottom:pref"));
    panel.setBorder(Borders.DLU4_BORDER);

    switchToStep(step);
  }

  private JPanel createButtonPanel(int[] btns) {
    mDoneBtn = new JButton(mLocalizer.msg("done", "Done"));
    mDoneBtn.setEnabled(false);
    mCancelBtn = new JButton(mLocalizer.msg("cancel", "Cancel"));
    mCancelBtn.setEnabled(false);
    mNextBtn = new JButton(mLocalizer.msg("next", "Next") + " >>");
    mNextBtn.setEnabled(false);
    mBackBtn = new JButton("<< " + mLocalizer.msg("back", "Back"));
    mBackBtn.setEnabled(false);

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();

    builder.addGriddedButtons(new JButton[] { mBackBtn, mNextBtn, mDoneBtn, mCancelBtn });

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

    return builder.getPanel();
  }

  private void switchToStep(WizardStep step) {
    mStep = step;
    setTitle(step.getTitle());
    getContentPane().removeAll();

    CellConstraints cc = new CellConstraints();

    mCurrentContentPanel = mStep.getContent(mHandler);
    getContentPane().add(mCurrentContentPanel, cc.xy(1, 1));
    mCurrentContentPanel.validate();
    mCurrentContentPanel.updateUI();

    mButtonPanel = createButtonPanel(step.getButtons());
    getContentPane().add(mButtonPanel, cc.xy(1, 3));
    getContentPane().validate();
    ((JPanel)getContentPane()).updateUI();
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
