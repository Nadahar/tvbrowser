/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2011-04-23 13:27:03 +0200 (Sa, 23 Apr 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6994 $
 */
package tvbrowserdataservice.file;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.lang.StringUtils;

import util.io.FileFormatException;
import util.io.IOUtilities;
import util.misc.HashCodeUtilities;
import util.ui.UiUtilities;
import devplugin.ProgramFieldType;

/**
 * @author Til Schneider, www.murfman.de
 */
public class ProgramField implements Cloneable {

    private static final Logger mLog
            = Logger.getLogger(ProgramField.class.getName());

    private static final String TEXT_CHARSET = "UTF-8";

    private int mTypeId;

    private ProgramFieldType mType;

    private byte[] mData;

    private int mDataFormat;

    /**
     * Maximum Size of Images. Images get resized to this
     * size if they exceed the limit
     */
    private static final int MAX_IMAGE_SIZE_X = 150;
    /**
     * Maximum Size of Images. Images get resized to this
     * size if they exceed the limit
     */
    private static final int MAX_IMAGE_SIZE_Y = 150;


    public ProgramField() {
        mDataFormat = ProgramFieldType.UNKNOWN_FORMAT;
        mType = null;
    }

    /**
     * Used for creating an instance of ProgramField to
     * read/store the additional ProgramFrame id in.
     *
     * @param o Dummy parameter.
     * @since 2.2.2
     */
    protected ProgramField(Object o) {
      mDataFormat = ProgramFieldType.UNKNOWN_FORMAT;
      mTypeId = 255;
    }

    public static ProgramField create(ProgramFieldType type, String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        ProgramField p = new ProgramField();
        p.setType(type);
        p.setTextData(text);
        return p;
    }

    public static ProgramField create(ProgramFieldType type, byte[] data) {
        if ((data == null) || (data.length == 0)) {
            return null;
        }

        ProgramField p = new ProgramField();
        p.setType(type);

        if (type == ProgramFieldType.PICTURE_TYPE) {
            // If the FieldType is a Picture it has to be resized and recompressed

            byte[] newdata = recreateImage(data);
            if (newdata == null) {
              return null;
            }

            p.setBinaryData(newdata);

        } else {
            p.setBinaryData(data);
        }

        return p;
    }

    /**
     * This Function loads an image using imageio,
     * resizes it to the Max-Size of Images in TVBrowser and
     * stores it as an compressed jpg.
     * <p/>
     * If the Image is smaller than the Max-Size, it isn't altered
     *
     * @param data Image-Data
     * @return resized Image-Data
     */
    private static byte[] recreateImage(byte[] data) {
        byte[] newdata = null;
        BufferedImage image = null;
        try {
            // Read Image
            image = ImageIO.read(new ByteArrayInputStream(data));

            int curx = image.getWidth(null);
            int cury = image.getHeight(null);

            // If the Size is < than max, use the original Data to reduce compression
            // artefacts
            if ((curx <= MAX_IMAGE_SIZE_X) && (cury <= MAX_IMAGE_SIZE_Y)) {
                return data;
            }

            int newx = MAX_IMAGE_SIZE_X;
            int newy = (int)((MAX_IMAGE_SIZE_X/(float)curx) * cury);

            if (newy > MAX_IMAGE_SIZE_Y) {
                newy = MAX_IMAGE_SIZE_Y;
                newx = (int)((MAX_IMAGE_SIZE_Y/(float)cury) * curx);
            }

            BufferedImage tempPic = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = tempPic.createGraphics();
            g2d.drawImage(image, null, null);
            g2d.dispose();

            BufferedImage newImage = UiUtilities.scaleIconToBufferedImage(tempPic, newx, newy);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Find a jpeg writer
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
            if (iter.hasNext()) {
                writer = iter.next();
            }

            if (writer != null) {
                // Prepare output file
                ImageOutputStream ios = ImageIO.createImageOutputStream(out);
                writer.setOutput(ios);

                JPEGImageWriteParam param = new JPEGImageWriteParam(null);

                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.85f);

                // Write the image
                writer.write(null, new IIOImage(newImage, null, null), param);

                // Cleanup
                ios.flush();
                writer.dispose();
                ios.close();

                newdata = out.toByteArray();
            } else {
                mLog.severe("No JPEG-Exporter found. Image is not stored in Data");
            }

        } catch (IOException e) {
            e.printStackTrace();
            newdata = null;
        }

        return newdata;
    }

    public static ProgramField create(ProgramFieldType type, int value) {
        ProgramField p = new ProgramField();
        p.setType(type);
        if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
            p.setTimeData(value);
        } else {
            p.setIntData(value);
        }
        return p;
    }


    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException exc) {
            // This will never happen, since this class implements Cloneable
            return null;
        }
    }


    public int getTypeId() {
        return mTypeId;
    }


    public ProgramFieldType getType() {
        if (mType == null) {
            mType = ProgramFieldType.getTypeForId(mTypeId);
        }

        return mType;
    }


    /**
     * @param type
     */
    public void setType(ProgramFieldType type) {
        mType = type;
        mTypeId = type.getTypeId();
    }


    /**
     * Is used for a field in an update file that should be deleted.
     */
    public void removeData() {
        mDataFormat = ProgramFieldType.UNKNOWN_FORMAT;
        mData = null;
    }


    public byte[] getBinaryData() {
        return mData;
    }


    /**
     * @param data
     */
    public void setBinaryData(byte[] data) {
        mDataFormat = ProgramFieldType.BINARY_FORMAT;

        mData = data;
    }


    public String getTextData() {
        if (mData == null) {
            return null;
        }

        try {
            return new String(mData, TEXT_CHARSET);
        }
        catch (UnsupportedEncodingException exc) {
            // This will never happen, because UTF-8 is always supported
            mLog.log(Level.SEVERE, "Charset " + TEXT_CHARSET + " is not supported", exc);

            return null;
        }
    }


    public void setTextData(String text) {
        mDataFormat = ProgramFieldType.TEXT_FORMAT;

        try {
            mData = StringUtils.trim(text).getBytes(TEXT_CHARSET);
        }
        catch (UnsupportedEncodingException exc) {
            // This will never happen, because UTF-8 is always supported
            mLog.log(Level.SEVERE, "Charset " + TEXT_CHARSET + " is not supported", exc);
        }
    }


    public int getIntData() {
        return dataToInt(mData);
    }


    public void setIntData(int value) {
        mDataFormat = ProgramFieldType.INT_FORMAT;

        mData = intToData(value);
    }


    public int getTimeData() {
        return dataToInt(mData);
    }


    public void setTimeData(int minutesAfter1970) {
        mDataFormat = ProgramFieldType.TIME_FORMAT;

        mData = intToData(minutesAfter1970);
    }


    /**
     * Gets a String representation of the data value.
     *
     * @return the data value as String.
     */
    public String getDataAsString() {
        if (mDataFormat == ProgramFieldType.TEXT_FORMAT) {
            return new StringBuilder("'").append(getTextData()).append("'")
          .toString();
        } else if (mDataFormat == ProgramFieldType.INT_FORMAT) {
            return Integer.toString(getIntData());
        } else if (mDataFormat == ProgramFieldType.TIME_FORMAT) {
            int time = getTimeData();
            int hours = time / 60;
            int minutes = time % 60;
            return new StringBuilder().append(hours).append(':').append(
          (minutes < 10) ? "0" : "").append(minutes).toString();
        } else if (mDataFormat == ProgramFieldType.BINARY_FORMAT) {
            return "(binary)";
        } else {
            return "(unknown)";
        }
    }


    private static int dataToInt(byte[] data) {
        if (data == null) {
            return 0;
        }

        return ((data[0] & 0xFF) << (3 * 8))
                | ((data[1] & 0xFF) << (2 * 8))
                | ((data[2] & 0xFF) << (1 * 8))
                | ((data[3] & 0xFF) << (0 * 8));
    }


    private static byte[] intToData(int value) {
        byte[] data = new byte[4];

        data[0] = (byte) (value >> (3 * 8));
        data[1] = (byte) (value >> (2 * 8));
        data[2] = (byte) (value >> (1 * 8));
        data[3] = (byte) (value >> (0 * 8));

        return data;
    }


    private void checkFormat() throws FileFormatException {
        // Check whether the field data has the right format
        if (!getType().isRightFormat(mDataFormat)) {
            throw new FileFormatException("The field '" + getType().getName()
                    + "' must have the " + ProgramFieldType.getFormatName(getType().getFormat())
                    + " but it has the " + ProgramFieldType.getFormatName(mDataFormat));
        }
    }


    public void readFromStream(InputStream stream)
            throws IOException, FileFormatException {
        mTypeId = stream.read();
        mType = null;

        mDataFormat = ProgramFieldType.UNKNOWN_FORMAT;

        int dataLength = ((stream.read() & 0xFF) << 16)
                | ((stream.read() & 0xFF) << 8)
                | (stream.read() & 0xFF);

        if (dataLength == 0) {
            mData = null;
        } else {
            mData = IOUtilities.readBinaryData(stream, dataLength);
        }
    }


    public void writeToStream(OutputStream stream)
      throws IOException, FileFormatException {
      writeToStream(stream, true);
    }

    /**
     * Writes the data to a stream.
     *
     * @param stream The stream to write on
     * @param check If the format should be checked
     * @throws IOException Thrown if something goes wrong.
     * @throws FileFormatException Thrown if something goes wrong.
     * @since 2.2.2
     */
    protected void writeToStream(OutputStream stream, boolean check)
            throws IOException, FileFormatException {
        // Check whether the field has the right format
      if(check) {
        checkFormat();
      }

        stream.write(mTypeId);

        if (mData == null) {
            stream.write(0); // Length highest byte
            stream.write(0); // Length middle byte
            stream.write(0); // Length lowest byte
        } else {
            // Write the data length
            stream.write((byte) (mData.length >> 16));
            stream.write((byte) (mData.length >> 8));
            stream.write((byte) (mData.length));

            // Write the data
            stream.write(mData);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof ProgramField) {
            ProgramField field = (ProgramField) obj;

            if (getTypeId() != field.getTypeId()) {
                return false;
            }

            return Arrays.equals(mData, field.mData);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
      int result = HashCodeUtilities.hash(getTypeId());
      result = HashCodeUtilities.hash(result, mData);
      return result;
    }
}
