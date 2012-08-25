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
package tvbrowser.extras.favoritesplugin;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.extras.common.AbstractInternalPluginProxy;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import devplugin.ActionMenu;
import devplugin.AfterDataUpdateInfoPanel;
import devplugin.ContextMenuIf;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginCenterPanelWrapper;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Encapsulates the FavoritesPlugin and manages the access to it.
 *
 * @author René Mach
 */
public class FavoritesPluginProxy extends AbstractInternalPluginProxy implements ButtonActionIf, ContextMenuIf, Marker {

  private static final String PROGRAM_TARGET_TITLE_FAVORITE = "target_favorite_title";
  private static FavoritesPluginProxy mInstance;
  private Icon mMarkIcon;
  private Icon[] mMarkIconArr;
  private Icon[] mMultipleIconArr;

  private FavoritesPluginProxy() {
    mInstance = this;
  }

  /**
   * @return The instance of the FavoritesPluginProxy
   */
  public static FavoritesPluginProxy getInstance() {
    if(mInstance == null) {
      new FavoritesPluginProxy();
    }

    return mInstance;
  }

  public ActionMenu getContextMenuActions(Program program) {
    return getFavoritesInstance().getContextMenuActions(program);
  }

  public String getId() {
    return FavoritesPlugin.getFavoritesPluginId();
  }

  public String toString() {
    return FavoritesPlugin.getName();
  }

  public Icon getMarkIcon() {
    createIcons();
    return mMarkIcon;
  }

  private void createIcons() {
    if (mMarkIcon == null) {
      mMarkIcon = FavoritesPlugin.getFavoritesIcon(16);
      mMultipleIconArr = new Icon[] { getDoubleIcon(mMarkIcon, 12) };
      mMarkIconArr = new Icon[] { mMarkIcon };
    }
  }

  public Icon[] getMarkIcons(final Program program) {
    createIcons();
    if (FavoriteTreeModel.getInstance().isInMultipleFavorites(program)) {
      return mMultipleIconArr;
    } else {
      return mMarkIconArr;
    }
  }

  public int getMarkPriorityForProgram(Program p) {
    return getFavoritesInstance().getMarkPriority();
  }

  public Icon getIcon() {
    return getMarkIcon();
  }

  public String getName() {
    return toString();
  }

  public String getButtonActionDescription() {
    return FavoritesPlugin.mLocalizer.msg("description","Automatically marks your favorite programs and passes them to other plugins.");
  }

  public SettingsTab getSettingsTab() {
    return new FavoritesSettingTab();
  }

  public String getSettingsId() {
    return SettingsItem.FAVORITE;
  }

  public ActionMenu getButtonAction() {
    return FavoritesPlugin.getButtonAction();
  }

  private Icon getDoubleIcon(final Icon icon, final int width) {
    try {
      // just in case the original icon could not be loaded
      if (icon == null) {
        return null;
      }
      // Create Image with Icon
      BufferedImage iconimage = new BufferedImage(icon.getIconWidth(), icon
          .getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = iconimage.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
      AffineTransform z = g2.getTransform();
      double scale = (double) width / icon.getIconWidth();
      z.scale(scale, scale);
      g2.setTransform(z);
      icon.paintIcon(null, g2, 0, 0);
      icon.paintIcon(null, g2, icon.getIconWidth() - width, icon
          .getIconHeight()
          - width);
      g2.dispose();

      // Return new Icon
      return new ImageIcon(iconimage);

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return icon;
  }

  @Override
  public void handleTvDataUpdateFinished() {
    getFavoritesInstance().handleTvDataUpdateFinished();
  }

  private static FavoritesPlugin getFavoritesInstance() {
    return FavoritesPlugin.getInstance();
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    getFavoritesInstance().addTitleFavorites(programArr);
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this,
        FavoritesPlugin.mLocalizer.msg("programTarget", "Create favorite from title"), PROGRAM_TARGET_TITLE_FAVORITE) };
  }

  @Override
  public String getPluginCategory() {
    return Plugin.OTHER_CATEGORY;
  }

  @Override
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return FavoritesPlugin.getInstance().getPluginCenterPanelWrapper();
  }

  @Override
  public AfterDataUpdateInfoPanel getAfterDataUpdateInfoPanel() {
    return FavoritesPlugin.getInstance().getAfterDataUpdateInfoPanel();
  }
}
