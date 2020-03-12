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

/**
 * Basic Java Bean implementation of {@code Sliceable}.
 */
@Value
public class SliceRequest implements Sliceable {
	
	/** 
	 * ページ番号. 
	 * 
	 * <p>1以上指定</p>
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
		return (pageNumber - 1) * maxContentSize;
	}
}
