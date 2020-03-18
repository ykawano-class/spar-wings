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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.springframework.data.domain.Sort;

import org.junit.Test;

/**
 * Test for {@link SliceRequest}
 */
public class SliceRequestTest {
	
	@Test
	public void testGetOffset() {
		// setup
		SliceRequest sut = new SliceRequest(3, Sort.Direction.ASC, 14);
		
		// exercise
		Integer actual = sut.getOffset();
		
		// verify
		assertThat(actual, is(3 * 14));
	}
	
	@Test
	public void testGetOffset_ZeroPageNumber() {
		// setup
		SliceRequest sut = new SliceRequest(0, Sort.Direction.ASC, 14);
		
		// exercise
		Integer actual = sut.getOffset();
		
		// verify
		assertThat(actual, is(0)); // 0 * 14
	}
	
	@Test
	public void testGetOffset_NullValue() {
		// setup
		SliceRequest sut = new SliceRequest(null, Sort.Direction.ASC, null);
		
		// exercise
		Integer actual = sut.getOffset();
		
		// verify
		assertThat(actual, is(nullValue()));
	}
}
