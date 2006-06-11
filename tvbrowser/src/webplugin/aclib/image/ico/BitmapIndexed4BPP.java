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
 * Bitmap with 16 color palette (4 bits per sample).
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapIndexed4BPP extends AbstractBitmapIndexed {
    /**
     * Create a 4 BPP bitmap.
     * @param pDescriptor
     */
    public BitmapIndexed4BPP(final BitmapDescriptor pDescriptor) {
        super(pDescriptor);
    }

    void readBitmap(final AbstractDecoder pDec) throws IOException {
        // One byte contains 2 samples.
        final int lWt = getBytesPerScanLine(getWidth(), 4);
        for (int lRowNo = 0; lRowNo < getHeight(); lRowNo++) {
            final byte[] lRow = pDec.readBytes(lWt, null);
            int lRowByte = 0;
            boolean lUpperNibbleP = true;
            int lOutputPos = (getHeight() - lRowNo - 1) * getWidth();
            for (int lColNo = 0; lColNo < getWidth(); lColNo++) {
                int lValue;
                if (lUpperNibbleP) {
                    lValue = (lRow[lRowByte] & 0xF0) >> 4;
                } else {
                    lValue = lRow[lRowByte] & 0x0F;
                    lRowByte++;
                }
                _pixels[lOutputPos++] = lValue & 0xFF;
                lUpperNibbleP = !lUpperNibbleP;
            }
        }
    }
}