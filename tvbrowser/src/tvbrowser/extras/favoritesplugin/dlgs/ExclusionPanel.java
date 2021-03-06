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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin.dlgs;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

/**
 * A class with exclusion settings.
 */
public class ExclusionPanel extends JPanel{
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ExclusionPanel.class);
  
  private JList<Exclusion> mExclusionsList;
  
  private JButton mNewExclusionBtn;
  private JButton mEditExclusionBtn;
  private JButton mDeleteExclusionBtn;
  
  private boolean mWasAdded;
  private boolean mWasEditedOrDeleted;

  /**
   * Creates an instance of this class.
   * 
   * @param exclusions
   *          The current exclusions.
   * @param parent
   *          The parent dialog of this panel.
   * @param favorite
   *          The favorite of the exclusions or <code>null</code> if it is for
   *          global exclusions.
   */
  public ExclusionPanel(Exclusion[] exclusions, final Window parent,
      final Favorite favorite) {
    mWasEditedOrDeleted = mWasAdded = false;
    
    setLayout(new FormLayout("5dlu, fill:pref:grow, 3dlu, pref",
        "pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:pref:grow"));

    CellConstraints cc = new CellConstraints();

    DefaultListModel<Exclusion> listModel = new DefaultListModel<>();
    mExclusionsList = new JList<>(listModel);
    
    Arrays.sort(exclusions);
    
    for (Exclusion exclusion : exclusions) {
      listModel.addElement(exclusion);
    }
   // mExclusionsList.setCellRenderer(new ExclusionListCellRenderer());
    
    mExclusionsList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        updateExclusionListButtons();
      }
    });

    mExclusionsList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2
            && mEditExclusionBtn.isEnabled()) {
          mEditExclusionBtn.getActionListeners()[0].actionPerformed(null);
        }
      }
    });

    add(new JScrollPane(mExclusionsList), cc.xywh(2, 1, 1, 5));

    Icon newIcon = TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL);
    Icon editIcon = TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL);
    Icon deleteIcon = TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL);

    mNewExclusionBtn = new JButton(newIcon);
    mEditExclusionBtn = new JButton(editIcon);
    mDeleteExclusionBtn = new JButton(deleteIcon);

    mNewExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);
    mEditExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);

    mNewExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.newExclusion", "New exclusion criteria"));
    mEditExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.editExclusion", "Edit exclusion criteria"));
    mDeleteExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.deleteExclusion", "Delete exclusion criteria"));

    add(mNewExclusionBtn, cc.xy(4, 1));
    add(mEditExclusionBtn, cc.xy(4, 3));
    add(mDeleteExclusionBtn, cc.xy(4, 5));
    
    mNewExclusionBtn.addActionListener(e -> {
      WizardHandler handler = new WizardHandler(parent, new ExcludeWizardStep(favorite));
      Exclusion exclusion = (Exclusion) handler.show();
      if (exclusion != null) {
        ((DefaultListModel<Exclusion>) mExclusionsList.getModel()).addElement(exclusion);
        mWasAdded = true;
      }
    });

    mEditExclusionBtn.addActionListener(e -> {
      Exclusion oldExclusion = (Exclusion) mExclusionsList.getSelectedValue();
      WizardHandler handler = new WizardHandler(parent, new ExcludeWizardStep(favorite, oldExclusion));
      Exclusion newExclusion = (Exclusion) handler.show();
      if (newExclusion != null) {
        int inx = mExclusionsList.getSelectedIndex();
        ((DefaultListModel<Exclusion>) mExclusionsList.getModel()).setElementAt(newExclusion, inx);
        mWasEditedOrDeleted = true;
      }
    });

    mDeleteExclusionBtn.addActionListener(e -> {
      Exclusion exclusion = (Exclusion) mExclusionsList.getSelectedValue();
      if (exclusion != null) {
        ((DefaultListModel<Exclusion>) mExclusionsList.getModel()).removeElement(exclusion);
        mWasEditedOrDeleted = true;
      }
    });

    updateExclusionListButtons();
  }

  private void updateExclusionListButtons() {
    Object selectedItem = mExclusionsList.getSelectedValue();
    mEditExclusionBtn.setEnabled(selectedItem != null);
    mDeleteExclusionBtn.setEnabled(selectedItem != null);
  }
  
  /**
   * Gets the exclusions of this panel.
   * <p>
   * @return The exclusions of this panel.
   */
  public Exclusion[] getExclusions() {
    int exclCnt = ((DefaultListModel<Exclusion>) mExclusionsList.getModel()).size();
    Exclusion[] exclArr = new Exclusion[exclCnt];
    ((DefaultListModel<Exclusion>) mExclusionsList.getModel()).copyInto(exclArr);
    
    return exclArr;
  }
  
  /**
   * Gets if the exlusions were changed.
   * <p>
   * @return <code>True</code> if the exlusions were changed, <code>false</code> otherwise.
   */
  public boolean wasChanged() {
    return mWasEditedOrDeleted || mWasAdded;
  }
  
  /**
   * Gets if entries to exclusion list were added.
   * <p>
   * @return <code>True</code> if entries were added, <code>false</code> otherwise.
   */
  public boolean wasAdded() {
    return mWasAdded;
  }
  
  /**
   * Gets if entries to exclusion list were edited or deleted.
   * <p>
   * @return <code>True</code> if entries were edited or deleted, <code>false</code> otherwise.
   */
  public boolean wasEditedOrDeleted() {
    return mWasEditedOrDeleted;
  }
}
