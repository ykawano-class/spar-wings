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

import org.springframework.data.domain.Sort.Direction;

import jp.xet.sparwings.spring.data.exceptions.InvalidSliceableException;

/**
 * Abstract interface for value-based pagination information.
 */
public interface Sliceable {
	
	/**
	 * ページ番号取得.
	 * @return ページ番号
	 */
	Integer getPageNumber();
	
	/**
	 * ソート順取得.
	 * @return ソート順
	 */
	Direction getDirection();
	
	/**
	 * slice 辺りの最大要素数取得.
	 * @return slice 辺りの最大要素数
	 */
	Integer getMaxContentSize();
	
	/**
	 * offset 算出.
	 * @return offset(計算できない場合、null)
	 */
	Integer getOffset();
	
	/**
	 * validate 処理.
	 * 
	 * @throws InvalidSliceableException 不正な Sliceable だった場合
	 */
	void validate();
}
