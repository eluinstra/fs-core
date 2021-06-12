package dev.luin.file.server.core.server.download.header;

import java.time.Instant;

import dev.luin.file.server.core.ValueObject;
import dev.luin.file.server.core.server.download.DownloadRequest;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
public class IfRange implements ValueObject<String>
{
	private final static String HEADER_NAME = "If-Range";
	@NonNull
	String value;

	public static Option<IfRange> of(@NonNull final DownloadRequest request)
	{
		return of(request.getHeader(HEADER_NAME));
	}

	public static Option<IfRange> of(final String ifRange)
	{
		return Option.of(ifRange).map(IfRange::new);
	}

	private IfRange(@NonNull final String ifRange)
	{
		value = ifRange;
	}

	public boolean isValid(@NonNull final Instant lastModified)
	{
		if (value.startsWith("\""))
		{
			val hashCode = new Integer(ETag.getHashCode(lastModified)).toString();
			val etag = value.substring(1, value.length() - 1);
			return hashCode.equals(etag);
		}
		else
			return getTime(value).map(t -> lastModified.toEpochMilli() <= t).getOrElse(false);
	}

	static Option<Long> getTime(@NonNull final String value)
	{
		return Try.of(() -> HttpDate.IMF_FIXDATE.getDateFormat().parse(value).getTime())
				.orElse(Try.of(() -> HttpDate.RFC_850.getDateFormat().parse(value).getTime()))
				.orElse(Try.of(() -> HttpDate.ANSI_C.getDateFormat().parse(value).getTime()))
				.toOption();
	}

}
