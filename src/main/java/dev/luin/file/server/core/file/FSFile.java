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
package dev.luin.file.server.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;

import dev.luin.file.server.core.server.download.range.ContentRange;
import dev.luin.file.server.core.service.file.FileDataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.val;

@Builder(access = AccessLevel.PACKAGE)
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FSFile
{
	@NonNull
	VirtualPath virtualPath;
	@NonNull
	@Getter(value=AccessLevel.PACKAGE)
	Path path;
	Filename name;
	@NonNull
	String contentType;
	@With
	Md5Checksum md5Checksum;
	@With
	Sha256Checksum sha256Checksum;
	@NonNull
	Instant timestamp;
	TimeFrame validTimeFrame;
	long userId;
	@With
	FileLength length;
	FileState state;

	public FSFile(@NonNull VirtualPath virtualPath, @NonNull Path path, Filename name, @NonNull String contentType, Md5Checksum md5Checksum, Sha256Checksum sha256Checksum, @NonNull Instant timestamp, Instant startDate, Instant endDate, long userId, FileLength length, FileState state)
	{
		this.virtualPath = virtualPath;
		this.path = path;
		this.name = name;
		this.contentType = contentType;
		this.md5Checksum = md5Checksum;
		this.sha256Checksum = sha256Checksum;
		this.timestamp = timestamp;
		this.validTimeFrame = new TimeFrame(startDate,endDate);
		this.userId = userId;
		this.length = length;
		this.state = state;
	}

	private File getFile()
	{
		return path.toFile();
	}

	public FileLength getFileLength()
	{
		return new FileLength(getFile().length());
	}

	public Instant getLastModified()
	{
		return Instant.ofEpochMilli(getFile().lastModified());
	}

	public boolean isBinary()
	{
		return !getContentType().matches("^(text/.*|.*/xml)$");
	}

	public boolean isCompleted()
	{
		return length.equals(getFileLength());
	}

	public boolean hasValidTimeFrame()
	{
		return validTimeFrame.isValid();
	}

	public DataSource toDataSource()
	{
		return new FileDataSource(getFile(),name.getOrNull(),contentType);
	}

	FSFile append(@NonNull final InputStream input, final FileLength length) throws IOException
	{
		val file = getFile();
		if (!file.exists() || isCompleted())
			throw new FileNotFoundException(virtualPath.getValue());
		try (val output = new FileOutputStream(file,true))
		{
			if (length != null)
				IOUtils.copyLarge(input,output,0,length.getOrNull());
			else
				IOUtils.copyLarge(input,output);
			if (isCompleted())
				return complete();
			else
				return this;
		}
	}

	private FSFile complete() throws IOException
	{
		val file = getFile();
		if (!file.exists())// || !fsFile.isCompleted())
			throw new FileNotFoundException(virtualPath.getValue());
		val result = this
				.withSha256Checksum(Sha256Checksum.of(file))
				.withMd5Checksum(Md5Checksum.of(file));
		return result;
	}

	public long write(@NonNull final OutputStream output) throws IOException
	{
		val file = getFile();
		if (!file.exists() || !isCompleted())
			throw new FileNotFoundException(virtualPath.getValue());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	public long write(@NonNull final OutputStream output, final ContentRange range) throws IOException
	{
		val file = getFile();
		if (!file.exists() || !isCompleted())
			throw new FileNotFoundException(virtualPath.getValue());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output,range.getFirst(getFileLength()),range.getLength(getFileLength()));
		}
	}

	public boolean delete() throws IOException
	{
		return Files.deleteIfExists(path);
	}

}
