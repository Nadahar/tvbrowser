/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * A ListCellRenderer for Channel-Lists
 */
public class ParamEntryCellRenderer extends DefaultListCellRenderer {
  /** Internal reused ChannelLabel */
  private JLabel mParam;

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (mParam == null) {
      mParam = new JLabel();
    }

    if (value instanceof ParamEntry) {

      mParam.setText(value.toString());
      mParam.setOpaque(label.isOpaque());
      mParam.setBackground(label.getBackground());
      mParam.setForeground(label.getForeground());

      if (!((ParamEntry)value).isEnabled()) {
        mParam.setEnabled(false);
      } else {
        mParam.setEnabled(true);
      }
      
      return mParam;
    }

    return label;
  }
}