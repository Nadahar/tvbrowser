package movieawardplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.ui.Localizer;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * Filter component class of this plugin.
 * 
 * @author Bodo Tasche
 */
public class MovieAwardFilterComponent extends PluginsFilterComponent {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MovieAwardFilterComponent.class);

  public String getUserPresentableClassName() {
    return mLocalizer.msg("name","Movie Awards");
  }

  public boolean accept(final Program program) {
    return MovieAwardPlugin.getInstance() == null || MovieAwardPlugin.getInstance().hasAwards(program);
  }

  public int getVersion() {
    return 1;
  }

  public void read(final ObjectInputStream in, final int version)
      throws IOException,
      ClassNotFoundException {
    // no user settings
  }

  public void write(final ObjectOutputStream out) throws IOException {
    // no user settings
  }
}
