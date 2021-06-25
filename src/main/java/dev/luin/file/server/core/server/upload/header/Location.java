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

import java.util.function.Consumer;

import dev.luin.file.server.core.server.upload.UploadResponse;
import io.vavr.Function1;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Location
{
	public static final Function1<String,Consumer<UploadResponse>> writeLocation = location -> response -> write(response,location);
	private static final String HEADER_NAME = "Location";

	public static void write(@NonNull final UploadResponse response, @NonNull final String location)
	{
		response.setHeader(HEADER_NAME,location);
	}
}
