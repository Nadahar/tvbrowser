/*
 * tv.java
 *
 * Created on February 10, 2005, 5:57 PM
 *
 * Hauptklasse der MicroTvBrowser-Version.
 *
 */

import javax.microedition.rms.RecordStore;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.StringItem;
import java.io.DataInputStream;
import java.util.Calendar;
import java.util.Date;
/**
 *
 * @author  pumpkin
 */
public class tv extends javax.microedition.midlet.MIDlet implements javax.microedition.lcdui.CommandListener{
	
	//Bewertungsicons:
	private static Image[] Bewertungs_Icons = new Image[6];
	
	//Info-Icons
	private static Image[] Info_Icons = new Image[12];
	
	private static Image Favorite_Icon;
	private static Image Reminder_Icon;
	private static Image Spacer_Icon;
	
	
	private static final int blockNavi = -2;
	private static final int dayNavi = -3;
	private static final int nowNavi = -4;
	
	
	private final static int STATUS_MAIN_MENU = 0;
	private final static int STATUS_PROG_LIST = 1;
	private final static int STATUS_DATE_LIST = 2;
	private final static int STATUS_CHAN_LIST = 3;
	
	private final static int STATUS_DETAIL = 4;
	
	private final static int STATUS_SEARCH_LIST_1 = 5;
	private final static int STATUS_SEARCH_REMINDER_1 = 6;
	private final static int STATUS_SEARCH_REMINDER_2 = 7;
	private final static int STATUS_SEARCH_FAVO_1 = 8;
	private final static int STATUS_SEARCH_FAVO_2 = 9;
	private final static int STATUS_DETAIL_REMINDER = 10;
	private final static int STATUS_DETAIL_FAVO = 11;
	
	private final static int STATUS_SEARCH_ENTER_TITLE = 12;
	private final static int STATUS_SEARCH_ENTER_TIME = 13;
	private final static int STATUS_SEARCH_TITLE_DATE = 14;
	private final static int STATUS_SEARCH_TIME_DATE = 15;
	
	private final static int STATUS_SEARCH_TITLE_RESULT = 16;
	private final static int STATUS_SEARCH_TIME_RESULT = 17;
	
	private final static int STATUS_SEARCH_TITLE_DETAIL = 18;
	private final static int STATUS_SEARCH_TIME_DETAIL = 19;
	private final static int STATUS_NOW_LIST = 20;
	
	private static int STATUS = STATUS_MAIN_MENU;
	
	
	//localized Strings
	protected static String error;
	protected static String too_old;
	protected static String show;
	protected static String exit;
	protected static String no_data;
	protected static String back;
	protected static String chan_list;
	protected static String date_list;
	protected static String wait;
	protected static String working;
	
	protected static String prog_list;
	protected static String search;
	protected static String title ;
	protected static String time ;
	protected static String favorite ;
	protected static String reminder;
	protected static String detail;
	protected static String searchFor;
	protected static String channel;
	protected static String now;
	
	//non-localized Strings:
	private static final String[] block_names = {"0-4h","4-8h","8-12h","12-16h","16-20h","20-24h"};
	protected static String OK = "OK";
	
	
	//Commandos:
	protected static Command CMD_GO_PROG_LIST;
	protected static Command CMD_EXIT;
	protected static Command CMD_BACK;
	
	protected static Command CMD_SEARCH;
	protected static Command CMD_OK;
	protected static Command CMD_NOW;
	protected static Command CMD_LIST;
	
	protected static Command CMD_VAR_1;
	protected static Command CMD_VAR_2;
	
	//Basic data:
	private static long data_create_time;
	private static long data_id_time;
	private static int data_days;
	private static int data_min_day;
	private static int data_number_of_channels;
	
	private static boolean useIconsInProglist = true;
	private static boolean useChannelNameInNowList = false;
	
	private static String[] channel_names;
	private static String[] extended_data;
	
	//raw data:
	private byte[] raw_data_cache;
	private int raw_data_cache_day;
	private int raw_data_cache_channel;
	
	//rms-stores:
	RecordStore prog_data_store;
	RecordStore title_data_store;
	
	//resused pointer for Displayables:
	List list;
	Form form;
	Gauge gauge;
	
	TextField searchTextField;
	DateField searchDateField;
	
	String searchTitle;
	Date searchDate;
	
	//reused Data:
	Calendar calendar = Calendar.getInstance();
	
	exec executer;
	
	
	int[] progListMapping;
	int detail_ID;
	
	int[] lastSearchData;
	
	//Wo stehen wir ?
	private int actuel_day;
	private int actuel_channel;
	private int actuel_block;
	
	private boolean insideProgList = false;
	private boolean insideDetail = false;
	
