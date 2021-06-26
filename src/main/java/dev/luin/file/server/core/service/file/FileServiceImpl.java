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
package dev.luin.file.server.core.service.file;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.activation.DataHandler;

import dev.luin.file.server.core.file.FSFile;
import dev.luin.file.server.core.file.FileSystem;
import dev.luin.file.server.core.file.UserId;
import dev.luin.file.server.core.file.VirtualPath;
import dev.luin.file.server.core.service.ServiceException;
import dev.luin.file.server.core.service.user.User;
import dev.luin.file.server.core.service.user.UserManager;
import io.vavr.Tuple;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@AllArgsConstructor
class FileServiceImpl implements FileService
{
	private static final Consumer<FSFile> logUploadedFile = file -> log.info("Uploaded file {}",file);
	private static final Consumer<FSFile> logDownloadedFile = file -> log.info("Downloaded file {}",file);
	private static final Consumer<FSFile> logDeletedFile = file -> log.info("Deleted file {}",file);
	@NonNull
	UserManager userManager;
	@NonNull
	FileSystem fs;

	@Override
	public String uploadFile(final long userId, @NonNull final NewFile file) throws ServiceException
	{
		log.debug("uploadFile userId={}, {}",userId,file);
		return Try.of(() -> 
				{
					val user = userManager.findUser(new UserId(userId));
					return user.toEither(() -> new ServiceException("User not found!"))
							.flatMap(u -> createFile(file,u).mapLeft(ServiceException::new))
							.peek(logUploadedFile)
							.get()
							.getVirtualPath().getValue();
				})
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public File downloadFile(@NonNull final String path) throws ServiceException
	{
		log.debug("downloadFile {}",path);
		return Try.of(() -> 
				{
					val fsFile = fs.findFile(new VirtualPath(path));
					val dataSource = fsFile.map(f -> f.toDataSource());
					return fsFile.filter(f -> f.isCompleted())
							.peek(logDownloadedFile)
							.flatMap(f -> dataSource.map(d -> new File(f,new DataHandler(d))))
							.getOrElseThrow(() -> new ServiceException("File not found!"));
				})
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<String> getFiles() throws ServiceException
	{
		log.debug("getFiles");
		return Try.of(() -> fs.getFiles()).getOrElseThrow(ServiceException.defaultExceptionProvider)
				.stream()
				.map(p -> p.getValue())
				.collect(Collectors.toList());
	}

	@Override
	public FileInfo getFileInfo(@NonNull final String path) throws ServiceException
	{
		log.debug("getFileInfo {}",path);
		return Try.of(() -> 
				{
					val fsFile = fs.findFile(new VirtualPath(path));
					return fsFile.map(FileInfo::new)
							.getOrElseThrow(() -> new ServiceException("File not found!"));
				})
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public Boolean deleteFile(@NonNull final String path, final Boolean force) throws ServiceException
	{
		log.debug("deleteFile {}",path);
		return Try.of(() -> 
				{
					val fsFile = fs.findFile(new VirtualPath(path));
					return fsFile
							.toEither(() -> new ServiceException("File not found!"))
							.map(f -> Tuple.of(f,fs.deleteFile(fsFile.get(),force != null && force)))
							.peek(t -> logDeletedFile.accept(t._1))
							.map(v -> v._2)
							.getOrElseThrow(t -> t)
							//TODO: IMPROVE
							.getOrElseThrow(t -> t);
				})
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private Either<IOException,FSFile> createFile(final NewFile file, final User user)
	{
		return fs.createNewFile(NewFSFileImpl.of(file),user);
	}
}
