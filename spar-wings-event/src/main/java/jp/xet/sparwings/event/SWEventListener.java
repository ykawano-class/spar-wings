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
package jp.xet.sparwings.event;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.context.ApplicationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * Spring のイベントリスナーの仕組みを使って SNS にイベント通知を行う
 * 
 * @since 0.16
 * @author daisuke
 */
@RequiredArgsConstructor
public class SWEventListener implements ApplicationListener<SWEvent> {
	
	private static Logger logger = LoggerFactory.getLogger(SWEventListener.class);
	
	private static final String EVENT_TYPE_KEY = "event_type";
	
	private static final String MESSAGE_ATTRIBUTE_DATATYPE_STRING = "String";
	
	@Getter
	private final AmazonSNS sns;
	
	@Getter
	private final ObjectMapper objectMapper;
	
	@Getter
	private final String eventTopicArn;
	
	@Getter
	@Setter
	private Consumer<Exception> exceptionHandler;
	
	
	@Override
	public void onApplicationEvent(SWEvent event) {
		if (Strings.isNullOrEmpty(eventTopicArn)) {
			return;
		}
		try {
			String message = objectMapper.writeValueAsString(event);
			String eventType = event.getEventType();
			PublishResult publishResult = publish(eventType, message);
			logger.info("SWEvent {} was published: {}", event.getEventType(), publishResult.getMessageId());
		} catch (Exception e) { // NOPMD
			if (exceptionHandler != null) {
				exceptionHandler.accept(e);
			} else {
				logger.error("Unexpected exception", e);
			}
		}
	}
	
	private PublishResult publish(String eventType, String message) {
		if (Strings.isNullOrEmpty(eventType)) {
			return sns.publish(new PublishRequest()
				.withTargetArn(eventTopicArn)
				.withMessage(message));
		} else {
			return sns.publish(new PublishRequest()
				.withTargetArn(eventTopicArn)
				.withMessageAttributes(ImmutableMap.of(EVENT_TYPE_KEY,
						new MessageAttributeValue().withDataType(MESSAGE_ATTRIBUTE_DATATYPE_STRING)
							.withStringValue(eventType)))
				.withMessage(message));
		}
	}
}
