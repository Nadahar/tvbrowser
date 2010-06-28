/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;

/**
 * The PluginChooserDlg class provides a Dialog for choosing plugins. The user
 * can choose from all Plugins that are able to receive Programs.
 */
public class MarkerChooserDlg extends JDialog implements WindowClosingIf {

  private static final long serialVersionUID = 1L;
  private Marker[] mResultPluginArr;
  private Marker[] mPluginArr;
  private SelectableItemList mPluginItemList;
  
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(MarkerChooserDlg.class);

  /**
   * 
   * @param parent
   * @param pluginArr
   *          The initially selected Plugins.
   * @param description
   *          A description text below the Plugin list.
   * @since 3.0
   */
  public MarkerChooserDlg(Window parent, Marker[] pluginArr, String description) {
    super(parent);
    setModal(true);
    init(pluginArr, description);
  }

  /**
   * 
   * @param parent
   * @param pluginArr
   *          The initially selected Plugins.
   * @param description
   *          A description text below the Plugin list.
   * @deprecated since 3.0
   */
  @Deprecated
  public MarkerChooserDlg(Dialog parent, Marker[] pluginArr, String description) {
    this((Window) parent, pluginArr, description);
  }

  /**
   * 
   * @param parent
   * @param pluginArr
   *          The initially selected Plugins.
   * @param description
   *          A description text below the Plugin list.
   * @deprecated since 3.0
   */
  @Deprecated
  public MarkerChooserDlg(Frame parent, Marker[] pluginArr, String description) {
    this((Window) parent, pluginArr, description);
  }
  
  private void init(Marker[] channelArr, String description) {
    setTitle(mLocalizer.msg("title","Choose Plugins"));
    UiUtilities.registerForClosing(this);
    
    if (channelArr == null) {
      mPluginArr = new Marker[]{};
      mResultPluginArr = new Marker[]{};
    }
    else {
      mPluginArr = channelArr;
      mResultPluginArr = channelArr;
    }

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();
    
    PluginAccess[] pluginAccess = Plugin.getPluginManager().getActivatedPlugins();
    
    ArrayList<Marker> list = new ArrayList<Marker>();
    
    list.add(FavoritesPluginProxy.getInstance());
    list.add(ReminderPluginProxy.getInstance());
    
    for (PluginAccess plugin : pluginAccess) {
      if (plugin.getMarkIcon() != null) {
        list.add(plugin);
      }
    }
    Collections.sort(list, new Comparator<Marker>() {
      public int compare(Marker arg0, Marker arg1) {
        return (arg0.toString().compareTo(arg1.toString()));
      }});

    mPluginItemList = new SelectableItemList(mResultPluginArr, list.toArray());

    int pos = 1;
    layout.appendRow(RowSpec.decode("fill:default:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));
    contentPane.add(mPluginItemList, cc.xy(1,pos));

    pos += 2;
    
    if (description != null) {
      JLabel lb = new JLabel(description);
      layout.appendRow(RowSpec.decode("pref"));
      layout.appendRow(RowSpec.decode("3dlu"));
      contentPane.add(lb, cc.xy(1,pos));
      pos += 2;
    }

    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Object[] o = mPluginItemList.getSelection();
        mResultPluginArr = new Marker[o.length];
        for (int i=0;i<o.length;i++) {
          mResultPluginArr[i]=(Marker)o[i];
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

    ButtonBarBuilder2 builder = new ButtonBarBuilder2();
    builder.addGlue();
    builder.addButton(new JButton[] {okBt, cancelBt});
    
    layout.appendRow(RowSpec.decode("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));
    
    pack();
  }

  /**
   *
   * @return an array of the selected plugins. If the user canceled the dialog,
   * the array from the constructor call is returned.
   */
  public Marker[] getMarker() {
    if (mResultPluginArr==null) {
      return mPluginArr;
    }
    return mResultPluginArr;
  }

  public void close() {
    setVisible(false);
  }

}