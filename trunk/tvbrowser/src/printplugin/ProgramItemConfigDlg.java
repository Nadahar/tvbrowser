/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package printplugin;

import javax.swing.*;

import util.ui.OrderChooser;

import devplugin.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class ProgramItemConfigDlg extends JDialog {
  
  private OrderChooser mOrderChooser;
  public static final int OK=0;
  public static final int CANCEL=1;
  private int mResult;
  
  public ProgramItemConfigDlg(Frame parent, ProgramFieldType[] fieldTypes) {
    
    super(parent,true);
    setTitle("Sendungen anpassen");
    
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    JPanel southPn = new JPanel(new BorderLayout());
    JPanel btnPn = new JPanel();
    
    JButton okBt = new JButton("OK");
    JButton cancelBt = new JButton("Abbrechen");
    
    btnPn.add(okBt);
    btnPn.add(cancelBt);
    southPn.add(btnPn,BorderLayout.NORTH);
    
    JPanel centerPn = new JPanel(new BorderLayout());
    centerPn.add(mOrderChooser = new OrderChooser(fieldTypes, getAvailableTypes()), BorderLayout.NORTH);
    JLabel lb = new JLabel("<html>Bestimmen Sie welche Informationen und in welcher <br>" +
                                 "Reihenfolge diese Informationen dargestellt werden.<br>" +
                                 "Beachten Sie, dass je nach verfuegbarem Platz auf dem Papier<br>" +
                                 "nicht alles dargestellt werden kann.</html>");
    mOrderChooser.selectAll();
    centerPn.add(lb,BorderLayout.SOUTH);
    
    contentPane.add(centerPn,BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);
    
    
    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResult = OK;
        hide();
      }      
      });
    
    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResult = CANCEL;
        hide();
      }      
    }); 
    mResult = CANCEL;
    
    pack(); 
  }
  
  public int getResult() {
    return mResult;
  }
  
  public ProgramFieldType[] getProgramItemFieldTypes() {
    Object[] items = mOrderChooser.getOrder();
    ProgramFieldType[] result = new ProgramFieldType[items.length];
    System.arraycopy(items,0,result,0,result.length);
    return result;
  }
  
 
  private ProgramFieldType[] getAvailableTypes() {
    ArrayList typeList = new ArrayList();
    
    Iterator typeIter = ProgramFieldType.getTypeIterator();
    while (typeIter.hasNext()) {
      ProgramFieldType type = (ProgramFieldType) typeIter.next();
      
      if ((type.getFormat() != ProgramFieldType.BINARY_FORMAT)
        && (type != ProgramFieldType.INFO_TYPE)
        && (type != ProgramFieldType.START_TIME_TYPE)
        && (type != ProgramFieldType.END_TIME_TYPE)
        && (type != ProgramFieldType.TITLE_TYPE))
      {
        typeList.add(type);
      }
    }
    
    ProgramFieldType[] typeArr = new ProgramFieldType[typeList.size()];
    typeList.toArray(typeArr);
    return typeArr;
  }
  
  
  
  
}