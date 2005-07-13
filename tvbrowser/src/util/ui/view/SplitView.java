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

package util.ui.view;

import java.awt.Component;

import javax.swing.JSplitPane;

import util.settings.Property;


public class SplitView extends AbstractView {

  private Component mComponent;
  private int mAbsoluteDividerLocation;


  public SplitView() {
  }

  public void setComponents(Component[] components) {

    if (components.length == 1) {
      mComponent = components[0];
    }
    else if (components.length == 2) {
      JSplitPane splitPane = new JSplitPane();
      splitPane.setContinuousLayout(true);
      splitPane.setOneTouchExpandable(true);
      splitPane.setLeftComponent(components[0]);
      splitPane.setRightComponent(components[1]);
      mComponent = splitPane;
    }
    else {
      throw new IllegalArgumentException("invalid number of components: "+components.length);
    }
  }

  public void update() {
      ViewProperty properties = getProperty();

      onPropertiesChanged(properties);
  }

  protected void onPropertiesChanged(ViewProperty properties) {

    if (mComponent instanceof JSplitPane && properties instanceof SplitViewProperty) {
      JSplitPane splitPane = (JSplitPane)mComponent ;
      SplitViewProperty prop = (SplitViewProperty)properties;
     // if (prop.getFixedComponent() == SplitViewProperties.LEFT) {
     //   splitPane.setResizeWeight(0.0);
     // }
      if (prop.getLeftComponentFixed()) {
        splitPane.setResizeWeight(0.0);
      }
      else {
        splitPane.setResizeWeight(1.0);
      }
      splitPane.setOrientation(prop.getVerticalSplit()?JSplitPane.VERTICAL_SPLIT:JSplitPane.HORIZONTAL_SPLIT);

      int abs = prop.getFixedComponentWidth();
      int width = mComponent.getWidth();
      int height = mComponent.getHeight();

      if (prop.getLeftComponentFixed()) {
        splitPane.setDividerLocation(abs);
      }
      else {
        int dividerLocation = (prop.getVerticalSplit()?height:width) - abs;
        splitPane.setDividerLocation(dividerLocation);
      }
    }
  }

  public Component getContent() {
    return mComponent;
  }

  public void storeProperties() {
    Property property = getProperty();
    if (property instanceof SplitViewProperty && mComponent instanceof JSplitPane) {
      JSplitPane splitPane = (JSplitPane)mComponent;
      SplitViewProperty prop = (SplitViewProperty)property;
      Component comp;
      if (prop.getLeftComponentFixed()) {
        comp = splitPane.getLeftComponent();
      }
      else {
        comp = splitPane.getRightComponent();
      }

      if (comp != null) {
        if (prop.getVerticalSplit()) {
          prop.setFixedComponentWidth(comp.getHeight() + 1);
        }
        else {
          if(prop.getLeftComponentFixed())
            prop.setFixedComponentWidth(comp.getWidth() + 1);
          else
            prop.setFixedComponentWidth(comp.getWidth() + splitPane.getDividerSize() + 1);
        }
      }
    }

  }
}