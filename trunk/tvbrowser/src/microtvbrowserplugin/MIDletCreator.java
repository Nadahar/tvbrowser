/*
 * PDA-Plugin
 * Copyright (C) 2004 gilson laurent pumpkin@gmx.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package microtvbrowserplugin;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.*;

import util.exc.TvBrowserException;
import util.ui.ImageUtilities;
import util.ui.progress.*;
import util.ui.Localizer;

import devplugin.*;

public class MIDletCreator implements Progress{
	
	static Localizer mLocalizer = Localizer.getLocalizerFor(MIDletCreator.class);
	
		
	private static final int block_size = 250;
	private ProgressWindow progress;
	private MicroTvBrowserPlugin mtbp;
	private File dir;
	private StringBuffer exportLog = new StringBuffer("");
	private Hashtable title_id;
	
	public MIDletCreator(MicroTvBrowserPlugin MTBP, File Dir, ProgressWindow Progress){
		mtbp = MTBP;
		progress = Progress;
		dir = Dir;
	}
	
	private int pipe(InputStream In, OutputStream Out) throws Exception{
		int counter = 0;
		int copy = In.read();
		while (copy !=-1){
			counter++;
			Out.write(copy);
			copy = In.read();
		}
		return counter;
	}
	
	
	private void copyFile(JarOutputStream jar, String s) throws Exception {
		try {
			JarFile JA = mtbp.getJarFilePublic();
			JarEntry JAE = JA.getJarEntry("microtvbrowserplugin/data/"+s);
			InputStream In = JA.getInputStream(JAE);
			
			ByteArrayOutputStream BOUT = new ByteArrayOutputStream();
			pipe(In,BOUT);
			
			JarEntry ZIP = new JarEntry(s);
			jar.putNextEntry(ZIP);
			DataOutputStream DOUT = new DataOutputStream(jar);
			DOUT.write(BOUT.toByteArray());
			DOUT.flush();
		} catch (Exception E){
		}
	} 
	
	private void writeTitleString(DataOutputStream dout, String s) throws Exception{
		Integer I = (Integer) title_id.get(s);
		if (I == null){
			System.out.println("writeTitleString called with unknown Title \"" +s+"\"");
			System.exit(-1);
		} else {
			dout.writeShort((I.intValue()));
		}
	}
	
	public void run(){
		try {
			progress.setMessage(mLocalizer.msg("collecting channels","collecting channels"));
			PluginManager PM = mtbp.getPluginManager();
			Channel[] CH = PM.getSubscribedChannels();
			Vector Export = new Vector();
			String[] channel_list = mtbp.getChannelList();
			for (int i =0;i<channel_list.length;i++){
				for (int j=0;j<CH.length;j++){
					if (CH[j].getName().equals(channel_list[i])){
						if (!Export.contains(CH[j])){
							Export.add(CH[j]);
						}
						break;
					}
				}
			}
			
			Channel[] CH_to_export = (Channel[]) Export.toArray(new Channel[Export.size()]);
			
			progress.setMessage(mLocalizer.msg("creating jar and config","creating jar and config"));

			//zuerst die MANIFEST-Datei:
			JarFile JA = mtbp.getJarFilePublic();
			
			JarEntry JAE = JA.getJarEntry("microtvbrowserplugin/data/MIDletMANI.MF");
			InputStream In = JA.getInputStream(JAE);
			
			Manifest MA = new Manifest(In);
			
			File MicroTvBrowser = new File(dir, "MicroTvBrowser.jar");
			JarOutputStream jar = new JarOutputStream(new FileOutputStream(MicroTvBrowser),MA);
			
			//die .class-Dateien:
			copyFile(jar,"tv.class");
			copyFile(jar,"exec.class");
			
			
			
			//LOG.append ("\nBasic.C:\n");
			jar.putNextEntry(new JarEntry("config"));
			DataOutputStream basic = new DataOutputStream(jar);
			
			
			basic.writeUTF(mLocalizer.msg("Error","Error"));
			basic.writeUTF(mLocalizer.msg("Data too old","Data too old"));
			basic.writeUTF(mLocalizer.msg("show","show"));
			basic.writeUTF(mLocalizer.msg("no data found","no data found"));
			basic.writeUTF(mLocalizer.msg("exit","exit"));
			basic.writeUTF(mLocalizer.msg("back","back"));
			basic.writeUTF(mLocalizer.msg("channels","channels"));
			basic.writeUTF(mLocalizer.msg("dates","dates"));
			basic.writeUTF(mLocalizer.msg("please wait","please wait"));
			basic.writeUTF(mLocalizer.msg("working...","working..."));
			
			
			basic.writeUTF(mLocalizer.msg("prog. list","prog. list"));
			basic.writeUTF(mLocalizer.msg("search","search"));
			basic.writeUTF(mLocalizer.msg("title","title"));
			basic.writeUTF(mLocalizer.msg("time","time"));

			basic.writeUTF(mLocalizer.msg("favorite","favorite"));
			basic.writeUTF(mLocalizer.msg("reminder","reminder"));

			basic.writeUTF(mLocalizer.msg("detail","detail"));
			basic.writeUTF(mLocalizer.msg("search for","search for"));

			basic.writeUTF(mLocalizer.msg("channel","channel"));
			basic.writeUTF(mLocalizer.msg("now","now"));
			
			
			java.util.Date now = new java.util.Date();
			java.util.Date now2 = new java.util.Date(now.getYear(),now.getMonth(),now.getDate(),0,1);
			basic.writeLong(now2.getTime());
			basic.writeLong(System.currentTimeMillis());
			int data_length = mtbp.getDaysToExport();
			basic.writeByte(data_length);
			
			
			ProgramFieldType[] dataToExport = null;
			switch (this.mtbp.getExportLevel()){
				case (0):{
				}
				case (1):{
					dataToExport = new ProgramFieldType[0];
					break;
				}
				case (2):{
					dataToExport = new ProgramFieldType[3];
					dataToExport[0] = ProgramFieldType.SHORT_DESCRIPTION_TYPE;
					dataToExport[1] = ProgramFieldType.PRODUCTION_YEAR_TYPE;
					dataToExport[2] = ProgramFieldType.ACTOR_LIST_TYPE;
					break;
				}
				case (3):{
					dataToExport = new ProgramFieldType[6];
					dataToExport[0] = ProgramFieldType.DESCRIPTION_TYPE;
					dataToExport[1] = ProgramFieldType.PRODUCTION_YEAR_TYPE;
					dataToExport[2] = ProgramFieldType.ACTOR_LIST_TYPE;
					dataToExport[3] = ProgramFieldType.MODERATION_TYPE;
					dataToExport[4] = ProgramFieldType.ORIGINAL_EPISODE_TYPE;
					dataToExport[5] = ProgramFieldType.ORIGINAL_TITLE_TYPE;
					break;
				}
				case (4):{
					Iterator it = ProgramFieldType.getTypeIterator();
					Vector tempForArray = new Vector();
					while (it.hasNext()){
						ProgramFieldType type = (ProgramFieldType) it.next();
						if ((type.getFormat() == type.TEXT_FORMAT) || (type.getFormat() == type.TIME_FORMAT) || (type.getFormat() == type.INT_FORMAT)){
							tempForArray.add(type);
						}
					}
					dataToExport = (ProgramFieldType[]) tempForArray.toArray(new ProgramFieldType[tempForArray.size()]);
					break;
				}
			}
			
			
			basic.writeByte(CH_to_export.length);
			basic.writeByte(dataToExport.length);
			for (int i =0;i<CH_to_export.length;i++){
				basic.writeUTF(CH_to_export[i].getName());
			}
			
			for (int i =0;i<dataToExport.length;i++){
				System.out.println ("EX: "+i+" "+dataToExport[i].getLocalizedName());
				basic.writeUTF(dataToExport[i].getLocalizedName());
			}
			basic.writeBoolean(mtbp.isUseIconsInProgList());
			basic.writeBoolean(mtbp.isChannelNameInNowList());
			basic.flush();
			
			
			boolean used_info[] = new boolean [12];
			for (int j=0;j<used_info.length;j++){
				used_info[j] = false;
			}
			boolean used_rating[] = new boolean [6];
			for (int j=0;j<used_rating.length;j++){
				used_rating[j] = false;
			}
			boolean usedFav = false;
			boolean usedRem = false;
			
			
			devplugin.Date today = new devplugin.Date();
			title_id = new Hashtable();

			progress.setMessage(mLocalizer.msg("collecting title strings","collecting title strings"));
			Hashtable title_hash = new Hashtable();
			for (int j =0;j<CH_to_export.length;j++){
				for (int i=0;i<data_length;i++){
					devplugin.Date D = new devplugin.Date(today);
					D = D.addDays(i);
					Iterator it = PM.getChannelDayProgram(D,CH_to_export[j]);
					if (it!=null){
						while (it.hasNext()){
							Program toSave = (Program) it.next();
							String title = toSave.getTitle();
							if (title == null){
								title = " ";
							}
							if (title.length() > 100){
								title = title.substring(0,100);
							}
							
							Integer I = (Integer) title_hash.get(title);
							if (I==null){
								title_hash.put(title,new Integer(1));
							} else {
								title_hash.put(title,new Integer(I.intValue()+1));
							}
						}
					}
				}
			}
			Vector titles = new Vector();
			Enumeration E = title_hash.keys();
			int singlecounter = 0;
			while (E.hasMoreElements()){
				String tit = (String) E.nextElement();
				titles.add(tit);
			}
				
			int counter = 0;
			int idcounter = 0;
			for (int i =0;i<titles.size();i++){
				title_id.put(titles.get(i),new Integer(i+1));
			}
			
			jar.putNextEntry(new JarEntry("title"));
			DataOutputStream lookup = new DataOutputStream(jar);
			lookup.writeInt(titles.size());
			for (int i =0;i<titles.size();i++){
				String s = (String)titles.get(i);
				byte[] temp = s.getBytes("ISO8859-1");
				lookup.writeInt(temp.length);
				lookup.write(temp);
			}
			lookup.flush();
			

			jar.putNextEntry(new JarEntry("prog"));
			DataOutputStream dayprog = new DataOutputStream(jar);
			dayprog.writeInt(data_length*CH_to_export.length);
			progress.setMessage(mLocalizer.msg("converting basic data","converting basic data"));
			for (int i=0;i<data_length;i++){
				devplugin.Date D = new devplugin.Date(today);
				D = D.addDays(i);
				for (int j =0;j<CH_to_export.length;j++){
					
					Iterator it = PM.getChannelDayProgram(D,CH_to_export[j]);
					
					Vector[] dayprog_parts = new Vector[6];
					for (int k =0;k<dayprog_parts.length;k++){
						dayprog_parts[k] = new Vector();
					}
					
					int part = 0;
					
					boolean[] reminder_flag = new boolean[6];
					boolean[] favorites_flag = new boolean[6];
					
					for (int k=0;k<favorites_flag.length;k++){
						favorites_flag[k] = false;
						reminder_flag[k] = false;
					}
					if (it!=null){
						Vector to_use = dayprog_parts[0];
						int limithour = 4;
						boolean error = false;
						while ((it.hasNext()) && (!error)){
							Program toSave = (Program) it.next();
							
							while (toSave.getHours()>=limithour){
								if (toSave.getHours() > 23){
									System.out.println ("BB "+toSave.getHours());
									System.out.println (toSave.toString());
									error = true;
									
									
									//"Can't export "+toSave.getTitle()+", invalid time: "+toSave.getHours()+":"+toSave.getMinutes()
									//"Can't export {0}, invalid time: {1}:{2}"
									
									Object[] O = {toSave.getTitle(),Integer.toString(toSave.getHours()),Integer.toString(toSave.getMinutes())};
									exportLog.append(mLocalizer.msg("Can't export 0, invalid time: 1:2","Can't export {0}, invalid time: {1}:{2}",O));
									exportLog.append("\n");
									limithour = toSave.getHours() + 1;
								} else {
									limithour +=4;
									part++;
									to_use = dayprog_parts[part];
								}
							}
							if (error){
								continue;
							}
							ByteArrayOutputStream temp_out = new ByteArrayOutputStream();
							DataOutputStream prog_out = new DataOutputStream(temp_out);
							String title = toSave.getTitle();
							
							if (title == null){
								
								//CH_to_export[j]+": Entry without title ? ("+D.getDayOfMonth()+"."+(D.getMonth()+1)+" "+toSave.getHours()+":"+toSave.getMinutes()+")\n"
								//"{0}: Entry without title ? ({1}.{2} {3}:{4})"
								Object[] O = new Object[5];
								O[0] = CH_to_export[j];
								if (D.getDayOfMonth()<10){
									O[1] = "0"+Integer.toString(D.getDayOfMonth());
								} else {
									O[1] = Integer.toString(D.getDayOfMonth());
								}
								if (D.getMonth()<10){
									O[2] = "0"+Integer.toString((D.getMonth()));
								} else {
									O[2] = Integer.toString((D.getMonth()));
								}
								if (toSave.getHours()<10){
									O[3] = "0"+Integer.toString(toSave.getHours());
								} else {
									O[3] = Integer.toString(toSave.getHours());
								}
								if (toSave.getMinutes()<10){
									O[4] = "0"+Integer.toString(toSave.getMinutes());
								} else {
									O[4] = Integer.toString(toSave.getMinutes());
								}
								
								exportLog.append(mLocalizer.msg("0: Entry without title ? (1.2 3:4)","{0}: Entry without title ? ({1}.{2} {3}:{4})",O));
								exportLog.append("\n");
								title = " ";
							}
							if (title.length() > 100){
								title = title.substring(0,100);
							}
							
							
							prog_out.write(toSave.getHours());
							prog_out.write(toSave.getMinutes());
							
							if (this.mtbp.getExportLevel() !=0){
								int info_short = toSave.getInfo();
								if (info_short<0){
									info_short = 0;
								} else {
									for (int ii =0;ii<used_info.length;ii++){
										if ((info_short & (0x01 << ii)) !=0){
											used_info[ii] = true;
										}
									}
								}
								prog_out.writeByte(info_short & 0xFF);
								
								info_short = info_short << 8;
								int temp = getRating(toSave);
								int copy = info_short;
								info_short = info_short | (temp << 5);
								if (temp !=6){
									used_rating[temp] = true;
								}
							
								if (markedBy(toSave,"favo")){
									info_short = info_short | (0x01 << 3);
									favorites_flag[part] = true;
									usedFav = true;
								}
								if (markedBy(toSave,"remin")){
									info_short = info_short | (0x01 << 4);
									reminder_flag[part] = true;
									usedRem = true;
								}
							
								prog_out.writeByte(info_short & 0xFF);
							} else {
								prog_out.writeByte(0);
								prog_out.writeByte(6 << 5);
							}
							
							writeTitleString(prog_out,title);
							/*
							prog_out.writeShort(4711);
							prog_out.writeShort(4711);
							*/
							prog_out.flush();
							
							to_use.add(temp_out.toByteArray());
						}
					} else {
						//"Can't export "+CH_to_export[j]+" on "+D.getDayOfMonth()+":"+(D.getMonth()+1)+" : No valid data found\n"
						//"Can't export {0} on {1}:{2} : No valid data found"
						Object[] O = {CH_to_export[j],Integer.toString(D.getDayOfMonth()),Integer.toString((D.getMonth()))};
						exportLog.append(mLocalizer.msg("Can't export 0 on 1.2 : No valid data found","Can't export {0} on {1}.{2} : No valid data found",O));
						exportLog.append("\n");						
					}
					
					int size_of_record = 0;
					int[] number_of_prog = new int[6];
					
					
					
					for (int k=0;k<number_of_prog.length;k++){
						number_of_prog[k] = dayprog_parts[k].size();
						for (int kk=0;kk<number_of_prog[k];kk++){
							size_of_record += ((byte[])dayprog_parts[k].get(kk)).length;
						}
					}
					dayprog.writeInt (size_of_record+6);
					for (int k=0;k<number_of_prog.length;k++){
						dayprog.write(number_of_prog[k]);
					}
					for (int k=0;k<number_of_prog.length;k++){
						for (int kk=0;kk<dayprog_parts[k].size();kk++){
							dayprog.write((byte[])dayprog_parts[k].get(kk));
						}
					}
					dayprog.flush();
				}
			}
			
			
			progress.setMessage(mLocalizer.msg("converting icons","converting icons"));
			jar.putNextEntry(new JarEntry("i"));
			DataOutputStream icons = new DataOutputStream(jar);
			
			Vector rawData = new Vector();
			ByteArrayOutputStream IconData = new ByteArrayOutputStream();
			
			JarEntry ICON_JAE = JA.getJarEntry("microtvbrowserplugin/data/Spacer.png");
			InputStream ICON_In = JA.getInputStream(ICON_JAE);
			int tempint = pipe(ICON_In,IconData);
			System.out.println("."+tempint);
			icons.writeShort(tempint);
				
			if (usedFav){
				ICON_JAE = JA.getJarEntry("microtvbrowserplugin/data/Fav.png");
				ICON_In = JA.getInputStream(ICON_JAE);
				tempint = pipe(ICON_In,IconData);
				System.out.println("."+tempint);
				icons.writeShort(tempint);
			} else {
				icons.writeShort(0);
			}
			
			if (usedRem){
				ICON_JAE = JA.getJarEntry("microtvbrowserplugin/data/Rem.png");
				ICON_In = JA.getInputStream(ICON_JAE);
				tempint = pipe(ICON_In,IconData);
				System.out.println("."+tempint);
				icons.writeShort(tempint);
			} else {
				icons.writeShort(0);
			}
			
			for (int i =0;i<6;i++){
				if (used_rating[i]){
					ICON_JAE = JA.getJarEntry("microtvbrowserplugin/data/b"+i);
					ICON_In = JA.getInputStream(ICON_JAE);
					tempint = pipe(ICON_In,IconData);
					System.out.println("."+tempint);
					icons.writeShort(tempint);
				} else {
					icons.writeShort(0);
				}
			}
			for (int i =0;i<used_info.length;i++){
				if (used_info[i]){
					try {
						ICON_JAE = JA.getJarEntry("microtvbrowserplugin/data/i"+i);
						ICON_In = JA.getInputStream(ICON_JAE);
						icons.writeShort(pipe(ICON_In,IconData));
					} catch (Exception E1){
						icons.writeShort(0);
					}
				} else {
					icons.writeShort(0);
				}
			}
			icons.write(IconData.toByteArray());
			icons.flush();
			
			
			if (dataToExport.length !=0){
				progress.setMessage(mLocalizer.msg("converting extended data","converting extended data"));
				for (int j =0;j<CH_to_export.length;j++){
					for (int i=0;i<data_length;i++){
						devplugin.Date D = new devplugin.Date (today);
						D = D.addDays (i);
						Iterator it = PM.getChannelDayProgram(D,CH_to_export[j]);
						
						jar.putNextEntry (new JarEntry (j+"."+i));
						
						DataOutputStream exDayprog = new DataOutputStream (jar);
						
						Vector exData = new Vector();
						if (it !=null){
							boolean error = false;
							while ((it.hasNext()) && (!error)){
								Program toSave = (Program) it.next();
								if (toSave.getHours() > 23){
									error = true;
									continue;
								}
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								DataOutputStream dout = new DataOutputStream(out);
								for (int kk=0;kk<dataToExport.length;kk++){
									String str = "";
									if (dataToExport[kk].getFormat() ==
											dataToExport[kk].TEXT_FORMAT){
											str = toSave.getTextField(dataToExport[kk]);
									} else {
										if (dataToExport[kk].getFormat() ==
												dataToExport[kk].TIME_FORMAT){
											str = toSave.getTimeFieldAsString(dataToExport[kk]);
										} else {
											if (dataToExport[kk].getFormat() ==
												dataToExport[kk].INT_FORMAT){
												str = toSave.getIntFieldAsString(dataToExport[kk]);
											} else {
												System.out.println ("error while saving: "+dataToExport[kk].getFormat()+" "+dataToExport[kk].getName());
												str = "internal error";
											}
										}
									}
									if (str !=null){
										dout.writeUTF(str);
									} else {
										dout.writeUTF("");
									}
								}
								dout.flush();
								byte[] data = out.toByteArray();
								exData.add (data);
								exDayprog.writeShort (data.length);
							}
						}
						for (int ii=0;ii<exData.size();ii++){
							exDayprog.write ((byte[])exData.get(ii));
						}
						exDayprog.flush();
					}
				}
			}
			
			jar.flush();
			jar.close();
			
			File JAD = new File(dir,"MicroTvBrowser.jad");
			
			BufferedWriter BUF = new BufferedWriter(new FileWriter(JAD));
			BUF.write(
			"MIDlet-1: MicroTvBrowser, MicroTvBrowser.png, tv\n"
			+"MIDlet-Jar-Size: "+MicroTvBrowser.length()+"\n"
			+"MIDlet-Jar-URL: MicroTvBrowser.jar\n"
			+"MIDlet-Name: MicroTvBrowser\n"
			+"MIDlet-Vendor: Unknown\n"
			+"MIDlet-Version: 1.0\n"
			+"MicroEdition-Configuration: CLDC-1.0\n"
			+"MicroEdition-Profile: MIDP-1.0\n"
			);
			BUF.flush();
			BUF.close();
			progress.setMessage(mLocalizer.msg("done","done"));
			try {
				String user_log = exportLog.toString();
				if (user_log.length()!=0){
					JOptionPane.showMessageDialog(mtbp.getParentFramePublic(),mLocalizer.msg("MIDlet created, but with problems","MIDlet created, but with problems")+":\n"+user_log,mLocalizer.msg("done","done"), JOptionPane.WARNING_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(mtbp.getParentFramePublic(),mLocalizer.msg("MIDlet created.","MIDlet created.")+"\n"+mLocalizer.msg("Please install now","Please install now")+"\n"+JAD.toString()+"\n"+mLocalizer.msg("on your mobile device.","on your mobile device."),mLocalizer.msg("done","done"), JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception E1){
				E1.printStackTrace(System.out);
			}
			
		}catch (Exception E){
			CharArrayWriter BOUT = new CharArrayWriter();
			E.printStackTrace(new PrintWriter(BOUT));
			//LOG.append (BOUT.toString());
			E.printStackTrace();
			JOptionPane.showMessageDialog(mtbp.getParentFramePublic(),mLocalizer.msg("MIDlet NOT created because:","MIDlet NOT created because:")+" \n"+BOUT.toString(),mLocalizer.msg("ERROR","ERROR"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private int getRating(Program p){
		Plugin rate = null;
		Plugin[] ps = mtbp.getPluginManager().getInstalledPlugins();
		for (int i=0;i<ps.length;i++){
			if (ps[i].getClass().toString().toLowerCase().indexOf("rate")!=-1){
				rate = ps[i];
			}
		}
		if (rate!=null){
			Icon[] I = rate.getProgramTableIcons(p);
			if (I== null){
				return 6;
			}
			if (I.length != 1){
				return 6;
			}
			BufferedImage BI = new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);
			I[0].paintIcon(new JLabel(" "), BI.getGraphics(), 0,0);
			int green = 0;
			int yellow = 0;
			for (int i =0;i<16;i++){
				for (int j =0;j<16;j++){
					int color = BI.getRGB(i,j);
					int B = color & 0xFF;
					int G = (color & 0xFF00) >> 8;
					int R = (color & 0xFF0000) >> 16;
					if ((R>200) & (G>200)){
						yellow++;
					}
				}
			}
			
			//Reale Werte: 5  13  26  42  45  66
			//              10  20  30  44  50
			if ((yellow!=5) && (yellow!=13) && (yellow!=26) && (yellow!=42) && (yellow!=45) && (yellow!=66)){
				exportLog.append(mLocalizer.msg("Can't parse rating for","Can't parse rating for")+" "+p.getTitle()+"\n");
				exportLog.append("\n");				
			}
			if (yellow < 10){
				return 5;
			} else if (yellow < 20){
				return 4;
			} else if (yellow < 30){
				return 3;
			} else if (yellow < 44){
				return 2;
			} else if (yellow < 50){
				return 1;
			} else {
				return 0;
			}
		}
		return 6;
	}
	
	private boolean markedBy(Program p, String name){
		return (getPlugin(p,name) !=null);
	}
	
	private Plugin getPlugin(Program p, String name){
		//LOG.append (" starting search for "+name+" in "+p.getTitle()+"\n");
		Plugin[] Ps = p.getMarkedByPlugins();
		//LOG.append (" starting search for "+name+"\n");
		for (int i=0;i<Ps.length;i++){
			//LOG.append (" Got: "+Ps[i].getClass().toString()+"\n");
			if (Ps[i].getClass().toString().toLowerCase().indexOf(name) != -1){
				//LOG.append ("OK \n");
				return Ps[i];
			}
		}
		//LOG.append ("NOK \n");
		return null;
	}
	
}
