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

package printplugin.dlgs.components;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import devplugin.Date;



public class DateRangePanel extends JPanel {

  static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(DateRangePanel.class);

  private JComboBox mDateCb;
  private JSpinner mDayCountSpinner;
  private JRadioButton mAllRb, mDateRb;

  public DateRangePanel(boolean provideAllOption) {

    setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("period","Zeitraum")));

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JPanel pn1 = new JPanel(new BorderLayout());
    mAllRb = new JRadioButton("Alles");
    pn1.add(mAllRb, BorderLayout.WEST);

    JPanel pn2 = new JPanel();
    pn2.setLayout(new BoxLayout(pn2,BoxLayout.X_AXIS));
    if (provideAllOption) {
      pn2.add(mDateRb = new JRadioButton());
    }
    pn2.add(new JLabel(mLocalizer.msg("from","Von")));
    pn2.add(mDateCb = new JComboBox(createDateObjects(21)));
    pn2.add(new JLabel(mLocalizer.msg("TVlistingsFor", "Programm fuer")));
    pn2.add(mDayCountSpinner = new JSpinner(new SpinnerNumberModel(5,1,28,1)));
    pn2.add(new JLabel(mLocalizer.msg("days","Tage")));

    // Dispache the KeyEvent to the RootPane for Closing the Dialog.
    // Needed for Java 1.4.
    mDayCountSpinner.getEditor().getComponent(0).addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
          mDayCountSpinner.getRootPane().dispatchEvent(e);
      }
    });
    
    JPanel pn3 = new JPanel(new BorderLayout());
    pn3.add(pn2, BorderLayout.WEST);

    if (provideAllOption) {
      add(pn1);
    }
    add(pn3);

    ButtonGroup group = new ButtonGroup();
    group.add(mAllRb);
    group.add(mDateRb);



  }

  private Date[] createDateObjects(int days) {
    Date[] result = new Date[days];
    Date today = Date.getCurrentDate();
    for (int i=0;i<result.length;i++) {
      result[i]=today.addDays(i);
    }
    return result;
  }

  public void setFromDate(Date date) {
    if (date == null) {
      mAllRb.setSelected(true);
    }
    else {
      mDateCb.setSelectedItem(date);
    }
  }

  public void setNumberOfDays(int days) {
    mDayCountSpinner.setValue(new Integer(days));
  }

  public Date getFromDate() {
    if (mAllRb.isSelected()) {
      return null;
    }
    return (Date)mDateCb.getSelectedItem();
  }

  public int getNumberOfDays() {
    return ((Integer)mDayCountSpinner.getValue()).intValue();
  }

}
