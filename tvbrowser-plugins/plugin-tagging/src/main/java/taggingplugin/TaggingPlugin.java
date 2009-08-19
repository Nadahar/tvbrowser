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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
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
import javax.swing.SwingUtilities;

import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

/**
 * @author bananeweizen
 * 
 */
final public class TaggingPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 0);

  private static TaggingPlugin mInstance;

	private PluginInfo mPluginInfo;

	private ImageIcon mIcon;

	/** The localizer for this class. */
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(TaggingPlugin.class);

	private static final String MAIN_URL = "http://tv-browser.appspot.com/";
//  private static final String MAIN_URL = "http://localhost:8080/";

  private static final String TARGET = "TAGGING_TARGET";

	private HashMap<String, ArrayList<String>> mTags = new HashMap<String, ArrayList<String>>();

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
		AbstractAction newTagAction = getNewTagAction(program);
    if (tagsAction != null && !tagsAction.isEmpty()) {
			tagsAction.add(0, ContextMenuSeparatorAction.getInstance());
			tagsAction.add(0, newTagAction);
			return new ActionMenu(new ContextMenuAction(mLocalizer.msg(
					"contextMenuTags", "Tags"), getPluginIcon()), tagsAction
					.toArray(new AbstractAction[tagsAction.size()]));
		}
    newTagAction.putValue(Action.SMALL_ICON, getPluginIcon());
		return new ActionMenu(newTagAction);
	}

	private ArrayList<AbstractAction> getTagsActions(final Program program) {
		ArrayList<String> tags = getProgramTags(program);
		if (tags == null || tags.isEmpty()) {
			return null;
		}
		ArrayList<AbstractAction> tagAction = new ArrayList<AbstractAction>();
		for (String tag : tags) {
			tagAction.add(getShowTagAction(tag));
		}
		if (!tagAction.isEmpty()) {
			return tagAction;
		}
		return null;
	}

	private AbstractAction getShowTagAction(final String tag) {
		return new AbstractAction(tag, getPluginIcon()) {
			public void actionPerformed(ActionEvent arg0) {
				ProgramListDialog dialog = new ProgramListDialog(getParentFrame(), findMatchingPrograms(tag), mLocalizer.msg("programsTagged", "Programs tagged '{0}'", tag));
				dialog.setVisible(true);
			}
		};
	}

	protected Program[] findMatchingPrograms(final String wantedTag) {
		ArrayList<Program> programs = new ArrayList<Program>();
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
						if (isProgramTagged(program, wantedTag)) {
							programs.add(program);
							break;
						}
					}
				}
			}
			date = date.addDays(1);
		}
		return programs.toArray(new Program[programs.size()]);
	}

	private AbstractAction getNewTagAction(final Program program) {
		AbstractAction action = new AbstractAction(mLocalizer.msg("contextMenuTag", "Add tag"), createImageIcon("actions","document-new",16)) {
			public void actionPerformed(ActionEvent e) {
				addTag(program);
			}
		};
		return action;
	}
	
	private String getTagInput(final String message) {
	  return TagValidation.makeValidTag(JOptionPane.showInputDialog(UiUtilities.getBestDialogParent(getParentFrame()), message));
	}

	protected void addTag(final Program program) {
		final String tag = getTagInput(mLocalizer.msg("addTag", "Add tag for {0}", program.getTitle()));
		Thread tagThread = new Thread("Add tag") {
		  @Override
		  public void run() {
		    if (postTagToServer(program, tag)) {
		      try {
            SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                updateRootNodeIfVisible();
              }
            });
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            e.printStackTrace();
          }
		    }
		  }
		};
		tagThread.start();
	}

	private boolean isProgramTagged(final Program program, final String tag) {
    ArrayList<String> tags = getProgramTags(program);
    if (tags != null) {
      for (String programTag : tags) {
        if (programTag.equalsIgnoreCase(tag)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean postTagToServer(final Program program, final String tag) {
    if (tag == null) {
      return false;
    }
    if (isProgramTagged(program, tag)) {
      return false;
    }
		try {
			HashMap<String,String> params = new HashMap<String, String>();
		  params.put("title", program.getTitle());
		  params.put("tag", tag);
			URLConnection conn = createUrl("addtag",params).openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			addTagInternal(program.getTitle(), tag);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private URL createUrl(final String target, final HashMap<String, String> params) throws MalformedURLException, UnsupportedEncodingException {
	  params.put("v", String.valueOf(getVersion().getMajor() * 100 + getVersion().getMinor()));
    StringBuilder builder = new StringBuilder(100);
    builder.append(MAIN_URL).append(target).append("?");
    for (Entry<String, String> param : params.entrySet()) {
      builder.append(param.getKey()).append('=').append(URLEncoder.encode(param.getValue(), "UTF-8")).append('&');
    }
    builder.deleteCharAt(builder.length() - 1);
    return new URL(builder.toString());
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
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				int pos = inputLine.indexOf('\t');
				if (pos >= 0) {
					String programTitle = inputLine.substring(0, pos).trim();
					// assume that any tag from server is corrupt
					String tag = TagValidation.makeValidTag(inputLine.substring(pos + 1));
					if (tag != null && !programTitle.isEmpty() && !tag.isEmpty()) {
					  if (addTagInternal(programTitle, tag)) {
						  System.out.println(programTitle + ": " + tag);
						}
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


  private boolean addTagInternal(final String programTitle, final String tag) {
    ArrayList<String> list = mTags.get(programTitle);
    if (list == null) {
      list = new ArrayList<String>();
      mTags.put(programTitle, list);
    }
    if (!list.contains(tag)) {
      list.add(tag);
      return true;
    }
    return false;
  }

  ArrayList<String> getProgramTags(final Program program) {
    return mTags.get(program.getTitle());
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
		HashMap<String, PluginTreeNode> nodes = new HashMap<String, PluginTreeNode>(
				30);

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
						ArrayList<String> tags = mTags.get(program.getTitle());
						if (tags != null) {
							for (String tag : tags) {
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

  public static TaggingPlugin getInstance() {
    return mInstance;
  }

  public TaggingPlugin() {
    mInstance = this;
  }
  
  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {TagFilterComponent.class};
  }
  
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    if (programArr == null || programArr.length == 0) {
      return true;
    }
    final String tag = getTagInput(mLocalizer.msg("tagPrograms", "Add tag to {0} programs", programArr.length));
    if (tag != null) {
      int count = 0;
      for (Program program : programArr) {
        if (postTagToServer(program, tag)) {
          count++;
          if (count >= 20) {
            break;
          }
        }
      }
      if (count > 0) {
        updateRootNodeIfVisible();
      }
    }
    return true;
  }
  
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    final ProgramReceiveTarget target = new ProgramReceiveTarget(this, mLocalizer.msg("target", "Tag program"), TARGET);
    return new ProgramReceiveTarget[] {target};
  }
}
