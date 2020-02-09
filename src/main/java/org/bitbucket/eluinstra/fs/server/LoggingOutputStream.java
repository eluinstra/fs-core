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
package org.bitbucket.eluinstra.fs.server;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

public class LoggingOutputStream extends FilterOutputStream
{
	protected transient Log messageLogger = LogFactory.getLog(getClass());
	private Map<String,List<String>> properties;
	private String charset;
	private StringBuffer sb = new StringBuffer();

	public LoggingOutputStream(Map<String,List<String>> properties, OutputStream out)
	{
		this(properties,out,"UTF-8");
		this.properties = properties;
	}

	public LoggingOutputStream(Map<String,List<String>> properties, OutputStream out, String charset)
	{
		super(out);
		this.charset = charset;
	}

	@Override
	public void write(int b) throws IOException
	{
		if (messageLogger.isDebugEnabled())
			sb.append(b);
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		if (messageLogger.isDebugEnabled())
			sb.append(new String(b,charset));
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if (messageLogger.isDebugEnabled())
			sb.append(new String(b,off,len,charset));
		out.write(b,off,len);
	}

	@Override
	public void close() throws IOException
	{
		String properties = this.properties.entrySet().stream()
		.map(e -> (e.getKey() != null ? e.getKey() + ": " : "") + StringUtils.collectionToCommaDelimitedString(e.getValue()))
		.collect(Collectors.joining("\n"));
		
		messageLogger.debug(">>>>\n" + properties + "\n" + sb.toString());
		super.close();
	}

}