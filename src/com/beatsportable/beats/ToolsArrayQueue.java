package com.beatsportable.beats;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

/** An array-based queue. **/
public class ToolsArrayQueue<T> extends AbstractQueue<T> {

	/*TODO: This queue doesn't set dequeued spaces to null (in poll and clear).
	  This is a memory leak. I tried fixing it, it looked like doing that
	  killed the framerate (probably because clear() would be O(n) instead of O(1))
	  but that may have been an illusion. */
	
	protected T[] elements;
	protected int start = 0; //Index of first element 
	protected int end = 0; //Index of element after last element
	/* Elements are stored in the range:   [start, end) mod elements.length
	   If start == end, the queue is empty. (Therefore the array can never be completely full.)
	   Elements are added to the end and removed from the start. */
	
	public ToolsArrayQueue() {
		this(32);
	}
	@SuppressWarnings("unchecked")
	public ToolsArrayQueue(int capacity) {
		elements = (T[]) new Object[capacity];
	}
	
	// ====== Queue functions ======
	
	public T peek() {
		if (end == start) return null;
		return elements[start];
	}
	
	public T poll() {
		if (end == start) return null;
		T out = elements[start];
		start = inc(start);
		return out;
	}
	
	public boolean offer(T o) {
		if (o == null) return false;
		
		int incend = inc(end);
		if (incend == start) {
			setCapacity(elements.length * 2);
			incend = inc(end);
		}
		
		elements[end] = o;
		end = incend;
		return true;
	}
	
	@Override
	public int size() {
		return (end >= start) ? (end - start) : (elements.length + end - start);
	}
	
	@Override
	public boolean isEmpty() { return end == start; }
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int pos = start;
			int _end = end;
			public boolean hasNext() {
				return pos != _end;
			}

			public T next() {
				if (pos != _end) {
					T o = elements[pos];
					pos = inc(pos);
					return o;
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException("ZE METHOD, ZIT DOES NOSSING EITHER!");				
			}
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] o = new Object[size()];
		arraycopy(o, 0);
		return o;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U> U[] toArray(U[] a) {
		int size = size();
		a = (size <= a.length) ? a : (U[]) new Object[size];
		arraycopy(a, 0);
		if (size < a.length)
			a[size] = null;
		return a;
	}
	
	@Override
	public void clear() {
		start = 0;
		end = 0;
	}
	
	// ======== Helpers ========
	
	private int inc(int idx) {
		idx++;
		return (idx >= elements.length) ? (idx - elements.length) : idx;
	}
	
	/*
	private int dec(int idx) {
		idx--;
		return (idx < 0) ? (idx + elements.length) : idx;
	}
	*/
	
	@SuppressWarnings("unchecked")
	private void setCapacity(int cap) {
		T[] newelements = (T[]) new Object[cap];
		arraycopy(newelements, 0);
		int s = size();
		elements = newelements;
		start = 0;
		end = s;
	}
	
	/* For cap elements, need an array of length at least cap+1 */
	private void ensureCapacity(int cap) {
		if (elements.length < cap+1) {
			setCapacity(cap*2);
		}
	}
	
	// ====== Copying =====
	
	//Ensure that the array indexing starts at 0.
	public void cleanup() {
		if (start != 0) setCapacity(elements.length);
	}
	
	/** Copies the contents of the given queue into consecutive elements
	 *   [  destpos, destpos + this.size()  )
	 * of the destination array.
	 * 
	 * Throws the same exceptions as System.arraycopy.
	 * 
	 * @param dest destination array
	 * @param destPos starting position in the destination array
	 * @return size = number of elements copied
	 */
	public int arraycopy(Object dest, int destPos) {
		return arraycopy(elements, elements.length, start, dest, destPos, size());
	}
	
	//TODO can this be addAll?
	public boolean addQueue(ToolsArrayQueue<T> that) {
		int thatsize = that.size();
		ensureCapacity(size() + thatsize);
		
		arraycopy(that.elements, that.elements.length, that.start,
				  elements, elements.length, end, thatsize);
		
		end += thatsize;
		if (end >= elements.length) end -= elements.length;
		return true;
	}
	
	/*
	private static final void fill(Object[] a, int fromIndex, int toIndex, Object val) {
		if (fromIndex <= toIndex) {
			Arrays.fill(a, fromIndex, toIndex, val);
		} else {
			//int size = toIndex - fromIndex + a.length;
			Arrays.fill(a, fromIndex, a.length, val);
			Arrays.fill(a, 0, toIndex, val);
		}
	}
	*/
	
