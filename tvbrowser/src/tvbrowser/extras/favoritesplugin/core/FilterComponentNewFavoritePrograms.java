package tvbrowser.extras.favoritesplugin.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

import devplugin.Program;
import tvbrowser.core.filters.filtercomponents.AbstractFilterComponent;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import util.ui.Localizer;

public class FilterComponentNewFavoritePrograms extends AbstractFilterComponent {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterComponentNewFavoritePrograms.class); 
  
  public FilterComponentNewFavoritePrograms() {
    this("","");
  }
  
  public FilterComponentNewFavoritePrograms(String name, String description) {
    super(name, description);
  }
  
  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public boolean accept(Program program) {
    return FavoritesPlugin.getInstance().isNewProgram(program);
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    // no settings to load
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    // no settings to write
  }
  
  public String getTypeDescription() {
    return LOCALIZER.msg("desc",
        "Accepts all programs that were found as new Favorite at last data update.");
  }
  
  @Override
  public String toString() {
    return LOCALIZER.msg("name", "New found Favorite programs");
  }
  
  @Override
  public JPanel getSettingsPanel() {
    return null;
  }

  @Override
  public void saveSettings() {}

}
