package recommendationplugin;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;
import devplugin.ActionMenu;
import devplugin.ProgramRatingIf;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JDialog;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.awt.event.ActionEvent;
import java.awt.Window;

import recommendationplugin.inputimpl.RatingInput;
import recommendationplugin.inputimpl.FavoriteInput;
import recommendationplugin.inputimpl.ReminderInput;

public class RecommendationPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RecommendationPlugin.class);
  protected static RecommendationPlugin mInstance;
  private Icon mIcon;
  private List<RecommendationInputIf> mEnabledInput;

  public static Version getVersion() {
    return new Version(0, 2, false);
  }

  /**
   * Creates an instance of this plugin.
   */
  public RecommendationPlugin() {
    mInstance = this;
    mEnabledInput = new ArrayList<RecommendationInputIf>();
  }

  @Override
  public void handleTvBrowserStartFinished() {
    for (ProgramRatingIf rating: getPluginManager().getAllProgramRatingIfs()) {
      mEnabledInput.add(new RatingInput(rating, 30));
    }

    mEnabledInput.add(new FavoriteInput(30));
    mEnabledInput.add(new ReminderInput(30));
  }

  @Override
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        showWeightDialog();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("pluginName", "Recommendation Plugin"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    action.putValue(BIG_ICON, getPluginIcon());
    return new ActionMenu(action);
  }

  public PluginInfo getInfo() {
    return new PluginInfo(RecommendationPlugin.class, mLocalizer.msg("pluginName", "Recommendation Plugin"),
        mLocalizer.msg("description", "Shows recommendation based on data from different sources"),
        "Bodo Tasche", "GPL");
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("recommendation.png"));
    }
    return mIcon;
  }

  public static RecommendationPlugin getInstance() {
    return mInstance;
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new RecommendationSettingsTab(this);
  }

  public List<RecommendationInputIf> getEnabledInput() {
    return mEnabledInput;
  }

  private void showWeightDialog() {
    WeightDialog dialog;

    Window w = UiUtilities.getLastModalChildOf(getParentFrame());

    if (w instanceof JFrame) {
      dialog = new WeightDialog((JFrame) w);
    } else {
      dialog = new WeightDialog((JDialog) w);
    }

    ArrayList<ProgramWeight> list = new ArrayList<ProgramWeight>();

    Date today = new Date();
    for (Channel ch:getPluginManager().getSubscribedChannels()) {
      Iterator<Program> it = getPluginManager().getChannelDayProgram(today, ch);
      while (it.hasNext()) {
        Program p = it.next();

        if (!p.isExpired()) {
          for (RecommendationInputIf input : mEnabledInput) {
            final int weight = input.calculate(p);
            if (weight > 0) {
              list.add(new ProgramWeight(p, weight));
            }
          }
        }
      }
    }

    Collections.sort(list);

    dialog.addAllPrograms(list);

    UiUtilities.centerAndShow(dialog);
  }

}