	/** If src is a wraparound array of capacity srcCap
	 *     with elements at [srcStart, srcEnd) mod srcCap
	 *     and 0 <= srcStart < srcCap
	 *  and dest is a regular array
	 *  copy copyLen elements from src into elements [destPos, destPos + copyLen) of dest
	 *  
	 *  Warning: this does not verify that copyLen < src.size()
	 *  
	 * @param src source array
	 * @param srcCap capacity (i.e. backing array length) of src
	 * @param srcStart src backing array start index
	 * @param dest destination array
	 * @param destPos destination array start index
	 * @param copyLen number of elements to copy
	 * @return number of elements copied = copyLen
	 */
	private static final int arraycopy(Object src, int srcCap, int srcStart,
			                           Object dest, int destPos, int copyLen) {
		if (srcStart + copyLen <= srcCap) {
			System.arraycopy(src, srcStart, dest, destPos, copyLen);
		} else {
			int array1len = srcCap - srcStart;
			//do second half first, so this is hopefully an atomic operation 
			System.arraycopy(src, 0, dest, destPos + array1len, copyLen - array1len);
			System.arraycopy(src, srcStart, dest, destPos, array1len);
		}
		return copyLen;
	}
	
	/** If src is a wraparound array of capacity srcCap with elements at [srcStart, srcEnd) mod srcCap
	 *  and dest is a wraparound array of capacity destCap with elements at [destStart, destEnd) mod srcCap
	 *  copy copyLen elements from src into elements [destPos, destPos + copyLen) of dest
	 *  
	 *  Warning: this does not verify that copyLen < src.size() or copyLen < dest.size()
	 *  
	 *  Returns # elements copied = copyLen */
	private static final int arraycopy(Object src, int srcCap, int srcStart,
			                           Object dest, int destCap, int destStart,
			                           int copyLen) {
		if (destStart + copyLen <= destCap) {
			arraycopy(src, srcCap, srcStart, dest, destStart, copyLen);
		} else {
			int array1len = destCap - destStart;
			int start2 = srcStart + array1len;
			if (start2 > srcCap) start2 -= srcCap;
			//do second half first, so this is hopefully an atomic operation 
			arraycopy(src, srcCap, start2, dest, 0, copyLen - array1len);
			arraycopy(src, srcCap, srcStart, dest, destStart, array1len);
		}
		return copyLen;
	}
	
	// ======== Tester ==========
	private static final String assertequal(String msg, Object a, Object b) {
		if ((a == null && b == null) || (a != null && a.equals(b)))
			return "";
		else
			return String.format(msg, a, b);
	}
	public static final void test() {
		Random r = new Random();
		String msg = "";
		while (true) {
			ToolsArrayQueue<Integer> taq= new ToolsArrayQueue<Integer>();
			LinkedList<Integer> ll = new LinkedList<Integer>();
			for (int i=0; i<2000000; i++) {
				msg = "";
				try {
					msg += assertequal("Size difference: LL = %d, TAQ = %d; ", ll.size(), taq.size());
					msg += assertequal("Emptiness difference: LL = %d, TAQ = %d; ", ll.isEmpty(), taq.isEmpty());
					
					Object llv, taqv;
					switch(r.nextInt(4)) {
						case 0: case 3:
							int rnd = r.nextInt();
							llv = ll.offer(rnd);
							taqv = taq.offer(rnd);
							msg += assertequal("Offer: LL = %s, TAQ = %s; ", llv, taqv);
							break;
						case 1: 
							llv = ll.peek();
							taqv = taq.peek();
							msg += assertequal("Peek: LL = %s, TAQ = %s; ", llv, taqv);
							break;
						case 2:
							llv = ll.poll();
							taqv = taq.poll();
							msg += assertequal("Poll: LL = %s, TAQ = %s; ", llv, taqv);
							break;
					}
	
				} catch (Exception e) {
					msg += "EXCEPTION [[" + e.toString() + "]]; ";
					e.printStackTrace();
				}
				if (!msg.equals("") || (i%100000 == 0))
					System.out.println(String.format("%d: %s", i, msg));
			}
			int size = taq.size();
			System.out.println("size: " + size);
			Integer[] fromArr = (Integer[]) taq.toArray(new Integer[size]);
			Integer[] fromIt = new Integer[size];
			int idx = 0; for (Integer ii: taq) { fromIt[idx++] = ii; }
			System.out.print(assertequal("fromIt size: expected %d got %d\n", size, idx));
			for (int i = 0; i<size; i++) {
				msg = "";
				msg += assertequal("arr/it: arr = %s, it = %s; ", fromArr[i], fromIt[i]);
				msg += assertequal("arr/Q: arr = %s, Q = %s; ", fromArr[i], taq.poll());
				if (!msg.equals(""))
					System.out.println(String.format("%d: %s", i, msg));
			}
			System.out.print(assertequal("size: expected %d got %d\n", 0, taq.size()));
			System.out.println("equality check done");
		}
	}

	//public static void main(String[] args) { test(); }

}
