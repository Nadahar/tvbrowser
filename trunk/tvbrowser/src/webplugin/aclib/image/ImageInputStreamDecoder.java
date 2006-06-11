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
package webplugin.aclib.image;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import webplugin.aclib.codec.AbstractDecoder;

/**
 * <p>
 * File decoder based on ImageInputStream (Chris' implementation).
 * </p>
 * &copy; 2001 Christian Treber, ct@ctreber.com
 * @author Christian Treber, ct@ctreber.com
 */
public class ImageInputStreamDecoder extends AbstractDecoder {
    private final ImageInputStream _stream;

    /**
     * Create a BIG_ENDIAN file decoder. See
     * {@link AbstractDecoder#setEndianess}to change the default behavior.
     * @param pStream
     *            The image input stream to read from.
     */
    public ImageInputStreamDecoder(final ImageInputStream pStream) {
        super();
        _stream = pStream;
    }

    public void seek(final long pPos) throws IOException {
        _stream.seek(pPos);
    }

    public byte[] readBytes(final long pBytes, final byte[] pBuffer)
            throws IOException {
        byte[] lBuffer = pBuffer;
        if (lBuffer == null) {
            lBuffer = new byte[(int) pBytes];
        } else {
            if (lBuffer.length < pBytes) {
                throw new IllegalArgumentException(
                        "Insufficient space in buffer");
            }
        }

        final int lBytesRead = _stream.read(pBuffer, 0, (int) pBytes);
        if (lBytesRead != pBytes) {
            throw new IOException("Tried to read " + pBytes
                    + " bytes, but obtained " + lBytesRead);
        }

        _pos += pBytes;

        return lBuffer;
    }

    public void close() throws IOException {
        _stream.close();
    }
}
