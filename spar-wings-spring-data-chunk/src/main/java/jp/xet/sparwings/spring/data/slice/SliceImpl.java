/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.sparwings.spring.data.slice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Slice の実装.
 * @param <T> Slice 要素の型
 */
@EqualsAndHashCode
public class SliceImpl<T> implements Slice<T> {
	
	/** 当該 Slice の要素. */
	@JsonProperty
	private final List<T> content = new ArrayList<>();
	
	/** Slice 条件. */
	@JsonIgnore
	@Getter
	private final Sliceable sliceable;
	
	/** 次の Slice が存在するか？ */
	@JsonIgnore
	@Getter
	private final boolean hasNext;
	
	
	/**
	 * コンストラクタ.
	 * @param content 当該 Slice の要素
	 * @param sliceable リクエスト
	 * @param hasNext 次の Slice が存在する場合、true
	 */
	public SliceImpl(List<T> content, Sliceable sliceable, boolean hasNext) {
		Assert.notNull(content, "Content must not be null!");
		this.content.addAll(content);
		this.sliceable = sliceable;
		this.hasNext = hasNext;
	}
	
	@Override
	public List<T> getContent() {
		return Collections.unmodifiableList(content);
	}
	
	@Override
	public Stream<T> stream() {
		return content.stream();
	}
	
	@Override
	public Integer getPageNumber() {
		return sliceable == null ? null : sliceable.getPageNumber();
	}
	
	@Override
	public boolean hasContent() {
		return content.isEmpty() == false;
	}
	
	@Override
	public boolean hasNext() {
		if (sliceable == null) {
			return false;
		}
		return hasNext;
	}
	
	@Override
	public Sliceable nextSlice() {
		if (hasNext() == false) {
			return null;
		}
		return new SliceRequest(sliceable.getPageNumber() + 1,
				sliceable.getDirection(), sliceable.getMaxContentSize());
	}
	
	@Override
	public <S> Slice<S> map(Converter<? super T, ? extends S> converter) {
		return new SliceImpl<>(getConvertedContent(converter), sliceable, hasNext);
	}
	
	/**
	 * Applies the given {@link Converter} to the content of the {@link Slice}.
	 *
	 * @param converter must not be {@literal null}.
	 * @return mapped content list
	 */
	protected <S> List<S> getConvertedContent(Converter<? super T, ? extends S> converter) {
		Assert.notNull(converter, "Converter must not be null!");
		return content.stream().map(converter::convert).collect(Collectors.toList());
	}
	
	// Collection
	
	@Override
	public int size() {
		return content.size();
	}
	
	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return content.contains(o);
	}
	
	@Override
	public Iterator<T> iterator() {
		return content.iterator();
	}
	
	@Override
	public Object[] toArray() {
		return content.toArray();
	}
	
	@Override
	public boolean add(T o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return content.containsAll(c);
	}
	
	@Override
	public <T1> T1[] toArray(T1[] a) {
		return content.toArray(a);
	}
}
