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

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;

/**
 * A part of item set.
 * @param <T> Type of item
 */
public interface Slice<T>extends Collection<T> {
	
	/**
	 * 当該 Slice の要素取得.
	 * @return 当該 Slice の要素
	 */
	List<T> getContent();
	
	/**
	 * 当該 Slice の要素を Stream で取得.
	 * @return Stream
	 */
	Stream<T> stream();
	
	/**
	 * Slice のページ番号取得.
	 * @return Slice のページ番号(Slice 条件が存在しない場合、null)
	 */
	Integer getPageNumber();
	
	/**
	 * 要素を保持しているか？
	 * @return 要素を保持している場合、true
	 */
	boolean hasContent();
	
	/**
	 * 次の Slice が存在するか？
	 * @return 次の Slice が存在する場合、true
	 */
	boolean hasNext();
	
	/**
	 * 次の Slice 条件取得.
	 * 
	 * @return 次の Slice 条件(次の Slice が存在しない場合、null)
	 */
	Sliceable nextSlice();
	
	/**
	 * convert.
	 * @param converter コンバート処理.
	 * @param <S> 変更後の型
	 * @return convert を行った Slice
	 */
	<S> Slice<S> map(Converter<? super T, ? extends S> converter);
	
	/**
	 * 現在の Slice 条件取得.
	 * @return 現在の Slice 条件
	 */
	Sliceable getSliceable();
}
