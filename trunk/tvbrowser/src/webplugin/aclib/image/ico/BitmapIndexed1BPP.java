/*
 * This Classes are slightly modified Versions of the
 * AC.lib-ICO.
 * 
 * The original can be found here:
 * 
 * http://www.acproductions.de/commercial/aclibico/index.html
 * 
 * The code is based on:
 * 
 * winicontoppm.c - read a MS Windows .ico file and write portable pixmap(s)
 * 
 * Copyright (C) 2000 by Lee Benfield - lee@recoil.org
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. This software is provided "as is" without express or implied warranty.
 * 
 * Addendum for Java code by Christian Treber:
 * The rules for the Java adaption are the same as stated above for the C version.
 * 
 */
package webplugin.aclib.image.ico;

import java.io.IOException;

import webplugin.aclib.codec.AbstractDecoder;

/**
 * <p>
 * Bitmap with 2 color palette (black and white icon). Not tested, but seems to
 * work.
 * </p>
 * <p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapIndexed1BPP extends AbstractBitmapIndexed {
    /**
     * Create a 1BPP bitmap.
     * @param pDescriptor
     */
    public BitmapIndexed1BPP(final BitmapDescriptor pDescriptor) {
        super(pDescriptor);
    }

    void readBitmap(final AbstractDecoder pDec) throws IOException {
        // One byte contains 8 samples.
        final int lBytesPerScanLine = getBytesPerScanLine(getWidth(), 1);
        for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++) {
            final byte[] lBitmapBytes = pDec.readBytes(lBytesPerScanLine, null);
            int lBitmapByteNo = 0;
            int lTestBitMask = 0x80;
            int lPixelNo = (getHeight() - 1 - lRowNo) * getWidth();
            for (int lColNo = 0; lColNo < getWidth(); lColNo++) {
                _pixels[lPixelNo++] = ((lBitmapBytes[lBitmapByteNo] & lTestBitMask) / lTestBitMask) & 0xFF;

                if (lTestBitMask == 0x01) {
                    // When the last bit (bit 0, mask 0x01) has been processed,
                    // advance to the next bitmap byte, and set the test bit to
                    // bit 7, mask 0x80.
                    lTestBitMask = 0x80;
                    lBitmapByteNo++;
                } else {
                    lTestBitMask >>= 1;
                }
            }
        }
    }
}
