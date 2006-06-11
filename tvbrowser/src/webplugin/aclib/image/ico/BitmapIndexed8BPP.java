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
 * Bitmap with 256 color palette (8 bits per sample).
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapIndexed8BPP extends AbstractBitmapIndexed {
    /**
     * Create a 8 BPP bitmap.
     * @param pDescriptor
     */
    public BitmapIndexed8BPP(final BitmapDescriptor pDescriptor) {
        super(pDescriptor);
    }

    void readBitmap(final AbstractDecoder pDec) throws IOException {
        // One byte contains one sample.
        final int lWt = getBytesPerScanLine(getWidth(), 8);
        for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++) {
            final byte[] lRow = pDec.readBytes(lWt, null);
            int lRowByte = 0;
            int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
            for (int lColNo = 0; lColNo < getWidth(); lColNo++) {
                _pixels[lOutputPos++] = lRow[lRowByte++] & 0xFF;
            }
        }
    }
}
