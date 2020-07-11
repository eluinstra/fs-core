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
package org.bitbucket.eluinstra.fs.core.server.download;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
public class LoggingOutputStream extends FilterOutputStream
{
	transient Logger messageLog = LoggerFactory.getLogger(getClass());
	@NonNull
	Map<String,Seq<String>> properties;
	String charset;
	StringBuffer sb = new StringBuffer();

	public LoggingOutputStream(@NonNull final Map<String,Seq<String>> properties, @NonNull final OutputStream out)
	{
		this(properties,out,"UTF-8");
	}

	@Builder
	public LoggingOutputStream(@NonNull final Map<String,Seq<String>> properties, @NonNull final OutputStream out, @NonNull final String charset)
	{
		super(out);
		this.properties = properties;
		this.charset = charset;
	}

	@Override
	public void write(final int b) throws IOException
	{
		if (messageLog.isDebugEnabled())
			sb.append(b);
		out.write(b);
	}

	@Override
	public void write(@NonNull final byte[] b) throws IOException
	{
		if (messageLog.isDebugEnabled())
			sb.append(new String(b,charset));
		out.write(b);
	}

	@Override
	public void write(@NonNull final byte[] b, final int off, final int len) throws IOException
	{
		if (messageLog.isDebugEnabled())
			sb.append(new String(b,off,len,charset));
		out.write(b,off,len);
	}

	@Override
	public void close() throws IOException
	{
		val properties = this.properties.toStream()
				.map(t -> (t._1 != null ? t._1 + ": " : "") + t._2.mkString(","))
				.mkString("\n");
		messageLog.debug(">>>>\n" + properties + "\n" + sb.toString());
		super.close();
	}

}