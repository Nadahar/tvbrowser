package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

import devplugin.Marker;
import devplugin.Program;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.ui.Localizer;

/**
 * This filter filters all programs that are marked by the Reminder
 *
 * @author René Mach
 *
 */
public class ReminderFilterComponent extends AbstractFilterComponent {

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ReminderFilterComponent.class);

  public ReminderFilterComponent(String name, String desc) {
    super(name, desc);
  }

  public ReminderFilterComponent() {
    this("", "");
  }

  @Override
  public String toString() {
    return mLocalizer.msg("name", "Reminder programs");
  }

  public int getVersion() {
    return 1;
  }

  public boolean accept(Program program) {
    String reminderPluginId = ReminderPlugin.getReminderPluginId();
    Marker[] mark = program.getMarkerArr();

    for (Marker element : mark) {
      if (reminderPluginId.equals(element.getId())) {
        return true;
      }
    }

    return false;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {

  }

  public void write(ObjectOutputStream out) throws IOException {
  }

  public String getTypeDescription() {
    return mLocalizer.msg("desc",
        "Accepts all programs that are marked by the Reminder.");
  }
  
  public JPanel getSettingsPanel() {
    return null;
  }

  public void saveSettings() {
  }

}
