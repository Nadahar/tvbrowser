package tvbrowser.extras.favoritesplugin.wizards;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import util.ui.TimePeriodChooser;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

public class LimitTimeWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LimitTimeWizardStep.class);

  private TimePeriodChooser mTimePeriodChooser;

  private Program mProgram;

  private WizardStep mCaller;

  public LimitTimeWizardStep(Program program) {
    this(null, program);
  }

  public LimitTimeWizardStep(WizardStep caller, Program program) {
    mProgram = program;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Time");
  }

  public JPanel createContent(WizardHandler handler) {
    JPanel panel = new JPanel(new FormLayout("fill:pref:grow", "pref, 3dlu, pref"));
    panel.setBorder(Borders.DLU4_BORDER);
    
    CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("timeLabel", "Choose Time in which the program has to run:")), cc.xy(1,1));
    
    int lowBnd;
    int upBnd;

    if (mProgram != null) {
      lowBnd = (mProgram.getHours() - 1) * 60;
      if (lowBnd < 0) {
        lowBnd = 0;
      }
      upBnd = lowBnd + 120;
      if (upBnd >= 24 * 60) {
        upBnd = 24 * 60 - 1;
      }
    } else {
      lowBnd = 0;
      upBnd = 24 * 60 - 1;
    }
    mTimePeriodChooser = new TimePeriodChooser(lowBnd, upBnd, TimePeriodChooser.ALIGN_LEFT);

    panel.add(mTimePeriodChooser, cc.xy(1,3));
    
    return panel;
  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite) obj;
    fav.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
    return fav;
  }

  public WizardStep next() {
    return new FinishWizardStep(this);
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    return mTimePeriodChooser.getFromTime() < mTimePeriodChooser.getToTime();
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_BACK, WizardStep.BUTTON_NEXT };
  }

}
