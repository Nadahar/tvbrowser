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
import java.util.ArrayList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;
import devplugin.Program;
import devplugin.ProgramInfoHelper;

/**
 * Filtert nach bestimmten Programm-Informationen (zum Beispiel Untertitel)
 * 
 * @author bodo
 */
public class ProgramInfoFilterComponent extends AbstractFilterComponent {

  private SelectableItemList mList;

  /**
   * Erzeugt einen leeren Filter
   */
  public ProgramInfoFilterComponent() {
    this("", "");
  }

  /**
   * Erzeugt einen Filter
   * 
   * @param name
   *          Name
   * @param description
   *          Beschreibung
   */
  public ProgramInfoFilterComponent(String name, String description) {
    super(name, description);
  }

  /**
   * Gibt die Version zurueck
   * 
   * @see tvbrowser.core.filters.FilterComponent#getVersion()
   */
  public int getVersion() {
    return 1;
  }

  /**
   * Wird dieses Programm akzeptiert von diesem Filter ?
   * 
   * @see tvbrowser.core.filters.FilterComponent#accept(devplugin.Program)
   */
  public boolean accept(Program program) {
    int info = program.getInfo();
    if (info < 1) {
      return false;
    }

    return bitSet(info, mSelectedBits);
  }

  /**
   * Liest die Einstellungen für dieses Plugin aus dem Stream
   * 
   * @see tvbrowser.core.filters.FilterComponent#read(java.io.ObjectInputStream,
   *      int)
   */
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mSelectedBits = in.readInt();
  }

  /**
   * Schreibt die Einstellungen dieses Plugins in den Stream
   * 
   * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
   */
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedBits);
  }

  /**
   * Gibt einen Panel zurueck, der es ermoeglicht, den Filter einzustellen
   * 
   * @see tvbrowser.core.filters.FilterComponent#getSettingsPanel()
   */
  public JPanel getSettingsPanel() {
    final JPanel centerPanel = new JPanel(new BorderLayout());
    String[] infoMessages = ProgramInfoHelper.getInfoIconMessages();
    int[] infoBits = ProgramInfoHelper.getInfoBits();
    final String[] allItems = new String[infoMessages.length];
    final ArrayList<String> selectedItems = new ArrayList<String>();
    for (int i = 0; i < infoMessages.length; i++) {
      final String item = infoMessages[i];
      allItems[i] = item;
      if (bitSet(mSelectedBits, infoBits[i])) {
        selectedItems.add(item);
      }
    }
    mList = new SelectableItemList(selectedItems.toArray(), allItems);
    mList.addCenterRendererComponent(String.class,
        new SelectableItemRendererCenterComponentIf() {
          private DefaultListCellRenderer mRenderer = new DefaultListCellRenderer();

          @Override
          public void calculateSize(JList list, int index, JPanel contentPane) {
          }

          @Override
          public JPanel createCenterPanel(JList list, Object value, int index,
              boolean isSelected, boolean isEnabled,
              JScrollPane parentScrollPane, int leftColumnWidth) {
            DefaultListCellRenderer label = (DefaultListCellRenderer) mRenderer
                .getListCellRendererComponent(list, value, index, isSelected,
                    false);
            label.setIcon(ProgramInfoHelper.getInfoIcons()[index]);
            label.setHorizontalAlignment(SwingConstants.LEADING);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setOpaque(false);

            JPanel panel = new JPanel(new BorderLayout());
            if (isSelected && isEnabled) {
              panel.setOpaque(true);
              panel.setForeground(list.getSelectionForeground());
              panel.setBackground(list.getSelectionBackground());
            } else {
              panel.setOpaque(false);
              panel.setForeground(list.getForeground());
              panel.setBackground(list.getBackground());
            }
            panel.add(label, BorderLayout.WEST);
            return panel;
          }
        });
    centerPanel.add(mList, BorderLayout.CENTER);
    return centerPanel;
  }

  public void saveSettings() {
    int bits = 0;

    String[] infoMessages = ProgramInfoHelper.getInfoIconMessages();
    int[] infoBits = ProgramInfoHelper.getInfoBits();
    final Object[] checked = mList.getSelection();
    for (Object element : checked) {
      final String item = (String) element;
      for (int infoIndex = 0; infoIndex < infoMessages.length; infoIndex++) {
        if (item.equals(infoMessages[infoIndex])) {
          bits |= infoBits[infoIndex];
          break;
        }
      }
    }
    mSelectedBits = bits;
  }

  /**
   * Gibt den Namen des Filters zurueck
   */
  @Override
  public String toString() {
    return mLocalizer.msg("ProgrammInfo", "Program-Info");
  }

  /**
   * Ueberprueft, ob Bits gesetzt sind
   * 
   * @param num
   *          hier pruefen
   * @param pattern
   *          diese pattern pruefen
   * @return Pattern gesetzt?
   */
  private boolean bitSet(final int num, final int pattern) {
    return (num & pattern) == pattern;
  }

  /**
   * Die gesetzten Bits
   */
  private int mSelectedBits = 0;

  /**
   * Der Lokalizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramInfoFilterComponent.class);

}