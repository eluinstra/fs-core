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

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;
import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.matchesPattern;

import dev.luin.file.server.core.ValueObject;
import dev.luin.file.server.core.file.Length;
import dev.luin.file.server.core.server.upload.UploadException;
import dev.luin.file.server.core.server.upload.UploadRequest;
import dev.luin.file.server.core.server.upload.UploadResponse;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.Value;

@Value
public class UploadOffset implements ValueObject<Long>
{
	public static final String HEADER_NAME = "Upload-Offset";
	@NonNull
	Long value;

	public static UploadOffset of(@NonNull final UploadRequest request)
	{
		return Option.of(request.getHeader(HEADER_NAME))
				.toTry(UploadException::missingUploadOffset)
				.map(v -> new UploadOffset(v))
				.get();
	}

	public static void write(@NonNull final UploadResponse response, @NonNull final Length length)
	{
		response.setHeader(HEADER_NAME,length.getStringValue());
	}

	@SuppressWarnings("unchecked")
	UploadOffset(@NonNull final String uploadOffset)
	{
		value = Try.success(uploadOffset)
				.andThenTry(v -> inclusiveBetween(0,19,v.length()))
				.andThenTry(v -> matchesPattern(v,"[0-9]+"))
				.mapTry(v -> Long.parseLong(v))
//				.peek(v -> isTrue(0 <= v && v <= Long.MAX_VALUE))
				.mapFailure(
						Case($(instanceOf(UploadException.class)),t -> t),
						Case($(),UploadException::invalidUploadOffset))
				.get();
	}

	public void validateFileLength(@NonNull final Length length)
	{
		if (!length.equals(new Length(value)))
			throw UploadException.invalidUploadOffset();
	}
}
