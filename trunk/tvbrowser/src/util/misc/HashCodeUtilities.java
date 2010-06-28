package util.misc;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 *
 * Example use case:
 *
 * <pre>
 * public int hashCode() {
 *   int result = HashCodeUtil.hash(someField);
 *   result = HashCodeUtil.hash(result, fPrimitive);
 *   result = HashCodeUtil.hash(result, fObject);
 *   result = HashCodeUtil.hash(result, fArray);
 *   return result;
 * }
 * </pre>
 */
public final class HashCodeUtilities {

  /**
   * An initial value for a <code>hashCode</code>, to which contributions from
   * fields are added. Using a non-zero value decreases collisions of
   * <code>hashCode</code> values.
   */
  private static final int SEED = 23;

  /**
   * booleans.
   */
  public static int hash(int aSeed, boolean aBoolean) {
    return firstTerm(aSeed) + (aBoolean ? 1 : 0);
  }

  public static int hash(final boolean value) {
    return hash(SEED, value);
  }

  /**
   * chars.
   */
  public static int hash(int aSeed, char aChar) {
    return firstTerm(aSeed) + aChar;
  }

  public static int hash(final char value) {
    return hash(SEED, value);
  }

  /**
   * ints.
   */
  public static int hash(int aSeed, int aInt) {
    /*
     * Implementation Note Note that byte and short are handled by this method,
     * through implicit conversion.
     */
    return firstTerm(aSeed) + aInt;
  }

  public static int hash(int value) {
    return hash(SEED, value);
  }

  /**
   * longs.
   */
  public static int hash(int aSeed, long aLong) {
    return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
  }

  public static int hash(final long value) {
    return hash(SEED, value);
  }

  /**
   * floats.
   */
  public static int hash(int aSeed, float aFloat) {
    return hash(aSeed, Float.floatToIntBits(aFloat));
  }

  public static int hash(final float value) {
    return hash(SEED, value);
  }

  /**
   * doubles.
   */
  public static int hash(int aSeed, double aDouble) {
    return hash(aSeed, Double.doubleToLongBits(aDouble));
  }

  public static int hash(final double value) {
    return hash(SEED, value);
  }

  /**
   * <code>aObject</code> is a possibly-null object field, and possibly an
   * array.
   *
   * If <code>aObject</code> is an array, then each element may be a primitive
   * or a possibly-null object.
   */
  public static int hash(final int seed, final Object object) {
    int result = seed;
    if (object == null) {
      result = hash(result, 0);
    } else if (!isArray(object)) {
      result = hash(result, object.hashCode());
    } else {
      int length = Array.getLength(object);
      for (int idx = 0; idx < length; ++idx) {
        Object item = Array.get(object, idx);
        // recursive call!
        result = hash(result, item);
      }
    }
    return result;
  }

  public static int hash(final Object value) {
    return hash(SEED, value);
  }

  private static final int ODD_PRIME_NUMBER = 37;

  private static int firstTerm(final int seed) {
    return ODD_PRIME_NUMBER * seed;
  }

  private static boolean isArray(final Object aObject) {
    return aObject.getClass().isArray();
  }
}
