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

/**
 * <p>
 * Parent class for RGB (16, 24, and 32 bits per pixel) bitmaps.
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public abstract class AbstractBitmapRGB extends AbstractBitmap {
    protected int[] _samples;

    /**
     * Create a RGB bitmap.
     * @param pDescriptor
     */
    public AbstractBitmapRGB(final BitmapDescriptor pDescriptor) {
        super(pDescriptor);

        _samples = new int[getWidth() * getHeight()];
    }
}
