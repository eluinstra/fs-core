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
package dev.luin.file.server.core.server.download;

import java.io.OutputStream;

import dev.luin.file.server.core.file.ContentType;
import dev.luin.file.server.core.file.FSFile;
import dev.luin.file.server.core.server.download.header.ContentRange;

public interface DownloadResponse
{
	void setStatusOk();
	void setStatusPartialContent();
	void setHeader(String headerName, String value);
	OutputStream getOutputStream();
	void sendContent(ContentType contentType, String content);
	void sendFileInfo(FSFile fsFile);
	void sendFile(FSFile fsFile, ContentRange ranges);
}
