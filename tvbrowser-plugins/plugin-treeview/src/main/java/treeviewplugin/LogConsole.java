package treeviewplugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogConsole {
   
    private SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String newline = System.getProperty("line.separator");
    private StringBuffer log=new StringBuffer();
    private boolean enabled=false;
    
    public LogConsole(){    
    }
   
   
    protected void msg(String msg){
        if(!enabled)return;
        log.append(df.format(new Date()));
        log.append(" ");
        log.append(msg);
        log.append(newline);
    }
    
    protected void addStackTrace(Exception exception){
    	StringWriter sw = new StringWriter();
    	exception.printStackTrace(new PrintWriter(sw));
    	log.append(sw.toString());
    	
    }
   
    protected void setEnabled(boolean e){
        enabled=e;
        log.append("[Status] MaxMemory: ");
        log.append(Runtime.getRuntime().maxMemory());
        log.append(" TotalMemory: ");
        log.append(Runtime.getRuntime().totalMemory());
        log.append(" FreeMemory: ");
        log.append(Runtime.getRuntime().freeMemory());
        log.append(newline);
        log.append("[Status] OS: ");
        log.append(System.getProperty("os.name"));
        log.append(" Java: ");
        log.append(System.getProperty("java.runtime.version"));  
        log.append(newline);
    }
   
    protected String getLog(){
       return log.toString();
    }
    
    protected boolean isEnabled(){
    	return enabled;
    }

}
