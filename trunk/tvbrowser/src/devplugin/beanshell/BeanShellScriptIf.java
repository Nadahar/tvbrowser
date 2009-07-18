/*
 * Created on 24.09.2004
 */
package devplugin.beanshell;

import java.awt.Frame;

import javax.swing.Icon;

import devplugin.ActionMenu;
import devplugin.PluginInfo;
import devplugin.Program;


/**
 * @author bodum
 */
public interface BeanShellScriptIf {

    public abstract PluginInfo getInfo();
    
    
    public void setParentFrame(Frame parent);
    
    public ActionMenu getContextMenuActions(Program program);
    

    public ActionMenu getButtonAction();
    
    public String getProgramTableIconText();
    
    public Icon[] getProgramTableIcons(Program program);
}
