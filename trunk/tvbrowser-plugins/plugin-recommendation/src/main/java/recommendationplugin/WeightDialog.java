package recommendationplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

public final class WeightDialog extends JDialog implements WindowClosingIf {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RecommendationPlugin.class);
  private ProgramWeightWrapperModel mModel = new ProgramWeightWrapperModel();

  public WeightDialog(final JDialog jDialog) {
    super(jDialog, true);
    createGui();
  }

  public WeightDialog(final JFrame jframe) {
    super(jframe, true);
    createGui();
  }

  private void createGui() {
    final FormLayout layout = new FormLayout("fill:min:grow");
    final CellConstraints cc = new CellConstraints();

    final JPanel panel = (JPanel) getContentPane();
    panel.setLayout(layout);
    panel.setBorder(Borders.DLU4_BORDER);

    int line = 1;

    layout.appendRow(RowSpec.decode("fill:min:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));

    ProgramList list = new ProgramList(mModel);

    panel.add(new JScrollPane(list), cc.xy(1, line));

    line += 2;
    layout.appendRow(RowSpec.decode("pref"));

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();

    final JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    builder.addGriddedButtons(new JButton[]{ok});

    panel.add(builder.getPanel(), cc.xy(1, line));

    setSize(Sizes.dialogUnitXAsPixel(200, this),
        Sizes.dialogUnitYAsPixel(300, this));

    UiUtilities.registerForClosing(this);
  }

  public void close() {
    setVisible(false);
  }

  public void addAllPrograms(final ArrayList<ProgramWeight> list) {
    for (ProgramWeight p : list) {
      mModel.addElement(p);
    }
  }
}
