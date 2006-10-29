package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;

import devplugin.Marker;
import devplugin.Program;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import util.ui.Localizer;

/**
 * This filter filters all programs that are marked as Favorites.
 * 
 * @author René Mach
 *
 */
public class FavoritesFilterComponent implements FilterComponent {

  private static Localizer mLocalizer = Localizer.getLocalizerFor(FavoritesFilterComponent.class);
  private String mName, mDescription;
  
  public FavoritesFilterComponent(String name, String desc) {
    mName = name;
    mDescription = desc;
  }
  
  public FavoritesFilterComponent() {
    this("","");
  }
  
  public String toString() {
    return mLocalizer.msg("name","Favorites");
  }
  
  public int getVersion() {
    return 1;
  }

  public boolean accept(Program program) {
    Marker[] mark = program.getMarkerArr();
    
    for(int i = 0; i < mark.length; i++)
      if(mark[i].getId().compareTo(FavoritesPlugin.getInstance().getId()) == 0)
        return true;
    
    return false;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {

  }

  public void write(ObjectOutputStream out) throws IOException {
  }

  public JPanel getSettingsPanel() {
    JPanel p1 = new JPanel();
    p1.add(new JLabel(mLocalizer.msg("desc","Accepts all programs that are marked as Favorite.")));
    return p1;
  }

  public void saveSettings() {
  }

  public String getName() {
    return mName;
  }

  public String getDescription() {
    return mDescription;
  }

  public void setName(String name) {
    mName = name;
  }

  public void setDescription(String desc) {
    mDescription = desc; 
  }

}
