package tvbrowser.extras.favoritesplugin.dlgs;

import devplugin.Plugin;
import devplugin.PluginAccess;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import util.ui.customizableitems.SelectableItemList;


public class PluginChooserDlg extends JDialog {

   public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginChooserDlg.class);

  private SelectableItemList mSelectableItemList;
  private PluginAccess[] mResultPluginArr;

  public PluginChooserDlg(Dialog parent, PluginAccess[] pluginArr) {
    super(parent, true);
    init(pluginArr);
  }

  private void init(PluginAccess[] pluginArr) {

    setTitle(mLocalizer.msg("choosePlugins","Choose Plugins"));

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();

    mSelectableItemList = new SelectableItemList(pluginArr, Plugin.getPluginManager().getActivatedPlugins());

     int pos = 1;
    layout.appendRow(new RowSpec("fill:pref:grow"));
    layout.appendRow(new RowSpec("3dlu"));
    contentPane.add(mSelectableItemList, cc.xy(1,pos));
    pos += 2;

    JButton okBt = new JButton(mLocalizer.msg("OK","OK"));
    JButton cancelBt = new JButton(mLocalizer.msg("Cancel","Cancel"));

    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Object[] o = mSelectableItemList.getSelection();
        mResultPluginArr = new PluginAccess[o.length];
        for (int i=0;i<o.length;i++) {
          mResultPluginArr[i]=(PluginAccess)o[i];
        }
        setVisible(false);
      }
      });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResultPluginArr = null;
        setVisible(false);
      }
    });

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[] {okBt, cancelBt});

    layout.appendRow(new RowSpec("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));

    pack();

  }

  public PluginAccess[] getPlugins() {
    return mResultPluginArr;
  }

}
