package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

import devplugin.Marker;
import devplugin.Program;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import util.ui.Localizer;

/**
 * This filter filters all programs that are marked as Favorites.
 *
 * @author René Mach
 *
 */
public class FavoritesFilterComponent extends AbstractFilterComponent {

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(FavoritesFilterComponent.class);

  public FavoritesFilterComponent(String name, String description) {
    super(name, description);
  }

  public FavoritesFilterComponent() {
    this("", "");
  }

  @Override
  public String toString() {
    return mLocalizer.msg("name", "Favorites");
  }

  public int getVersion() {
    return 1;
  }

  public boolean accept(final Program program) {
    final String favPluginId = FavoritesPlugin.getFavoritesPluginId();
    for (Marker marker : program.getMarkerArr()) {
      if (favPluginId.equals(marker.getId())) {
        return true;
      }
    }

    return false;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    // no settings to load
  }

  public void write(ObjectOutputStream out) throws IOException {
    // no settings to store
  }
  
  public String getTypeDescription() {
    return mLocalizer.msg("desc",
        "Accepts all programs that are marked as Favorite.");
  }

  public JPanel getSettingsPanel() {
    return null;
  }

  public void saveSettings() {
  }

}
