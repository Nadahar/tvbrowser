using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Threading;

namespace TVBrowserMini
{
    class TCPTransfer
    {
        private int port;
        private TVBrowserControll con;
        private Thread thread;
        private Transfer transfer;
        private TcpClient client;
        protected static bool _StopRequest = false;

        public TCPTransfer(TVBrowserControll con, int port)
        {
            this.port = port;
            this.con = con;

            this.thread = new Thread(new ThreadStart(start));
            this.thread.IsBackground = true;
            this.thread.Start();
        }


        public void endThread()
        {
            _StopRequest = true;
        }

        private void start()
        {
            FileStream fs = null;
            TcpListener server = null;
            try
            {
                while (!_StopRequest)
                {
                    Thread.Sleep(1000);
                    this.transfer = new Transfer(this.con);
                    server = new TcpListener(this.port);
                    server.Start();

                    Byte[] bytes = new Byte[1];//8
                    int length = 0;

                    client = server.AcceptTcpClient();
                    this.transfer.Show();
                    this.transfer.BringToFront();
                    this.transfer.Refresh();


                    NetworkStream stream = client.GetStream();

                    int receiveTVData = 0;
                    int sendReminders = 0;
                    int sendFavorites = 0;

                    receiveTVData = stream.ReadByte();
                    sendReminders = stream.ReadByte();
                    sendFavorites = stream.ReadByte();
                    stream.WriteByte(1);

                    if (receiveTVData == 1) //receive File
                    {
                        this.transfer.setTextLabelPercent("TVDaten empfangen");
                        Thread.Sleep(500);
                        try
                        {
                            this.con.closeDBConnect();
                            Thread.Sleep(250);
                        }
                        catch
                        {
                            this.transfer.setTextLabelPercent("Datenbank konnte nicht abgebaut werden");
                            this.transfer.Refresh();
                            Thread.Sleep(500);
                        }
                        try
                        {
                            File.Delete(this.con.getDBPath());
                            //Thread.Sleep(10000);
                        }
                        catch
                        {
                            this.transfer.setTextLabelPercent("Datei konnte nicht gelöscht werden");
                            this.transfer.Refresh();
                            Thread.Sleep(500);
                        }
                        try
                        {
                            fs = new FileStream(this.con.getDBPath(), FileMode.Create);
                        }
                        catch
                        {
                            this.transfer.setTextLabelPercent("Filestream konnte nicht geöffnet werden");
                            this.transfer.Refresh();
                            Thread.Sleep(500);
                        }
                        int stellen = stream.ReadByte();
                        String len = "";
                        for (int j = 0; j < stellen; j++)
                        {
                            int value = stream.ReadByte();
                            len += value;
                        }
                        length = Int32.Parse(len);
                        this.transfer.setTextLabelPercent("Empfange: " + length + " Bytes");
                        Thread.Sleep(250);

                        this.transfer.setTextLabelPercent("ReceiveBufferSize: " + client.ReceiveBufferSize);
                        Thread.Sleep(250);

                        int bytecounter = 0;
                        while (bytecounter < length)
                        {
                            bytes = new Byte[client.ReceiveBufferSize];
                            int value = stream.Read(bytes, 0, bytes.Length);
                            stream.Flush();
                            fs.Write(bytes, 0, value);
                            bytecounter += value;
                            this.transfer.setTextLabelPercent(bytecounter + " / " + length + " Bytes");
                        }
                        fs.Close();
                        stream.Flush();
                        this.transfer.setTextLabelPercent("- TVDaten Empfang abgeschlossen");
                        Thread.Sleep(250);
                        this.transfer.setTextLabelPercent("- Datenbankverbindung aufbauen");
                        if (!this.con.checkDBExists())
                        {
                            this.transfer.setTextLabelPercent("- Datenbankverbindung konnte nicht aufgebaut werden");
                            Thread.Sleep(250);
                        }
                        else
                        {
                            this.transfer.setTextLabelPercent("- Erinnerungen werden übernommen");
                            Thread.Sleep(250);
                            try
                            {
                                this.con.transferReminders();
                            }
                            catch
                            {
                                this.transfer.setTextLabelPercent("- Fehler beim importieren");
                                this.transfer.Refresh();
                                Thread.Sleep(250);
                            }
                        }
                        stream.WriteByte(1);
                    }
                    if (sendReminders == 1)//send reminders
                    {
                        this.transfer.setTextLabelPercent("Erinnerungen senden");
                        this.transfer.Refresh();
                        Thread.Sleep(1000);
                        if (stream.ReadByte() == 1 && File.Exists(this.con.getSystemPath() + "\\reminderBackup.csv"))
                        {
                            this.transfer.setTextLabelPercent("- Erinnerungen senden");
                            Thread.Sleep(250);
                            StreamReader re = File.OpenText(this.con.getSystemPath() + "\\reminderBackup.csv");
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
                                    DateTime dt = new DateTime(Int32.Parse(start.Substring(0, 4)), Int32.Parse(start.Substring(5, 2)), Int32.Parse(start.Substring(8, 2)));
                                    if (dt > DateTime.Now.AddDays(-1) && reminderString.Equals("True"))
                                    {
                                        stream.WriteByte(1);
                                        stream.Write(System.Text.Encoding.GetEncoding(1252).GetBytes(channelid.ToCharArray()), 0, System.Text.Encoding.GetEncoding(1252).GetBytes(channelid.ToCharArray()).Length);
                                        stream.Flush();
                                        stream.ReadByte();
                                        stream.Write(System.Text.Encoding.GetEncoding(1252).GetBytes(tvbrowserid.ToCharArray()), 0, System.Text.Encoding.GetEncoding(1252).GetBytes(tvbrowserid.ToCharArray()).Length);
                                        stream.Flush();
                                        stream.ReadByte();
                                        stream.Write(System.Text.Encoding.GetEncoding(1252).GetBytes(start.ToCharArray()), 0, System.Text.Encoding.GetEncoding(1252).GetBytes(start.ToCharArray()).Length);
                                        stream.Flush();
                                        stream.ReadByte();
                                    }
                                }
                                catch
                                {

                                }
                            }
                            re.Close();
                            stream.WriteByte(0);
                            stream.Flush();
                        }
                    }
                    if (sendFavorites == 1) // Send Favorites
                    {
                        //TODO
                    }
                    stream.Close();
                    this.transfer.Hide();
                    if (client != null)
                        client.Close();
                    if (server != null)
                        server.Stop();
                    this.start();
                }
            }

            catch (StackOverflowException ex)
            {
                this.transfer.setTextLabelPercent("- StackOverflowException!!!");
                this.transfer.Refresh();
                if (client != null)
                    client.Close();
                if (fs != null)
                    fs.Close();
                if (server != null)
                    server.Stop();
                Thread.Sleep(5000);
                this.transfer.Hide();
                this.start();
            }
            catch
            {

                this.transfer.setTextLabelPercent("- Datenübertragung wurde unterbrochen");
                this.transfer.Refresh();
                if (client != null)
                    client.Close();
                if (fs != null)
                    fs.Close();
                if (server != null)
                    server.Stop();
                this.transfer.Hide();
                Thread.Sleep(100);
                this.start();
            }
        }
    }
}