	public void commandAction(javax.microedition.lcdui.Command command, javax.microedition.lcdui.Displayable displayable) {
		//System.out.println("commandAction start: STATUS = "+STATUS);
		try {
			//macht den ganzen programlist <=> details Kram:
			if (insideDetail){
				if (command == CMD_BACK){
					insideDetail = false;
					Display.getDisplay(this).setCurrent(list);
					CMD_VAR_1 = list.SELECT_COMMAND;
					return;
				}
				/*
				if (command == CMD_VAR_1){
					this.getRawData(detail_ID);
					int prog = (detail_ID  & 0xFF);
					this.actuel_day = (detail_ID & 0xFF00) >> 8;
					this.actuel_channel = (detail_ID & 0xFF0000) >> 16;
					actuel_block = 0;
					for (;actuel_block<6;actuel_block++){
						prog = prog - this.raw_data_cache[actuel_block];
						if (prog <0){
							break;
						}
					}
					STATUS = STATUS_PROG_LIST;
					insideDetail = false;
					this.createProgList();
					return;
				}
				 */
			}
			if (insideProgList){
				if (command == CMD_VAR_1){
					
					int com = progListMapping[list.getSelectedIndex()];
					if (com >=0){
						/*
						insideDetail = true;
						Command toAdd = null;
						if (STATUS != STATUS_PROG_LIST){
							toAdd = CMD_LIST;
						}
						createDetail(progListMapping[list.getSelectedIndex()],toAdd);
						return;
						 */
					} else {
						if (com == -3){
							AlertType.ERROR.playSound(Display.getDisplay(this));
							return;
						}
					}
				}
				if (command == CMD_BACK){
					insideProgList = false;
				}
			}
			if (command == CMD_EXIT){
				this.destroyApp(true);
				return;
			}
			
			//FAVO_SEARCH
			if (STATUS == STATUS_SEARCH_FAVO_1){
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					createSearchList1();
					return;
				}
				/*
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_FAVO_2;
					actuel_day = list.getSelectedIndex()+this.data_min_day;
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x08);
						createProgList(favorite+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}
				 */
			}
			if (STATUS == STATUS_SEARCH_FAVO_2){
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_FAVO_1;
					this.createDateList(favorite);
					list.setSelectedIndex(actuel_day,true);
					return;
				}
				/*
				if (command == CMD_VAR_1){
					int com = progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x08);
						createProgList(favorite+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
			}
			
			//REMI_SEARCH
			if (STATUS == STATUS_SEARCH_REMINDER_1){
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					createSearchList1();
					return;
				}/*
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_REMINDER_2;
					actuel_day = list.getSelectedIndex()+this.data_min_day;
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x10);
						createProgList(reminder+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
			}
			if (STATUS == STATUS_SEARCH_REMINDER_2){
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_REMINDER_1;
					this.createDateList(reminder);
					list.setSelectedIndex(actuel_day,true);
					return;
				}/*
				if (command == CMD_VAR_1){
					int com = progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x10);
						createProgList(reminder+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
			}
			
			//TIME_SEARCH
			if (STATUS == STATUS_SEARCH_ENTER_TIME){
				if (command == CMD_OK){
					STATUS = STATUS_SEARCH_TIME_DATE;
					searchDate = this.searchDateField.getDate();
					searchDateField = null;
					this.createDateList(search);
					return;
				}
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					this.createSearchList1();
					return;
				}
			}
			if (STATUS == STATUS_SEARCH_TIME_DATE){
				/*
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_TIME_RESULT;
					actuel_day = this.list.getSelectedIndex() + data_min_day;
					synchronized (this.prog_data_store){
						calendar.setTime(searchDate);
						lastSearchData = searchTime(actuel_day,calendar.get(calendar.HOUR_OF_DAY),calendar.get(calendar.MINUTE));
						createProgList(search+" "+calendar.get(calendar.HOUR_OF_DAY)+":"+(calendar.get(calendar.MINUTE)),this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_ENTER_TIME;
					this.createEnterTime();
					searchDateField.setDate(searchDate);
					return;
				}
			}
			if (STATUS == STATUS_SEARCH_TIME_RESULT){
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_TIME_DATE;
					this.createDateList(search);
					return;
				}/*
				if (command == CMD_VAR_1){
					int com = this.progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					synchronized (this.prog_data_store){
						calendar.setTime(searchDate);
						lastSearchData = searchTime(actuel_day,calendar.get(calendar.HOUR_OF_DAY),calendar.get(calendar.MINUTE));
						createProgList(search+" "+calendar.get(calendar.HOUR_OF_DAY)+":"+(calendar.get(calendar.MINUTE)),this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
			}
			
			//TITLE_SEARCH:
			if (STATUS == STATUS_SEARCH_TITLE_RESULT){
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_TITLE_DATE;
					this.createDateList(search);
					list.setSelectedIndex(actuel_day,true);
					return;
				}/*
				if (command == CMD_VAR_1){
					int com = this.progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					synchronized (this.prog_data_store){
						lastSearchData = searchText(actuel_day,searchTitle.toLowerCase());
						createProgList(search+" "+searchTitle,this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
			}
			if (STATUS == STATUS_SEARCH_TITLE_DATE){
				/*
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_TITLE_RESULT;
					actuel_day = this.list.getSelectedIndex() + data_min_day;
					
					synchronized (this.prog_data_store){
						lastSearchData = searchText(actuel_day,searchTitle.toLowerCase());
						createProgList(search+" "+searchTitle,this.dayNavi,null,lastSearchData);
					}
					return;
				}*/
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_ENTER_TITLE;
					this.createEnterText();
					searchTextField.setString(searchTitle);
					return;
				}
			}
			if (STATUS == STATUS_SEARCH_ENTER_TITLE){
				if (command == CMD_OK){
					searchTitle = searchTextField.getString();
					searchTextField = null;
					STATUS = STATUS_SEARCH_TITLE_DATE;
					this.createDateList(search);
					return;
				}
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					this.createSearchList1();
					return;
				}
			}
			
			if (STATUS == STATUS_SEARCH_LIST_1){
				if (command == CMD_BACK){
					STATUS = STATUS_MAIN_MENU;
					startApp();
					return;
				}
				if (command == CMD_VAR_1){
					//fix me
					String toDo = list.getString(list.getSelectedIndex());
					if (toDo == title){
						//title
						STATUS = STATUS_SEARCH_ENTER_TITLE;
						this.createEnterText();
						return;
					}
					if (toDo == time){
						//time
						STATUS = this.STATUS_SEARCH_ENTER_TIME;
						this.createEnterTime();
						return;
					}
					if (toDo == favorite){
						//favo
						STATUS = STATUS_SEARCH_FAVO_1;
						this.createDateList(favorite);
						return;
					}
					if (toDo == reminder){
						//remin
						STATUS = STATUS_SEARCH_REMINDER_1;
						this.createDateList(reminder);
						return;
					}
					return;
				}
			}
			if (STATUS == this.STATUS_MAIN_MENU){
				if (command == CMD_GO_PROG_LIST){
					this.createChannelList(chan_list);
					STATUS = this.STATUS_CHAN_LIST;
					return;
				}
				if (command == CMD_SEARCH){
					STATUS = STATUS_SEARCH_LIST_1;
					createSearchList1();
					return;
				}
				/*
				if (command == CMD_NOW){
					STATUS = STATUS_NOW_LIST;
					long nowTime = System.currentTimeMillis();
					long delta = nowTime - data_create_time;
					actuel_day = (int)((delta) / (24*60*60*1000));
					calendar.setTime(new Date(nowTime));
					lastSearchData = this.searchTime(actuel_day,calendar.get(calendar.HOUR_OF_DAY)+1,calendar.get(calendar.MINUTE));
					this.createProgList(now+(calendar.get(calendar.HOUR_OF_DAY)+1)+":"+calendar.get(calendar.MINUTE),0,null,lastSearchData);
					return;
				}*/
			}
			if (STATUS == STATUS_NOW_LIST){
				if (command == CMD_BACK){
					STATUS = STATUS_MAIN_MENU;
					startApp();
					return;
				}
			}
			if (STATUS == this.STATUS_CHAN_LIST){
				if (command == CMD_VAR_1){
					STATUS = STATUS_DATE_LIST;
					actuel_channel = list.getSelectedIndex();
					this.createDateList(date_list);
					return;
				}
				if (command == this.CMD_BACK){
					STATUS = STATUS_MAIN_MENU;
					startApp();
					return;
				}
			}
			if (STATUS == this.STATUS_DATE_LIST){
				if (command == CMD_VAR_1){
					STATUS = STATUS_PROG_LIST;
					actuel_day = list.getSelectedIndex() + this.data_min_day;
					actuel_block = 5;
					createProgList();
					return;
				}
				if (command == CMD_BACK){
					STATUS = this.STATUS_CHAN_LIST;
					this.createChannelList(chan_list);
					return;
				}
			}
			if (STATUS == this.STATUS_PROG_LIST){
				if (command == this.CMD_BACK){
					STATUS = STATUS_DATE_LIST;
					this.createDateList(date_list);
					list.setSelectedIndex(actuel_day-data_min_day,true);
					return;
				}/*
				if (command == CMD_VAR_1){
					//etwas in der Liste selektiert
					int com = this.progListMapping[list.getSelectedIndex()];
					if (com == -1){
						//einen Block zurück
						actuel_block--;
					} else {
						//einen Block nach vorne
						actuel_block++;
					}
					createProgList();
					return;
				}
				if (command == CMD_VAR_2){
					this.actuel_channel++;
					createProgList();
					return;
				}*/
			}
		}catch (Exception E){
			E.printStackTrace();
			destroyApp(true);
		}
		Form f = new Form(this.wait);
		f.append(this.working);
		Display.getDisplay(this).setCurrent(f);
		this.executer.execute(command);
	}
	
	protected void commandActionSlow(javax.microedition.lcdui.Command command) {
		try {
			if (insideDetail){
				/*****
				if (command == CMD_BACK){
					insideDetail = false;
					Display.getDisplay(this).setCurrent(list);
					CMD_VAR_1 = list.SELECT_COMMAND;
					return;
				}*/
				
				if (command == CMD_VAR_1){
					this.getRawData(detail_ID);
					int prog = (detail_ID  & 0xFF);
					this.actuel_day = (detail_ID & 0xFF00) >> 8;
					this.actuel_channel = (detail_ID & 0xFF0000) >> 16;
					actuel_block = 0;
					for (;actuel_block<6;actuel_block++){
						prog = prog - this.raw_data_cache[actuel_block];
						if (prog <0){
							break;
						}
					}
					STATUS = STATUS_PROG_LIST;
					insideDetail = false;
					this.createProgList();
					return;
				}
			}
			if (insideProgList){
				//System.out.println ("insideProgList");
				if (command == CMD_VAR_1){
					int com = progListMapping[list.getSelectedIndex()];
					//System.out.println ("CMD_VAR_1 "+com);
					if (com >=0){
						insideDetail = true;
						Command toAdd = null;
						if (STATUS != STATUS_PROG_LIST){
							toAdd = CMD_LIST;
						}
						createDetail(progListMapping[list.getSelectedIndex()],toAdd);
						return;
						 
					}/* else {
						if (com == -3){
							AlertType.ERROR.playSound(Display.getDisplay(this));
							return;
						}
					}*/
				}/*
				if (command == CMD_BACK){
					insideProgList = false;
				}*/
			}
			/*
			if (command == CMD_EXIT){
				this.destroyApp(true);
				return;
			}*/
			
			//FAVO_SEARCH
			if (STATUS == STATUS_SEARCH_FAVO_1){
				/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					createSearchList1();
					return;
				}*/
				
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_FAVO_2;
					actuel_day = list.getSelectedIndex()+this.data_min_day;
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x08);
						createProgList(favorite+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}
			}
			if (STATUS == STATUS_SEARCH_FAVO_2){
				/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_FAVO_1;
					this.createDateList(favorite);
					list.setSelectedIndex(actuel_day,true);
					return;
				}*/
				if (command == CMD_VAR_1){
					int com = progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x08);
						createProgList(favorite+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}
			}
			
			//REMI_SEARCH
			if (STATUS == STATUS_SEARCH_REMINDER_1){
				/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					createSearchList1();
					return;
				}*/
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_REMINDER_2;
					actuel_day = list.getSelectedIndex()+this.data_min_day;
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x10);
						createProgList(reminder+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}
			}
			if (STATUS == STATUS_SEARCH_REMINDER_2){
				/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_REMINDER_1;
					this.createDateList(reminder);
					list.setSelectedIndex(actuel_day,true);
					return;
				}*/
				if (command == CMD_VAR_1){
					int com = progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
					synchronized (this.prog_data_store){
						lastSearchData = searchFlag(actuel_day,0x10);
						createProgList(reminder+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),this.dayNavi,null,lastSearchData);
					}
					return;
				}
			}
			/*
			//TIME_SEARCH
			if (STATUS == STATUS_SEARCH_ENTER_TIME){
				if (command == CMD_OK){
					STATUS = STATUS_SEARCH_TIME_DATE;
					searchDate = this.searchDateField.getDate();
					searchDateField = null;
					this.createDateList(search);
					return;
				}
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					this.createSearchList1();
					return;
				}
			}
			 */
			if (STATUS == STATUS_SEARCH_TIME_DATE){
				
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_TIME_RESULT;
					actuel_day = this.list.getSelectedIndex() + data_min_day;
					synchronized (this.prog_data_store){
						calendar.setTime(searchDate);
						lastSearchData = searchTime(actuel_day,calendar.get(calendar.HOUR_OF_DAY),calendar.get(calendar.MINUTE));
						createProgList(search+" "+calendar.get(calendar.HOUR_OF_DAY)+":"+(calendar.get(calendar.MINUTE)),this.dayNavi,null,lastSearchData);
					}
					return;
				}/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_ENTER_TIME;
					this.createEnterTime();
					searchDateField.setDate(searchDate);
					return;
				}*/
			}
			if (STATUS == STATUS_SEARCH_TIME_RESULT){
				/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_TIME_DATE;
					this.createDateList(search);
					return;
				}*/
				if (command == CMD_VAR_1){
					int com = this.progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					synchronized (this.prog_data_store){
						calendar.setTime(searchDate);
						lastSearchData = searchTime(actuel_day,calendar.get(calendar.HOUR_OF_DAY),calendar.get(calendar.MINUTE));
						createProgList(search+" "+calendar.get(calendar.HOUR_OF_DAY)+":"+(calendar.get(calendar.MINUTE)),this.dayNavi,null,lastSearchData);
					}
					return;
				}
			}
			
			//TITLE_SEARCH:
			if (STATUS == STATUS_SEARCH_TITLE_RESULT){
				/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_TITLE_DATE;
					this.createDateList(search);
					list.setSelectedIndex(actuel_day,true);
					return;
				}*/
				if (command == CMD_VAR_1){
					int com = this.progListMapping[list.getSelectedIndex()];
					if (com == -1){
						actuel_day--;
					} else {
						actuel_day++;
					}
					synchronized (this.prog_data_store){
						lastSearchData = searchText(actuel_day,searchTitle.toLowerCase());
						createProgList(search+" "+searchTitle,this.dayNavi,null,lastSearchData);
					}
					return;
				}
			}
			if (STATUS == STATUS_SEARCH_TITLE_DATE){
				
				if (command == CMD_VAR_1){
					STATUS = STATUS_SEARCH_TITLE_RESULT;
					actuel_day = this.list.getSelectedIndex() + data_min_day;
					
					synchronized (this.prog_data_store){
						lastSearchData = searchText(actuel_day,searchTitle.toLowerCase());
						createProgList(search+" "+searchTitle,this.dayNavi,null,lastSearchData);
					}
					return;
				}/*
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_ENTER_TITLE;
					this.createEnterText();
					searchTextField.setString(searchTitle);
					return;
				}*/
			}
			/*
			if (STATUS == STATUS_SEARCH_ENTER_TITLE){
				if (command == CMD_OK){
					searchTitle = searchTextField.getString();
					searchTextField = null;
					STATUS = STATUS_SEARCH_TITLE_DATE;
					this.createDateList(search);
					return;
				}
				if (command == CMD_BACK){
					STATUS = STATUS_SEARCH_LIST_1;
					this.createSearchList1();
					return;
				}
			}
			
			if (STATUS == STATUS_SEARCH_LIST_1){
				if (command == CMD_BACK){
					STATUS = STATUS_MAIN_MENU;
					startApp();
					return;
				}
				if (command == CMD_VAR_1){
					//fix me
					String toDo = list.getString(list.getSelectedIndex());
					if (toDo == title){
						//title
						STATUS = STATUS_SEARCH_ENTER_TITLE;
						this.createEnterText();
						return;
					}
					if (toDo == time){
						//time
						STATUS = this.STATUS_SEARCH_ENTER_TIME;
						this.createEnterTime();
						return;
					}
					if (toDo == favorite){
						//favo
						STATUS = STATUS_SEARCH_FAVO_1;
						this.createDateList(favorite);
						return;
					}
					if (toDo == reminder){
						//remin
						STATUS = STATUS_SEARCH_REMINDER_1;
						this.createDateList(reminder);
						return;
					}
					return;
				}
			}
			*/
			if (STATUS == this.STATUS_MAIN_MENU){
				/*
				if (command == CMD_GO_PROG_LIST){
					this.createChannelList(chan_list);
					STATUS = this.STATUS_CHAN_LIST;
					return;
				}
				if (command == CMD_SEARCH){
					STATUS = STATUS_SEARCH_LIST_1;
					createSearchList1();
					return;
				}*/
				
				if (command == CMD_NOW){
					STATUS = STATUS_NOW_LIST;
					long nowTime = System.currentTimeMillis();
					long delta = nowTime - data_create_time;
					actuel_day = (int)((delta) / (24*60*60*1000));
					calendar.setTime(new Date(nowTime));
					lastSearchData = this.searchTime(actuel_day,calendar.get(calendar.HOUR_OF_DAY),calendar.get(calendar.MINUTE));
					String title = now+" ";
					if (calendar.get(calendar.HOUR_OF_DAY) < 10){
						title += "0"+calendar.get(calendar.HOUR_OF_DAY);
					} else {
						title += calendar.get(calendar.HOUR_OF_DAY);
					}
					title += ":";
					if (calendar.get(calendar.MINUTE) < 10){
						title += "0"+calendar.get(calendar.MINUTE);
					} else {
						title += calendar.get(calendar.MINUTE);
					}
					this.createProgList(title,nowNavi,null,lastSearchData);
					return;
				}
			}
			/*
			if (STATUS == STATUS_NOW_LIST){
				if (command == CMD_BACK){
					STATUS = STATUS_MAIN_MENU;
					startApp();
					return;
				}
			}
			if (STATUS == this.STATUS_CHAN_LIST){
				if (command == CMD_VAR_1){
					STATUS = STATUS_DATE_LIST;
					actuel_channel = list.getSelectedIndex();
					this.createDateList(date_list);
					return;
				}
				if (command == this.CMD_BACK){
					STATUS = STATUS_MAIN_MENU;
					startApp();
					return;
				}
			}
			if (STATUS == this.STATUS_DATE_LIST){
				if (command == CMD_VAR_1){
					STATUS = STATUS_PROG_LIST;
					actuel_day = list.getSelectedIndex() + this.data_min_day;
					actuel_block = 5;
					createProgList();
					return;
				}
				if (command == CMD_BACK){
					STATUS = this.STATUS_CHAN_LIST;
					this.createChannelList(chan_list);
					return;
				}
			}
			 */
			if (STATUS == this.STATUS_PROG_LIST){
				/*
				if (command == this.CMD_BACK){
					STATUS = STATUS_DATE_LIST;
					this.createDateList(date_list);
					list.setSelectedIndex(actuel_day-data_min_day,true);
					return;
				}*/
				if (command == CMD_VAR_1){
					//etwas in der Liste selektiert
					int com = this.progListMapping[list.getSelectedIndex()];
					if (com == -1){
						//einen Block zurück
						actuel_block--;
					} else {
						//einen Block nach vorne
						actuel_block++;
					}
					createProgList();
					return;
				}
				if (command == CMD_VAR_2){
					this.actuel_channel++;
					createProgList();
					return;
				}
			}
		} catch (Exception E){
			E.printStackTrace();
		}
		//System.out.println ("run out of code "+STATUS);
		destroyApp(true);
	}
	
	protected int[] searchFlag(int day, int flag) throws Exception {
		int[] toReturn = new int[5];
		int counter = 0;
		for (int i =0;i<this.channel_names.length;i++){
			int ID = (i << 16) | (day << 8);
			getRawData(ID);
			int toSearch = 0;
			for (int j=0;j<5;j++){
				toSearch += raw_data_cache[j];
			}
			for (int j=0;j<toSearch;j++){
				int base = 6+(6*j);
				if ((raw_data_cache[base+3] & flag)!=0){
					toReturn[counter] = ID | j;
					counter++;
					if (toReturn.length == counter){
						int[] temp = new int[toReturn.length+5];
						System.arraycopy(toReturn,0,temp,0,toReturn.length);
						toReturn = temp;
					}
				}
			}
		}
		if (counter != toReturn.length){
			int[] temp = new int[counter];
			System.arraycopy(toReturn,0,temp,0,temp.length);
			toReturn = temp;
		}
		return toReturn;
	}
	
	protected int[] searchTime(int day, int hour, int min) throws Exception {
		int[] toReturn = new int[channel_names.length];
		int time = (hour*60) + min;
		int counter = 0;
		for (int i =0;i<channel_names.length;i++){
			int ID = (i << 16) | (day << 8);
			getRawData(ID);
			int toSearch = 0;
			
			int base = 6;
			int time2 = (raw_data_cache[base]*60) + raw_data_cache[base+1];
			if (time2 > time){
				//Es ist der letzte des Vortags.
				if (day > data_min_day){
					ID = (i << 16) | ((day-1) << 8);
					getRawData(ID);
					toSearch = 0;
					for (int j=0;j<6;j++){
						toSearch += raw_data_cache[j];
					}
					toReturn[i] = ID | (toSearch-1);
				} else {
					toReturn[i] = ID | -1;
				}
			} else {
				//Es ist einer aus diesem Tag.
				toSearch = 0;
				for (int j=0;j<6;j++){
					toSearch += raw_data_cache[j];
				}
				int last = 0;
				for (int j =1;j<toSearch;j++){
					base = 6 + (6*j);
					time2 = (raw_data_cache[base]*60) + raw_data_cache[base+1];
					if (time2 > time){
						break;
					}
					last = j;
				}
				toReturn[i] = ID | last;
			}
		}
		return toReturn;
	}
	
	
	protected int[] searchText(int day, String text) throws Exception {
		int[] toReturn = new int[5];
		int counter = 0;
		byte[] searchFor = text.getBytes("ISO8859_1");
		for (int i =0;i<this.channel_names.length;i++){
			int ID = (i << 16) | (day << 8);
			getRawData(ID);
			int toSearch = 0;
			for (int j=0;j<5;j++){
				toSearch += raw_data_cache[j];
			}
			for (int j=0;j<toSearch;j++){
				int base = 6+(6*j);
				
				int byte1 = raw_data_cache[base+4];
				int byte2 = raw_data_cache[base+5];
				if (byte1<0){
					byte1 = 256 + byte1;
				}
				if (byte2<0){
					byte2 = 256 + byte2;
				}
				int titleID = ((byte1 << 8) | (byte2));
				byte[] toCheck = title_data_store.getRecord(titleID);

				boolean found = false;
				
				for (int ii=0;(ii<(toCheck.length - searchFor.length)) && (!found);ii++){
					for (int jj=0;jj<searchFor.length;jj++){
						found = true;
						int diff = searchFor[jj] - toCheck[ii+jj];
						if (!((diff == 0) || (diff == -32) || (diff == 32))){
							found = false;
							jj = searchFor.length;
						}
					}
				}
				if (found){
					toReturn[counter] = ID | j;
					counter++;
					if (toReturn.length == counter){
						int[] temp = new int[toReturn.length+5];
						System.arraycopy(toReturn,0,temp,0,toReturn.length);
						toReturn = temp;
					}
				}
			}
		}
		if (counter != toReturn.length){
			int[] temp = new int[counter];
			System.arraycopy(toReturn,0,temp,0,temp.length);
			toReturn = temp;
		}
		return toReturn;
	}
	
	
	protected void createSearchList1(){
		list = new List(search,List.IMPLICIT);
		if (Reminder_Icon != null){
			list.append(reminder,Reminder_Icon);
		}
		if (Favorite_Icon != null){
			list.append(favorite,Favorite_Icon);
		}
		list.append(title,null);
		list.append(time,null);
		CMD_VAR_1 = list.SELECT_COMMAND;
		list.addCommand(CMD_BACK);
		list.setCommandListener(this);
		Display.getDisplay(this).setCurrent(list);
	}
	
	protected void createEnterText(){
		form = new Form(search);
		searchTextField = new TextField(searchFor,"",10,0);
		form.append(searchTextField);
		form.setCommandListener(this);
		form.addCommand(CMD_BACK);
		form.addCommand(CMD_OK);
		Display.getDisplay(this).setCurrent(form);
	}
	
	protected void createEnterTime(){
		form = new Form(search);
		searchDateField = new DateField(time,DateField.TIME);
		searchDateField.setDate(new Date(0));
		form.append(searchDateField);
		form.setCommandListener(this);
		form.addCommand(CMD_BACK);
		form.addCommand(CMD_OK);
		Display.getDisplay(this).setCurrent(form);
	}
	
	
	protected int[] getProgIDforBlock(int blockID){
		int start = 0;
		int[] array = null;
		for (int i = 0;i<blockID;i++){
			start += this.raw_data_cache[i];
		}
		array = new int[raw_data_cache[blockID]];
		int progmask = this.raw_data_cache_channel << 16;
		progmask = progmask | this.raw_data_cache_day << 8;
		for (int i =0;i<array.length;i++){
			array[i] = (start + i) | progmask;
		}
		return array;
	}
	
	protected void destroyApp(boolean param){
		try {
			prog_data_store.closeRecordStore();
		} catch (Exception E){
		}
		try {
			title_data_store.closeRecordStore();
		} catch (Exception E){
		}
		executer.stopExecution();
		executer = null;
		this.notifyDestroyed();
	}
	
	protected void pauseApp() {
		this.notifyPaused();
	}
	
	protected void startApp(){
		if (executer==null){
			form = new Form("MircoTvBrowser");
			form.append("loading\n");
			gauge = new Gauge("init:\n",false,11,0);
			form.append(gauge);
			Display.getDisplay(this).setCurrent(form);
			executer = new exec(this);
			executer.start();
		} else {
			gauge = null;
			form = new Form("MircoTvBrowser");
			form.append("version 0.12\n");
			form.append("www.tvBrowser.org\n");
			form.append("pumpkin@gmx.de\n");
			form.addCommand(CMD_EXIT);
			form.addCommand(CMD_GO_PROG_LIST);
			form.addCommand(CMD_SEARCH);
			form.addCommand(CMD_NOW);
			form.setCommandListener(this);
			Display.getDisplay(this).setCurrent(form);
			try {
				if (prog_data_store == null){
					prog_data_store = RecordStore.openRecordStore("prog",false);
					title_data_store = RecordStore.openRecordStore("title",false);
				}
			} catch (Exception E){
				E.printStackTrace();
			}
		}
	}
	
	protected void createDateList(String title){
		list = new List(title,List.IMPLICIT);
		for (int i=data_min_day;i<data_days;i++){
			calendar.setTime(new Date(data_create_time + (i*24*60*60*1000)));
			list.append(calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1),null);
		}
		list.addCommand(CMD_BACK);
		list.setCommandListener(this);
		CMD_VAR_1 = list.SELECT_COMMAND;
		Display.getDisplay(this).setCurrent(list);
	}
	
	protected void createChannelList(String title){
		list = new List(title,List.IMPLICIT,channel_names,null);
		list.addCommand(CMD_BACK);
		list.setCommandListener(this);
		CMD_VAR_1 = list.SELECT_COMMAND;
		Display.getDisplay(this).setCurrent(list);
	}
	
	
	/** zeigt den über actuel_channel/day/block selektierten Datenteil an*/
	protected void createProgList() throws Exception {
		synchronized (this.prog_data_store){
			if (actuel_block < 0){
				actuel_day --;
				actuel_block = 5;
			}
			if (actuel_block > 5){
				actuel_day ++;
				actuel_block = 0;
			}
			actuel_channel = actuel_channel % channel_names.length;
			this.getRawData((actuel_channel << 16) | (actuel_day << 8));
			int[] temp = getProgIDforBlock(actuel_block);
			
			calendar.setTime(new Date(data_create_time + (actuel_day*24*60*60*1000)));
			
			this.createProgList(
			channel_names[actuel_channel]+" "+calendar.get(calendar.DAY_OF_MONTH)+"."+(calendar.get(calendar.MONTH)+1)
			,this.blockNavi
			,new Command(channel_names[(actuel_channel+1)%channel_names.length],Command.ITEM,0)
			,temp);
		}
	}
	
	protected void createDetail(int ID, Command toAdd) throws Exception {
		
		detail_ID = ID;
		int prog = 0xFF & ID;
		
		int chan = (ID & 0xFF0000) >> 16;
		int day = (ID & 0xFF00) >> 8;
		
		int base = 6 + (6*prog);
		getRawData(ID);
		
		form = new Form(detail);
		form.append(new StringItem(channel, this.channel_names[chan]));
		
		int	hour = raw_data_cache[base];
		int min = raw_data_cache[base+1];
		String s = null;
		if (hour < 10){
			s = "0"+hour+":";
		} else {
			s = hour+":";
		}
		if (min < 10){
			s += "0"+min;
		} else {
			s += min;
		}
		form.append(new StringItem(time,s));
		
		int byte1 = raw_data_cache[base+4];
		int byte2 = raw_data_cache[base+5];
		if (byte1<0){
			byte1 = 256 + byte1;
		}
		if (byte2<0){
			byte2 = 256 + byte2;
		}
		int titleID = ((byte1 << 8) | (byte2));
		
		form.append(
		new StringItem(
		title,
		new String(title_data_store.getRecord(titleID),"ISO8859_1")));
		
		int rating = (raw_data_cache[base+3] & 0xE0) >> 5;
		if (rating != 6){
			form.append(Bewertungs_Icons[rating]);
		}
		//favo ?
		if ((raw_data_cache[base+3] & 0x08)!=0){
			form.append(Favorite_Icon);
		}
		//reminder ?
		if ((raw_data_cache[base+3] & 0x10)!=0){
			form.append(Reminder_Icon);
		}
		
		int info = raw_data_cache[base+2] | ((raw_data_cache[base+3] & 0x07) << 8);
		for (int i=0;i<Info_Icons.length;i++){
			if ((info & (0x01 << i)) !=0){
				System.out.println ("7");
				form.append(Info_Icons[i]);
			}
		}
		
		form.setCommandListener(this);
		form.addCommand(CMD_BACK);
		
		if (toAdd != null){
			form.addCommand(toAdd);
			CMD_VAR_1 = toAdd;
		}
		
		int length = 0;
		for (int i =0;i<6;i++){
			length += this.raw_data_cache[i];
		}
		
		int pos = 0;
		try {
			DataInputStream Din = new DataInputStream(this.getClass().getResourceAsStream(chan+"."+day));
		
			for (int i=0;i<prog;i++){
				pos += Din.readUnsignedShort();
			}
			for (int i = prog;i<length;i++){
				Din.readUnsignedShort();
			}
		
			while (pos != 0){
				pos -= Din.skip(pos);
			}
		
			for (int i = 0;i<extended_data.length;i++){
				String data = Din.readUTF();
				if (data.length()!=0){
					form.append(new StringItem(extended_data[i],data));
				}
			}
			Din.close();
		} catch (Exception E){
		}
		Display.getDisplay(this).setCurrent(form);
	}
	
	
	protected void createProgList(String title, int mode, Command to_use, int[] progID) throws Exception {
		insideProgList = true;
		String naviNext = null;
		String naviPrev = null;
		//System.out.println("createProgList 1");
		if (mode == blockNavi){
			if ((actuel_block<5) ||((actuel_day+1)<data_days)){
				naviNext  = block_names[(actuel_block + 1) % block_names.length];
			}
			
			if ((actuel_block>0)||(actuel_day>data_min_day)){
				if (actuel_block - 1 < 0){
					naviPrev = block_names[5];
				} else {
					naviPrev = block_names[actuel_block - 1];
				}
			}
		}
		//System.out.println("createProgList 2");
		if (mode == dayNavi){
			if (actuel_day>data_min_day){
				naviPrev = "";
			}
			if ((actuel_day+1)<this.data_days){
				naviNext = "";
			}
		}
		boolean withChannel = useChannelNameInNowList && (mode == nowNavi);
		list = new List(title,List.IMPLICIT);
		int counter = Math.max(1,progID.length);
		if (naviNext != null){
			counter++;
		}
		if (naviPrev != null){
			counter++;
		}
		progListMapping = new int[counter];
		counter = 0;
		if (naviPrev != null){
			progListMapping[counter] = -1;
			counter++;
			list.append("<< "+naviPrev,null);
		}
		if ((progID == null) || (progID.length == 0)){
			progListMapping[counter] = -3;
			list.append(this.no_data,null);
			counter++;
		} else {
			for (int i =0;i<progID.length;i++){
				if (((byte)(progID[i] & 0xFF)) == -1){
					progListMapping[counter] = -3;
					list.append(this.no_data,null);
				} else {
					getRawData(progID[i]);
					progListMapping[counter] = progID[i];
					list.append(getTitle(progID[i],withChannel),getIcon(progID[i]));
				}
				counter++;
			}
		}
		if (naviNext != null){
			progListMapping[counter] = -2;
			counter++;
			list.append(">> "+naviNext,null);
		}
		list.addCommand(this.CMD_BACK);
		if (to_use !=null){
			CMD_VAR_2 = to_use;
			list.addCommand(to_use);
		}
		CMD_VAR_1 = list.SELECT_COMMAND;
		list.setCommandListener(this);
		Display.getDisplay(this).setCurrent(list);
	}
	
	
	protected String getTitle(int ID, boolean withChannel) throws Exception{
		
		String s = null;
		
		int prog = (ID & 0xFF);
		int channel = (ID & 0xFF0000) >> 16;

		//ab da steht das Programm:
		int base = 6+(6*prog);

		//hour:
		if (raw_data_cache[base] < 10){
			s = "0"+raw_data_cache[base]+":";
		} else {
			s = raw_data_cache[base] +":";
		}
		
		//min:
		if (raw_data_cache[base+1] < 10){
			s = s + "0"+raw_data_cache[base+1];
		} else {
			s = s +raw_data_cache[base+1];
		}
		if (withChannel){
			s += " "+this.channel_names[channel];
		}
		//title-ID:
		int byte1 = raw_data_cache[base+4];
		int byte2 = raw_data_cache[base+5];
		if (byte1<0){
			byte1 = 256 + byte1;
		}
		if (byte2<0){
			byte2 = 256 + byte2;
		}
		int titleID = ((byte1 << 8) | (byte2));
		s = s + " "+new String(title_data_store.getRecord(titleID),"ISO8859_1");
		return s;
	}
	
	
	protected void getRawData(int ID) throws Exception{
		int channel = (ID & 0xFF0000) >> 16;
		int day = (ID & 0xFF00) >> 8;
		if ((raw_data_cache==null) || (raw_data_cache_day!=day) || (raw_data_cache_channel!=channel)){
			int RMSID = (day*channel_names.length) + channel + 1;
			raw_data_cache = prog_data_store.getRecord(RMSID);
			this.raw_data_cache_channel = channel;
			this.raw_data_cache_day = day;
		}
	}
	
	
	protected Image getIcon(int ID) throws Exception{
		if (useIconsInProglist){
			int prog = (ID & 0xFF);
			//ab da steht das Programm:
			int base = 6+(6*prog);
			/*
			System.out.println("getIcon for "+prog);
			for (int i =0;i<6;i++){
				System.out.print(raw_data_cache[base+i]+" ");
			}
			System.out.println();
			 */
			Image toshow = Spacer_Icon;
			//bewertung ?
			int rating = (raw_data_cache[base+3] & 0xE0) >> 5;
			if (rating != 6){
				//System.out.println("rating "+rating);
				toshow = this.Bewertungs_Icons[rating];
			}
			//favo ?
			if ((raw_data_cache[base+3] & 0x08)!=0){
				//System.out.println("Favo");
				toshow = Favorite_Icon;
			}
			//reminder ?
			if ((raw_data_cache[base+3] & 0x10)!=0){
				//System.out.println("rem");
				toshow = this.Reminder_Icon;
			}
			return toshow;
		} else {
			return null;
		}
	}
	
	protected void loadConfig() throws Exception {
		DataInputStream DIN = new DataInputStream(this.getClass().getResourceAsStream("config"));
		gauge.setValue(0);
		error =   DIN.readUTF();
		too_old = DIN.readUTF();
		gauge.setValue(1);
		show =    DIN.readUTF();
		no_data = DIN.readUTF();
		gauge.setValue(2);
		exit =    DIN.readUTF();
		back =    DIN.readUTF();
		gauge.setValue(3);
		chan_list = DIN.readUTF();
		date_list = DIN.readUTF();
		gauge.setValue(4);
		wait = DIN.readUTF();
		working = DIN.readUTF();
		gauge.setValue(5);
		
		
		prog_list = DIN.readUTF();
		search = DIN.readUTF();
		gauge.setValue(6);
		title = DIN.readUTF();
		time = DIN.readUTF();
		gauge.setValue(7);
		favorite = DIN.readUTF();
		reminder = DIN.readUTF();
		gauge.setValue(8);
		detail = DIN.readUTF();
		searchFor = DIN.readUTF();
		gauge.setValue(9);
		channel	= DIN.readUTF();
		now = DIN.readUTF();

		
		CMD_GO_PROG_LIST = new Command(show,Command.OK,0);
		CMD_EXIT = new Command(exit,Command.EXIT,0);
		CMD_BACK = new Command(back,Command.CANCEL,0);
		CMD_SEARCH = new Command(this.search,Command.ITEM,0);
		CMD_OK = new Command(OK,Command.OK,0);
		
		CMD_NOW = new Command(now,Command.ITEM,0);
		CMD_LIST = new Command(prog_list,Command.ITEM,0);
		
		data_create_time = DIN.readLong();
		
		data_id_time = DIN.readLong();
		
		data_days = DIN.readUnsignedByte();
		
		Calendar C = Calendar.getInstance();
		C.setTime(new Date(System.currentTimeMillis()));
		
		long max_delta_time = (1+data_days)*24*60*60*1000;
		long delta = System.currentTimeMillis() - data_create_time;
		data_min_day = (int)((delta) / (24*60*60*1000));
		data_min_day = Math.max(0,data_min_day);
		
		gauge.setValue(10);
		if (data_min_day >= data_days){
			Form F = new Form(error);
			F.append(too_old);
			F.addCommand(CMD_EXIT);
			F.setCommandListener(this);
			Display.getDisplay(this).setCurrent(F);
			return;
		}
		data_number_of_channels = DIN.readUnsignedByte();
		extended_data = new String [DIN.readUnsignedByte()];
		gauge.setValue(0);
		gauge.setMaxValue(data_number_of_channels+extended_data.length);
		channel_names = new String[data_number_of_channels];
		for (int i=0;i<data_number_of_channels;i++){
			channel_names[i] = DIN.readUTF();
			gauge.setValue(i);
		}
		for (int i=0;i<extended_data.length;i++){
			extended_data[i] = DIN.readUTF();
			gauge.setValue(data_number_of_channels+i);
		}
		
		useIconsInProglist = DIN.readBoolean();
		useChannelNameInNowList = true;//DIN.readBoolean();
		DIN.close();
	}
	
	protected void loadIcons(){
		try {
			int IconLength = 3+6+12;
			int[] maxlength = new int[IconLength];
			DataInputStream iconIn = new DataInputStream(this.getClass().getResourceAsStream("i"));
			//System.out.println("2");
			for (int i =0;i<IconLength;i++){
				maxlength[i] = iconIn.readUnsignedShort();
			}
			int counter = 0;
			if (maxlength[0] != 0){
				byte[] data = new byte[maxlength[0]];
				iconIn.readFully(data);
				Spacer_Icon = Image.createImage(data,0,maxlength[0]);
			}
			if (maxlength[1] !=0){
				byte[] data = new byte[maxlength[1]];
				iconIn.readFully(data);
				Favorite_Icon = Image.createImage(data,0,maxlength[1]);
			}
			if (maxlength[2] !=0){
				byte[] data = new byte[maxlength[2]];
				iconIn.readFully(data);
				Reminder_Icon = Image.createImage(data,0,maxlength[2]);
			}
			for (int i =0;i<6;i++){
				if (maxlength[3+i] !=0){
					byte[] data = new byte[maxlength[3+i]];
					iconIn.readFully(data);
					Bewertungs_Icons[i] = Image.createImage(data,0,maxlength[3+i]);
				}
			}
			counter = 3+6;
			for (int i =0;i<12;i++){
				if (maxlength[i+counter]!=0){
					byte[] data = new byte[maxlength[i+counter]];
					iconIn.readFully(data);
					Info_Icons[i] = Image.createImage(data,0,maxlength[i+counter]);
				}
			}
		} catch (Throwable E1){
			E1.printStackTrace();
		}
	}
	
	protected boolean firstStart() throws Exception {
		RecordStore rsConfig = null;
		try {
			rsConfig = RecordStore.openRecordStore("config",false);
			byte[] temp = rsConfig.getRecord(1);
			
			long config_time =
			(((long)(temp[0] & 0xff) << 56) |
			((long)(temp[1] & 0xff) << 48) |
			((long)(temp[2] & 0xff) << 40) |
			((long)(temp[3] & 0xff) << 32) |
			((long)(temp[4] & 0xff) << 24) |
			((long)(temp[5] & 0xff) << 16) |
			((long)(temp[6] & 0xff) <<  8) |
			((long)(temp[7] & 0xff)));
			
			rsConfig.closeRecordStore();
			//System.out.println ("config_time:"+config_time +" data_create_time "+data_id_time);
			if (config_time == data_id_time){
				return false;
			}
		} catch (Exception E){
			//System.out.println("firstStart() 1");
			E.printStackTrace();
		}
		try {
			rsConfig.closeRecordStore();
		} catch (Exception E1){
			//System.out.println("firstStart() 2");
			E1.printStackTrace();
		}
		return true;
	}
	
	protected void endFirstStart() throws Exception{
		try {
			RecordStore.deleteRecordStore("config");
		} catch (Exception E1){
			//System.out.println("endFirstStart() 1");
			E1.printStackTrace();
		}
		RecordStore rsConfig = RecordStore.openRecordStore("config",true);
		byte[] temp = new byte[8];
		temp[0] = (byte)(0xff & (data_id_time >> 56));
		temp[1] = (byte)(0xff & (data_id_time >> 48));
		temp[2] = (byte)(0xff & (data_id_time >> 40));
		temp[3] = (byte)(0xff & (data_id_time >> 32));
		temp[4] = (byte)(0xff & (data_id_time >> 24));
		temp[5] = (byte)(0xff & (data_id_time >> 16));
		temp[6] = (byte)(0xff & (data_id_time >>  8));
		temp[7] = (byte)(0xff & data_id_time);
		rsConfig.addRecord(temp,0,8);
		rsConfig.closeRecordStore();
	}
	
	
	protected void copyData() throws Exception{
		gauge.setLabel("first start");
		gauge.setValue(0);
		form.append("decompressing");
		try {
			RecordStore.deleteRecordStore("title");
		} catch (Exception E){
			//System.out.println("copyData() 2");
			E.printStackTrace();
		}
		try {
			RecordStore.deleteRecordStore("prog");
		} catch (Exception E){
			//System.out.println("copyData() 1");
			E.printStackTrace();
		}
		title_data_store = RecordStore.openRecordStore("title",true);
		int number_of_recordes2 = 0;
		byte[] temp = new byte[100];
		try {
			DataInputStream DIN = new DataInputStream(this.getClass().getResourceAsStream("title"));
			number_of_recordes2 = DIN.readInt();
			gauge.setMaxValue(number_of_recordes2+500);
			for (int i=0;i<number_of_recordes2;i++){
				int length = DIN.readInt();
				if (length > temp.length){
					temp = new byte[length];
					System.out.println ("allco 1: "+length);
				}
				int offset = 0;
				while (offset!=length){
					offset += DIN.read(temp,offset,length-offset);
				}
				
				title_data_store.addRecord(temp,0,length);
				gauge.setValue(i);
			}
			//System.out.println("copied "+number_of_recordes2+" titles");
		} catch (Exception E){
			//System.out.println("copyData() 4");
			E.printStackTrace();
		}
		title_data_store.closeRecordStore();
		prog_data_store = RecordStore.openRecordStore("prog",true);
		try {
			DataInputStream DIN = new DataInputStream(this.getClass().getResourceAsStream("prog"));
			int number_of_recordes = DIN.readInt();
			gauge.setMaxValue(number_of_recordes+number_of_recordes2);
			//System.out.println("GO");
			for (int i=0;i<number_of_recordes;i++){
				int length = DIN.readInt();
				if (length > temp.length){
					temp = new byte[length];
					System.out.println ("allco 2: "+length);
				}
				int offset = 0;
				while (offset!=length){
					offset += DIN.read(temp,offset,length-offset);
				}
				prog_data_store.addRecord(temp,0,length);
				gauge.setValue(i+number_of_recordes2);
			}
			//System.out.println("copied "+number_of_recordes+" progs");
		} catch (Exception E){
			//System.out.println("copyData() 3");
			E.printStackTrace();
		}
		prog_data_store.closeRecordStore();
		prog_data_store = null;
		title_data_store = null;
	}
}