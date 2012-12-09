package smartlistadapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;

/** A list which allows being observed.
 * This smart list, retrieved from
 * http://androidworkz.com/2010/08/14/beyond-smart-lists-how-observable-singletons-change-the-game/
 * and presumably public domain implements a list interface with the observable
 * pattern.
 *
 * The only change I've made is to make the constructor public, since we don't
 * actually want to use the default singleton pattern and prefer multiple smart
 * list instances.
 */
public class SmartList<T> extends Observable implements List<T> {

	List<T> delegate;

	public SmartList(List<T> delegate) {
		this.delegate = delegate;
	}

	public SmartList() {
		this.delegate = new ArrayList<T>();
	}

	@SuppressWarnings("rawtypes")
	public static synchronized SmartList<?> getInstance() {
		if (instance == null)
			instance = new SmartList();
		return instance;
	}

	private static SmartList<?> instance;
	
	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public boolean add(T item) {
		boolean result = delegate.add(item);
		if (result) {
			setChanged();
		}
		notifyObservers();
		return result;
	}

	@Override
	public void add(int position, T item) {
		delegate.add(position, item);
		setChanged();
		notifyObservers();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = delegate.addAll(c);
		if(result) {
			setChanged();
		}
		notifyObservers();
		return result;
	}

	@Override
	public boolean addAll(int position, Collection<? extends T> c) {
		boolean result = delegate.addAll(position, c);
		if(result) {
			setChanged();
		}
		notifyObservers();
		return result;
	}

	public boolean replace(Collection<? extends T> c) {
		delegate.clear();
		return addAll(c);
	}

	@Override
	public void clear() {
		delegate.clear();
		setChanged();
		notifyObservers();
	}

	@Override
	public boolean contains(Object item) {
		return delegate.contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public T get(int position) {
		return delegate.get(position);
	}

	@Override
	public int indexOf(Object item) {
		return delegate.indexOf(item);
	}
	
	@Override
	public boolean equals(Object item) {
		return delegate.equals(item);
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	@Override
	public int lastIndexOf(Object item) {
		return delegate.lastIndexOf(item);
	}

	@Override
	public ListIterator<T> listIterator() {
		return delegate.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int position) {
		return delegate.listIterator(position);
	}

	@Override
	public T remove(int position) {
		T result = delegate.remove(position);
		setChanged();
		notifyObservers();
		return result;
	}

	@Override
	public boolean remove(Object item) {
		boolean result = delegate.remove(item);
		if (result) {
			setChanged();
		}
		notifyObservers();
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = delegate.removeAll(c);
		if (result) {
			setChanged();
		}
		notifyObservers();
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = delegate.retainAll(c);
		if (result) {
			setChanged();
		}
		notifyObservers();
		return result;
	}

	@Override
	public T set(int position, T item) {
		T result = delegate.set(position, item);
		setChanged();
		notifyObservers();
		return result;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public List<T> subList(int fromPosition, int toPosition) {
		return delegate.subList(fromPosition, toPosition);
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}
}
