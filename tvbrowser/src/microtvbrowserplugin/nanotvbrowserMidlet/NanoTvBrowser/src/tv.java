/*
 * tv.java
 *
 * Created on February 10, 2005, 5:57 PM
 *
 * Hauptklasse der NanoTvBrowser-Version.
 *
 */

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import java.io.DataInputStream;
import java.util.Calendar;
import java.util.Date;
/**
 *
 * @author  pumpkin
 */
public class tv extends javax.microedition.midlet.MIDlet implements javax.microedition.lcdui.CommandListener{
	
	private final static int STATUS_MAIN_MENU = 0;
	private final static int STATUS_PROG_LIST = 1;
	private final static int STATUS_DATE_LIST = 2;
	private final static int STATUS_CHAN_LIST = 3;
	
	private static int STATUS = STATUS_MAIN_MENU;
	
	private static Command CMD_GO_PROG_LIST;
	private static Command CMD_EXIT;
	private static Command CMD_BACK;
	
	private static Command CMD_VAR_1;
	private static Command CMD_VAR_2;
	
	private static String[] channel_names;
	
	private static long data_create_time;
	private static int data_days;
	private static int data_min_day;
	private static int data_number_of_channels;
	
	private static int day_to_show;
	private static int channel_to_show;
	private static int block_to_show;
	
	private final static String[] block_names = {"0-4h","4-8h","8-12h","12-16h","16-20h","20-24h"};
	
	private DataInputStream data_in;
	
	private int[] progs_in_block = new int[6];
	private int[] size_of_block = new int[6];
	
	private byte prog_hour;
	private byte prog_min;
	private String prog_title;
	private String prog_time;
	
	private static String error;
	private static String too_old;
	private static String show;
	private static String exit;
	private static String no_data;
	private static String back;
	private static String chan_list;
	private static String date_list;
	private static String wait;
	private static String working;
	
	private static exec command_exec;
	
	private static List list;
	
	private Calendar calendar;
	
	protected void destroyApp(boolean param) throws javax.microedition.midlet.MIDletStateChangeException {
		command_exec.stop = true;
		command_exec.execute(null,null);
		command_exec = null;
		notifyDestroyed();
	}
	
	/**
	 * pauseApp ist ein no-op. Die im Moment ausgeführte Operation wird abgeschlossen, fertig.
	 **/
	protected void pauseApp() {
		command_exec.stop = true;
		command_exec.execute(null,null);
		command_exec = null;
		notifyPaused();
	}
	
	protected void startApp(){
		Form F = new Form("NanoTvBrowser");
		F.append("NanoTvBrowser\n");
		if ((command_exec==null) || (!command_exec.isAlive())){
			F.append("loading...\n");
			command_exec = new exec(this);
			command_exec.start();
		} else {
			F.append("v. 0.12\n");
			F.append("www.tvbrowser.org\n");
			F.append("pumpkin@gmx.de\n");
			F.addCommand(CMD_GO_PROG_LIST);
			F.addCommand(CMD_EXIT);
		}
		F.setCommandListener(this);
		Display.getDisplay(this).setCurrent(F);
	}
	
	public void show_chan_list(){
		list = new List(chan_list,List.IMPLICIT,channel_names,null);
		list.addCommand(CMD_BACK);
		list.setCommandListener(this);
		CMD_VAR_1 = list.SELECT_COMMAND;
		Display.getDisplay(this).setCurrent(list);
	}
	
