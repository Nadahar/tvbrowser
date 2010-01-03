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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import util.settings.ProgramPanelSettings;
import devplugin.Date;
import devplugin.Program;

/**
 * A list cell renderer that renders Programs.
 * <p>
 * <i>Keep in mind:</i> This Renderer internally uses "static" data for each
 * displayed program. If program data changes the container using this renderer
 * should be repainted to display the changed data.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramListCellRenderer extends DefaultListCellRenderer {

  private static final Color SECOND_ROW_COLOR = new Color(220, 220, 220, 150);
  private static final Color SECOND_ROW_COLOR_EXPIRED = new Color(220, 220, 220, 55);

  private JPanel mMainPanel;
  private JLabel mHeaderLb;
  private ProgramPanel mProgramPanel;

  /**
   * Creates a new instance of ProgramListCellRenderer
   */
  public ProgramListCellRenderer() {
    this(new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, false, true, 10));
  }

  /**
   * Creates a new instance of ProgramListCellRenderer
   * 
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramListCellRenderer(ProgramPanelSettings settings) {
    initializeSettings(settings);
  }

  private void initializeSettings(ProgramPanelSettings settings) {
    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setOpaque(true);

    mHeaderLb = new JLabel();
    mMainPanel.add(mHeaderLb, BorderLayout.NORTH);

    mProgramPanel = new ProgramPanel(settings);
    mMainPanel.add(mProgramPanel, BorderLayout.CENTER);
  }

  /**
   * Creates a new instance of ProgramListCellRenderer
   * 
   * @param settings
   *          The settings for the program panel.
   * @param axis
   *          The axis of the progress bar of the program panel.
   * @deprecated Since 2.7 Use
   *             {@link #ProgramListCellRenderer(ProgramPanelSettings)} instead.
   */
  @Deprecated
  public ProgramListCellRenderer(ProgramPanelSettings settings, int axis) {
    if (settings == null) {
      settings = new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, 1080, 1380, false, true, 90, axis);
    } else {
      settings = new ProgramPanelSettings(settings.getPictureShowingType(), settings.getPictureTimeRangeStart(),
          settings.getPictureTimeRangeEnd(), settings.isShowingOnlyDateAndTitle(), settings
              .isShowingPictureDescription(), settings.getDuration(), axis);
    }
    initializeSettings(settings);
  }

  /**
   * Return a component that has been configured to display the specified value.
   * That component's <code>paint</code> method is then called to "render" the
   * cell. If it is necessary to compute the dimensions of a list because the
   * list cells do not have a fixed size, this method is called to generate a
   * component on which <code>getPreferredSize</code> can be invoked.
   * 
   * @param list
   *          The JList we're painting.
   * @param value
   *          The value returned by list.getModel().getElementAt(index).
   * @param index
   *          The cells index.
   * @param isSelected
   *          True if the specified cell was selected.
   * @param cellHasFocus
   *          True if the specified cell has the focus.
   * @return A component whose paint() method will render the specified value.
   * 
   * @see JList
   * @see ListSelectionModel
   * @see ListModel
   */
  public Component getListCellRendererComponent(final JList list, Object value, final int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof Program) {

      Program program = (Program) value;
      Insets borderInsets = label.getBorder().getBorderInsets(label);
      mProgramPanel.setWidth(list.getWidth() - borderInsets.left - borderInsets.right);
      mProgramPanel.setProgram(program);
      mProgramPanel.setPaintExpiredProgramsPale(!isSelected);
      mProgramPanel.setTextColor(label.getForeground());
      mProgramPanel.setBackground(label.getBackground());

      program.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (list != null) {
            Object source = e.getSource();
            if (source instanceof Program) {
              Program program = (Program) source;
              AbstractListModel model = (AbstractListModel) list.getModel();
              ListDataListener[] listeners = model.getListDataListeners();
              int itemIndex = -1;
              for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i) == program) {
                  itemIndex = i;
                  break;
                }
              }
              if (itemIndex >= 0) {
                for (int i = 0; i < listeners.length; i++) {
                  listeners[i].contentsChanged(new ListDataEvent(program, ListDataEvent.CONTENTS_CHANGED, itemIndex, itemIndex));
                }
              }
            }
          }
        }
      });

      StringBuilder labelString = new StringBuilder();
      int days = program.getDate().getNumberOfDaysSince(Date.getCurrentDate());

      switch (days) {
      case -1: {
        labelString.append(Localizer.getLocalization(Localizer.I18N_YESTERDAY));
        labelString.append(", ").append(program.getDateString());
        break;
      }
      case 0: {
        labelString.append(Localizer.getLocalization(Localizer.I18N_TODAY));
        labelString.append(", ").append(program.getDateString());
        break;
      }
      case 1: {
        labelString.append(Localizer.getLocalization(Localizer.I18N_TOMORROW));
        labelString.append(", ").append(program.getDateString());
        break;
      }
      default: {
        labelString.append(program.getDate().toString());
      }
      }

      labelString.append(" - ").append(program.getChannel().getName());
      mHeaderLb.setText(labelString.toString());

      if (program.isExpired() && !isSelected) {
        mHeaderLb.setForeground(Color.gray);
      } else {
        mHeaderLb.setForeground(label.getForeground());
      }

      mMainPanel.setBackground(label.getBackground());

      if (isSelected) {
        mMainPanel.setForeground(label.getForeground());
      }

      mMainPanel.setEnabled(label.isEnabled());
      mMainPanel.setBorder(label.getBorder());

      if (((index & 1) == 1) && (!isSelected) && program.getMarkPriority() < Program.MIN_MARK_PRIORITY) {
        mMainPanel.setBackground(program.isExpired() ? SECOND_ROW_COLOR_EXPIRED : SECOND_ROW_COLOR);
      }
      
      return mMainPanel;
    }

    return label;
  }

}
