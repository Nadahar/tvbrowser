package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tvbrowser.core.filters.FilterComponent;
import util.ui.LineNumberHeader;
import devplugin.Program;

public class MassFilterComponent implements FilterComponent {

  private String mName, mDescription;
  
  public MassFilterComponent() {
    this("", "");
  }

  public MassFilterComponent(String name, String description) {
    mName = name;
    mDescription = description;
  }
  
  public int getVersion() {
    return 1;
  }

  public boolean accept(Program program) {
    return false;
  }

  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    // TODO Auto-generated method stub

  }

  public void write(ObjectOutputStream out) throws IOException {
    // TODO Auto-generated method stub

  }

  public JPanel getPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    
    JTextArea area = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(area);
    LineNumberHeader header = new LineNumberHeader(area);
    scrollPane.setRowHeaderView(header);
    
    panel.add(scrollPane, BorderLayout.CENTER);
    
    return panel;
  }

  public void ok() {
    // TODO Auto-generated method stub
  }

  public String toString() {
    return "MassFilterComponent";
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
