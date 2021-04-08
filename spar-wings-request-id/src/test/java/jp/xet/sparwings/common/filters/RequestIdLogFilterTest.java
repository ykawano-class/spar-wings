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
package jp.xet.sparwings.common.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * RequestIdLogFilterTest
 * <p>
 *     RequestIdがMDCに登録されることをTest
 * </p>
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class RequestIdLogFilterTest {
	
	RequestIdLogFilter sut;
	
	@Mock
	HttpServletRequest request;
	
	@Mock
	HttpServletResponse response;
	
	@Mock
	RequestIdGenerator generator;
	
	static String MDC_KEY = "requestId";
	
	
	@Before
	public void setUp() {
		sut = new RequestIdLogFilter();
		sut.setGenerator(generator);
	}
	
	@Test
	public void testDispatcherType_REQUEST_generateId() throws IOException, ServletException {
		// setup
		String expectedRequestId = "56a89f35-0ad8-4c3f-9817-24537253a13c";
		when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
		when(generator.generateRequestId(any())).thenReturn(expectedRequestId);
		
		// exercise
		sut.doFilter(request, response, new MDCLog(is(expectedRequestId)));
		// verify
		verify(request, never()).getAttribute(any());
		verify(request, times(1)).setAttribute(eq(sut.getRequestIdAttribute()), eq(expectedRequestId));
		verify(response, times(1)).setHeader(eq(sut.getRequestIdHeader()), eq(expectedRequestId));
	}
	
	@Test
	public void testDispatcherType_ASYNC_getAttribute_null() throws IOException, ServletException {
		// setup
		when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);
		when(request.getAttribute(any())).thenReturn(null);
		
		FilterChain filterChain = spy(new MDCLog(nullValue()));
		// exercise
		sut.doFilter(request, response, filterChain);
		// verify
		verify(request, times(1)).getAttribute(eq(MDC_KEY));
		verify(filterChain, times(1)).doFilter(any(), any());
		verify(request, never()).setAttribute(any(), any());
		verify(response, never()).setHeader(any(), any());
	}
	
	@Test
	public void testDispatcherType_ASYNC_getAttribute() throws IOException, ServletException {
		// setup
		String expectedRequestId = "testRequestId";
		when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);
		when(request.getAttribute(any())).thenReturn(expectedRequestId);
		// exercise
		sut.doFilter(request, response, new MDCLog(Matchers.is(expectedRequestId)));
		// verify
		verify(request, times(1)).getAttribute(eq(MDC_KEY));
		verify(request, times(1)).setAttribute(eq(sut.getRequestIdAttribute()), eq(expectedRequestId));
		verify(response, times(1)).setHeader(eq(sut.getRequestIdHeader()), eq(expectedRequestId));
	}
	
	
	@AllArgsConstructor
	@Slf4j
	static class MDCLog implements FilterChain {
		
		Matcher<Object> expected;
		
		
		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			assertThat(MDC.get(MDC_KEY), expected);
		}
	}
}
