package recommendationplugin;

import devplugin.Program;

import javax.swing.DefaultListModel;
import javax.swing.AbstractListModel;
import java.util.ArrayList;

public class ProgramWeightWrapperModel extends AbstractListModel {
  ArrayList<ProgramWeight> mData = new ArrayList<ProgramWeight>();

  public int getSize() {
    return mData.size();
  }

  public Object getElementAt(int i) {
    return mData.get(i).getProgram();
  }

  public void addElement(ProgramWeight p) {
    mData.add(p);
    fireIntervalAdded(this, mData.indexOf(p), mData.indexOf(p));
  }
}
