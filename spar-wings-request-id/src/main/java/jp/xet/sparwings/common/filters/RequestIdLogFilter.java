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

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

/**
 * Servlet {@link Filter} implementation to generate Request-ID.
 * 
 * <p>Generated Request-ID is set to {@code X-Request-Id} response header
 * and {@link MDC} value which identified by {@code requestId}.
 * If requestId already exists in request attributes, use it</p>
 *
 * @see RequestIdFilter
 * @author kawano
 */
@Slf4j
public class RequestIdLogFilter implements Filter {
	
	private static final String DEFAULT_REQUEST_ID_ATTRIBUTE = "requestId";
	
	private static final String DEFAULT_REQUEST_ID_HEADER = "Request-Id";
	
	private static final String DEFAULT_REQUEST_ID_MDC_KEY = "requestId";
	
	@Getter
	@Setter
	private String requestIdAttribute = DEFAULT_REQUEST_ID_ATTRIBUTE;
	
	@Getter
	@Setter
	private String requestIdMdcKey = DEFAULT_REQUEST_ID_MDC_KEY;
	
	@Getter
	@Setter
	private String requestIdHeader = DEFAULT_REQUEST_ID_HEADER;
	
	@NonNull
	@Getter
	@Setter
	private RequestIdGenerator generator = new UuidRequestIdGenerator();
	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("RequestIdLogFilter just supports HTTP requests");
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		doFilterInternal(httpRequest, httpResponse, chain);
	}
	
	@Override
	public void destroy() {
		// nothing to do
	}
	
	void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.trace("Start issue request ID");
		String requestId = generateRequestId(request);
		if (requestId == null) {
			filterChain.doFilter(request, response);
			return;
		}
		log.info("Request ID issued: {}", requestId);
		if (requestIdAttribute != null) {
			request.setAttribute(requestIdAttribute, requestId);
		}
		if (requestIdMdcKey != null) {
			MDC.put(requestIdMdcKey, requestId);
		}
		if (requestIdHeader != null) {
			response.setHeader(requestIdHeader, requestId);
		}
		try {
			filterChain.doFilter(request, response);
		} finally {
			if (requestIdMdcKey != null) {
				MDC.remove(requestIdMdcKey);
			}
		}
	}
	
	private String generateRequestId(final HttpServletRequest request) {
		final DispatcherType dispatcherType = request.getDispatcherType();
		switch (dispatcherType) {
			case FORWARD:
			case INCLUDE:
			case ASYNC:
			case ERROR:
				return (String) request.getAttribute(requestIdAttribute);
			case REQUEST:
			default:
				return generator.generateRequestId(request);
		}
	}
}
