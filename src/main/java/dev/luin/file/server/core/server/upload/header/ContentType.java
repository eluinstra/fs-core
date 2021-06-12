/**
 * Copyright 2020 E.Luinstra
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.luin.file.server.core.server.upload.header;

import org.apache.commons.lang3.StringUtils;

import dev.luin.file.server.core.server.upload.UploadException;
import dev.luin.file.server.core.server.upload.UploadRequest;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentType
{
	private static final String HEADER_NAME = "Content-Type";
	private static final String VALUE = "application/offset+octet-stream";

	public static void validate(@NonNull final UploadRequest request)
	{
		validate(request.getHeader(HEADER_NAME));
	}

	static void validate(final String value)
	{
		Option.of(value)
			.flatMap(ContentType::parseValue)
			.toTry()
			.filter(VALUE::equals)
			.getOrElseThrow(UploadException::invalidContentType);
	}

	private static Option<String> parseValue(final String s)
	{
		return s != null ? Option.of(s.split(";")[0].trim()).filter(StringUtils::isNotEmpty) : Option.none();
	}
}
