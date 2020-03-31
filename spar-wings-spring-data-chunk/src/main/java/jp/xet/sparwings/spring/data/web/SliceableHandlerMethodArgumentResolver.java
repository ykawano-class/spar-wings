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
package jp.xet.sparwings.spring.data.web;

import java.lang.reflect.Method;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jp.xet.sparwings.spring.data.exceptions.InvalidSliceableException;
import jp.xet.sparwings.spring.data.slice.SliceRequest;
import jp.xet.sparwings.spring.data.slice.Sliceable;
import jp.xet.sparwings.spring.web.httpexceptions.HttpBadRequestException;

/**
 * Extracts paging information from web requests and thus allows injecting {@link Sliceable} instances into controller
 * methods. Request properties to be parsed can be configured. Default configuration uses request parameters beginning
 * with {@link #DEFAULT_SIZE_PARAMETER}, {@link #DEFAULT_PAGE_NUMBER_PARAMETER}
 * {@link #DEFAULT_DIRECTION_PARAMETER}, {@link #DEFAULT_QUALIFIER_DELIMITER}.
 */
@Slf4j
public class SliceableHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver { // NOPMD - cc
	
	private static final String INVALID_DEFAULT_PAGE_SIZE =
			"Invalid default page size configured for method %s! Must not be less than one!";
	
	private static final String DEFAULT_SIZE_PARAMETER = "size";
	
	private static final String DEFAULT_DIRECTION_PARAMETER = "direction";
	
	private static final String DEFAULT_PAGE_NUMBER_PARAMETER = "page_number";
	
	private static final String DEFAULT_PREFIX = "";
	
	private static final String DEFAULT_QUALIFIER_DELIMITER = "_";
	
	private static final int DEFAULT_MAX_PAGE_SIZE = 2000;
	
	private static final int DEFAULT_PAGE_NUMBER = 0;
	
	static final Sliceable DEFAULT_SLICE_REQUEST = new SliceRequest(DEFAULT_PAGE_NUMBER, null, DEFAULT_MAX_PAGE_SIZE);
	
	
	private static Sliceable getDefaultSliceRequestFrom(MethodParameter parameter) {
		SliceableDefault defaults = parameter.getParameterAnnotation(SliceableDefault.class);
		
		if (defaults == null) {
			throw new IllegalArgumentException("MethodParameter must have @SliceableDefault");
		}
		
		int defaultPageSize = defaults.size();
		if (defaultPageSize == 10) {
			defaultPageSize = defaults.value();
		}
		
		if (defaultPageSize < 1) {
			Method annotatedMethod = parameter.getMethod();
			throw new IllegalStateException(String.format(Locale.ENGLISH, INVALID_DEFAULT_PAGE_SIZE, annotatedMethod));
		}
		
		return new SliceRequest(defaults.pageNumber(), defaults.direction(), defaultPageSize);
	}
	
	
	@NonNull
	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Sliceable fallbackSliceable = DEFAULT_SLICE_REQUEST;
	
	@NonNull
	@Getter(AccessLevel.PROTECTED)
	@Setter
	private String sizeParameterName = DEFAULT_SIZE_PARAMETER;
	
	@NonNull
	@Getter(AccessLevel.PROTECTED)
	@Setter
	private String directionParameterName = DEFAULT_DIRECTION_PARAMETER;
	
	@NonNull
	@Getter(AccessLevel.PROTECTED)
	@Setter
	private String pageNumberParameterName = DEFAULT_PAGE_NUMBER_PARAMETER;
	
	@Getter(AccessLevel.PROTECTED)
	private String prefix = DEFAULT_PREFIX;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter
	private String qualifierDelimiter = DEFAULT_QUALIFIER_DELIMITER;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter
	private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter
	private int initPageNumber = DEFAULT_PAGE_NUMBER;
	
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Sliceable.class.equals(parameter.getParameterType());
	}
	
	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, // NOPMD - cc
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception { // NOPMD - ex
		SpringDataSliceableAnnotationUtils.assertSliceableUniqueness(methodParameter);
		
		String pageSizeString = webRequest.getParameter(getParameterNameToUse(sizeParameterName, methodParameter));
		String directionString =
				webRequest.getParameter(getParameterNameToUse(directionParameterName, methodParameter));
		String pageNumberString =
				webRequest.getParameter(getParameterNameToUse(pageNumberParameterName, methodParameter));
		
		Sliceable defaultOrFallback = getDefaultFromAnnotationOrFallback(methodParameter);
		if (StringUtils.hasText(pageNumberString) == false
				&& StringUtils.hasText(pageSizeString) == false
				&& StringUtils.hasText(directionString) == false) {
			return defaultOrFallback;
		}
		
		Integer pageSize = defaultOrFallback.getMaxContentSize() != null
				? defaultOrFallback.getMaxContentSize() : maxPageSize;
		if (StringUtils.hasText(pageSizeString)) {
			try {
				pageSize = Integer.parseInt(pageSizeString);
			} catch (NumberFormatException e) {
				log.trace("invalid page size: {}", pageSizeString);
			}
		}
		
		// Limit lower bound
		pageSize = pageSize < 1 ? 1 : pageSize;
		// Limit upper bound
		pageSize = pageSize > maxPageSize ? maxPageSize : pageSize;
		
		Direction direction = Direction.fromOptionalString(directionString).orElse(null);
		
		Integer pageNumber =
				defaultOrFallback.getPageNumber() != null ? defaultOrFallback.getPageNumber() : DEFAULT_PAGE_NUMBER;
		if (StringUtils.hasText(pageNumberString)) {
			try {
				pageNumber = Integer.parseInt(pageNumberString);
			} catch (NumberFormatException e) {
				log.trace("invalid page number: {}", pageNumberString);
			}
		}
		
		Sliceable sliceable = new SliceRequest(pageNumber, direction, pageSize);
		try {
			sliceable.validate();
		} catch (InvalidSliceableException e) {
			throw new HttpBadRequestException(e.getMessage(), e);
		}
		return sliceable;
	}
	
	private Sliceable getDefaultFromAnnotationOrFallback(MethodParameter methodParameter) {
		if (methodParameter.hasParameterAnnotation(SliceableDefault.class)) {
			return getDefaultSliceRequestFrom(methodParameter);
		}
		
		return fallbackSliceable;
	}
	
	/**
	 * Returns the name of the request parameter to find the {@link Sliceable} information in. Inspects the given
	 * {@link MethodParameter} for {@link Qualifier} present and prefixes the given source parameter name with it.
	 * 
	 * @param source the basic parameter name.
	 * @param parameter the {@link MethodParameter} potentially qualified.
	 * @return the name of the request parameter.
	 */
	protected String getParameterNameToUse(String source, MethodParameter parameter) {
		StringBuilder builder = new StringBuilder(prefix);
		
		if (parameter != null) {
			Qualifier qualifier = parameter.getParameterAnnotation(Qualifier.class);
			if (qualifier != null) {
				builder.append(qualifier.value());
				builder.append(qualifierDelimiter);
			}
		}
		
		return builder.append(source).toString();
	}
}
