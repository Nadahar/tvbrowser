/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package taggingplugin;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
final public class TaggingPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 0);

  private PluginInfo mPluginInfo;

  private ImageIcon mIcon;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TaggingPlugin.class);

  private static final String MAIN_URL = "http://tv-browser.appspot.com/";
  
  private HashMap<String, String> mTags = new HashMap<String, String>();

  private PluginTreeNode mRootNode;

  private boolean mStartFinished;

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Tags");
      final String desc = mLocalizer.msg("description",
          "Allows grouping programs with tags");
      mPluginInfo = new PluginInfo(TaggingPlugin.class, name, desc,
          "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    // special handling of example program
    if (program == null
        || program.equals(getPluginManager().getExampleProgram())
        || getPluginManager().getFilterManager() == null) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("contextMenu",
          "Tagging"), getPluginIcon()));
    }
    
    ArrayList<AbstractAction> tagsAction = getTagsActions(program);
    if (tagsAction != null && !tagsAction.isEmpty()) {
      tagsAction.add(0, ContextMenuSeparatorAction.getInstance());
      tagsAction.add(0, getNewTagAction(program));
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("contextMenuTags", "Tags")),tagsAction.toArray(new AbstractAction[tagsAction.size()]));
    }
    return new ActionMenu(getNewTagAction(program));
  }
  
  private ArrayList<AbstractAction> getTagsActions(final Program program) {
    String title = program.getTitle();
    ArrayList<AbstractAction> tags = new ArrayList<AbstractAction>();
    for (Entry<String, String> titleTag : mTags.entrySet()) {
      if (titleTag.getKey().equals(title)) {
        tags.add(new AbstractAction(titleTag.getValue()) {
          public void actionPerformed(ActionEvent arg0) {
          }
        });
      }
    }
    if (!tags.isEmpty()) {
      return tags;
    }
    return null;
  }

  private AbstractAction getNewTagAction(final Program program) {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (addTag(program)) {
          updateRootNodeIfVisible();
        }
      }};
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuTag", "Add tag"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    return action;
  }

  protected boolean addTag(final Program program) {
    String tag = JOptionPane.showInputDialog(UiUtilities.getBestDialogParent(getParentFrame()), mLocalizer.msg("addTag", "Add tag for {0}", program.getTitle()));
    if (tag != null) {
      tag = tag.trim();
      if (tag.length() < 3) {
        return false;
      }
      if (tag.length() > 40) {
        return false;
      }
      return postTagToServer(program, tag);
    }
    return false;
  }

  private boolean postTagToServer(final Program program, final String tag) {
    try {
      URL url = new URL(MAIN_URL + "tag?title=" + URLEncoder.encode(program.getTitle(), "UTF-8") + "&tag=" + URLEncoder.encode(tag, "UTF-8"));
      URLConnection conn = url.openConnection();

      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
      return true;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("tag.png"));
    }
    return mIcon;
  }
  
  @Override
  public void handleTvDataUpdateFinished() {
    updateRootNodeIfVisible();
  }

  private void updateTagsFromServer() {
    try {
      URL url = new URL(MAIN_URL + "tags");
      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        System.out.println(inputLine);
        int pos = inputLine.indexOf('\t');
        if (pos >= 0) {
          String programTitle = inputLine.substring(0, pos).trim();
          String tag = inputLine.substring(pos + 1).trim();
          if (!programTitle.isEmpty() && !tag.isEmpty()) {
            mTags.put(programTitle, tag);
          }
        }
      }
      in.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this, false);
      mRootNode.getMutableTreeNode().setShowLeafCountEnabled(false);
      if (mStartFinished) {
        // update the tree as the plugin view has been switched on for the first
        // time after start
        new Thread(new Runnable() {
            public void run() {
                updateRootNode();
            }
        }).start();
      }
    }
    return mRootNode;
  }
  
  @Override
  public void handleTvBrowserStartFinished() {
    updateRootNodeIfVisible();
  }

  /**
   * trigger an update of the plugin tree, but only if it is visible at all
   */
  private void updateRootNodeIfVisible() {
    mStartFinished = true;
    // update tree, but only if it is shown at all
    if (mRootNode != null) {
      updateRootNode();
    }
  }

  private void updateRootNode() {
    // do nothing if the tree is not visible or the main program still has work
    // to do
    if (!mStartFinished) {
      return;
    }
    updateTagsFromServer();
    mRootNode.removeAllChildren();
    HashMap<String, PluginTreeNode> nodes = new HashMap<String, PluginTreeNode>(30);
    
    // search all programs
    final Channel[] channels = devplugin.Plugin.getPluginManager()
        .getSubscribedChannels();
    Date date = Date.getCurrentDate();
    for (int days = 0; days < 30; days++) {
      for (Channel channel : channels) {
        final Iterator<Program> iter = Plugin.getPluginManager()
            .getChannelDayProgram(date, channel);
        if (iter != null) {
          while (iter.hasNext()) {
            final Program program = iter.next();
            // collect tagged programs in new nodes
            String tag = mTags.get(program.getTitle());
            if (tag != null) {
              PluginTreeNode node = nodes.get(tag);
              if (node == null) {
                node = new PluginTreeNode(tag);
                nodes.put(tag, node);
              }
              node.addProgram(program);
            }
          }
        }
      }
      date = date.addDays(1);
    }
    PluginTreeNode[] nodeArray = new PluginTreeNode[nodes.size()];
    nodes.values().toArray(nodeArray);
    Arrays.sort(nodeArray);
    for (PluginTreeNode node : nodeArray) {
      mRootNode.add(node);
    }
    mRootNode.update();
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }
}
