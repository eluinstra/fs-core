package dev.luin.file.server.core.server.download;

import java.io.IOException;

import dev.luin.file.server.core.FileExtension;
import dev.luin.file.server.core.file.FSFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class Md5FileHandler implements FileHandler
{
	FSFile fsFile;
	FileExtension extension;

	@Override
	public void handle(DownloadRequest request, DownloadResponse response) throws IOException
	{
		log.debug("GetMD5Checksum {}",fsFile);
		response.sendContent(extension.getContentType(),fsFile.getMd5Checksum().getValue());
	}

}
