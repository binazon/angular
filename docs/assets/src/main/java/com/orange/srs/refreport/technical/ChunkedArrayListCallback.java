package com.orange.srs.refreport.technical;

import java.util.List;

public interface ChunkedArrayListCallback<T> {

	public abstract List<T> update(int offset, int chunkSize);
}