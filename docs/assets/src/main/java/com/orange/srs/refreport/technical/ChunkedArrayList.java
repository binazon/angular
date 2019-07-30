package com.orange.srs.refreport.technical;

import java.util.ArrayList;
import java.util.Iterator;

public class ChunkedArrayList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 7546163547338883819L;

	private int chunkSize;
	private int offsetShift;
	private ChunkedArrayListCallback<T> callback;

	public ChunkedArrayList(int chunkSize, ChunkedArrayListCallback<T> callback) {
		super(chunkSize);
		this.offsetShift = 0;
		this.chunkSize = chunkSize;
		this.callback = callback;
		super.addAll(callback.update(offsetShift, chunkSize));
	}

	@Override
	public T get(int index) {
		dataLoad(index);
		return super.get(index - offsetShift);
	}

	public boolean hasElement(int index) {
		dataLoad(index);
		return ((index - offsetShift) < super.size());
	}

	private void dataLoad(int index) {
		if (index < offsetShift || index >= (offsetShift + chunkSize)) {
			offsetShift = (index / chunkSize) * chunkSize;
			super.clear();
			super.addAll(callback.update(offsetShift, chunkSize));

		}
	}

	@Override
	public int size() {
		return super.size() + this.offsetShift;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> it = new Iterator<T>() {

			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				return hasElement(currentIndex);
			}

			@Override
			public T next() {
				return get(currentIndex++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove method not implemented");
			}
		};
		return it;
	}
}
