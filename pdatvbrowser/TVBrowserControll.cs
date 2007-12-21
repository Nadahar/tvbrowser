#region Using-Direktiven

using System;
using System.Collections;
using System.Data;
using System.IO;
using System.Text;
using System.Reflection;
using System.Data.SQLite;
using System.Data.Common;
using System.Drawing;
using System.Data.SqlTypes;
using System.Windows.Forms;
using System.Threading;
using Microsoft.WindowsCE.Forms;


#endregion

namespace TVBrowserMini
{
    /// <summary>
    /// Controll-Class from TV-Browser Mini
    /// </summary>
    public class TVBrowserControll
    {
        private ArrayList channels;
        private Channel currentChannel;
        private String systemPath;
        private String dbPath;
        private DbConnection dbConnect;
        private DbCommand dbCommand;
        private DbDataReader dbReader;
        private DateTime currentDate;
        private bool dB;
        private bool filterFavorites;
        private bool filterReminders;
        private bool refreshReminders = true;
        private Mainform main;
        private bool popupReminder, playReminderSound;
        private bool popupFavorite;
        private String soundReminder;
        private String soundFavorite;
        private int soundReminderTime = 2;
        private int soundFavoriteTime = 0;
        private int refreshTime = -1;
        private int lastView; //1=normal; 2=current; 3=next; 4=evening; 5=reminderAt; 6=allFavorites; 7= allReminders 0=nothing to refresh
        private SizeF currentScreen;
        private bool startAsCurrent;
        private bool landscape;
        private bool showRadio;
        private bool showTV;
        private String currentLanguage;
        private Hashtable languages;
        private String videoMode;
        private String currentDB;
        private String lastDB;
        private ArrayList errorMessages;
        private int startHour;
        private int endHour;
        private int listenPort;
        private bool listen;
        private TCPTransfer trans;

        public TVBrowserControll(Mainform main)
        {
            this.filterFavorites = false;
            this.filterReminders = false;
            this.lastDB = "";
            this.currentDB = "";
            this.errorMessages = new ArrayList();
            this.showRadio = true;
            this.showTV = true;
            this.main = main;
            this.init();
            this.loadConf();
            if (listen)
            {
                this.trans = new TCPTransfer(this, listenPort);
            }
            this.channels = new ArrayList();
            this.loadLastSettings();
            this.checkDBExists();
        }

        public bool checkDBExists()
        {
            if (File.Exists(this.dbPath))
            {
                this.dB = true;
                this.sqlinit();
                readChannels();
                if (this.channels.Count > 0)
                {
                    currentChannel = (Channel)this.channels[0];
                }
                return true;
            }
            else
            {
                this.dB = false;
                return false;
            }
        }

        private void init()
        {
            this.dB = false;
            this.lastView = 1;
            this.currentDate = DateTime.Now;
            this.systemPath = System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase);
            Directory.SetCurrentDirectory(this.systemPath);
        }

        public void readChannels()
        {
            if (this.dB)
            {
                this.channels.Clear();
                if (this.showTV || this.showRadio)
                {
                    this.dbCommand.CommandText = "SELECT id, name, category FROM channel where";
                    if (this.showTV)
                    {
                        this.dbCommand.CommandText += " category = 't'";
                    }
                    if (this.showRadio)
                    {
                        if (this.showTV)
                            this.dbCommand.CommandText += " or";

                        this.dbCommand.CommandText += " category = 'r'";
                    }
                    try
                    {
                        this.dbReader = this.dbCommand.ExecuteReader();
                        while (this.dbReader.Read())
                        {
                            this.channels.Add(new Channel(this.dbReader["id"].ToString(), this.dbReader["name"].ToString(), this.dbReader["category"].ToString()[0]));
                        }
                        this.dbReader.Close();
                    }
                    catch (Exception ex)
                    {
                        this.dB = false;
                        String dummy = ex.Message.ToString();
                        this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                        Application.Exit();
                    }
                }
            }
        }

