/*
 * Created on 24.09.2004
 */
package tvbrowser.core.plugin.beanshell;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.Icon;

import devplugin.PluginInfo;
import devplugin.Program;


/**
 * @author bodum
 */
public interface BeanShellScriptIf {

    /* (non-Javadoc)
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetInfo()
     */
    public abstract PluginInfo getInfo();
    
    
    /* (non-Javadoc)
     * @see tvbrowser.core.plugin.AbstractPluginProxy#setParentFrame(java.awt.Frame)
     */
    public void setParentFrame(Frame parent);
    
    /* (non-Javadoc)
     * @see tvbrowser.core.plugin.AbstractPluginProxy#goGetContextMenuActions(devplugin.Program)
     */    
    public Action[] getContextMenuActions(Program program);
    

    /* (non-Javadoc)
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetButtonAction()
     */
    public Action getButtonAction();
    
    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetProgramTableIconText()
     */
    public String getProgramTableIconText();
    
    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetProgramTableIcons(devplugin.Program)
     */
    public Icon[] getProgramTableIcons(Program program);
}
