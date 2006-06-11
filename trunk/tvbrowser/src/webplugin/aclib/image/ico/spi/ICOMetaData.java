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
package webplugin.aclib.image.ico.spi;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

import webplugin.aclib.image.ico.BitmapDescriptor;

/**
 * <p>
 * ICO image meta data. I have no idea whether what I'm doing here is what is
 * expected from me!
 * </p>
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public class ICOMetaData extends IIOMetadata {
    private final BitmapDescriptor _entry;

    /**
     * @param pEntry
     */
    public ICOMetaData(final BitmapDescriptor pEntry) {
        super();
        _entry = pEntry;
    }

    public Node getAsTree(final String pFormatName) {
        final IIOMetadataNode lRoot = new IIOMetadataNode(
                "javax_imageio_ico_image_1.0");

        IIOMetadataNode lNode = new IIOMetadataNode("width");
        lNode.setNodeValue("" + _entry.getWidth());
        lRoot.appendChild(lNode);
        lNode = new IIOMetadataNode("height");
        lNode.setNodeValue("" + _entry.getHeight());
        lRoot.appendChild(lNode);

        return lRoot;
    }

    public boolean isReadOnly() {
        return true;
    }

    public void mergeTree(final String pFormatName, final Node pRoot) {
        // Not needed.
    }

    public void reset() {
        // Not needed.
    }
}