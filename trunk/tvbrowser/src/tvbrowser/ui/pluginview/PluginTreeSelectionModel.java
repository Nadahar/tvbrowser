package tvbrowser.ui.pluginview;

import devplugin.Plugin;
import devplugin.ProgramItem;

import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 31.12.2004
 * Time: 17:36:44
 */
public class PluginTreeSelectionModel implements TreeSelectionModel {

  private DefaultTreeSelectionModel mModel;
  private Plugin mCurrentSelectedPlugin;

  public PluginTreeSelectionModel() {
    mModel = new DefaultTreeSelectionModel();
  }

  public void addSelectionPath(TreePath path) {
    Plugin p = PluginTreeModel.getPlugin(path);
    if (p!=null && p.equals(mCurrentSelectedPlugin) && isPathToProgram(path)) {
      mModel.addSelectionPath(path);
    }
    else {
      setSelectionPath(path);
    }
  }

  public void setSelectionPath(TreePath path) {
    mCurrentSelectedPlugin = PluginTreeModel.getPlugin(path);
    mModel.setSelectionPath(path);
  }

  private boolean isPathToProgram(TreePath path) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
    Object o = node.getUserObject();
    return (o instanceof ProgramItem);

  }


  public int getLeadSelectionRow() {
    return mModel.getLeadSelectionRow();
  }

  public int getMaxSelectionRow() {
    return mModel.getMaxSelectionRow();
  }

  public int getMinSelectionRow() {
    return mModel.getMinSelectionRow();
  }

  public int getSelectionCount() {
    return mModel.getSelectionCount();
  }

  public int getSelectionMode() {
    return mModel.getSelectionMode();
  }

  public void clearSelection() {
   mModel.clearSelection();
  }

  public void resetRowSelection() {
    mModel.resetRowSelection();
  }

  public boolean isSelectionEmpty() {
    return mModel.isSelectionEmpty();
  }

  public int[] getSelectionRows() {
    return mModel.getSelectionRows();
  }

  public void setSelectionMode(int mode) {
    mModel.setSelectionMode(mode);
  }

  public boolean isRowSelected(int row) {
    return mModel.isRowSelected(row);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    mModel.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    mModel.removePropertyChangeListener(listener);
  }

  public void addTreeSelectionListener(TreeSelectionListener x) {
    mModel.addTreeSelectionListener(x);
  }

  public void removeTreeSelectionListener(TreeSelectionListener x) {
    mModel.removeTreeSelectionListener(x);
  }

  public RowMapper getRowMapper() {
    return mModel.getRowMapper();
  }

  public void setRowMapper(RowMapper newMapper) {
    mModel.setRowMapper(newMapper);
  }

  public TreePath getLeadSelectionPath() {
    return mModel.getLeadSelectionPath();
  }

  public TreePath getSelectionPath() {
    return mModel.getSelectionPath();
  }

  public TreePath[] getSelectionPaths() {
    return mModel.getSelectionPaths();
  }

  public void removeSelectionPath(TreePath path) {
    mModel.removeSelectionPath(path);
  }

  public boolean isPathSelected(TreePath path) {
    return mModel.isPathSelected(path);
  }

  public void addSelectionPaths(TreePath[] paths) {
    mModel.addSelectionPaths(paths);
  }

  public void removeSelectionPaths(TreePath[] paths) {
    mModel.removeSelectionPaths(paths);
  }

  public void setSelectionPaths(TreePath[] paths) {
    if (paths != null && paths.length > 0) {
      ArrayList list = new ArrayList();
      for (int i=0; i<paths.length; i++) {
        Plugin plugin = PluginTreeModel.getPlugin(paths[i]);
        if (mCurrentSelectedPlugin.equals(plugin) && isPathToProgram(paths[i])) {
          list.add(paths[i]);
        }
      }
      TreePath[] newPaths = new TreePath[list.size()];
      list.toArray(newPaths);
      mModel.setSelectionPaths(newPaths);
    }
    else {
      mModel.setSelectionPaths(paths);
    }
  }


}
