package captureplugin.drivers.elgatodriver;

import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import captureplugin.drivers.utils.TimeDateChooserPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;

public class TimeChooserDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer
          .getLocalizerFor(TimeChooserDialog.class);
  
  private int mButtonPressed = JOptionPane.CANCEL_OPTION;
  private Program mProgram;
  private TimeDateChooserPanel mTimePanel;
  
  public TimeChooserDialog(JDialog dialog, Program program) {
    super(dialog, true);
    mProgram = program;
    createGui();
  }

  public TimeChooserDialog(JFrame frame, Program program) {
    super(frame, true);
    mProgram = program;
    createGui();
  }

  private void createGui() {
    Calendar cal = mProgram.getDate().getCalendar();
    cal.set(Calendar.HOUR_OF_DAY, mProgram.getHours());
    cal.set(Calendar.MINUTE, mProgram.getMinutes());
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();
    
    setTitle(mLocalizer.msg("title", "No endtime"));
    
    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DIALOG_BORDER);
    panel.setLayout(new FormLayout("fill:150dlu:grow","pref, 3dlu, pref, fill:3dlu:grow, pref"));
    
    CellConstraints cc = new CellConstraints();

    panel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("help","No endtime defined")), cc.xy(1,1));
    
    mTimePanel = new TimeDateChooserPanel(date);
    panel.add(mTimePanel, cc.xy(1,3));
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        mButtonPressed = JOptionPane.OK_OPTION;
        setVisible(false);
      };
    });
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setVisible(false);
      };
    });
    
    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[] {ok, cancel});
    
    getRootPane().setDefaultButton(ok);

    UiUtilities.registerForClosing(this);
    
    panel.add(builder.getPanel(), cc.xy(1,5));

    setSize(Sizes.dialogUnitXAsPixel(180, this),
        Sizes.dialogUnitYAsPixel(100, this));
    
  }

  /*
   * (non-Javadoc)
   * @see util.ui.WindowClosingIf#close()
   */
  public void close() {
    setVisible(false);
  }

  public boolean wasOkPressed() {
    return mButtonPressed == JOptionPane.OK_OPTION;
  }

  public Date getDate() {
    return mTimePanel.getDate();
  }

}
