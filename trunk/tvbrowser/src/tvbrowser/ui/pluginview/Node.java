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

package tvbrowser.ui.pluginview;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Date;
import devplugin.NodeFormatter;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 21:41:07
 */
public class Node extends DefaultMutableTreeNode {

  // We distinguish between the following node types:
  public static final int ROOT = 0;
  public static final int PLUGIN_ROOT = 1;
  public static final int PROGRAM = 2;
  public static final int STRUCTURE_NODE = 3; // a node created by the PluginTreeNode object
  public static final int CUSTOM_NODE = 4;   // a node created by the plugin

  private byte mType;

  private ArrayList<ActionMenu> mActionMenuList;

  private NodeFormatter mNodeFormatter;
  
  private boolean mShowLeafCount;
  
  private int mLeafCount = 0;
  
  private Icon mIcon;
  
  private ProgramReceiveTarget mReceiveTarget;
  
  private static NodeFormatter mDefaultNodeFormatter = new NodeFormatter(){
    public String format(ProgramItem item) {
      if (item == null) {
        return "<null>";
      }
      Program program = item.getProgram();
      if (program == null) {
        return "<null>";
      }
      StringBuilder builder = new StringBuilder().append(
          program.getTimeString()).append("  ").append(program.getTitle())
          .append(" (").append(program.getChannel().getName()).append(')');

      /*if(program.getDate().equals(Date.getCurrentDate().addDays(-1))) {
        builder.insert(0," ").insert(0,Localizer.getLocalization(Localizer.I18N_YESTERDAY));
      }
      else if(program.getDate().equals(Date.getCurrentDate())) {
        builder.insert(0," ").insert(0,Localizer.getLocalization(Localizer.I18N_TODAY));
      }
      else if(program.getDate().equals(Date.getCurrentDate().addDays(1))) {
        builder.insert(0," ").insert(0,Localizer.getLocalization(Localizer.I18N_TOMORROW));
      }
      else {
        builder.insert(0," ").insert(0,program.getDate().toString());
      }*/
      
      return builder.toString();
    }
  };
  
  private static NodeFormatter mDefaultDateNodeFormatter = new NodeFormatter(){
    public String format(ProgramItem item) {
      if (item == null) {
        return "<null>";
      }
      Program program = item.getProgram();
      if (program == null) {
        return "<null>";
      }
      int h = program.getHours();
      int m = program.getMinutes();
      
      StringBuilder builder = new StringBuilder().append(h).append(':').append(m < 10 ? "0" : "").append(m).append("  ").append(program.getTitle()).append(" (").append(program.getChannel().getName()).append(')');

      if(program.getDate().equals(Date.getCurrentDate().addDays(-1))) {
        builder.insert(0," ").insert(0,Localizer.getLocalization(Localizer.I18N_YESTERDAY));
      }
      else if(program.getDate().equals(Date.getCurrentDate())) {
        builder.insert(0," ").insert(0,Localizer.getLocalization(Localizer.I18N_TODAY));
      }
      else if(program.getDate().equals(Date.getCurrentDate().addDays(1))) {
        builder.insert(0," ").insert(0,Localizer.getLocalization(Localizer.I18N_TOMORROW));
      }
      else {
        builder.insert(0," ").insert(0,program.getDate().toString());
      }
      
      return builder.toString();
    }
  };

  public Node(byte type, Object object) {
    super(object);
    mType = type;
    mActionMenuList = null; // defer initialization to save memory
    if(type == ROOT) {
      mShowLeafCount = false;
    } else {
      mShowLeafCount = true;
    }
  }

  public Node(int type, Object object) {
    this((byte)type, object);
  }

  public Node(ProgramItem programItem) {
    this(PROGRAM, programItem);
    setAllowsChildren(false);
  }
  
  public void setIcon(Icon icon) {
    mIcon = icon;
  }
  
  public Icon getIcon() {
    return mIcon;
  }
  
  public void setProgramReceiveTarget(ProgramReceiveTarget receiveTarget) {
    mReceiveTarget = receiveTarget;
  }
  
  public ProgramReceiveTarget getProgramReceiveTarget() {
    return mReceiveTarget;
  }

  public void setNodeFormatter(NodeFormatter formatter) {
    mNodeFormatter = formatter;
  }
  
  public void setShowLeafCountEnabled(boolean enable) {
    mShowLeafCount = enable;
  }
  
  public boolean isShowLeafCount() {
    return mShowLeafCount;
  }

  public NodeFormatter getNodeFormatter() {
    return getNodeFormatter(false);
  }
  
  /**
   * Gets the NodeFormatter for this node.
   * <p>
   * @param isWeekNodesEnabled If the programs are shown in week orders.
   * @return The node formatter for this node.
   * @since 2.7
   */
  public NodeFormatter getNodeFormatter(boolean isWeekNodesEnabled) {
    if (mNodeFormatter != null) {
      return mNodeFormatter;
    }
    Node parentNode = (Node)getParent();
    if (parentNode != null) {
      return parentNode.getNodeFormatter();
    }
    return isWeekNodesEnabled ? mDefaultDateNodeFormatter : mDefaultNodeFormatter;
  }

  public void addActionMenu(ActionMenu menu) {
    if (mActionMenuList == null) {
      mActionMenuList = new ArrayList<ActionMenu>(1);
    }
    mActionMenuList.add(menu);
  }

  public void removeActionMenu(ActionMenu menu) {
    if (mActionMenuList == null) {
      return;
    }
    mActionMenuList.remove(menu);
  }

  public void removeAllActionMenus() {
    if (mActionMenuList == null) {
      return;
    }
    mActionMenuList.clear();
    mActionMenuList = null;
  }

  public ActionMenu[] getActionMenus() {
    if (mActionMenuList == null) {
      return new ActionMenu[0];
    }
    ActionMenu[] result = new ActionMenu[mActionMenuList.size()];
    mActionMenuList.toArray(result);
    return result;
  }

  public int getType() {
    return mType;
  }

  public boolean isLeaf() {
    return !getAllowsChildren();
  }
  
  public void setLeafCount(int leafs) {
    mLeafCount = leafs;
  }
  
  public int getLeafCount() {
    return mLeafCount;
  }
  
  public boolean isDirectoryNode() {
    return mType != PROGRAM;
  }

}