	public void show_date_list(){
		list = new List(date_list,List.IMPLICIT);
		for (int i=data_min_day;i<data_days;i++){
			calendar.setTime(new Date(data_create_time + (i*24*60*60*1000)));
			list.append(calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),null);
		}
		list.addCommand(CMD_BACK);
		list.setCommandListener(this);
		CMD_VAR_1 = list.SELECT_COMMAND;
		Display.getDisplay(this).setCurrent(list);
	}
	
	public boolean setActuel(){
		try {
			data_in = new DataInputStream(this.getClass().getResourceAsStream(channel_to_show+"."+this.day_to_show));
			for (int i =0;i<progs_in_block.length;i++){
				progs_in_block[i] = data_in.readUnsignedByte();
				size_of_block[i] = data_in.readUnsignedShort();
			}
			this.block_to_show = 0;
			return true;
		} catch (Exception E){
			return false;
		}
	}
	
	public boolean readProg(){
		try {
			prog_hour = (byte) data_in.readByte();
			prog_min = (byte) data_in.readByte();
			int length = data_in.readUnsignedByte();
			byte[] prog_title_raw = new byte[length];
			data_in.readFully(prog_title_raw);
			prog_title = new String(prog_title_raw,"ISO8859_1");
			if (prog_hour<10){
				prog_time = "0"+prog_hour+":";
			} else {
				prog_time = prog_hour+":";
			}
			if (prog_min<10){
				prog_time = prog_time + "0" + prog_min;
			} else {
				prog_time = prog_time + prog_min;
			}
			return true;
		} catch (Exception E){
			E.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Zeigt den nächsten Block an.
	 */
	public void show_prog_block(){
		calendar.setTime(new Date(data_create_time + (day_to_show*24*60*60*1000)));
		list = new List(channel_names[channel_to_show]+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),List.IMPLICIT);
		int prevblock = block_to_show - 1;
		System.out.println("prevblock: "+prevblock);
		if ( (prevblock >= 0) || ((prevblock < 0) && ((day_to_show) > data_min_day)) ){
			if (prevblock < 0){
				prevblock = 5;
			}
			System.out.println("prevblock NOW: "+prevblock);
			list.append("<< "+block_names[prevblock],null);
		}
		
		if (progs_in_block[block_to_show] == 0){
			list.append(no_data,null);
		} else {
			for (int i=0;i<progs_in_block[block_to_show];i++){
				readProg();
				list.append(prog_time+" "+prog_title,null);
			}
		}
		System.out.println("block_to_show: "+block_to_show);
		System.out.println("day_to_show: "+day_to_show);
		if ((block_to_show <= 6) || (block_to_show > 6) && (day_to_show+1 > data_days)){
			System.out.println  (block_to_show);
			int temp = block_to_show + 1;
			if (temp > 5){
				temp = 0;
			}
			list.append(">> "+block_names[temp],null);
		}
		block_to_show++;
		list.addCommand(CMD_BACK);
		CMD_VAR_1 = new Command(channel_names[((channel_to_show+1) % channel_names.length)],Command.OK,0);
		list.addCommand(CMD_VAR_1);
		CMD_VAR_2 = list.SELECT_COMMAND;
		list.setCommandListener(this);
		Display.getDisplay(this).setCurrent(list);
	}
	
	public void commandAction(javax.microedition.lcdui.Command command, javax.microedition.lcdui.Displayable displayable) {
		try {
			if (STATUS == this.STATUS_MAIN_MENU){
				if (command == CMD_GO_PROG_LIST){
					STATUS = this.STATUS_CHAN_LIST;
					show_chan_list();
					return;
				}
			}
			if (STATUS == this.STATUS_CHAN_LIST){
				if (command == this.CMD_BACK){
					STATUS = this.STATUS_MAIN_MENU;
					startApp();
					return;
				}
				if (command == this.CMD_VAR_1){
					channel_to_show = list.getSelectedIndex();
					list = null;
					STATUS = this.STATUS_DATE_LIST;
					show_date_list();
					return;
				}
			}
			if (command == CMD_EXIT){
				notifyDestroyed();
			}
			if (STATUS == this.STATUS_DATE_LIST){
				if (command == this.CMD_BACK){
					STATUS = this.STATUS_CHAN_LIST;
					show_chan_list();
					return;
				}
			}
			if (STATUS == this.STATUS_PROG_LIST){
				if (command == CMD_BACK){
					STATUS = STATUS_DATE_LIST;
					show_date_list();
					list.setSelectedIndex(day_to_show-data_min_day,true);
					return;
				}
			}
		} catch (Exception E){
			E.printStackTrace();
		}
		Displayable display = Display.getDisplay(this).getCurrent();
		Form F = new Form(wait);
		F.append (working);
		Display.getDisplay(this).setCurrent(F);
		command_exec.execute(command,display);
	}
	
	public void skipBlock() throws Exception {
		long temp = size_of_block[block_to_show];
		while (temp !=0){
			temp -= data_in.skip(temp);
		}
		block_to_show++;
	}
	
	public boolean commandActionSlow(Command command){
		try {
			if (STATUS == this.STATUS_DATE_LIST){
				if (command == this.CMD_VAR_1){
					day_to_show = list.getSelectedIndex() + data_min_day;
					list = null;
					setActuel();
					calendar.setTime(new Date(System.currentTimeMillis()));
					int to_seek = calendar.get(calendar.HOUR_OF_DAY) / 4;
					for (int i=0;i<to_seek;i++){
						skipBlock();
					}
					STATUS = STATUS_PROG_LIST;
					show_prog_block();
					return true;
				}
			}
			
			if (STATUS == this.STATUS_PROG_LIST){
				if (command == this.CMD_VAR_1){
					channel_to_show = (channel_to_show + 1) % channel_names.length;
					int block_temp = block_to_show-1;
					setActuel();
					for (int i=0;i<block_temp;i++){
						skipBlock();
					}
					show_prog_block();
					return true;
				}
				if (command == this.CMD_VAR_2){
					if (list.getSelectedIndex() == 0){
						int day_temp = day_to_show;
						int block_temp = block_to_show;
						block_temp -= 2;
						if (block_temp < 0){
							day_temp--;
							block_temp = 5;
						}
						if (day_temp >= data_min_day){
							day_to_show = day_temp;
							setActuel();
							for (int i=0;i<block_temp;i++){
								skipBlock();
							}
							show_prog_block();
							return true;
						}
						return false;
					} else 	if (list.getSelectedIndex() == list.size()-1){
						System.out.println("block_to_show: "+block_to_show+" "+data_days);
						if (block_to_show < 6){
							show_prog_block();
							return true;
						} else {
							if (day_to_show+1 < data_days){
								day_to_show++;
								setActuel();
								show_prog_block();
								return true;
							}
							return false;
						}
					} else {
						return false;
					}
				}
			}
		} catch (Exception E){
		}
		notifyDestroyed();
		return true;
	}
	
	public void init() {
		calendar = Calendar.getInstance();
		try {
			/**
			 * Datenformat von conf:
			 * StringUTF:
			 *    error
			 *    too_old
			 *    show
			 *    no_data
			 *    exit
			 *    back
			 *    chan_list
			 *    date_list
			 *    wait
			 *    working
			 **/
			DataInputStream DIN = new DataInputStream(this.getClass().getResourceAsStream("conf"));
			
			error =   DIN.readUTF();
			too_old = DIN.readUTF();
			show =    DIN.readUTF();
			no_data = DIN.readUTF();
			exit =    DIN.readUTF();
			back =    DIN.readUTF();
			chan_list = DIN.readUTF();
			date_list = DIN.readUTF();
			wait = DIN.readUTF();
			working = DIN.readUTF();
			
			CMD_GO_PROG_LIST = new Command(show,Command.OK,0);
			CMD_EXIT = new Command(exit,Command.EXIT,0);
			CMD_BACK = new Command(back,Command.CANCEL,0);
			
			data_create_time = DIN.readLong();
			data_days = DIN.readUnsignedByte();
			
			Calendar C = Calendar.getInstance();
			C.setTime(new Date(System.currentTimeMillis()));
			
			long max_delta_time = (1+data_days)*24*60*60*1000;
			long delta = System.currentTimeMillis() - data_create_time;
			data_min_day = (int)((delta) / (24*60*60*1000));
			data_min_day = Math.max(0,data_min_day - 1);
			
			if (data_min_day >= data_days){
				Form F = new Form(error);
				F.append(too_old);
				F.addCommand(CMD_EXIT);
				F.setCommandListener(this);
				Display.getDisplay(this).setCurrent(F);
				return;
			}
			data_number_of_channels = DIN.readUnsignedByte();
			channel_names = new String[data_number_of_channels];
			for (int i=0;i<data_number_of_channels;i++){
				channel_names[i] = DIN.readUTF();
			}
			DIN.close();
			startApp();
		} catch (Exception E){
			Alert A = new Alert("Error","conf load failed",null,AlertType.ERROR);
			A.setCommandListener(this);
			Display.getDisplay(this).setCurrent(A);
		}
	}
}
