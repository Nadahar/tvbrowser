import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;
import primarydatamanager.primarydataservice.AbstractPrimaryDataService;
import primarydatamanager.primarydataservice.PrimaryDataService;
import primarydatamanager.primarydataservice.ProgramFrameDispatcher;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.FileFormatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is a small PDS-Example
 */
public class SimplePDS extends AbstractPrimaryDataService {

    private static int DAYS_TO_DOWNLOAD = 7;
    private Channel[] mChannelList;

    /**
     * Create the Channellist
     */
    public SimplePDS() {
        java.util.TimeZone zone = java.util.TimeZone.getTimeZone("GMT+1");
        String c = "(c) by TV-Browser";

        mChannelList = new Channel[] {
            new Channel(null, "Kanal 12", "kanal12", zone, "de", c),
            new Channel(null, "Quiz 44", "quiz44", zone, "de", c),
            new Channel(null, "Einkauf", "einkauf", zone, "de", c),
        };
    }

    /**
     * Execute this PDS
     * @param dir Directory that will used for the result
     */
    protected void execute(String dir) {
        ProgramFrameDispatcher[] dispatcher = new ProgramFrameDispatcher[mChannelList.length];
        for (int i = 0; i < dispatcher.length; i++) {
            dispatcher[i] = new ProgramFrameDispatcher(mChannelList[i]);
        }

        Date date = Date.getCurrentDate();
        for (int d = 0; d < DAYS_TO_DOWNLOAD; d++) {
            for (int ch = 0; ch < mChannelList.length; ch++) {
                try {
                    extract(date, mChannelList[ch], dispatcher[ch]);
                } catch (Exception e) {
                    logException(e);
                }
            }
            date = date.addDays(1);
        }

        for (ProgramFrameDispatcher aDispatcher : dispatcher) {
            try {
                aDispatcher.store(dir);
            }
            catch (IOException e) {
                logException(e);
            }
            catch (FileFormatException e) {
                logException(e);
            }
        }
    }

    /**
     * Extracts all Information for a given Date on a given Channel and
     * stores the Result in the dispatcher
     *
     * @param date Date to grab
     * @param channel Channel to grab
     * @param dispatcher Store Result here
     *
     * @throws IOException If download goes Wrong
     */
    private void extract(Date date, Channel channel, ProgramFrameDispatcher dispatcher) throws IOException {

        StringBuffer buf = new StringBuffer("http://www.tvbrowser.org/pdsstarterkit/");
        buf.append("pgv.php?day=")
                .append(date.getDayOfMonth())
                .append("&month=")
                .append(date.getMonth())
                .append("&year=")
                .append(date.getYear())
                .append("&channel=")
                .append(channel.getId());
        System.out.println(buf);
        URL url = new URL(buf.toString());

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        InputStream in = con.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Matcher matcher;
        Pattern pattern = Pattern.compile("<tr><td>(\\d+):(\\d+)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td></tr>");

        String line = reader.readLine();
        while (line != null) {

            matcher = pattern.matcher(line);
            if (matcher.find()) {
                // System.out.println("h: "+matcher.group(1)+"; m: "+matcher.group(2)+"; title: "+matcher.group(3)+"; short: "+matcher.group(4)+"; desc: "+matcher.group(5));
                int h = Integer.parseInt(matcher.group(1));
                int m = Integer.parseInt(matcher.group(2));
                String title = matcher.group(3);
                String shortInfo = matcher.group(4);
                String description = matcher.group(5);

                ProgramFrame frame = new ProgramFrame();
                frame.addProgramField(ProgramField.create(ProgramFieldType.START_TIME_TYPE, h * 60 + m));
                frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, title));
                frame.addProgramField(ProgramField.create(ProgramFieldType.SHORT_DESCRIPTION_TYPE, shortInfo));
                frame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, description));
                dispatcher.dispatchProgramFrame(frame, date);

            }
            line = reader.readLine();
        }
    }

    /**
     * @return List of all Available Channels
     */
    public Channel[] getAvailableChannels() {
        return mChannelList;
    }

    /**
     * If you want to have some statistics about your PDS you can
     * return the Bytes you processed
     *
     * @return Number of Bytes
     */
    public int getReadBytesCount() {
        return 0;
    }

    /**
     * For Testing only, don't use this!
     * 
     * @param args Arguments
     */
    public static void main(String[] args) {
        PrimaryDataService pds = new SimplePDS();
        pds.execute("temp", System.out);
    }

}
