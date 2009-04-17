package movieawardplugin;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.customizableitems.SelectableItemList;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * filter for a specific award
 * 
 * @author Bananeweizen
 */
public class SelectedAwardsFilterComponent extends PluginsFilterComponent {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SelectedAwardsFilterComponent.class);

  private SelectableItemList mAwardsList;
  private List<MovieAward> mSelectedAwards = new ArrayList<MovieAward>();

  public String getUserPresentableClassName() {
    return mLocalizer.msg("name","Selected Movie Awards");
  }

  public boolean accept(final Program program) {
    if (MovieAwardPlugin.getInstance() == null) {
      return true;
    }
    for (MovieAward award : mSelectedAwards) {
      if (award.containsAwardFor(program)) {
        return true;
      }
    }
    return false;
  }

  public JPanel getSettingsPanel() {
    final JPanel content = new JPanel(new BorderLayout());
    final ArrayList<MovieAward> allAwards = new ArrayList<MovieAward>();
    allAwards.addAll(MovieAwardPlugin.getInstance().getMovieAwards());
    Collections.sort(allAwards, new Comparator<MovieAward>(){

      public int compare(MovieAward award1, MovieAward award2) {
        return award1.getName().compareToIgnoreCase(award2.getName());
      }});
    mAwardsList = new SelectableItemList(mSelectedAwards.toArray(), allAwards.toArray());
    content.add(mAwardsList, BorderLayout.CENTER);
    return content;
  }

  public int getVersion() {
    return 1;
  }

  public void read(final ObjectInputStream in, final int version)
      throws IOException,
      ClassNotFoundException {
    try {
      final int count = in.readInt();
      mSelectedAwards = new ArrayList<MovieAward>(count);
      List<MovieAward> allAwards = MovieAwardPlugin.getInstance().getMovieAwards();
      // if the awards are not initialized, we trigger the database loading from here
      if (allAwards == null) {
        MovieAwardPlugin.getInstance().initDatabase();
        allAwards = MovieAwardPlugin.getInstance().getMovieAwards();
      }
      for (int i = 0; i < count; i++) {
        final String name = (String) in.readObject();
        for (MovieAward award : allAwards) {
          if (award.getName().equalsIgnoreCase(name)) {
            mSelectedAwards.add(award);
            break;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      mSelectedAwards = new ArrayList<MovieAward>();
    }
  }

  public void write(final ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedAwards.size());
    for (MovieAward movieAward : mSelectedAwards) {
      out.writeObject(movieAward.getName());
    }
  }

  @Override
  public void saveSettings() {
    mSelectedAwards = new ArrayList<MovieAward>();
    for (Object selected : mAwardsList.getSelection()) {
      mSelectedAwards.add((MovieAward) selected);
    }
  }
}
