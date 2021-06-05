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

import static org.apache.commons.lang3.Validate.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.luin.file.server.core.server.upload.UploadException;
import io.vavr.control.Option;

public class TusResumable
{
	public static final String HEADER_NAME = "Tus-Resumable";
	public static final String VALUE = TusVersion.VALUE;

	public static void validate(HttpServletRequest request)
	{
		validate(request.getHeader(HEADER_NAME));
	}

	private static void validate(String value)
	{
		Option.of(value)
			.toTry()
			.andThenTry(v -> inclusiveBetween(0,VALUE.length(),v.length()))
			.filterTry(VALUE::equals)
			.getOrElseThrow(UploadException::invalidTusVersion);
	}

	public static void write(HttpServletResponse response)
	{
		response.setHeader(HEADER_NAME,VALUE);
	}
}
