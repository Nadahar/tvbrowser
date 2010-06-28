package util.misc;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a Cache using soft references.
 * As soon as the garbage collector tries to remove
 * an object, it gets removed from the cache.
 *
 * @since 2.7
 */
public class SoftReferenceCache<T,K> {
  /**
   * A map with references to the objects
   */
  private Map<T, SoftReference<K>> mCacheHashMap = new HashMap<T, SoftReference<K>>();
  /**
   * This map is used for the garbage collection of all items
   */
  private Map<SoftReference<K>, T> mRefHashMap = new HashMap<SoftReference<K>, T>();

  /**
   * This queue gets notified if an element gets removed by the garbage collector
   */
  private ReferenceQueue mRefQueue = new ReferenceQueue();

  /**
   * Get the value from the Cache
   * @param key get value for this key
   */
  public K get(T key) {
    cleanUp();
    SoftReference<K> ref = mCacheHashMap.get(key);

    K value = null;

    if (ref != null) {
      value = ref.get();
    }

    return value;
  }

  /**
   * Put a object into the cache
   *
   * @param key key for this object
   * @param object object to store in the cache
   */
  public void put(T key, K object) {
    cleanUp();
    SoftReference<K> ref = new SoftReference<K>(object, mRefQueue);
    mCacheHashMap.put(key, ref);
    mRefHashMap.put(ref, key);
  }

  /**
   * Remove item from cache
   * @param key remove item that references this key
   */
  public void remove(T key) {
    cleanUp();
    SoftReference<K> ref = mCacheHashMap.remove(key);
    if (ref != null) {
      mRefHashMap.remove(ref);
    }
  }

  /**
   * Clear the cache
   */
  public void clear() {
    mCacheHashMap.clear();
    mRefHashMap.clear();
  }

  /**
   * Cleanup the cache. Every garbage collected item must be removed
   */
  public void cleanUp(){
    Reference ref = mRefQueue.poll();
    while (ref != null) {
      ref = mRefQueue.poll();
      mRefHashMap.remove(ref);
      if (ref != null) {
        T key = mRefHashMap.get(ref);
        mCacheHashMap.remove(key);
      }
    }
  }

  /**
   * Checks if the cache contains a specific key
   *
   * @param key search for this key
   * @return <code>true</code> if this cache contains this key
   */
  public boolean containsKey(T key) {
    return mCacheHashMap.containsKey(key);
  }
}
