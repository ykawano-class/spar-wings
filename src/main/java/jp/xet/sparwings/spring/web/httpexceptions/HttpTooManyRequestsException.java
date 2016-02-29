/*
 * Copyright 2015 Miyamoto Daisuke.
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
package jp.xet.sparwings.spring.web.httpexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * TODO for daisuke
 * 
 * @since #version#
 * @author daisuke
 */
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class HttpTooManyRequestsException extends Exception {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param millisecsToWait 要待機ミリ秒
	 */
	public HttpTooManyRequestsException(long millisecsToWait) {
		super(String.format("Please wait %d ms before next request", millisecsToWait));
	}
}
