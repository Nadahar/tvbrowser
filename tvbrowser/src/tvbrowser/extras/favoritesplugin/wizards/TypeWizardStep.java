package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.favoritesplugin.core.TopicFavorite;
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

  private JRadioButton mTitleRb;

  private JRadioButton mTopicRb;

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
          "Warum moechten Sie an diese Sendung als Lieblingssendung markieren?");
    }
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Create new Favorite");
  }

  public JPanel createContent(final WizardHandler handler) {

    LinkButton expertBtn = new LinkButton(mLocalizer.msg("advancedView", "Switch to Classic View"), null);

    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("5dlu, pref, pref:grow",
        "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    panelBuilder.add(new JLabel(mMainQuestion), cc.xyw(1, 1, 3));
    panelBuilder
        .add(mTitleRb = new JRadioButton(mLocalizer.msg("option.title", "Ich mag diese Sendung:")), cc.xy(2, 3));
    panelBuilder.add(mProgramNameTf = new JTextField(), cc.xy(3, 3));
    panelBuilder.add(mTopicRb = new JRadioButton(mLocalizer.msg("option.topic", "Mich interessiert das Thema:")), cc
        .xy(2, 5));
    panelBuilder.add(mTopicTf = new JTextField(), cc.xy(3, 5));
    panelBuilder.setBorder(Borders.DLU4_BORDER);
    panelBuilder.add(expertBtn, cc.xyw(1, 7, 3));
    ButtonGroup group = new ButtonGroup();
    group.add(mTitleRb);
    group.add(mTopicRb);

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
  }

  private Favorite createFavorite() {
    if (mTitleRb.isSelected()) {
      String title = mProgramNameTf.getText();
      if (title != null && title.length() > 0) {
        return new TitleFavorite(mProgramNameTf.getText());
      }
    } else if (mTopicRb.isSelected()) {
      String topic = mTopicTf.getText();
      if (topic != null && topic.length() > 0) {
        return new TopicFavorite(mTopicTf.getText());
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
      return title != null && title.length() > 0;
    } else if (mTopicRb.isSelected()) {
      String topic = mTopicTf.getText();
      return topic != null && topic.length() > 0;
    }

    return false;
  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL, WizardStep.BUTTON_NEXT };
  }

}
