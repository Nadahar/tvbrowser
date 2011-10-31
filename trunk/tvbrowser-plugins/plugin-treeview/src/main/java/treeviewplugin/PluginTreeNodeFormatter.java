package treeviewplugin;

import devplugin.NodeFormatter;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramItem;

public class PluginTreeNodeFormatter implements NodeFormatter {

	public String format(ProgramItem item) {
		try{
            StringBuffer buffer=new StringBuffer();               			    		
    		Program program=item.getProgram();
			// Add program name and date            
			String name=program.getChannel().getName();
            if(name!=null){
                buffer.append(program.getChannel().getName());
            }
            buffer.append(" - ");
            buffer.append(program.getDateString());
            buffer.append(" ");
            buffer.append(program.getTimeFieldAsString(ProgramFieldType.START_TIME_TYPE));
            String endTime=program.getTimeFieldAsString(ProgramFieldType.END_TIME_TYPE);
            if(endTime!=null){
            	buffer.append("-");
            	buffer.append(endTime);
            }
						            
			// Add episode
			String episode=program.getTextField(ProgramFieldType.EPISODE_TYPE);
			if((episode!=null)&&(episode.length()>0)){				
                buffer.append(" - ");
                buffer.append(episode);
			}			
			return buffer.toString();
		}catch(Exception e){				
			
			e.printStackTrace();
		}
		return "";
	}

}
