/*
 * Created on 24.09.2004
 */
package tvbrowser.core.plugin;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import tvdataservice.MutableChannelDayProgram;
import util.exc.TvBrowserException;
import bsh.Interpreter;
import devplugin.ActionMenu;
import devplugin.ChannelDayProgram;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramRatingIf;
import devplugin.ProgramReceiveTarget;
import devplugin.beanshell.BeanShellScriptIf;


/**
 * @author bodum
 */
public class BeanShellPluginProxy extends AbstractPluginProxy {

    private static final ImageIcon BEANICON = new ImageIcon("imgs/beanshell.png");

    private File mBshFile;

    private BeanShellScriptIf mScript;

    public BeanShellPluginProxy(File bshFile) {
        mBshFile = bshFile;
        loadScript(mBshFile);
    }

    /**
     * @param bshFile
     */
    private void loadScript(File bshFile) {
        try {
            mScript = (BeanShellScriptIf) new Interpreter().source(bshFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            mScript = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#setParentFrame(java.awt.Frame)
     */
    void setParentFrame(Frame parent) {

        if (mScript == null) { return; }

        try {
            mScript.setParentFrame(parent);
        } catch (Exception e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doLoadSettings(java.io.File)
     */
    protected void doLoadSettings(File userDirectory) throws TvBrowserException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doSaveSettings(java.io.File)
     */
    protected void doSaveSettings(File userDirectory) throws TvBrowserException {
      doSaveSettings(userDirectory, true);
    }

    protected void doSaveSettings(File userDirectory, boolean log) throws TvBrowserException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetInfo()
     */
    protected PluginInfo doGetInfo() {
        if (mScript != null) {
            try {
                return mScript.getInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new PluginInfo(mBshFile.getName());
    }


    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetSettingsTab()
     */
    protected SettingsTabProxy doGetSettingsTab() {
        // TODO Auto-generated method stub
        return null;
    }

    private void setActionMenuDefaultValues(ActionMenu menu) {
      Action action = menu.getAction();
      if (action != null) {
        if (action.getValue(Action.SMALL_ICON) == null) {
          action.putValue(Action.SMALL_ICON, BEANICON);
        }
        if (action.getValue(Action.NAME) == null) {
          action.putValue(Action.NAME, mBshFile.getName());
        }
      }
      else {
        ActionMenu[] subItems = menu.getSubItems();
        for (int i=0; i<subItems.length; i++) {
          setActionMenuDefaultValues(subItems[i]);
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#goGetContextMenuActions(devplugin.Program)
     */
    protected ActionMenu goGetContextMenuActions(Program program) {
        if (mScript == null) { return null; }

        try {

          ActionMenu actionMenu = mScript.getContextMenuActions(program);
          setActionMenuDefaultValues(actionMenu);

          return actionMenu;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetButtonAction()
     */
    protected ActionMenu doGetButtonAction() {
        
        if (mScript == null) { return null; }

        try {
            ActionMenu menu = mScript.getButtonAction();
            setActionMenuDefaultValues(menu);
            return menu;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetMarkIcons()
     */
    protected Icon[] doGetMarkIcons(Program p) {
        return new Icon[] {BEANICON};
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetProgramTableIconText()
     */
    protected String doGetProgramTableIconText() {
        if (mScript != null) {
            try {
                return mScript.getProgramTableIconText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doGetProgramTableIcons(devplugin.Program)
     */
    protected Icon[] doGetProgramTableIcons(Program program) {
        if (mScript != null) {
            try {
                return mScript.getProgramTableIcons(program);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doHandleTvDataUpdateFinished()
     */
    protected void doHandleTvDataUpdateFinished() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doHandleTvDataAdded(devplugin.ChannelDayProgram)
     */
    protected void doHandleTvDataAdded(ChannelDayProgram newProg) {
        // TODO Auto-generated method stub

    }
    
    /* 
     * (non-Javadoc)
     * 
     * @see  tvbrowser.core.plugin.AbstractPluginProxy#doHandleTvBrowserStartFinished()
     */
    protected void doHandleTvBrowserStartFinished() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see tvbrowser.core.plugin.AbstractPluginProxy#doHandleTvDataDeleted(devplugin.ChannelDayProgram)
     */
    protected void doHandleTvDataDeleted(ChannelDayProgram oldProg) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.PluginAccess#getId()
     */
    public String getId() {
        return "bsh." + mBshFile.getName();
    }

 
    public void doOnActivation() {        
    }

    public void doOnDeactivation() {       
    }

    public boolean doCanUseProgramTree() {
        return false;
    }
       
    public PluginTreeNode getRootNode() {
      return null;
    }

    @Override
    protected boolean doCanReceiveProgramsWithTarget() {
      // TODO Automatisch erstellter Methoden-Stub
      return false;
    }

    @Override
    protected boolean doReceivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
      // TODO Automatisch erstellter Methoden-Stub
      return false;
    }

    @Override
    protected ProgramReceiveTarget[] doGetProgramReceiveTargets() {
      // TODO Automatisch erstellter Methoden-Stub
      return null;
    }

    @Override
    protected PluginsProgramFilter[] doGetAvailableFilter() {
      // TODO Automatisch erstellter Methoden-Stub
      return null;
    }

    @Override
    protected boolean doIsAllowedToDeleteProgramFilter(PluginsProgramFilter programFilter) {
      // TODO Automatisch erstellter Methoden-Stub
      return false;
    }

    @Override
    protected  Class<? extends PluginsFilterComponent>[] doGetAvailableFilterComponentClasses() {
      // TODO Automatisch erstellter Methoden-Stub
      return null;
    }

    @Override
    protected int doGetMarkPriorityForProgram(Program p) {
      // TODO Automatisch erstellter Methoden-Stub
      return Program.MIN_MARK_PRIORITY;
    }

    public String getPluginFileName() {
      return mBshFile.getPath();
    }

  public ProgramRatingIf[] getProgramRatingIfs() {
    return null;
  }

  public String getButtonActionDescription() {
      return mScript.getInfo().getDescription();
    }

    @Override
    protected void doHandleTvDataAdded(MutableChannelDayProgram newProg) {
      // TODO Auto-generated method stub
      
    }

    @Override
    protected boolean doReceiveValues(String[] values,
        ProgramReceiveTarget receiveTarget) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    protected void doHandleTvDataTouched(ChannelDayProgram removedDayProgram,
        ChannelDayProgram addedDayProgram) {
      // TODO Auto-generated method stub
      
    }
 }