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
 * SVN information:
 *     $Date: 2009-08-14 14:31:20 +0200 (Fr, 14 Aug 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5904 $
 */
package i18nplugin;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import util.ui.UiUtilities;

/**
 * This is a CellRenderer that highlights TreeNodes that have a incomplete translation
 * 
 * @author bodum
 */
public class PropertiesTreeCellRenderer extends DefaultTreeCellRenderer {

  private Locale mLocale;
  
  /**
   * Icon instance used for each node.
   */
  private CompositeIcon mIcon = new CompositeIcon();
  /**
   * Icon used for nodes with missing translation.
   */
  private static Icon errorIcon = UiUtilities.scaleIcon(UIManager.getIcon("OptionPane.errorIcon"), 8);
  /**
   * Icon used for nodes with not well formed translations.
   */
  private static Icon warningIcon = UiUtilities.scaleIcon(UIManager.getIcon("OptionPane.warningIcon"), 8);
  
  public PropertiesTreeCellRenderer(Locale locale) {
    mLocale = locale;
  }
  
  public void setCurrentLocale(Locale locale) {
    mLocale = locale;
  }
  
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    int state = LanguageNodeIf.STATE_OK;
    if (value instanceof LanguageNodeIf) {
      if (sel) {
      	label.setForeground(UIManager.getColor("List.selectionForeground"));
      	label.setBackground(UIManager.getColor("List.selectionBackground"));
      }
      else {
        LanguageNodeIf entry = (LanguageNodeIf) value;
        state = entry.translationStateFor(mLocale);
        label.setForeground(I18NPlugin.getInstance().getTranslationColor(state));
      }
    }
    
    // Display the number of matches if a filter is set.
    if (value instanceof FilterNodeIf && !(value instanceof PropertiesEntryNode)) {
      FilterNodeIf node = (FilterNodeIf) value;
      if (node.getMatchCount() > 0) {
        String labelText = label.getText();
        if (labelText != null && !labelText.startsWith("<html>")) {
          labelText = "<html>" + labelText + " <b>(" + node.getMatchCount() + ")</b></html>";
          label.setText(labelText);
        }
      }
    }
    
    // Set the icon
    Icon icon = label.getIcon();
    mIcon.setTargetIcon(icon);
    Icon stateIcon = null;
    if (state != LanguageNodeIf.STATE_OK) {
      if (state == LanguageNodeIf.STATE_MISSING_TRANSLATION) {
        stateIcon = errorIcon;
      } else {
        stateIcon = warningIcon;
      }
    }
    mIcon.setStateIcon(stateIcon);
    label.setIcon(mIcon);
    
    return label;
  }
  
  /**
   * @author Torsten Keil
   * 
   * This composite icon draws the target icon. If the target icon and a state icon is set it
   * draws the state icon over the lower right corner of the target icon.
   *
   */
  private static class CompositeIcon implements Icon {
    
    /**
     * The "original" icon.
     */
    private Icon mTargetIcon;
    /**
     * The icon indicating the state.
     */
    private Icon mStateIcon;

    public int getIconHeight() {
      return mTargetIcon != null ? mTargetIcon.getIconHeight() : 0;
    }

    public int getIconWidth() {
      return mTargetIcon != null ? mTargetIcon.getIconWidth() : 0;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (mTargetIcon != null) {
        mTargetIcon.paintIcon(c, g, x, y);
        if (mStateIcon != null) {
          int xPos = x + mTargetIcon.getIconWidth() - mStateIcon.getIconWidth();
          int yPos = c.getHeight() - mStateIcon.getIconHeight();
          mStateIcon.paintIcon(c, g, xPos, yPos);
        }
      }
    }

    /**
     * Sets a new target icon.
     * 
     * @param targetIcon
     */
    public void setTargetIcon(Icon targetIcon) {
      this.mTargetIcon = targetIcon;
    }

    /**
     * Sets a new state icon.
     * 
     * @param stateIcon
     */
    public void setStateIcon(Icon stateIcon) {
      this.mStateIcon = stateIcon;
    }
  } // CompositeIcon
  
}