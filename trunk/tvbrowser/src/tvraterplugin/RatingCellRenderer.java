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
 */

package tvraterplugin;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * The CellRenderer for the Ratings
 * 
 * @author bodo
 */
public class RatingCellRenderer extends JLabel implements ListCellRenderer {


    public RatingCellRenderer() {
        setOpaque(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object obj,
            int index, boolean isSelected, boolean hasFocus) {

        if (obj instanceof Integer) {
            Integer rating = ((Integer) obj);
            if (isSelected || hasFocus) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            ImageIcon rateing = RatingIconTextFactory.getImageIconForRating(rating.intValue());
            this.setIcon(rateing);
            this.setText(RatingIconTextFactory.getStringForRating(rating.intValue()));

            return this;
        }

        return new JLabel(obj.toString());
    }

}
