package treeviewplugin;

import java.awt.Color;

public class Tools {

	/**
	 * Inserts html line breaks into given StringBuffer
	 * @param buffer buffer to be processed
	 * @param len num of chars after we start search of blanks
	 * @return String that contains line breaks
	 * @throws Exception
	 */
	protected static String addHTMLLineBreaks(StringBuffer buffer,int len)throws Exception{
		int pos=len;
		while(pos<buffer.length()){
			int gab=buffer.indexOf(" ",pos);
			if(gab>0){
				buffer.replace(gab,gab+1,"<br>");
				pos=gab+len;
			}else{
				pos=buffer.length();
			}
		}
		return buffer.toString();
	}
	
	protected static String stringToHtmlDoc(String b,int len)throws Exception{
		return "<html><body>"+addHTMLLineBreaks(new StringBuffer(b), len)+"</body></html>";		
	}
	
	
	/**
	 * Returns a HTML color representation of a given color object
	 * @param color
	 * @return HTML color representation
	 */
	protected static String getHTMLColor(Color color){
		return Integer.toHexString(color.getRGB() & 0x00ffffff );
	}
	
	 /**
     * Checks wether the memory consumption has reached a critical limit.
     * @return true if there is enough memory.
     */
	protected static boolean isEnoughFreeMemory(LogConsole log){
		
		  /*System.out.println(" free: " +
		  Runtime.getRuntime().freeMemory() + " total: " +
		  Runtime.getRuntime().totalMemory() + " max: " +
		  Runtime.getRuntime().maxMemory());*/
		 

		if (Runtime.getRuntime().maxMemory()
				- Runtime.getRuntime().totalMemory()
				+ Runtime.getRuntime().freeMemory() < 10000000) {
			log.msg("[TVP.buildDistinctTable] HIGH MEMORY consumption");
			/*
			 * System.out.println(" free: " +
			 * Runtime.getRuntime().freeMemory() + " total: " +
			 * Runtime.getRuntime().totalMemory() + " max: " +
			 * Runtime.getRuntime().maxMemory());
			 */

			Runtime.getRuntime().gc();

			return false;

		}
		return true;
	}
	protected static void dbgPrintMemoryConsumption(){
		
		  System.out.println(" free: " +
		  Runtime.getRuntime().freeMemory() + " total: " +
		  Runtime.getRuntime().totalMemory() + " max: " +
		  Runtime.getRuntime().maxMemory()+ " available: "+
		  (Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()));
		 
	}
}
