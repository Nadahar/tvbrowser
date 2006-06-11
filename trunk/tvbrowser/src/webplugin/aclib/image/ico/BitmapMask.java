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
 * Transparency mask, which is a 1 Bit per pixel information whether a pixel is
 * transparent (1) or opaque (0).
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class BitmapMask {
    private final BitmapIndexed1BPP _mask;

    /**
     * @param pDescriptor
     */
    // @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:32
    public BitmapMask(final BitmapDescriptor pDescriptor) {
        _mask = new BitmapIndexed1BPP(pDescriptor);
    }

    /**
     * @param pDec The decoder.
     * @throws IOException
     */
    void read(final AbstractDecoder pDec) throws IOException {
        _mask.readBitmap(pDec);
    }

    /**
     * @param pXPos
     * @param pYPos
     * @return
     */
    public int getPaletteIndex(final int pXPos, final int pYPos) {
        return _mask.getPaletteIndex(pXPos, pYPos);
    }

    /**
     * @param pDescriptor
     */
    void setDescriptor(final BitmapDescriptor pDescriptor) {
        _mask.setDescriptor(pDescriptor);
    }

    /**
     * @param pXPos
     * @param pYPos
     * @return
     */
    public boolean isOpaque(final int pXPos, final int pYPos) {
        return _mask.getPaletteIndex(pXPos, pYPos) == 0;
    }
}
