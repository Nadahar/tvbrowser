package tvbrowser.extras.favoritesplugin.wizards;

import util.ui.OrderChooser;

import javax.swing.*;
import java.awt.*;

import devplugin.Plugin;
import devplugin.Channel;
import devplugin.Program;
import tvbrowser.extras.favoritesplugin.core.Favorite;

public class LimitChannelWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(LimitChannelWizardStep.class);

  private WizardStep mNextStep;
  private OrderChooser mChannelChooser;
  private Program mProgram;
  private WizardStep mCaller;

  public LimitChannelWizardStep(WizardStep caller, Program program) {
    this(caller, null, program);
  }

  public LimitChannelWizardStep(WizardStep caller, WizardStep nextStep, Program program) {
    mNextStep = nextStep;
    mProgram = program;
    mCaller = caller;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Channel");
  }

  public JPanel createContent(WizardHandler handler) {

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.NORTH, new JLabel(mLocalizer.msg("selectChannels","Select channels:")));

    Channel[] chArr;
    if (mProgram != null) {
      chArr = new Channel[] {mProgram.getChannel()};
    }
    else {
      chArr = new Channel[]{};
    }
    mChannelChooser = new OrderChooser(chArr, Plugin.getPluginManager().getSubscribedChannels());
    panel.add(BorderLayout.CENTER, mChannelChooser);
    return panel;
  }

  public Object createDataObject(Object obj) {
    Object[] order = mChannelChooser.getOrder();
    Favorite fav = (Favorite)obj;
    Channel[] ch = new Channel[order.length];
    for (int i=0; i<ch.length; i++) {
      ch[i] = (Channel)order[i];
    }
    fav.getLimitationConfiguration().setChannels(ch);
    return fav;
  }

  public WizardStep next() {
    if (mNextStep != null) {
      return mNextStep;
    }
    else {
      return new FinishWizardStep(this);
    }
  }

  public WizardStep back() {
    return mCaller;
  }

  public boolean isValid() {
    return mChannelChooser.getOrder().length > 0;
  }

  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_BACK, WizardStep.BUTTON_NEXT};
  }

}
