package printplugin.dlgs.printfromqueuedialog;

import printplugin.dlgs.components.DateRangePanel;

import javax.swing.*;
import java.awt.*;

public class GeneralTab extends JPanel {

  private DateRangePanel mDateRangePanel;

  public GeneralTab() {

    super();
    setLayout(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    mDateRangePanel = new DateRangePanel(true);

    content.add(mDateRangePanel);

    add(content, BorderLayout.NORTH);
    add(new JCheckBox("Queue nach dem Drucken leeren"), BorderLayout.SOUTH);
  }
}
