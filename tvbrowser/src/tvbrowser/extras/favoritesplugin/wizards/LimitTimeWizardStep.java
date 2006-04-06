package tvbrowser.extras.favoritesplugin.wizards;


import javax.swing.*;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import devplugin.Program;
import util.ui.TimePeriodChooser;


public class LimitTimeWizardStep implements WizardStep {

   public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(LimitTimeWizardStep.class);

  private TimePeriodChooser mTimePeriodChooser;
  private Program mProgram;

  public LimitTimeWizardStep(Program program) {
    mProgram = program;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Time");
  }

  public JPanel getContent(WizardHandler handler) {



    int lowBnd;
    int upBnd;

    if (mProgram != null) {
      lowBnd = (mProgram.getHours()-1)*60;
      if (lowBnd < 0) {
        lowBnd = 0;
      }
      upBnd = lowBnd + 120;
      if (upBnd >= 24*60) {
        upBnd = 24*60 -1;
      }
    }
    else {
      lowBnd = 0;
      upBnd = 24*60 - 1;
    }
    mTimePeriodChooser = new TimePeriodChooser(lowBnd, upBnd, TimePeriodChooser.ALIGN_LEFT);

    return mTimePeriodChooser;
  }

  public Object createDataObject(Object obj) {
    Favorite fav = (Favorite)obj;
    fav.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
    return fav;
  }

  public WizardStep next() {
    return new FinishWizardStep();
  }

  public boolean isValid() {
    return mTimePeriodChooser.getFromTime() < mTimePeriodChooser.getToTime();
  }

  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_NEXT};
  }
  
}
