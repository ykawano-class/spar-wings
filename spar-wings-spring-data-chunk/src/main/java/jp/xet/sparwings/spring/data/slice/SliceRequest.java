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

import lombok.Value;

import org.springframework.data.domain.Sort.Direction;

import jp.xet.sparwings.spring.data.exceptions.InvalidSliceableException;

/**
 * Basic Java Bean implementation of {@code Sliceable}.
 */
@Value
public class SliceRequest implements Sliceable {
	
	/** 
	 * 取得可能な最大要素数.
	 * 
	 * <p>
	 * この件数を超えて要素を取得することはできません。
	 * </p>
	 */
	private static final int MAX_TOTAL_CONTENT_LIMIT = 2000;
	
	/** 
	 * ページ番号. 
	 * 
	 * <p>0以上指定</p>
	 */
	private Integer pageNumber;
	
	/**
	 * ソート順.
	 */
	private Direction direction;
	
	/**
	 * slice 辺りの最大要素数.
	 */
	private Integer maxContentSize;
	
	
	@Override
	public Integer getOffset() {
		
		if (pageNumber == null || maxContentSize == null) {
			return null;
		}
		return pageNumber * maxContentSize;
	}
	
	@Override
	public void validate() {
		
		if (pageNumber == null) {
			throw new InvalidSliceableException("pageNumber must be not null.");
		}
		if (maxContentSize == null) {
			throw new InvalidSliceableException("maxContentSize must be not null.");
		}
		
		if (pageNumber * maxContentSize + maxContentSize > MAX_TOTAL_CONTENT_LIMIT) {
			throw new InvalidSliceableException("Cannot get elements beyond 2000.");
		}
	}
}
