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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.customizableitems;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class CustomizableItemsPanel extends JPanel {


  private final DefaultListModel leftListModel, rightListModel;
  private final JList leftList, rightList;

  public CustomizableItemsPanel(String leftText, String rightText) {

    setLayout(new GridLayout(1,2));

    JPanel leftPanel=new JPanel(new BorderLayout());
    JPanel rightPanel=new JPanel(new BorderLayout());

    leftListModel=new DefaultListModel();
    rightListModel=new DefaultListModel();

    leftList=new JList(leftListModel);
    rightList=new JList(rightListModel);

    leftList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (leftList.getSelectedIndex()>=0) {
          rightList.clearSelection();
        }
      }
    }
    );

    rightList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (rightList.getSelectedIndex()>=0) {
          leftList.clearSelection();
        }
      }
    }
    );

    leftList.setVisibleRowCount(10);
    rightList.setVisibleRowCount(10);

    JLabel leftLabel=new JLabel(leftText);
    JLabel rightLabel=new JLabel(rightText);

    leftLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
    rightLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

    leftPanel.add(leftLabel,BorderLayout.NORTH);
    rightPanel.add(rightLabel,BorderLayout.NORTH);

    leftPanel.add(new JScrollPane(leftList),BorderLayout.CENTER);
    rightPanel.add(new JScrollPane(rightList),BorderLayout.CENTER);

    JPanel leftButtons=new JPanel(new GridLayout(2,1));
    JPanel rightButtons=new JPanel(new GridLayout(2,1));

    JPanel panel2=new JPanel(new BorderLayout());
    JPanel panel3=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());
    JPanel panel5=new JPanel(new BorderLayout());

    JButton btnRight=new JButton("-->");
    JButton btnLeft=new JButton("<--");
    JButton btnUp=new JButton("up");
    JButton btnDown=new JButton("down");

    panel2.add(btnRight,BorderLayout.SOUTH);
    panel3.add(btnLeft,BorderLayout.NORTH);
    panel4.add(btnUp,BorderLayout.SOUTH);
    panel5.add(btnDown,BorderLayout.NORTH);

    leftButtons.add(panel2);
    leftButtons.add(panel3);

    rightButtons.add(panel4);
    rightButtons.add(panel5);

    leftButtons.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    rightButtons.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));

    leftPanel.add(leftButtons,BorderLayout.EAST);
    rightPanel.add(rightButtons,BorderLayout.EAST);

    add(leftPanel);
    add(rightPanel);


    btnRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int inx=leftList.getSelectedIndex();
        if (inx<0) {
          return;
        }

        String ch=(String)leftListModel.remove(inx);
        int inPos=rightListModel.size();
        rightListModel.add(inPos,ch);
        rightList.setSelectedIndex(inPos);
        if (inx<leftListModel.size()) {
          leftList.setSelectedIndex(inx);
        }else {
          leftList.setSelectedIndex(inx-1);
        }
        leftList.ensureIndexIsVisible(inx);
      }
    });

    btnLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int inx=rightList.getSelectedIndex();
        if (inx<0) {
          return;
        }

        String ch=(String)rightListModel.remove(inx);
        leftListModel.add(0,ch);
        leftList.ensureIndexIsVisible(0);
        leftList.setSelectedIndex(0);
        if (inx<rightListModel.size()) {
          rightList.setSelectedIndex(inx);
        }else {
          rightList.setSelectedIndex(inx-1);
        }
        rightList.ensureIndexIsVisible(inx);
      }
    });

    btnUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int inx=rightList.getSelectedIndex();
        if (inx<1) {
          return;
        }

        String ch=(String)rightListModel.remove(inx--);
        rightListModel.add(inx,ch);
        rightList.setSelectedIndex(inx);
        rightList.ensureIndexIsVisible(inx);


      }
    });

    btnDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        int inx=rightList.getSelectedIndex();
        if (inx<0 || inx==rightListModel.size()-1) {
          return;
        }

        String ch=(String)rightListModel.remove(inx++);
        rightListModel.add(inx,ch);
        rightList.setSelectedIndex(inx);
        rightList.ensureIndexIsVisible(inx);
      }
    });


  }

  public void addElementLeft(String item) {
    leftListModel.addElement(item);
  }

  public void addElementRight(String item) {
    rightListModel.addElement(item);
  }

  public Object[] getElementsLeft() {
    return leftListModel.toArray();
  }

  public Object[] getElementsRight() {
    return rightListModel.toArray();
  }

  public String getLeftSelection() {
    return (String)leftList.getSelectedValue();
  }

  public String getRightSelection() {
    return (String)rightList.getSelectedValue();
  }

  public void addListSelectionListenerLeft(final CustomizableItemsListener listener) {
    leftList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listener.leftListSelectionChanged(e);
      }
    }
    );
  }

  public void addListSelectionListenerRight(final CustomizableItemsListener listener) {
    rightList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        listener.rightListSelectionChanged(e);
      }
    }
    );
  }

  public static CustomizableItemsPanel createCustomizableItemsPanel(String leftText, String rightText) {
    return new CustomizableItemsPanel(leftText, rightText);
  }
}