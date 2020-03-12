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
package jp.xet.sparwings.spring.data.repository;

import java.io.Serializable;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.NoRepositoryBean;

import jp.xet.sparwings.spring.data.slice.Slice;
import jp.xet.sparwings.spring.data.slice.Sliceable;

/**
 * Repository interface to retrieve slice of entities.
 * 
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
@NoRepositoryBean
public interface SliceableRepository<E, ID extends Serializable>extends ReadableRepository<E, ID> {
	
	/**
	 * Returns a {@link Slice} of entities meeting the slicing restriction provided in the {@code Sliceable} object.
	 * 
	 * @param sliceable slicing information
	 * @return a slice of entities
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @throws NullPointerException 引数に{@code null}を与えた場合
	 */
	Slice<E> findAll(Sliceable sliceable);
	
}
