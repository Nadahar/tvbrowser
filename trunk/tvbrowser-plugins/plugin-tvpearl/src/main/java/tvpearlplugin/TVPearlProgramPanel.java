/*
 * TV-Pearl by Reinhard Lehrbaum
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
package tvpearlplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Marker;
import devplugin.Program;

public class TVPearlProgramPanel extends JPanel implements ChangeListener {
  private static final long serialVersionUID = 1L;

  transient private TVPProgram mPearlProgram;
  transient private Program mProgram;
  private Color mTextColor;
  private Font mHeaderFont;
  private Font mBodyFont;
  private Icon[] mIconList = null;

  public TVPearlProgramPanel(final TVPProgram p) {
    mPearlProgram = p;
    mProgram = p.getProgram();
    mHeaderFont = new Font("Dialog", Font.PLAIN, 12);
    mBodyFont = new Font("Dialog", Font.BOLD, 12);
    fillIconList();
    createUI();
    addNotify();
  }

  private void createUI() {
    Color color = mTextColor;
    final Calendar now = Calendar.getInstance();

    if ((mProgram != null && mProgram.isExpired()) || mPearlProgram.getStart().before(now)) {
      color = Color.gray;
    }

    setLayout(new FormLayout("pref, pref, fill:min:grow, pref", "pref, pref"));
    CellConstraints cc = new CellConstraints();
    setOpaque(true);
    JLabel label = new JLabel(TVPearlPlugin.getInstance().getProgramIcon(mProgram != null));
    label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    add(label, cc.xywh(1, 1, 1, 2));

    label = new JLabel(getHeader());
    label.setForeground(color);
    label.setFont(mHeaderFont);
    add(label, cc.xy(2, 1));

    label = new JLabel(getAuthor());
    label.setForeground(color);
    label.setFont(mHeaderFont);
    label.setAlignmentX(Component.RIGHT_ALIGNMENT);
    add(label, cc.xyw(3, 1, 2, CellConstraints.RIGHT, CellConstraints.TOP));

    label = new JLabel(getBody());
    label.setForeground(color);
    label.setFont(mBodyFont);
    add(label, cc.xyw(2, 2, 2));

    if (mIconList != null) {
      JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 1));
      iconPanel.setOpaque(false);
      iconPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
      add(iconPanel, cc.xy(4, 2));
      for (int i = 0; i < mIconList.length; i++) {
        label = new JLabel(mIconList[i]);
        label.setOpaque(false);
        iconPanel.add(label);
      }
    }

  }

  private String getHeader() {
    return TVPearlPlugin.getDayName(mPearlProgram.getStart(), true) + ", "
        + DateFormat.getDateInstance().format(mPearlProgram.getStart().getTime()) + " - " + mPearlProgram.getChannel()
        + "   ";
  }

  private String getAuthor() {
    return mPearlProgram.getAuthor();
  }

  private String getBody() {
    final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    return timeFormat.format(mPearlProgram.getStart().getTime()) + " " + mPearlProgram.getTitle();
  }

  private void fillIconList() {
    if (mProgram != null) {
      final ArrayList<Icon> list = new ArrayList<Icon>();

      final Marker[] markedByPluginArr = mProgram.getMarkerArr();
      for (Marker marker : markedByPluginArr) {
        final Icon[] icons = marker.getMarkIcons(mProgram);
        if (icons != null) {
          for (int i = icons.length - 1; i >= 0; i--) {
            list.add(icons[i]);
          }
        }
      }
      mIconList = list.toArray(new Icon[list.size()]);
    }
  }

  public void setTextColor(final Color col) {
    mTextColor = col;
  }

  public void stateChanged(final ChangeEvent e) {
    if (e.getSource() == mProgram) {
      TVPearlPlugin.getInstance().updateDialog();
    }
  }

  public void addNotify() {
    super.addNotify();
    if (mProgram != null) {
      mProgram.addChangeListener(this);
    }
  }

  public void removeNotify() {
    super.removeNotify();
    if (mProgram != null) {
      mProgram.removeChangeListener(this);
    }
  }
}
