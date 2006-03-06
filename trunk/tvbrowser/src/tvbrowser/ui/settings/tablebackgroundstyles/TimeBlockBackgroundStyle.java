package tvbrowser.ui.settings.tablebackgroundstyles;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.ProgramTableSettingsTab;
import util.ui.TabLayout;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 30.04.2005
 * Time: 17:48:12
 */
public class TimeBlockBackgroundStyle implements TableBackgroundStyle {

   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TimeBlockBackgroundStyle.class);


  private JSpinner mTimeBlockSizeSp;
  private JTextField mTimeBlockBackground1TF, mTimeBlockBackground2TF, mTimeBlockWestImage1TF, mTimeBlockWestImage2TF;
  private JCheckBox mTimeBlockShowWestChB;
  private JLabel mTimeBlockWestImage1Lb, mTimeBlockWestImage2Lb;
  private JButton mTimeBlockWestImage1Bt, mTimeBlockWestImage2Bt;

  private JPanel mContent;

  public TimeBlockBackgroundStyle() {

  }

  public boolean hasContent() {
    return true;
  }

  public JPanel createSettingsContent() {

    mContent = new JPanel(new TabLayout(1));

    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    mContent.add(p1);
    p1.add(new JLabel(mLocalizer.msg("timeBlock.blockSize", "Block size")));
    mTimeBlockSizeSp = new JSpinner(new SpinnerNumberModel(Settings.propTimeBlockSize.getInt(), 1, 23, 1));

    p1.add(mTimeBlockSizeSp);
    p1.add(new JLabel(mLocalizer.msg("timeBlock.hours", "hours")));

    p1 = new JPanel(new TabLayout(3));
    mContent.add(p1);

    p1.add(new JLabel(mLocalizer.msg("timeBlock.background1", "Image 1")));
    mTimeBlockBackground1TF = new JTextField(Settings.propTimeBlockBackground1.getString(), 25);
    p1.add(mTimeBlockBackground1TF);
    p1.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeBlockBackground1TF));

    p1.add(new JLabel(mLocalizer.msg("timeBlock.background2", "Image 2")));
    mTimeBlockBackground2TF = new JTextField(Settings.propTimeBlockBackground2.getString(), 25);
    p1.add(mTimeBlockBackground2TF);
    p1.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeBlockBackground2TF));

    mTimeBlockShowWestChB = new JCheckBox(mLocalizer.msg("timeBlock.showWest", "Show left border"), Settings.propTimeBlockShowWest.getBoolean());
    mTimeBlockShowWestChB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleTimeBlockShowWest();
      }
    });
    mContent.add(mTimeBlockShowWestChB);

    mTimeBlockShowWestChB.setSelected(Settings.propTimeBlockShowWest.getBoolean());

    p1 = new JPanel(new TabLayout(3));
    mContent.add(p1);

    mTimeBlockWestImage1Lb = new JLabel(mLocalizer.msg("timeBlock.west1", "Border image 1"));
    p1.add(mTimeBlockWestImage1Lb);
    mTimeBlockWestImage1TF = new JTextField(Settings.propTimeBlockWestImage1.getString(), 25);
    p1.add(mTimeBlockWestImage1TF);
    mTimeBlockWestImage1Bt = ProgramTableSettingsTab.createBrowseButton(mContent, mTimeBlockWestImage1TF);
    p1.add(mTimeBlockWestImage1Bt);

    mTimeBlockWestImage2Lb = new JLabel(mLocalizer.msg("timeBlock.west2", "Border image 2"));
    p1.add(mTimeBlockWestImage2Lb);
    mTimeBlockWestImage2TF = new JTextField(Settings.propTimeBlockWestImage2.getString(), 25);
    p1.add(mTimeBlockWestImage2TF);
    mTimeBlockWestImage2Bt = ProgramTableSettingsTab.createBrowseButton(mContent, mTimeBlockWestImage2TF);
    p1.add(mTimeBlockWestImage2Bt);


    return mContent;
  }

  private void handleTimeBlockShowWest() {
    boolean enabled = mTimeBlockShowWestChB.isSelected();

    mTimeBlockWestImage1Lb.setEnabled(enabled);
    mTimeBlockWestImage1TF.setEnabled(enabled);
    mTimeBlockWestImage1Bt.setEnabled(enabled);
    mTimeBlockWestImage2Lb.setEnabled(enabled);
    mTimeBlockWestImage2TF.setEnabled(enabled);
    mTimeBlockWestImage2Bt.setEnabled(enabled);
  }

  public void storeSettings() {
    if (mContent == null) {
      return;
    }
    Integer blockSize = (Integer) mTimeBlockSizeSp.getValue();
    Settings.propTimeBlockSize.setInt(blockSize.intValue());
    Settings.propTimeBlockBackground1.setString(mTimeBlockBackground1TF.getText());
    Settings.propTimeBlockBackground2.setString(mTimeBlockBackground2TF.getText());
    Settings.propTimeBlockShowWest.setBoolean(mTimeBlockShowWestChB.isSelected());
    Settings.propTimeBlockWestImage1.setString(mTimeBlockWestImage1TF.getText());
    Settings.propTimeBlockWestImage2.setString(mTimeBlockWestImage2TF.getText());
  }

  public String getName() {
    return mLocalizer.msg("style","Time block");
  }

  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "timeBlock";
  }
}
