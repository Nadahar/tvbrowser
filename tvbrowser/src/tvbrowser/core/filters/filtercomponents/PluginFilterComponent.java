/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import devplugin.Marker;
import devplugin.Program;

public class PluginFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(PluginFilterComponent.class);

  private JComboBox mBox;

  private String mPluginId;

  public PluginFilterComponent(String name, String desc) {
    super(name, desc);
  }

  public PluginFilterComponent() {
    this("", "");
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    if (version == 1) {
      String pluginClassName = (String) in.readObject();
      mPluginId = "java." + pluginClassName;
    } else {
      mPluginId = (String) in.readObject();
    }
  }

  public void write(ObjectOutputStream out) throws IOException {
    if (mPluginId == null) {
      out.writeObject("[invalid]");
    } else {
      out.writeObject(mPluginId);
    }
  }

  public boolean accept(final Program program) {
    if (mPluginId != null) {
      for (Marker marker : program.getMarkerArr()) {
        if (mPluginId.equals(marker.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return The id of the used plugin.
   */
  public String getPluginId() {
    return mPluginId;
  }

  public JPanel getSettingsPanel() {

    JPanel content = new JPanel(new BorderLayout(0, 7));
    JTextArea ta = new JTextArea(mLocalizer.msg("desc",
        "Accept all programs marked by plugin:"));
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setOpaque(false);
    ta.setEditable(false);
    ta.setFocusable(false);
    content.add(ta, BorderLayout.NORTH);

    PluginProxy[] plugins = PluginProxyManager.getInstance()
        .getActivatedPlugins();
    Arrays.sort(plugins, new PluginProxy.Comparator());

    mBox = new JComboBox(plugins);
    if (mPluginId != null) {
      PluginProxy plugin = PluginProxyManager.getInstance().getPluginForId(mPluginId);
      if (plugin != null) {
        mBox.setSelectedItem(plugin);
      }
    }
    content.add(mBox, BorderLayout.CENTER);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }

  @Override
  public String toString() {
    return mLocalizer.msg("Plugin", "Plugin");
  }

  public void saveSettings() {
    mPluginId = ((PluginProxy) mBox.getSelectedItem()).getId();
  }

  public int getVersion() {
    return 2;
  }

}
