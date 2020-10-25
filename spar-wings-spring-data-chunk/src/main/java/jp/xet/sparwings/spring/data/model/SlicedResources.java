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
package jp.xet.sparwings.spring.data.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jp.xet.sparwings.spring.data.slice.Slice;

/**
 * Slice のレスポンス.
 * 
 * <p>Controller のレスポンスとして使用します。</p>
 */
@ToString
@EqualsAndHashCode
@XmlRootElement(name = "slicedEntities")
public class SlicedResources<T> {
	
	@Getter
	@XmlElement(name = "embedded")
	@JsonProperty("_embedded")
	private Map<String, Collection<T>> content;
	
	@Getter
	@XmlElement(name = "page")
	@JsonProperty("page")
	private SliceMetadata metadata;
	
	
	/**
	 * Creates a {@link SlicedResources} instance with {@link Slice}.
	 * 
	 * @param key must not be {@code null}.
	 * @param slice The {@link Slice}
	 * @param wrapperFunction function coverts {@code U} to {@code T}
	 */
	public <U> SlicedResources(String key, Slice<U> slice, Function<U, T> wrapperFunction) {
		this(key, slice.stream()
			.map(wrapperFunction)
			.collect(Collectors.toList()), new SliceMetadata(slice));
	}
	
	/**
	 * Creates a {@link SlicedResources} instance with {@link Slice}.
	 * 
	 * @param key must not be {@code null}.
	 * @param slice The {@link Slice}
	 */
	public SlicedResources(String key, Slice<T> slice) {
		this(key, slice.getContent(), new SliceMetadata(slice));
	}
	
	/**
	 * Creates a {@link SlicedResources} instance with content collection.
	 * 
	 * @param key must not be {@code null}.
	 * @param content The contents
	 * @since 0.11
	 */
	public SlicedResources(String key, Collection<T> content) {
		this(key, content, new SliceMetadata(content.size(), null, false));
	}
	
	/**
	 * Creates a {@link SlicedResources} instance with iterable and metadata.
	 * 
	 * @param key must not be {@code null}.
	 * @param content must not be {@code null}.
	 * @param metadata must not be {@code null}.
	 * @since 0.11
	 */
	public SlicedResources(String key, Collection<T> content, SliceMetadata metadata) {
		Assert.notNull(key, "The key must not be null");
		Assert.notNull(content, "The content must not be null");
		Assert.notNull(metadata, "The metadata must not be null");
		this.content = Collections.singletonMap(key, content);
		this.metadata = metadata;
	}
	
	
	/**
	 * Value object for pagination metadata.
	 */
	@ToString
	@EqualsAndHashCode
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	public static class SliceMetadata {
		
		@XmlAttribute
		@JsonProperty("size")
		@Getter(onMethod = @__(@JsonIgnore))
		private long size;
		
		@XmlAttribute
		@JsonProperty("number")
		@Getter(onMethod = @__(@JsonIgnore))
		private Integer pageNumber;
		
		@XmlAttribute
		@JsonProperty("has_next_page")
		@Getter(onMethod = @__(@JsonIgnore))
		private Boolean hasNextSlice;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param slice 当該 Slice
		 */
		public SliceMetadata(Slice<?> slice) {
			this(slice.getContent().size(), slice.getPageNumber(), slice.hasNext());
		}
	}
}
