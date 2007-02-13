package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
import tvbrowser.core.filters.FilterComponent;
import util.ui.Localizer;

public class ProgramMarkingPriorityFilterComponent implements FilterComponent {

  private static Localizer mLocalizer = Localizer.getLocalizerFor(ProgramMarkingPriorityFilterComponent.class);
  private String mName, mDescription;
  
  private int mMarkPriority = Program.MIN_MARK_PRIORITY;
  private JComboBox mValueSelection;
  
  public ProgramMarkingPriorityFilterComponent() {
    this("","");
  }
  
  public ProgramMarkingPriorityFilterComponent(String name, String desc) {
    mName = name;
    mDescription = desc;
  }
  
  public boolean accept(Program program) {
    return program.getMarkPriority() == mMarkPriority;
  }

  public String getDescription() {
    return mDescription;
  }

  public String getName() {
    return mName;
  }

  public JPanel getSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    JPanel p = new JPanel(new FormLayout("default","pref"));
    
    String[] values = {mLocalizer.msg("min","Minimum priority"),
        mLocalizer.msg("medium","Medium priority"),
        mLocalizer.msg("max","Maximum priority")};
    
    mValueSelection = new JComboBox(values);
    mValueSelection.setSelectedIndex(mMarkPriority);
    
    p.add(mValueSelection, cc.xy(1,1));
    
    return p;
  }

  public int getVersion() {
    return 1;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mMarkPriority = in.readInt();
  }

  public void saveSettings() {
    if(mValueSelection != null)
      mMarkPriority = mValueSelection.getSelectedIndex();
  }

  public void setDescription(String desc) {
    mDescription = desc;
  }

  public void setName(String name) {
    mName = name;
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mMarkPriority);
  }
  
  public String toString() {
    return mLocalizer.msg("name","Marking priority");
  }
}
