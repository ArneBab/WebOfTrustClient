/* This code is part of WoT, a plugin for Freenet. It is distributed 
 * under the GNU General Public License, version 2 (or at your option
 * any later version). See http://www.gnu.org/ for details of the GPL. */
package plugins.WebOfTrust.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;

import plugins.WebOfTrust.exceptions.DuplicateObjectException;

/**
 * HashSet with the ability to return a random item.
 * All operations are armortised O(1).
 * 
 * @author xor (xor@freenetproject.org)
 * @param E The type of the elements.
 */
public final class RandomGrabHashSet<E> {

    /**
     * Random generator used as backend for {@link #getRandom()}.
     */
	private final Random mRandom; 

    /**
     * Stores all elements.
     * Allows {@link #getRandom()} to execute in O(1).
     */
	private final ArrayList<E> mArray = new ArrayList<E>();
	
    /**
     * Tells which slot each element resides in {{@link #mArray}}.
     * This allows {@link #contains(Object)} and {@link #remove(Object)} to execute in amortised O(1).
     */
	private final HashMap<E, Integer> mIndex = new HashMap<E, Integer>();
	
	
	public RandomGrabHashSet(final Random random) {
		mRandom = random;
	}
	
	
	/***
	 * Debug function.
	 * Add assert(indexIsValid()) to any functions which modify stuff.
	 */
	protected boolean indexIsValid() {
		if(mIndex.size() != mArray.size())
			return false;
		
		for(Entry<E, Integer> entry : mIndex.entrySet()) {
			if(!mArray.get(entry.getValue()).equals(entry.getKey()))
				return false;
		}
		
		return true;
	}
	
	public void add(final E item) {
		if(contains(item))
			throw new DuplicateObjectException(item.toString());
		
		mArray.add(item);
		mIndex.put(item, mArray.size()-1);
		
		assert(mIndex.size() == mArray.size());
		assert(mArray.get(mIndex.get(item)) == item);
	}
	
	public boolean contains(final E item) {
		return mIndex.containsKey(item);
	}
	
	public void remove(final E toRemove) {
		final Integer indexOfRemovedItem = mIndex.remove(toRemove) ;
		if(indexOfRemovedItem == null)
			throw new NoSuchElementException();
		
		assert(mArray.get(indexOfRemovedItem).equals(toRemove));
		
		// We cannot use ArrayList.remove() because it would shift all following elements.
		// Instead of that, we replace the now-empty slot with the last element
		final int indexOfLastItem = mArray.size()-1;
		final E lastItem = mArray.remove(indexOfLastItem);
		if(indexOfRemovedItem != indexOfLastItem) {
			mArray.set(indexOfRemovedItem, lastItem);
			mIndex.put(lastItem, indexOfRemovedItem);
		}
		
		assert(mIndex.size() == mArray.size());
		assert(mIndex.get(toRemove) == null);
		assert(lastItem.equals(toRemove) || mArray.get(mIndex.get(lastItem)).equals(lastItem));
	}
	
	public int size() {
		assert(mArray.size() == mIndex.size());
		return mArray.size();
	}
	
	public E getRandom() {
		return mArray.get(mRandom.nextInt(mArray.size()));
	}

}