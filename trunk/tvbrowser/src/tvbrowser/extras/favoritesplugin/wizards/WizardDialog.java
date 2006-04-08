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


public class WizardDialog extends JDialog implements WindowClosingIf {

  public static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(WizardDialog.class);


  public static final int CANCEL = 0;
  public static final int NEXT = 1;
  public static final int FINISH = 2;

  private int mResult;
  private WizardStep mStep;

  private JButton mNextBtn;
  private JButton mDoneBtn;
  private JButton mCancelBtn;

  public WizardDialog(Frame parent, WizardHandler handler, WizardStep step) {
    super(parent, true);
    init(handler, step);
  }

  public WizardDialog(Dialog parent, WizardHandler handler, WizardStep step) {
    super(parent, true);
    init(handler, step);
  }

  private void init(WizardHandler handler, WizardStep step) {
    setSize(500,200);
    UiUtilities.registerForClosing(this);
    mStep = step;
    mResult = CANCEL;
    setTitle(step.getTitle());

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(BorderLayout.SOUTH, getButtonPanel());

    JPanel content = step.getContent(handler);
    content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    getContentPane().add(BorderLayout.CENTER, content);

   // pack();
  }

  private JPanel getButtonPanel() {

    mDoneBtn = new JButton(mLocalizer.msg("done","Done"));
    mCancelBtn = new JButton(mLocalizer.msg("cancel","Cancel"));
    mNextBtn = new JButton(mLocalizer.msg("next","Next")+" >>");

    FormLayout layout = new FormLayout("fill:pref:grow, pref, 3dlu, pref, 3dlu, pref", "pref");
    layout.setColumnGroups(new int[][] { { 2, 4, 6 } });
    JPanel buttonPanel = new JPanel(layout);

    buttonPanel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    int[] btns = mStep.getButtons();
    for (int i=0; i<btns.length; i++) {
      int p = 8 - 2*btns.length  + i*2;

      if (btns[i] == WizardStep.BUTTON_DONE) {
        buttonPanel.add(mDoneBtn, cc.xy(p, 1));
      }
      else if (btns[i] == WizardStep.BUTTON_CANCEL) {
        buttonPanel.add(mCancelBtn, cc.xy(p, 1));
      }
      else if (btns[i] == WizardStep.BUTTON_NEXT) {
        buttonPanel.add(mNextBtn, cc.xy(p, 1));
      }
    }

    mDoneBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if (mStep.isValid()) {
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
          close(NEXT);
        }
      }
    });

    return buttonPanel;

  }

  public void close() {
    close(CANCEL);
  }

  public void close(int val) {
    mResult = val;
    hide();
  }


  public int getResult() {
    return mResult;
  }

  public void allowNext(boolean allow) {
    mNextBtn.setEnabled(allow);
  }

  public void allowFinish(boolean allow) {
    mDoneBtn.setEnabled(allow);
  }

  public void allowCancel(boolean allow) {
    mCancelBtn.setEnabled(allow);
  }

}
