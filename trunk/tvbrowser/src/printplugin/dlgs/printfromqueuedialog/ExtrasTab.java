package printplugin.dlgs.printfromqueuedialog;

import printplugin.dlgs.components.ProgramPreviewPanel;
import printplugin.settings.ProgramIconSettings;

import javax.swing.*;
import java.awt.*;

import util.ui.TabLayout;

public class ExtrasTab extends JPanel {

  /** The localizer for this class. */
   private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(ExtrasTab.class);


  private ProgramPreviewPanel mProgramPreviewPanel;

  public ExtrasTab(final Frame dlgParent) {
    super();

    setLayout(new BorderLayout());

    JPanel content = new JPanel(new TabLayout(1));

    JPanel previewPn = new JPanel(new BorderLayout());

    previewPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("programItem","Program item")));
    mProgramPreviewPanel = new ProgramPreviewPanel(dlgParent);
    previewPn.add(mProgramPreviewPanel, BorderLayout.CENTER);

    content.add(previewPn);

    add(content, BorderLayout.NORTH);
  }

  public void setProgramIconSettings(ProgramIconSettings programIconSettings) {
    mProgramPreviewPanel.setProgramIconSettings(programIconSettings);
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramPreviewPanel.getProgramIconSettings();
  }

}
