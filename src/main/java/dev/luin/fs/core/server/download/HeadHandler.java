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
package dev.luin.fs.core.server.download;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.luin.fs.core.file.FileSystem;
import dev.luin.fs.core.http.HttpException;
import dev.luin.fs.core.server.BaseHandler;
import dev.luin.fs.core.service.model.User;
import lombok.val;

class HeadHandler extends BaseHandler
{
	public HeadHandler(FileSystem fs)
	{
		super(fs);
	}

	@Override
	public void handle(final HttpServletRequest request, final HttpServletResponse response, User user) throws IOException
	{
		val path = request.getPathInfo();
		val fsFile = getFs().findFile(user,path).getOrElseThrow(() -> HttpException.notFound());
		new ResponseWriter(getFs(),response).setStatus200Headers(fsFile);
	}
}