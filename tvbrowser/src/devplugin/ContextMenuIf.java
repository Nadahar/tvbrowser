package devplugin;

/**
 * A interface for a object that supports return an ActionMenu
 * for a program.
 * 
 * @author Ren� Mach
 *
 */
public interface ContextMenuIf {
  
  public ActionMenu getContextMenuActions(Program program);
  
  public String getId();
  
}
