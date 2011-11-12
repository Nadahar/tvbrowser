/**
 * Created on 12.11.2011
 */
package captureplugin.drivers.topfield;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;

/**
 * 
 * @author Wolfgang
 */
public class TopfieldProgramTimeComponent extends JPanel {
  private static final long serialVersionUID = 1L;

  private static final String REPEAT_LABEL = "recordRepeat";
  private static final String DEFAULT_REPEAT_LABEL = "Repeat:";
  private static final String PROTECT_CHECK_LABEL = "protectTimerCheck"; // @jve:decl-index=0:
  private static final String DEFAULT_PROTECT_CHECK_LABEL = "Protect timer (AutoExtender)"; // @jve:decl-index=0:

  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldProgramTimeComponent.class);

  private final TopfieldConfiguration configuration;

  private JComboBox repeatSelector = null;
  private JLabel repeatLabel = null;
  private JCheckBox protectTimerCheck = null;

  /**
   * This is the default constructor
   */
  public TopfieldProgramTimeComponent() {
    super();
    configuration = null;
    initialize();
  }

  /**
   * Constructor supplying the device configuration.
   * 
   * @param config
   *          The Device configuration
   */
  public TopfieldProgramTimeComponent(TopfieldConfiguration config) {
    super();
    configuration = config;
    initialize();
  }

  /**
   * This method initializes this
   */
  private void initialize() {
    GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
    gridBagConstraints2.gridx = 0;
    gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints2.anchor = GridBagConstraints.WEST;
    gridBagConstraints2.gridwidth = 2;
    gridBagConstraints2.gridy = 1;
    GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridy = 0;
    repeatLabel = new JLabel();
    repeatLabel.setText(localizer.msg(REPEAT_LABEL, DEFAULT_REPEAT_LABEL));
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(0, 3, 0, 0);
    gridBagConstraints.gridx = 1;
    this.setSize(300, 200);
    this.setLayout(new GridBagLayout());
    this.add(getRepeatSelector(), gridBagConstraints);
    this.add(repeatLabel, gridBagConstraints1);
    this.add(getProtectTimerCheck(), gridBagConstraints2);
  }

  /**
   * This method initializes repeatSelector
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getRepeatSelector() {
    if (repeatSelector == null) {
      repeatSelector = new JComboBox();
      for (TopfieldTimerMode mode : TopfieldTimerMode.values()) {
        repeatSelector.addItem(mode);
      }
    }
    return repeatSelector;
  }

  /**
   * Get the selected timer mode.
   * 
   * @return The selected timer mode
   */
  public TopfieldTimerMode getSelectedTimerMode() {
    return (TopfieldTimerMode) repeatSelector.getSelectedItem();
  }

  /**
   * This method initializes protectTimerCheck
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getProtectTimerCheck() {
    if (protectTimerCheck == null) {
      protectTimerCheck = new JCheckBox();
      protectTimerCheck.setText(localizer.msg(PROTECT_CHECK_LABEL, DEFAULT_PROTECT_CHECK_LABEL));
      if (configuration != null) {
        protectTimerCheck.setEnabled(!configuration.getAxProtectionPrefix().isEmpty());
      } else {
        protectTimerCheck.setEnabled(false);
      }
    }
    return protectTimerCheck;
  }

  /**
   * Should the timer be protected?
   * 
   * @return <code>true</code> if the timer should be protected
   */
  public boolean isProtectTimerChecked() {
    return protectTimerCheck.isSelected();
  }
}
