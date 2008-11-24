package mediathekplugin;

import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

public class MediathekProgramNode extends PluginTreeNode {

  @Override
  public boolean contains(Program prog, boolean recursive) {
    return false;
  }

  @Override
  public boolean contains(Program prog) {
    return false;
  }

  @Override
  public ProgramItem[] getProgramItems() {
    return new ProgramItem[0];
  }

  @Override
  public Program[] getPrograms() {
    return new Program[0];
  }

  public MediathekProgramNode(String title) {
    super(title);
    setGroupingByDateEnabled(false);
    getMutableTreeNode().setShowLeafCountEnabled(false);
  }

}
