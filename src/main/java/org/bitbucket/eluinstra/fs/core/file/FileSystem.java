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
package org.bitbucket.eluinstra.fs.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import io.vavr.Function1;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@AllArgsConstructor
@Transactional(transactionManager = "dataSourceTransactionManager")
public class FileSystem
{
	public static final Function1<String,File> getFile = path -> Paths.get(path).toFile();
	@NonNull
	FSFileDAO fsFileDAO;
	@NonNull
	SecurityManager securityManager;
	int virtualPathLength;
	@NonNull
	String baseDir;
	int filenameLength;

	public boolean existsFile(@NonNull final String virtualPath)
	{
		//TODO
		return fsFileDAO.findFileByVirtualPath(virtualPath).isDefined();
	}

	public String createVirtualPath()
	{
		while (true)
		{
			val result = RandomStringUtils.randomAlphanumeric(virtualPathLength);
			if (!existsFile(result))
				return "/" + result.toString();
		}
	}

	public Option<FSFile> findFile(@NonNull final String virtualPath)
	{
		return fsFileDAO.findFileByVirtualPath(virtualPath);
	}

	public Option<FSFile> findFile(@NonNull final byte[] clientCertificate, @NonNull final String virtualPath)
	{
		val result = fsFileDAO.findFileByVirtualPath(virtualPath);
		return result.filter(r -> securityManager.isAuthorized(clientCertificate,r) && isValidTimeFrame(result.get()));
	}

	public DataSource createDataSource(FSFile fsFile)
	{
		return new FileDataSource(fsFile.getFile());
	}

	public List<String> getFiles()
	{
		return fsFileDAO.selectFiles();
	}

	public FSFile createFile(
			final String filename,
			@NonNull final String contentType,
			final String sha256checksum,
			final Instant startDate,
			final Instant endDate,
			@NonNull final Long clientId,
			@NonNull final InputStream content) throws IOException
	{
		val virtualPath = createVirtualPath();
		val realPath = createRandomFile();
		val file = getFile.apply(realPath);
		write(content,file);
		val calculatedSha256Checksum = calculateSha256Checksum(file);
		if (validateChecksum(sha256checksum,calculatedSha256Checksum))
		{
			val md5Checksum = calculateMd5Checksum(file);
			val result = FSFile.builder()
					.virtualPath(virtualPath)
					.realPath(realPath)
					.filename(filename)
					.contentType(contentType)
					.md5Checksum(md5Checksum)
					.sha256Checksum(calculatedSha256Checksum)
					.startDate(startDate)
					.endDate(endDate)
					.clientId(clientId)
					.build();
			fsFileDAO.insertFile(result);
			return result;
		}
		else
			throw new IOException("Checksum error for file " + virtualPath + ". Checksum of the file uploaded (" + calculatedSha256Checksum + ") is not equal to the provided checksum (" + sha256checksum + ")");
	}
	
	public FSFile createPartialFile(
			final String filename,
			@NonNull final String contentType,
			@NonNull final Long clientId) throws IOException
	{
		val virtualPath = createVirtualPath();
		val realPath = createRandomFile();
		val result = FSFile.builder()
				.virtualPath(virtualPath)
				.realPath(realPath)
				.filename(filename)
				.contentType(contentType)
				.clientId(clientId)
				.build();
		fsFileDAO.insertFile(result);
		return result;
	}

	public FSFile finishPartialFile(@NonNull final FSFile fsFile) throws IOException
	{
		val file = fsFile.getFile();
		val result = fsFile
				.withSha256Checksum(calculateSha256Checksum(file))
				.withMd5Checksum(calculateMd5Checksum(file));
		fsFileDAO.updateFile(result);
		return result;
	}

	public long write(@NonNull final FSFile fsFile, @NonNull final InputStream input) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists() || !fsFile.isPartialFile())
			throw new FileNotFoundException(fsFile.getVirtualPath());
		try (val output = new FileOutputStream(file,true))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	public long write(@NonNull final FSFile fsFile, @NonNull final OutputStream output) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists())
			throw new FileNotFoundException(fsFile.getVirtualPath());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	public long write(@NonNull final FSFile fsFile, @NonNull final OutputStream output, final long first, final long length) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists())
			throw new FileNotFoundException(fsFile.getVirtualPath());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output,first,length);
		}
	}

	public boolean deleteFile(@NonNull final FSFile fsFile, final boolean force)
	{
		val result = Try.of(() -> fsFile.getFile().delete()).onFailure(t -> log.error("",t));
		if (force || result.isSuccess())
			fsFileDAO.deleteFile(fsFile.getVirtualPath());
		return force || result.getOrElse(false);
	}

	private String createRandomFile() throws IOException
	{
		while (true)
		{
			val filename = RandomStringUtils.randomNumeric(filenameLength);
			val result = Paths.get(baseDir,filename);
			if (result.toFile().createNewFile())
				return result.toString();
		}
	}

	private long write(final InputStream input, final File file) throws IOException
	{
		try (val output = new FileOutputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	private String calculateMd5Checksum(File file) throws IOException
	{
		try (val is = new FileInputStream(file))
		{
			return DigestUtils.md5Hex(is);
		}
	}

	private boolean validateChecksum(final String checksum, final String calculatedChecksum)
	{
		return StringUtils.isEmpty(checksum) || checksum.equalsIgnoreCase(calculatedChecksum);
	}

	private String calculateSha256Checksum(final File file) throws IOException
	{
		try (val is = new FileInputStream(file))
		{
			return DigestUtils.sha256Hex(is);
		}
	}

	private boolean isValidTimeFrame(final FSFile fsFile)
	{
		val now = Instant.now();
		return (fsFile.getStartDate() == null || fsFile.getStartDate().compareTo(now) <= 0
				&& fsFile.getEndDate() == null || fsFile.getEndDate().compareTo(now) > 0);
	}
}
