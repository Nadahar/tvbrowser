package devplugin;

/**
 * A interface for a object that supports return an ActionMenu
 * for a program.
 * 
 * @author René Mach
 *
 */
public interface ContextMenuIf {
  
  public ActionMenu getContextMenuActions(Program program);
  
  public String getId();
  
}
