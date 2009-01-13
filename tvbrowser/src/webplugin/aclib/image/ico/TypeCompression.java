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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration class for bitmap compression types.
 * @author &copy; Christian Treber, ct@ctreber.com
 */
public final class TypeCompression {
    /**
     * Maps type values to TypeCompression objects.
     */
    private static final Map<Long, TypeCompression> TYPES;

    /**
     * Uncompressed (any BPP).
     */
    public static final TypeCompression BI_RGB = new TypeCompression("BI_RGB",
            0, "Uncompressed (any BPP)");

    /**
     * 8 Bit RLE Compression (8 BPP only).
     */
    public static final TypeCompression BI_RLE8 = new TypeCompression(
            "BI_RLE8", 1, "8 Bit RLE Compression (8 BPP only)");

    /**
     * 4 Bit RLE Compression (4 BPP only).
     */
    public static final TypeCompression BI_RLE4 = new TypeCompression(
            "BI_RLE4", 2, "4 Bit RLE Compression (4 BPP only)");

    /**
     * Uncompressed (16 & 32 BPP only).
     */
    public static final TypeCompression BI_BITFIELDS = new TypeCompression(
            "BI_BITFIELDS", 3, "Uncompressed (16 & 32 BPP only)");

    static {
        TYPES = new HashMap<Long, TypeCompression>();
        register(BI_RGB);
        register(BI_RLE8);
        register(BI_RLE4);
        register(BI_BITFIELDS);
    }

    private final int _value;

    private final String _name;

    private final String _comment;

    /**
     * @param pName
     * @param pValue
     */
    // @PMD:REVIEWED:CallSuperInConstructor: by Chris on 06.03.06 10:29
    private TypeCompression(final String pName, final int pValue,
            final String pComment) {
        _name = pName;
        _value = pValue;
        _comment = pComment;

    }

    /**
     * @param pType
     */
    private static void register(final TypeCompression pType) {
        TYPES.put(Long.valueOf(pType.getValue()), pType);
    }

    /**
     * Returns the name of the type and a comment.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return _name + " (" + _comment + ")";
    }

    /**
     * Get the symbolic name.
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the numerical value.
     * @return Returns the value.
     */
    public int getValue() {
        return _value;
    }

    /**
     * Get a type for the specified numerical value.
     * @param pValue
     *            Compression type integer value.
     * @return Type for the value specified.
     */
    public static TypeCompression getType(final long pValue) {
        final TypeCompression lResult = TYPES.get(pValue);
        if (lResult == null) {
            throw new IllegalArgumentException("Compression type " + pValue
                    + " unknown");
        }

        return lResult;
    }
}