        public ArrayList getDates(String channelId)
        {
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = "SELECT DISTINCT strftime('%Y-%m-%d', b.start) as start FROM broadcast b WHERE channel_id = '" + channelId + "'";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strDate = this.dbReader["start"].ToString();
                        DateTime date = new DateTime(Int32.Parse(strDate.Split('-')[0]), Int32.Parse(strDate.Split('-')[1]), Int32.Parse(strDate.Split('-')[2]));
                        result.Add(new TVBrowserDate(date, this));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getAllDates()
        {
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = "SELECT DISTINCT strftime('%Y-%m-%d', b.start) as start FROM broadcast b";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strDate = this.dbReader["start"].ToString();
                        DateTime date = new DateTime(Int32.Parse(strDate.Split('-')[0]), Int32.Parse(strDate.Split('-')[1]), Int32.Parse(strDate.Split('-')[2]));
                        //DateTime date = new DateTime(Int32.Parse(this.dbReader[0].ToString()), Int32.Parse(this.dbReader[1].ToString()), Int32.Parse(this.dbReader[2].ToString()));
                        result.Add(new TVBrowserDate(date, this));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getBroadcastInformation(int id)
        {
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = "SELECT * FROM info WHERE broadcast_id = '" + id + "'";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    for (int i = 0; i < this.dbReader.FieldCount; i++)
                    {
                        if (!this.dbReader.GetName(i).Equals("broadcast_id"))
                        {
                            if (!(this.dbReader[i].ToString().Equals("") || this.dbReader[i].ToString().Equals("-1")))
                            {
                                String[] element = { this.dbReader.GetName(i), this.dbReader[i].ToString() };
                                result.Add(element);
                            }
                        }
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getColumnNames()
        {
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = "SELECT * FROM info limit 1";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    for (int i = 0; i < this.dbReader.FieldCount; i++)
                    {
                        if (!this.dbReader.GetName(i).Equals("broadcast_id"))
                        {
                            result.Add(this.dbReader.GetName(i));
                        }
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getBroadcasts(String channelId, DateTime date)
        {
            this.lastView = 1;
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                DateTime nextDay = date.AddDays(1);

                this.dbCommand.CommandText = "SELECT b.id as id, b.tvbrowserID as tvbrowserid,  c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder  FROM channel c, broadcast b WHERE c.id = b.channel_id and channel_id = '" + channelId + "'";
                if (this.filterReminders)
                {
                    this.dbCommand.CommandText += " AND b.reminder='1'";
                }
                if (this.filterFavorites)
                {
                    this.dbCommand.CommandText += " AND b.favorite='1'";
                }
                //Exact one day
                //this.dbCommand.CommandText += " AND date(b.start) = date('" + date.Year + "-" + month + "-" + day + "') ORDER BY b.start";
                this.dbCommand.CommandText += " AND (b.end BETWEEN datetime('" + date.ToString("yyyy-MM-dd") + "', '+" + this.startHour + " hours') AND datetime('" + nextDay.ToString("yyyy-MM-dd") + "'," + "'+" + this.endHour + " hours','+59 minutes')) AND b.start >= datetime('" + date.ToString("yyyy-MM-dd") + "', '+" + this.startHour + " hours') AND b.start<= datetime('" + nextDay.ToString("yyyy-MM-dd") + "', '+" + this.endHour + " hours') ORDER BY b.start";
                
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        //result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["channel"].ToString(), this.dbReader["title"].ToString(), this.dbReader["short"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                        result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getCurrentBroadcasts()
        {
            this.lastView = 2;
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = "SELECT b.id as id, b.tvbrowserID as tvbrowserid, c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder  FROM channel c, broadcast b WHERE (c.id = b.channel_id) AND (b.start <= datetime('now', 'localtime')) AND (datetime(b.end) > datetime('now', 'localtime'))";
                if (this.showRadio)
                {
                    if (this.showTV)
                        this.dbCommand.CommandText += " AND ((c.category='r') OR (c.category='t'))";
                    else
                        this.dbCommand.CommandText += " AND (c.category='r')";
                }
                else if (this.showTV)
                {
                    this.dbCommand.CommandText += " AND (c.category='t')";
                }
                if (this.filterReminders)
                {
                    this.dbCommand.CommandText += " AND (b.reminder='1')";
                }
                if (this.filterFavorites)
                {
                    this.dbCommand.CommandText += " AND (b.favorite='1')";
                }
                
                this.dbCommand.CommandText += " AND (b.start BETWEEN datetime('now', 'localtime', '-12 hours') AND datetime('now', 'localtime')) ORDER BY b.id";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        if (dtEnd > DateTime.Now)
                            result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getBroadcastsRunningAt(DateTime dt)
        {
            this.lastView = 5;
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                String hour = dt.Hour.ToString();
                if (dt.Hour < 10)
                    hour = "0" + dt.Hour;
                String minute = dt.Minute.ToString();
                if (dt.Minute < 10)
                    minute = "0" + dt.Minute;
                String day = dt.Day.ToString();
                if (dt.Day < 10)
                    day = "0" + dt.Day;
                String month = dt.Month.ToString();
                if (dt.Month < 10)
                    month = "0" + dt.Month;

                this.dbCommand.CommandText = "SELECT b.id as id, b.tvbrowserID as tvbrowserid, c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder  FROM channel c, broadcast b WHERE (c.id = b.channel_id) AND (b.start <= datetime('" + dt.Year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00')) AND (datetime(b.end) > datetime('" + dt.Year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00'))";
                if (this.showRadio)
                {
                    if (this.showTV)
                        this.dbCommand.CommandText += " AND ((c.category='r') OR (c.category='t'))";
                    else
                        this.dbCommand.CommandText += " AND (c.category='r')";
                }
                else if (this.showTV)
                {
                    this.dbCommand.CommandText += " AND (c.category='t')";
                }
                if (this.filterReminders)
                {
                    this.dbCommand.CommandText += " AND (b.reminder='1')";
                }
                if (this.filterFavorites)
                {
                    this.dbCommand.CommandText += " AND (b.favorite='1')";
                }
                this.dbCommand.CommandText += " AND (b.start BETWEEN datetime('" + dt.Year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00', '-3 hours') AND datetime('" + dt.Year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00')) ORDER BY b.id";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false; 
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getNextBroadcasts()
        {
            this.lastView = 3;
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = "SELECT b.id as id, b.tvbrowserID, c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder  FROM channel c, broadcast b WHERE (c.id = b.channel_id) AND b.id IN";
                this.dbCommand.CommandText += " (SELECT b.id+1 as id FROM channel c, broadcast b WHERE (c.id = b.channel_id) AND (b.start <= datetime('now', 'localtime')) AND (datetime(b.end) > datetime('now', 'localtime'))";
                if (this.showRadio)
                {
                    if (this.showTV)
                        this.dbCommand.CommandText += " AND ((c.category='r') OR (c.category='t'))";
                    else
                        this.dbCommand.CommandText += " AND (c.category='r')";
                }
                else if (this.showTV)
                {
                    this.dbCommand.CommandText += " AND (c.category='t')";
                }
                if (this.filterReminders)
                {
                    this.dbCommand.CommandText += " AND (b.reminder='1')";
                }
                if (this.filterFavorites)
                {
                    this.dbCommand.CommandText += " AND (b.favorite='1')";
                }
                this.dbCommand.CommandText += " AND (b.start BETWEEN datetime('now', 'localtime', '-12 hours') AND datetime('now', 'localtime')) ORDER BY b.id)";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getPrimetimeBroadcasts(DateTime date)
        {
            this.lastView = 4;
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                String month = "";
                String day = "";
                if (date.Month < 10)
                    month += "0";
                month += date.Month;
                if (date.Day < 10)
                    day += "0";
                day += date.Day;

                this.dbCommand.CommandText = "SELECT b.id as id, b.tvbrowserID as tvbrowserid, c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder  FROM channel c, broadcast b WHERE (c.id = b.channel_id) AND (b.start BETWEEN datetime('" + date.Year + "-" + month + "-" + day + " 19:59:59') AND datetime('" + date.Year + "-" + month + "-" + day + " 20:16:00')) ORDER BY b.id";
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getBroadcastsByFilter(bool favorite, bool reminder, DateTime date)
        {
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                String month = "";
                String day = "";
                if (date.Month < 10)
                    month += "0";
                month += date.Month;
                if (date.Day < 10)
                    day += "0";
                day += date.Day;
                String stmt = "SELECT b.id as id, b.tvbrowserID as tvbrowserid, c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder FROM channel c, broadcast b WHERE c.id = b.channel_id and date(b.start) = date('" + date.Year + "-" + month + "-" + day + "')";
                if (favorite)
                    stmt += " AND b.favorite=1 ";
                if (reminder)
                    stmt += " AND b.reminder=1 ";
                this.dbCommand.CommandText = stmt;
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    result.Sort(new BroadcastComparer());
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
            }
            return result;
        }

        public ArrayList getBroadcastsBySearch(String search)
        {
            ArrayList result = new ArrayList();
            if (this.dB)
            {
                this.dbCommand.CommandText = search;
                try
                {
                    this.dbReader = this.dbCommand.ExecuteReader();
                    while (this.dbReader.Read())
                    {
                        String strStart = this.dbReader["start"].ToString();
                        String strEnd = this.dbReader["end"].ToString();
                        DateTime dtStart = new DateTime(Int32.Parse(strStart.Split('-')[0]), Int32.Parse(strStart.Split('-')[1]), Int32.Parse(strStart.Split('-')[2]), Int32.Parse(strStart.Split('-')[3]), Int32.Parse(strStart.Split('-')[4]), 0);
                        DateTime dtEnd = new DateTime(Int32.Parse(strEnd.Split('-')[0]), Int32.Parse(strEnd.Split('-')[1]), Int32.Parse(strEnd.Split('-')[2]), Int32.Parse(strEnd.Split('-')[3]), Int32.Parse(strEnd.Split('-')[4]), 0);
                        result.Add(new Broadcast(Int32.Parse(this.dbReader["id"].ToString()), this.dbReader["tvbrowserid"].ToString(), this.dbReader["channel"].ToString(), this.dbReader["channel_id"].ToString(), this.dbReader["title"].ToString(), dtStart, dtEnd, Boolean.Parse(this.dbReader["favorite"].ToString()), Boolean.Parse(this.dbReader["reminder"].ToString())));
                    }
                    this.dbReader.Close();
                }
                catch
                {
                    this.dB = false;
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.Error", "Error"), this.getLanguageElement("TVBrowserControll.DBError", "Database - Error. Your Export-Plugin isn't compatible with TV-Browser Mini Version"), DialogResult.OK, "Application.Exit()"));
                }
                result.Sort(new BroadcastComparer());
            }
            return result;
        }

        public void updateBroadcastReminder(int id, bool v)
        {
            if (this.dB)
            {
                int value = 1;
                if (!v)
                    value = 0;
                String stmt = "UPDATE 'broadcast' SET reminder='" + value + "' WHERE id='" + id + "'";
                this.updateBroadcast(stmt);
            }
        }

        public void updateBroadcastFavorite(int id, bool v)
        {
            if (this.dB)
            {
                int value = 1;
                if (!v)
                    value = 0;
                String stmt = "update 'broadcast' set favorite='" + value + "' WHERE id='" + id + "'";
                this.updateBroadcast(stmt);
            }
        }

        private void updateBroadcast(String stmt)
        {
            this.dbCommand.CommandText = stmt;
            this.dbCommand.ExecuteScalar();
        }

        public ArrayList getChannels()
        {
            return this.channels;
        }

        public void setCurrentChannel(Channel aChannel)
        {
            this.currentChannel = aChannel;
        }

        public Channel getCurrentChannel()
        {
            return this.currentChannel;
        }

        public bool hasDB()
        {
            return this.dB;
        }

        public DateTime getCurrentDate()
        {
            return this.currentDate;
        }

        public void setCurrentDate(DateTime aDate)
        {
            this.currentDate = aDate;
        }

        public String getSystemPath()
        {
            return this.systemPath;
        }

        public Mainform getMainform()
        {
            return this.main;
        }

        public bool getFilterFavorites()
        {
            return this.filterFavorites;
        }

        public bool isPlayReminderSound()
        {
            return this.playReminderSound;
        }

        public void setFilterFavorites(bool value)
        {
            this.filterFavorites = value;
        }

        public bool getFilterReminders()
        {
            return this.filterReminders;
        }

        public ArrayList getErrorMessages()
        {
            return this.errorMessages;
        }

        public void setFilterReminders(bool value)
        {
            this.filterReminders = value;
        }

        public bool isShowRadio()
        {
            return this.showRadio;
        }

        public void setShowRadio(bool value)
        {
            this.showRadio = value;
        }

        public bool isShowTV()
        {
            return this.showTV;
        }

        public void setShowTV(bool value)
        {
            this.showTV = value;
        }

        public void setSoundReminder(String value)
        {
            this.soundReminder = value;
        }

        public void setSoundFavorite(String value)
        {
            this.soundFavorite = value;
        }

        public String getSoundReminder()
        {
            return this.soundReminder;
        }

        public String getSoundFavorite()
        {
            return this.soundFavorite;
        }

        public void setPopupReminder(bool value)
        {
            this.popupReminder = value;
        }

        public void setPopupFavorite(bool value)
        {
            this.popupFavorite = value;
        }

        public bool isPopupReminder()
        {
            return this.popupReminder;
        }

        public bool isPopupFavorite()
        {
            return this.popupFavorite;
        }

        public bool isStartAsCurrent()
        {
            return this.startAsCurrent;
        }

        public int getSoundReminderTime()
        {
            return this.soundReminderTime;
        }

        public int getSoundFavoriteTime()
        {
            return this.soundFavoriteTime;
        }

        public int getRefrehTime()
        {
            return this.refreshTime;
        }

        public int getLastView()
        {
            return this.lastView;
        }

        public void setLastView(int view)
        {
            this.lastView = view;
        }

        public bool isLandscape()
        {
            return this.landscape;
        }

        public void setLandscape(bool value)
        {
            this.landscape = value;
        }

        public bool isRefreshReminders()
        {
            return this.refreshReminders;
        }

        public void setRefreshReminders(bool value)
        {
            this.refreshReminders = value;
        }

        public void openDBConnect()
        {
            try
            {
                this.dbConnect.Open();
            }
            catch
            {

            }
        }

        public void closeDBConnect()
        {
            this.dB = false;
            this.dbReader.Close();
            this.dbCommand.CommandText = "";
            this.dbConnect.Close();
            this.dbConnect.Dispose();
            Thread.Sleep(1000);
               
        }

        public void setCurrentScreen(SizeF value)
        {
            this.currentScreen = value;
        }

        public SizeF getCurrentScreen()
        {
            return this.currentScreen;
        }

        public int getPixel(int percent)
        {
            return (percent * this.main.Width / 100);
        }

        public void sqlinit()
        {
            try
            {
                FileInfo fi = new FileInfo(this.dbPath);
                this.currentDB = fi.CreationTime.ToString("yyyy-MM-dd-HH-mm-ss");
                if (!this.lastDB.Equals(this.currentDB) && !this.lastDB.Equals(""))
                {
                    this.errorMessages.Add(new ErrorMessage(this.getLanguageElement("TVBrowserControll.newDB","new database found"), this.getLanguageElement("TVBrowserControll.TransferReminders","Would you like to transfer reminders to the new database?"), DialogResult.Cancel, "TVBrowserControll.transferReminders()"));
                }
                DateTime created = fi.CreationTime;
                this.dbConnect = new SQLiteConnection("Data Source=" + this.dbPath);
                this.dbConnect.Open();
                this.dbCommand = this.dbConnect.CreateCommand();
            }
            catch (Exception ex)
            {
                String dummy = ex.Message;
            }
        }

        public void saveLastSettings()
        {
            try
            {
                if (File.Exists(this.getSystemPath() + "\\save.csv"))
                {
                    File.Delete(this.getSystemPath() + "\\save.csv");
                }
                FileInfo t = new FileInfo(this.getSystemPath() + "\\save.csv");
                StreamWriter writer = t.CreateText();

                writer.WriteLine(this.currentChannel.getId());//letzter Sender
                if (this.lastView == 2)
                {
                    writer.WriteLine(1); //Letzte Ansicht "Current"
                }
                else
                {
                    writer.WriteLine(0); // Normale Ansicht
                }
                if (this.showTV)
                {
                    writer.WriteLine(1);
                }
                else
                {
                    writer.WriteLine(0);
                }
                if (this.showRadio)
                {
                    writer.WriteLine(1);
                }
                else
                {
                    writer.WriteLine(0);
                }
                writer.WriteLine(this.currentDB);
                writer.Close();
            }
            catch (Exception ex)
            {
                ex.ToString();
            }
        }

        public void loadLastSettings()
        {
            if (File.Exists(this.getSystemPath() + "\\save.csv"))
            {
                StreamReader reader = File.OpenText(this.getSystemPath() + "\\save.csv");
                String input = null;
                int index = 0;
                while ((input = reader.ReadLine()) != null)
                {
                    if (index == 0)
                    {
                        for (int i = 0; i < this.channels.Count; i++)
                        {
                            Channel temp = (Channel)this.channels[i];
                            if (temp.getId().Equals(input))
                            {
                                this.currentChannel = temp;
                                break;
                            }
                        }
                    }
                    else if (index == 1)
                    {
                        if (input == "1")
                        {
                            this.startAsCurrent = true;
                            this.lastView = 1;
                        }
                    }
                    else if (index == 2)
                    {
                        if (input == "1")
                            this.showTV = true;
                        else
                            this.showTV = false;
                    }
                    else if (index == 3)
                    {
                        if (input == "1")
                            this.showRadio = true;
                        else
                            this.showRadio = false;
                    }
                    else if (index == 4)
                    {
                        this.lastDB = input;
                    }
                    index++;
                }
                reader.Close();
            }
        }

        public void loadConf()
        {
            StreamReader re = File.OpenText(this.getSystemPath() + "\\conf.csv");
            String input = null;
            while ((input = re.ReadLine()) != null)
            {
                try
                {
                    if (input.StartsWith("#"))
                    {
                    }
                    else if (input.StartsWith("[$dbPath]"))
                    {
                        input = input.Remove(0, 9);
                        this.dbPath = input;
                    }
                    else if (input.StartsWith("[$reload]"))
                    {
                        input = input.Remove(0, 9);
                        int intInput = Int32.Parse(input);
                        if (intInput == 0)
                            this.soundReminderTime = -1;
                        else if (intInput == 1)
                            this.refreshTime = 60;
                        else if (intInput == 2)
                            this.refreshTime = 120;
                        else if (intInput == 3)
                            this.refreshTime = 180;
                        else if (intInput == 4)
                            this.refreshTime = 240;
                        else if (intInput == 5)
                            this.refreshTime = 300;
                        else if (intInput == 6)
                            this.refreshTime = 600;
                        else if (intInput == 7)
                            this.refreshTime = 1800;
                    }
                    else if (input.StartsWith("[$reminderWav]"))
                    {
                        input = input.Remove(0, 14);
                        this.soundReminder = input;
                    }
                    else if (input.StartsWith("[$reminderMinutes]"))
                    {
                        input = input.Remove(0, 18);
                        int intInput = Int32.Parse(input);
                        this.soundReminderTime = intInput;
                    }
                    else if (input.StartsWith("[$reminderPopup]"))
                    {
                        input = input.Remove(0, 16);
                        if (input == "True")
                        {
                            this.popupReminder = true;
                        }
                        else if (input == "False")
                        {
                            this.popupReminder = false;
                        }
                    }
                    else if (input.StartsWith("[$reminderPlaySound]"))
                    {
                        input = input.Remove(0, 20);
                        if (input == "True")
                        {
                            this.playReminderSound = true;
                        }
                        else if (input == "False")
                        {
                            this.playReminderSound = false;
                        }
                    }

                    else if (input.StartsWith("[$favoriteWav]"))
                    {
                        input = input.Remove(0, 14);
                        this.soundFavorite = input;
                    }
                    else if (input.StartsWith("[$favoriteMinutes]"))
                    {
                        input = input.Remove(0, 18);
                        this.soundFavoriteTime = Int32.Parse(input);
                        int intInput = Int32.Parse(input);
                        if (intInput == 0)
                            this.soundFavoriteTime = -1;
                        else if (intInput == 1)
                            this.soundFavoriteTime = 0;
                        else if (intInput == 2)
                            this.soundFavoriteTime = 5;
                        else if (intInput == 3)
                            this.soundFavoriteTime = 10;
                        else if (intInput == 4)
                            this.soundFavoriteTime = 15;
                        else if (intInput == 5)
                            this.soundFavoriteTime = 30;
                        else if (intInput == 6)
                            this.soundFavoriteTime = 60;
                        else if (intInput == 7)
                            this.soundFavoriteTime = 120;
                    }
                    else if (input.StartsWith("[$favoritePopup]"))
                    {
                        input = input.Remove(0, 16);
                        if (input == "True")
                        {
                            this.popupFavorite = true;
                        }
                        else if (input == "False")
                        {
                            this.popupFavorite = false;
                        }
                    }
                    else if (input.StartsWith("[$language]"))
                    {
                        input = input.Remove(0, 11);
                        this.soundFavorite = input;
                        if (input.Equals(null) || input == "")
                            input = "English";
                        this.currentLanguage = input;
                    }
                    else if (input.StartsWith("[$daystartsat]"))
                    {
                        input = input.Remove(0, 14);
                        this.startHour = Int32.Parse(input);
                    }
                    else if (input.StartsWith("[$dayendsat]"))
                    {
                        input = input.Remove(0, 12);
                        this.endHour = Int32.Parse(input);
                    }

                    else if (input.StartsWith("[$port]"))
                    {
                        input = input.Remove(0, 7);
                        this.listenPort = Int32.Parse(input);
                    }
                    else if (input.StartsWith("[$trans]"))
                    {
                        input = input.Remove(0, 8);
                        if (input == "True")
                        {
                            this.listen = true;
                        }
                        else if (input == "False")
                        {
                            this.listen = false;
                        }
                    }

                }
                catch
                {
                }
            }
            re.Close();
            this.loadLanguage();
        }

        public ListItem createColoredListItemForSearchRunningAt(DateTime selectedDateTime, Broadcast broadcast, String text)
        {
            ListItem result = new ListItem();
            if (broadcast.isFavourite() || broadcast.isReminder())
            {
                result = new ListItem(text, Color.Black, Color.FromArgb(244, 255, 175), Color.FromArgb(229, 255, 60), this.getPixel(broadcast.getRelativePercent(selectedDateTime)));
            }
            else
            {
                result = new ListItem(text, Color.Black, Color.FromArgb(213, 244, 210), Color.FromArgb(105, 243, 91), this.getPixel(broadcast.getRelativePercent(selectedDateTime)));
            }
            return result;
        }


        public ListItem createColoredListItem(Broadcast broadcast, String text)
        {
            ListItem result = new ListItem();
            //bradcasts in the past
            if (broadcast.getEnd() < DateTime.Now)
            {

                if (broadcast.isFavourite() || broadcast.isReminder())
                {
                    result = new ListItem(text, Color.FromArgb(165, 165, 165), Color.FromArgb(255, 237, 237), Color.FromArgb(255, 237, 237), this.getPixel(100));
                }
                else
                {
                    result = new ListItem(text, Color.FromArgb(165, 165, 165), Color.White, Color.White, this.getPixel(100));
                }
            }
            //current broadcasts
            else if (broadcast.getStart() <= DateTime.Now && DateTime.Now <= broadcast.getEnd())
            {
                if (broadcast.isReminder() || broadcast.isFavourite())
                {
                    result = new ListItem(text, Color.Black, Color.FromArgb(228, 203, 230), Color.FromArgb(201, 176, 230), this.getPixel(broadcast.getPercent()));
                }
                else
                {
                    result = new ListItem(text, Color.Black, Color.FromArgb(225, 225, 225), Color.FromArgb(195, 195, 255), this.getPixel(broadcast.getPercent()));
                }
            }
            //broadcasts in the future
            else if (broadcast.isFavourite() || broadcast.isReminder())
            {
                result = new ListItem(text, Color.Black, Color.FromArgb(255, 215, 215), Color.FromArgb(255, 215, 215), this.getPixel(100));
            }
            else
            {
                result = new ListItem(text, Color.Black, Color.White, Color.White, this.getPixel(100));
            }
            return result;
        }

        public void addLanguageElement(String key, String value)
        {
            try
            {
                this.languages.Add(key, value);
            }
            catch (Exception ex)
            {
                String message = ex.Message;
            }
        }

        public String getLanguageElement(String key, String def)
        {
            String result = "";
            if (this.languages.ContainsKey(key))
                result = this.languages[key].ToString();
            else
                result = def;
            result.Replace("\\n","\r\n");
            return result;
        }

        public void setCurrentLanguage(String value)
        {
            this.currentLanguage = value;
        }

        public void setMainform(Mainform form)
        {
            this.main = form;
        }

        public void setVideoMode(String value)
        {
            this.videoMode = value;
        }

        public String getVideoMode()
        {
            return this.videoMode;
        }

        public String getDBPath()
        {
            return this.dbPath;
        }

        public void setDBPath(String path)
        {
            this.dbPath = path;
        }

        public void transferReminders()
        {
            if (this.dB)
            {
                String backup = "";
                if (File.Exists(this.getSystemPath() + "\\reminderBackup.csv"))
                {
                    StreamReader re = File.OpenText(this.getSystemPath() + "\\reminderBackup.csv");
                    String input = null;
                    while ((input = re.ReadLine()) != null)
                    {
                        try
                        {
                            String channelid = input.Split('|')[0];
                            String tvbrowserid = input.Split('|')[1];
                            String start = input.Split('|')[2];
                            String reminderString = input.Split('|')[3];
                            channelid = channelid.Replace("@_@", "|");

                            int reminder = 1;
                            if (reminderString.Equals("False"))
                                reminder = 0;

                            DateTime dt = new DateTime(Int32.Parse(start.Substring(0,4)),Int32.Parse(start.Substring(5,2)),Int32.Parse(start.Substring(8,2)));
                            if (dt > DateTime.Now.AddDays(-1))
                            {
                                backup += channelid + "|" + tvbrowserid + "|" + start+ "|" + reminderString + "\r\n";
                                String stmt = "UPDATE 'broadcast' SET reminder='" + reminder + "' WHERE tvbrowserID='" + tvbrowserid + "' AND start='" + start + "'";
                                this.updateBroadcast(stmt);
                            }
                        }
                        catch
                        {
                        }
                    }
                    try
                    {
                        re.Close();
                        FileInfo t = new FileInfo(this.getSystemPath() + "\\reminderBackup.csv");
                        StreamWriter writer = t.CreateText();
                        writer.Write(backup);
                        writer.Close();
                        this.saveLastSettings();
                    }
                    catch
                    {
                    }
                }
            }
            else
            {
                this.checkDBExists();
            }
        }

        public void backupReminder(Broadcast broadcast)
        {
            try
            {
                if (!File.Exists(this.getSystemPath() + "\\reminderBackup.csv"))
                {
                    FileStream f = File.Create(this.getSystemPath() + "\\reminderBackup.csv");
                    f.Close();
                }
                FileInfo t = new FileInfo(this.getSystemPath() + "\\reminderBackup.csv");
                StreamWriter writer = t.AppendText();
                writer.WriteLine(broadcast.getChannelID().Replace("|", "@_@") + "|" + broadcast.getTVBrowserID() + "|"+ broadcast.getStart().ToString("yyyy-MM-dd HH:mm:ss") + "|" + broadcast.isReminder());
                writer.Close();
            }
            catch (Exception ex) 
            {
                throw new Exception(ex.Message.ToString());
            }
        }

        public void killThread()
        {
            try
            {
                this.trans.endThread();
            }
            catch
            {
            }
        }

        private void loadLanguage()
        {
            this.languages = new Hashtable();
            String input = null;
            try
            {
                StreamReader re = new StreamReader(this.systemPath + "\\languages\\" + this.currentLanguage + ".txt", System.Text.Encoding.Default);
                while ((input = re.ReadLine()) != null)
                {
                    String key = input.Split('=')[0];
                    key = key.Substring(0, key.Length - 1);
                    String value = input.Split('=')[1];
                    value = value.Substring(1, value.Length-1);
                    this.addLanguageElement(key, value);
                }
            }
            catch
            {
                StreamReader re = new StreamReader(this.systemPath + "\\languages\\English.txt", System.Text.Encoding.Default);
                while ((input = re.ReadLine()) != null)
                {
                    if (input != "")
                    {
                        String key = "";
                        String value = "";
                        try
                        {
                            key = input.Split('=')[0];
                            key = key.Substring(0, key.Length - 1);
                            value = input.Split(    '=')[1];
                            value = value.Substring(1, value.Length - 1);
                            this.addLanguageElement(key, value);
                        }
                        catch (Exception ex)
                        {
                            ex.ToString();
                        }
                    }
                }
            }
        }
    }
}
