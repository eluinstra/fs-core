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

import java.util.function.Supplier;

import dev.luin.file.server.core.ValueObject;
import dev.luin.file.server.core.file.Length;
import dev.luin.file.server.core.server.upload.UploadException;
import dev.luin.file.server.core.server.upload.UploadRequest;
import io.vavr.Function1;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadLength implements ValueObject<Long>
{
	public static final String HEADER_NAME = "Upload-Length";
	private static final Function1<String,Either<String,String>> checkLength = inclusiveBetween.apply(0L,19L);
	private static final Function1<String,Either<String,String>> checkPattern = matchesPattern.apply("^[0-9]*$");
	private static final Function1<String,Either<String,Long>> validateAndTransform = 
			(uploadLength) -> Either.<String,String>right(uploadLength)
				.flatMap(isNotNull)
				.flatMap(checkLength)
				.flatMap(checkPattern)
				.flatMap(v -> safeToLong.apply(v)
						.map(Either::<String,Long>right)
						.getOrElse(Either.left("Invalid number")))
				/*.flatMap(isPositive)*/;
	@NonNull
	Long value;

	public static Either<UploadException,Option<UploadLength>> of(@NonNull final UploadRequest request, final TusMaxSize maxSize)
	{
		return of(request.getHeader(HEADER_NAME),maxSize,() -> UploadDeferLength.isDefined(request));
	}

	static Either<UploadException,Option<UploadLength>> of(final String value, final TusMaxSize maxSize, @NonNull final Supplier<Boolean> isUploadDeferLengthDefined)
	{
		return value == null 
				? (isUploadDeferLengthDefined.get()
						? Either.<UploadException,Option<UploadLength>>right(Option.none())
						: Either.<UploadException,Option<UploadLength>>left(UploadException.missingUploadLength()))
				:	validateAndTransform.apply(value)
						.map(UploadLength::new)
						.toEither(UploadException::invalidContentLength)
						.filterOrElse(v -> (maxSize == null ? true : v.getValue() <= maxSize.getValue()),l -> UploadException.fileTooLarge())
						.map(Option::some);
	}

	public Length toFileLength()
	{
		return new Length(value);
	}
}
