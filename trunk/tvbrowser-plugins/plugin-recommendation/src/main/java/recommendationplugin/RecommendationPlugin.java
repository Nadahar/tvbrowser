/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package recommendationplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Timer;

import recommendationplugin.weighting.FavoriteWeighting;
import recommendationplugin.weighting.FilterWeighting;
import recommendationplugin.weighting.MarkerWeighting;
import recommendationplugin.weighting.RatingWeighting;
import recommendationplugin.weighting.ReminderWeighting;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramRatingIf;
import devplugin.SettingsTab;
import devplugin.Version;

public final class RecommendationPlugin extends Plugin {
  private static final boolean PLUGIN_IS_STABLE = false;
  private static final Version PLUGIN_VERSION = new Version(0, 2, PLUGIN_IS_STABLE);
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RecommendationPlugin.class);
  private static RecommendationPlugin mInstance;
  private Icon mIcon;
  private List<RecommendationWeighting> mWeightings = new ArrayList<RecommendationWeighting>();

  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);
  private ArrayList<ProgramWeight> mRecommendations;
  private RecommendationSettings mSettings;

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  /**
   * Creates an instance of this plugin.
   */
  public RecommendationPlugin() {
    mInstance = this;
  }

  @Override
  public void handleTvBrowserStartFinished() {
    initializeWeightings();
    mRootNode.setGroupingByDateEnabled(true); // to force neither alphabetic nor
                                              // date sorting
    updateRecommendations();
    // update the recommendations regularly (to remove expired programs)
    Timer updateTimer = new Timer(10 * 60 * 1000, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        updateRecommendations();
      }
    });
    updateTimer.start();
  }

  public void initializeWeightings() {
    mWeightings = new ArrayList<RecommendationWeighting>();
    for (ProgramRatingIf rating : getPluginManager().getAllProgramRatingIfs()) {
      initializeWeighting(new RatingWeighting(rating));
    }
    ProgramFilter allFilter = getPluginManager().getFilterManager().getAllFilter();
    for (ProgramFilter filter : getPluginManager().getFilterManager().getAvailableFilters()) {
      if (filter != allFilter) {
        initializeWeighting(new FilterWeighting(filter));
      }
    }
    for (PluginAccess plugin : getPluginManager().getActivatedPlugins()) {
      final String iconText = plugin.getProgramTableIconText();
      if (iconText != null) {
        final Icon[] icons = plugin.getProgramTableIcons(PluginManagerImpl.getInstance().getExampleProgram());
        if (icons != null && icons.length > 0) {
          initializeWeighting(new MarkerWeighting(plugin));
        }
      }
    }

    initializeWeighting(new FavoriteWeighting());
    initializeWeighting(new ReminderWeighting());
  }

  private void initializeWeighting(final RecommendationWeighting weighting) {
    weighting.setWeighting(mSettings.getWeighting(weighting.getId()));
    mWeightings.add(weighting);
  }

  @Override
  public ActionMenu getButtonAction() {
    final AbstractAction action = new AbstractAction() {
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
    return new PluginInfo(RecommendationPlugin.class, mLocalizer.msg("pluginName", "Recommendation Plugin"), mLocalizer
        .msg("description", "Shows recommendation based on data from different sources"),
        "Bodo Tasche, Michael Keppler", "GPL 3");
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
    return new RecommendationSettingsTab(this, mSettings);
  }

  List<RecommendationWeighting> getAllWeightings() {
    return mWeightings;
  }

  synchronized private void showWeightDialog() {
    final WeightDialog dialog = new WeightDialog(UiUtilities.getLastModalChildOf(getParentFrame()));

    updateRecommendations();
    dialog.addAllPrograms(mRecommendations);

    UiUtilities.centerAndShow(dialog);
  }

  synchronized void updateRecommendations() {
    mRootNode.clear();
    mRecommendations = new ArrayList<ProgramWeight>();

    // speedup calculation by only using weightings > 0
    final ArrayList<RecommendationWeighting> usedWeightings = new ArrayList<RecommendationWeighting>();
    for (RecommendationWeighting weighting : mWeightings) {
      if (weighting.getWeighting() != 0) {
        usedWeightings.add(weighting);
      }
    }
    if (usedWeightings.isEmpty()) {
      return;
    }
    ProgramWeight.resetMaxWeight();
    final HashMap<String, RecommendationNode> nodes = new HashMap<String, RecommendationNode>(200);

    final Date today = Date.getCurrentDate();
    for (Channel channel : getPluginManager().getSubscribedChannels()) {
      final Iterator<Program> it = getPluginManager().getChannelDayProgram(today, channel);
      if (it == null) {
        continue;
      }
      while (it.hasNext()) {
        final Program program = it.next();

        if (!program.isExpired()) {
          int sumWeight = 0;
          for (RecommendationWeighting weighting : usedWeightings) {
            sumWeight += weighting.getWeight(program);
          }
          if (sumWeight > 0) {
            ProgramWeight programWeight = new ProgramWeight(program, (short) sumWeight);
            mRecommendations.add(programWeight);
          }
        }
      }
    }

    Collections.sort(mRecommendations);
    for (ProgramWeight programWeight : mRecommendations) {
      String key = String.valueOf(programWeight.getWeight()) + programWeight.getProgram().getTitle();
      RecommendationNode node = nodes.get(key);
      if (node == null) {
        node = new RecommendationNode(programWeight);
        nodes.put(key, node);
      }
      node.addProgram(programWeight.getProgram());
    }

    ArrayList<RecommendationNode> nodeList = new ArrayList<RecommendationNode>(nodes.values());
    Collections.sort(nodeList);

    for (RecommendationNode recommendationNode : nodeList) {
      mRootNode.add(recommendationNode);
    }

    mRootNode.update();
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  @Override
  public void handleTvDataUpdateFinished() {
    updateRecommendations();
  }

  @Override
  public void loadSettings(Properties properties) {
    mSettings = new RecommendationSettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }
}