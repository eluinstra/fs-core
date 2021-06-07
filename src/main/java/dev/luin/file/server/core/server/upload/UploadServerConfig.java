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
package dev.luin.file.server.core.server.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.luin.file.server.core.file.FileSystem;
import dev.luin.file.server.core.server.upload.header.TusMaxSize;
import dev.luin.file.server.core.service.user.AuthenticationManager;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadServerConfig
{
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	FileSystem fs;
	@Value("${server.path}")
	String basePath;
	@Value("${file.maxFileSize}")
	Long maxFileSize;

	@Bean("UploadHttpHandler")
	public UploadHandler uploadHandler()
	{
		return UploadHandler.builder()
				.authenticationManager(authenticationManager)
				.tusOptionsHandler(new TusOptionsHandler(fs))
				.fileInfoHandler(new FileInfoHandler(fs))
				.createFileHandler(new CreateFileHandler(fs,basePath + "/upload"))
				.uploadFileHandler(new UploadFileHandler(fs))
				.deleteFileHandler(new DeleteFileHandler(fs))
				.build();
	}

	@Bean
	public TusMaxSize tusMaxSize()
	{
		return TusMaxSize.of(maxFileSize);
	}
}
