/*
 * Created on 01.11.2004
 */
package devplugin.beanshell;

import devplugin.Program;


/**
 * @author bodum
 */
public interface BeanShellProgramFilterIf {

    public boolean accept(Program prg);
}
