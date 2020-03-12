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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import lombok.experimental.UtilityClass;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;

import jp.xet.sparwings.spring.data.slice.Slice;
import jp.xet.sparwings.spring.data.slice.Sliceable;

/**
 * SliceableDefault に関する Utils.
 */
@UtilityClass
class SpringDataSliceableAnnotationUtils {
	
	/**
	 * Asserts uniqueness of all {@link Slice} parameters of the method of the given {@link MethodParameter}.
	 * 
	 * @param parameter must not be {@literal null}.
	 */
	public static void assertSliceableUniqueness(MethodParameter parameter) {
		Method method = parameter.getMethod();
		
		if (method == null) {
			throw new IllegalArgumentException("MethodParameter must has method");
		}
		
		if (containsMoreThanOneSliceableParameter(method)) {
			Annotation[][] annotations = method.getParameterAnnotations();
			assertQualifiersFor(method.getParameterTypes(), annotations);
		}
	}
	
	/**
	 * Returns whether the given {@link Method} has more than one {@link Sliceable} parameter.
	 * 
	 * @param method must not be {@literal null}.
	 * @return Sliceable をパラメータにしている場合、true
	 */
	private static boolean containsMoreThanOneSliceableParameter(Method method) {
		boolean sliceableFound = false;
		for (Class<?> type : method.getParameterTypes()) {
			if (sliceableFound && type.equals(Sliceable.class)) {
				return true;
			}
			if (type.equals(Sliceable.class)) {
				sliceableFound = true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the value of the given specific property of the given annotation. If the value of that property is the
	 * properties default, we fall back to the value of the {@code value} attribute.
	 * 
	 * @param annotation must not be {@literal null}.
	 * @param property must not be {@literal null} or empty.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSpecificPropertyOrDefaultFromValue(Annotation annotation, String property) {
		Object propertyDefaultValue = AnnotationUtils.getDefaultValue(annotation, property);
		Object propertyValue = AnnotationUtils.getValue(annotation, property);
		
		return (T) (ObjectUtils.nullSafeEquals(propertyDefaultValue, propertyValue)
				? AnnotationUtils.getValue(annotation)
				: propertyValue);
	}
	
	/**
	 * Asserts that every {@link Sliceable} parameter of the given parameters carries an {@link Qualifier} annotation to
	 * distinguish them from each other.
	 * 
	 * @param parameterTypes must not be {@literal null}.
	 * @param annotations must not be {@literal null}.
	 */
	public static void assertQualifiersFor(Class<?>[] parameterTypes, Annotation[][] annotations) { // NOPMD
		Set<String> values = new HashSet<>();
		for (int i = 0; i < annotations.length; i++) {
			if (Sliceable.class.equals(parameterTypes[i])) {
				Qualifier qualifier = findAnnotation(annotations[i]);
				if (null == qualifier) {
					throw new IllegalStateException("Ambiguous Sliceable arguments in handler method."
							+ " If you use multiple parameters of type Sliceable"
							+ " you need to qualify them with @Qualifier");
				}
				if (values.contains(qualifier.value())) {
					throw new IllegalStateException("Values of the user Qualifiers must be unique!");
				}
				values.add(qualifier.value());
			}
		}
	}
	
	/**
	 * Returns a {@link Qualifier} annotation from the given array of {@link Annotation}s. Returns {@literal null} if the
	 * array does not contain a {@link Qualifier} annotation.
	 * 
	 * @param annotations must not be {@literal null}.
	 * @return
	 */
	public static Qualifier findAnnotation(Annotation... annotations) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof Qualifier) {
				return (Qualifier) annotation;
			}
		}
		
		return null;
	}
}
