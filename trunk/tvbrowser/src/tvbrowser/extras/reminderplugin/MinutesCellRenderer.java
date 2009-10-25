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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.reminderplugin;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import util.ui.TVBrowserIcons;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The cell renderer for the minutest column of the reminder list.
 */
public class MinutesCellRenderer extends DefaultTableCellRenderer {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MinutesCellRenderer.class);

  private JPanel mPanel;
  private JLabel mTextLabel, mIconLabel;
  private JLabel mNoteLabel;
  
  /**
   * Creates an instance of this class.
   */
  public MinutesCellRenderer() {
    mPanel = new JPanel(new FormLayout("pref,pref:grow,pref,2dlu",
        "pref:grow,pref,2dlu,pref,pref:grow"));
    CellConstraints cc = new CellConstraints();
    mTextLabel = new JLabel();
    mIconLabel = new JLabel(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mNoteLabel = new JLabel("");
    
    mPanel.add(mTextLabel, cc.xy(1, 2));
    mPanel.add(mIconLabel, cc.xy(3, 2));
    mPanel.add(mNoteLabel, cc.xy(1, 4));
  }
  
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component def = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    if (value instanceof ReminderListItem) {
      final ReminderListItem listItem = (ReminderListItem) value;
      Integer minutes = listItem.getMinutes();
      
      mTextLabel.setText(ReminderFrame.getStringForMinutes(minutes.intValue()));
      
      mTextLabel.setOpaque(def.isOpaque());
      mTextLabel.setForeground(def.getForeground());
      mTextLabel.setBackground(def.getBackground());
      
      mPanel.setOpaque(def.isOpaque());
      mPanel.setBackground(def.getBackground());
      
      final String comment = listItem.getComment();
      if (comment != null && !comment.isEmpty()) {
        mNoteLabel.setVisible(true);
        mNoteLabel.setText(mLocalizer.msg("note", "Note: {0}", comment));
        mNoteLabel.setOpaque(def.isOpaque());
        mNoteLabel.setForeground(def.getForeground());
        mNoteLabel.setBackground(def.getBackground());
      } else {
        mNoteLabel.setVisible(false);
      }
      
      return mPanel;
    }
    
    return def;
  }
}
