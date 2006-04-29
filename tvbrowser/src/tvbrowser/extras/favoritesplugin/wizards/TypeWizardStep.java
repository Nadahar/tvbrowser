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

package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.*;
import tvbrowser.extras.favoritesplugin.dlgs.EditFavoriteDialog;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import util.ui.LinkButton;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

public class TypeWizardStep extends AbstractWizardStep {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TypeWizardStep.class);

  private JTextField mProgramNameTf;

  private JTextField mTopicTf;

  private JTextField mActorsTf;

  private JRadioButton mTitleRb;

  private JRadioButton mTopicRb;

  private JRadioButton mActorsRb;

  private Favorite mFavorite;

  private JPanel mContent;

  private Program mProgram;

  private String mMainQuestion;

  public TypeWizardStep() {
    this(null);
  }

  public TypeWizardStep(Program program) {
    mProgram = program;
    if (mProgram == null) {
      mMainQuestion = mLocalizer.msg("mainQuestion.create",
          "Waehlen Sie eine Bedingung die die Lieblingssendung erfüllen muß:");
    } else {
      mMainQuestion = mLocalizer.msg("mainQuestion.edit",
          "Warum moechten Sie diese Sendung als Lieblingssendung markieren?");
    }
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Create new Favorite");
  }

  public JPanel createContent(final WizardHandler handler) {

    LinkButton expertBtn = new LinkButton(mLocalizer.msg("advancedView", "Switch to Classic View"), null);

    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("5dlu, pref, pref:grow",
        "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.add(new JLabel(mMainQuestion), cc.xyw(1, 1, 3));
    panelBuilder
        .add(mTitleRb = new JRadioButton(mLocalizer.msg("option.title", "Ich mag diese Sendung:")), cc.xy(2, 3));
    panelBuilder.add(mProgramNameTf = new JTextField(), cc.xy(3, 3));
    panelBuilder.add(mTopicRb = new JRadioButton(mLocalizer.msg("option.topic", "Mich interessiert das Thema:")), cc.xy(2, 5));
    panelBuilder.add(mTopicTf = new JTextField(), cc.xy(3, 5));

    panelBuilder.add(mActorsRb = new JRadioButton(mLocalizer.msg("option.actors","I like these actors:")), cc.xy(2,7));
    panelBuilder.add(mActorsTf = new JTextField(), cc.xy(3,7));
    panelBuilder.setBorder(Borders.DLU4_BORDER);
    panelBuilder.add(expertBtn, cc.xyw(1, 9, 3));
    ButtonGroup group = new ButtonGroup();
    group.add(mTitleRb);
    group.add(mTopicRb);
    group.add(mActorsRb);

    mTitleRb.setSelected(true);

    updateTextfields();

    mTitleRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateTextfields();
      }
    });

    mTopicRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateTextfields();
      }
    });

    mActorsRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateTextfields();
      }
    });

    expertBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handler.closeCurrentStep();
        String title;
        if (mProgram != null) {
          title = mProgram.getTitle();
        } else {
          title = "";
        }
        AdvancedFavorite favorite = new AdvancedFavorite(title);
        Component parent = UiUtilities.getBestDialogParent(null);
        EditFavoriteDialog dlg;
        if (parent instanceof Dialog) {
          dlg = new EditFavoriteDialog((Dialog) parent, favorite);
        } else {
          dlg = new EditFavoriteDialog((Frame) parent, favorite);
        }
        UiUtilities.centerAndShow(dlg);
        if (dlg.getOkWasPressed()) {
          FavoritesPlugin.getInstance().addFavorite(favorite);
          FavoritesPlugin.getInstance().updateRootNode();
          
          if(ManageFavoritesDialog.getInstance() != null)
            ManageFavoritesDialog.getInstance().addFavorite(favorite);
        }
      }
    });

    if (mProgram != null) {
      mProgramNameTf.setText(mProgram.getTitle());
    }

    mContent = panelBuilder.getPanel();
    return mContent;
  }

  private void updateTextfields() {
    mProgramNameTf.setEnabled(mTitleRb.isSelected());
    mTopicTf.setEnabled(mTopicRb.isSelected());
    mActorsTf.setEnabled(mActorsRb.isSelected());
  }

  private Favorite createFavorite() {
    if (mTitleRb.isSelected()) {
      String title = mProgramNameTf.getText();
      if (title != null && title.length() > 0) {
        return new TitleFavorite(title);
      }
    } else if (mTopicRb.isSelected()) {
      String topic = mTopicTf.getText();
      if (topic != null && topic.length() > 0) {
        return new TopicFavorite(topic);
      }
    } else if (mActorsRb.isSelected()) {
      String actors = mActorsTf.getText();
      if (actors != null && actors.length() > 0) {
        return new ActorsFavorite(actors);
      }
    }

    return null;
  }

  public Object createDataObject(Object obj) {
    mFavorite = createFavorite();
    return mFavorite;
  }

  public WizardStep next() {
    return new NotificationWizardStep(this, mProgram);
  }

  public WizardStep back() {
    return null;
  }

  public boolean isValid() {
    if (mTitleRb.isSelected()) {
      String title = mProgramNameTf.getText();
      if (title != null && title.trim().length() > 0) {
        return true;
      }
      JOptionPane.showMessageDialog(mContent,
          mLocalizer.msg("warningTitleMessage", "Enter Title!"), 
          mLocalizer.msg("warningTitleTitle", "Enter Title"), 
          JOptionPane.WARNING_MESSAGE);
    } else if (mTopicRb.isSelected()) {
      String topic = mTopicTf.getText();
      if (topic != null && topic.trim().length() > 0) {
        return true;
      }
      JOptionPane.showMessageDialog(mContent,
          mLocalizer.msg("warningTopicMessage", "Enter Topic!"), 
          mLocalizer.msg("warningTopicTitle", "Enter Topic"), 
          JOptionPane.WARNING_MESSAGE);
    } else if (mActorsRb.isSelected()) {
      String actor = mActorsTf.getText();
      if (actor != null && actor.trim().length() > 0) {
        return true;
      }
      JOptionPane.showMessageDialog(mContent,
          mLocalizer.msg("warningActorsMessage", "Enter Actors!"),
          mLocalizer.msg("warningActorsTitle", "Enter Actors"),
          JOptionPane.WARNING_MESSAGE);
    }

    return false;
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_NEXT };
  }

}
