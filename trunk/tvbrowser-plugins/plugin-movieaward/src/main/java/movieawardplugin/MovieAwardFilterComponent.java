package movieawardplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import devplugin.PluginsFilterComponent;
import devplugin.Program;
import util.ui.Localizer;

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

  public boolean accept(Program program) {
    return MovieAwardPlugin.getInstance() == null || MovieAwardPlugin.getInstance().hasAwards(program);
  }

  public int getVersion() {
    return 1;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
  }

  public void write(ObjectOutputStream out) throws IOException {
  }
}
