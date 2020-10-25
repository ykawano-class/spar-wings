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

import java.util.Collections;

import lombok.experimental.UtilityClass;

/**
 * Utilities about {@link Slice}.
 * 
 * <p>
 * 主にテストで使用する想定です。
 * </p>
 */
@UtilityClass
public class Slices {
	
	/**
	 * Empty slice shared instance.
	 */
	@SuppressWarnings("rawtypes")
	public static final Slice EMPTY_SLICE = new SliceImpl<>(Collections.emptyList(), null, false);
	
	
	/**
	 * Returns a empty slice.
	 *
	 * @return Empty slice
	 */
	@SuppressWarnings("unchecked")
	public static final <T> Slice<T> emptySlice() {
		return EMPTY_SLICE;
	}
	
}
