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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.ActorsFavorite;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.favoritesplugin.core.TopicFavorite;
import tvbrowser.extras.favoritesplugin.dlgs.EditFavoriteDialog;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteNode;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.ui.mainframe.MainFrame;
import util.program.ProgramUtilities;
import util.ui.LinkButton;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

public class TypeWizardStep extends AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TypeWizardStep.class);

  private JTextField mProgramNameTf;

  private JTextField mTopicTf;

  private JComboBox mActorsCb;

  private JRadioButton mTitleRb;

  private JRadioButton mTopicRb;

  private JRadioButton mActorsRb;

  private JPanel mContent;

  private Program mProgram;

  private String mMainQuestion;

  /**
   * preselected actor in type step
   */
  private String mActor;
  
  private FavoriteNode mParentNode;

  /**
   * preselected topic in type step
   */
  private String mTopic;

  public TypeWizardStep() {
    this(null);
  }
  
  public TypeWizardStep(Program program) {
    this(program, (FavoriteNode) FavoriteTreeModel.getInstance().getRoot());
  }

  public TypeWizardStep(Program program, FavoriteNode parent) {
    mProgram = program;
    mParentNode = parent;
    
    if (mProgram == null) {
      mMainQuestion = mLocalizer.msg("mainQuestion.create",
          "Choose the condition that your favorite program needs to match:");
    } else {
      mMainQuestion = mLocalizer.msg("mainQuestion.edit",
          "Why is this program a favorite of yours?");
    }
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Create new Favorite");
  }

  public JPanel createContent(final WizardHandler handler) {

    LinkButton expertBtn = new LinkButton(mLocalizer.msg("advancedView", "Switch to expert view"), null);

    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("5dlu, pref, default:grow",
        "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.add(new JLabel(mMainQuestion), cc.xyw(1, 1, 3));
    panelBuilder
        .add(mTitleRb = new JRadioButton(mLocalizer.msg("option.title", "I like this program:")), cc.xy(2, 3));
    panelBuilder.add(mProgramNameTf = new JTextField(), cc.xy(3, 3));
    panelBuilder.add(mTopicRb = new JRadioButton(mLocalizer.msg("option.topic", "I like this subject:")), cc.xy(2, 5));
    panelBuilder.add(mTopicTf = new JTextField(), cc.xy(3, 5));

    panelBuilder.add(mActorsRb = new JRadioButton(mLocalizer.msg("option.actors","I like these actors:")), cc.xy(2,7));
    mActorsCb = new JComboBox();
    mActorsCb.setEditable(true);
    panelBuilder.add(mActorsCb, cc.xy(3,7));
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
        Window parent = UiUtilities
            .getLastModalChildOf(MainFrame.getInstance());
        EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
        UiUtilities.centerAndShow(dlg);
        if (dlg.getOkWasPressed()) {
          FavoriteTreeModel.getInstance().addFavorite(favorite, mParentNode);
          FavoritesPlugin.getInstance().updateRootNode(true);
          
          if(ManageFavoritesDialog.getInstance() != null) {
            ManageFavoritesDialog.getInstance().addFavorite(favorite, false);
          }
        }
      }
    });

    if (mProgram != null) {
      mProgramNameTf.setText(mProgram.getTitle());
      String[] actors = ProgramUtilities.getActorNames(mProgram);
      if (actors != null) {
        for (String actor : actors) {
          mActorsCb.addItem(actor);
        }
      }
    }

    mContent = panelBuilder.getPanel();
    
    mContent.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        handleFocusEvent();
      }
    });
    
    mContent.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        handleFocusEvent();
      }
    });
    
    if (mTopic != null) {
      mTopicRb.setSelected(true);
      updateTextfields();
      mTopicTf.setText(mTopic);
      // the topic might also be an actor name
      if (mActor == null && ProgramUtilities.getActorNames(mProgram) == null) {
        mActorsCb.setSelectedItem(mTopic);
      }
    } else if (mActor != null) {
      mActorsRb.setSelected(true);
      updateTextfields();
      mActorsCb.setSelectedItem(mActor);
    }
    
    return mContent;
  }
  
  private void handleFocusEvent() {
    if (mProgramNameTf.isEnabled()) {
      mProgramNameTf.requestFocusInWindow();
    } else if (mTopicTf.isEnabled()) {
      mTopicTf.requestFocusInWindow();
    } else if (mActorsCb.isEnabled()) {
      mActorsCb.requestFocusInWindow();
    }
  }

  protected void updateTextfields() {
    mProgramNameTf.setEnabled(mTitleRb.isSelected());
    mTopicTf.setEnabled(mTopicRb.isSelected());
    mActorsCb.setEnabled(mActorsRb.isSelected());
    handleFocusEvent();
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
      String actors = (String) mActorsCb.getSelectedItem();
      if (actors != null && actors.trim().length() > 0) {
        return new ActorsFavorite(actors);
      }
    }

    return null;
  }

  public Object createDataObject(Object obj) {
    return createFavorite();
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
      mProgramNameTf.requestFocusInWindow();
    } else if (mTopicRb.isSelected()) {
      String topic = mTopicTf.getText();
      if (topic != null && topic.trim().length() > 0) {
        return true;
      }
      JOptionPane.showMessageDialog(mContent,
          mLocalizer.msg("warningTopicMessage", "Enter Topic!"), 
          mLocalizer.msg("warningTopicTitle", "Enter Topic"), 
          JOptionPane.WARNING_MESSAGE);
      mTopicTf.requestFocusInWindow();
    } else if (mActorsRb.isSelected()) {
      String actor = (String) mActorsCb.getSelectedItem();
      if (actor != null && actor.trim().length() > 0) {
        return true;
      }
      JOptionPane.showMessageDialog(mContent,
          mLocalizer.msg("warningActorsMessage", "Enter Actors!"),
          mLocalizer.msg("warningActorsTitle", "Enter Actors"),
          JOptionPane.WARNING_MESSAGE);
      mActorsCb.requestFocusInWindow();
    }

    return false;
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_NEXT };
  }
  
  public void setActor(String actor) {
    if (actor == null || actor.trim().length() == 0) {
      return;
    }
    mActor = actor;
  }

  public void setTopic(String topic) {
    if (topic == null || topic.trim().length() == 0) {
      return;
    }
    mTopic = topic;
  }

}
