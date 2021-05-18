package dev.luin.file.server.core.server.upload.http;

import javax.servlet.http.HttpServletResponse;

import dev.luin.file.server.core.file.FSFile;
import dev.luin.file.server.core.server.upload.UploadResponse;
import dev.luin.file.server.core.server.upload.header.CacheControl;
import dev.luin.file.server.core.server.upload.header.Location;
import dev.luin.file.server.core.server.upload.header.TusExtension;
import dev.luin.file.server.core.server.upload.header.TusMaxSize;
import dev.luin.file.server.core.server.upload.header.TusResumable;
import dev.luin.file.server.core.server.upload.header.TusVersion;
import dev.luin.file.server.core.server.upload.header.UploadOffset;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UploadResponseImpl implements UploadResponse
{
	HttpServletResponse response;

	@Override
	public void sendTusOptionsResponse()
	{
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		TusResumable.get().write(response);
		TusVersion.get().write(response);
		TusMaxSize.get().forEach(h -> h.write(response));
		TusExtension.get().write(response);
	}

	@Override
	public void sendFileInfoResponse(final FSFile file)
	{
		response.setStatus(HttpServletResponse.SC_CREATED);
		UploadOffset.of(file.getFileLength()).write(response);
		TusResumable.get().write(response);
		CacheControl.get().write(response);
	}

	@Override
	public void sendCreateFileResponse(final FSFile file, String uploadPath)
	{
		response.setStatus(HttpServletResponse.SC_CREATED);
		Location.of(uploadPath + file.getVirtualPath()).forEach(h -> h.write(response));
		TusResumable.get().write(response);
	}

	@Override
	public void sendUploadFileResponse(final FSFile file)
	{
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		UploadOffset.of(file.getLength()).write(response);
		TusResumable.get().write(response);
	}

	@Override
	public void sendDeleteFileResponse()
	{
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		TusResumable.get().write(response);
	}
}