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
package org.bitbucket.eluinstra.fs.core.service;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.bitbucket.eluinstra.fs.core.service.model.User;

@WebService(targetNamespace="http://bitbucket.org/eluinstra/fs/core/1.0")
public interface UserService
{
	@WebResult(name="user")
	User getUser(@WebParam(name="id") long id) throws ServiceException;
	@WebResult(name="users")
	List<User> getUsers() throws ServiceException;
	@WebResult(name="id")
	long createUser(@WebParam(name="user") @XmlElement(required=true) User user) throws ServiceException;
	void updateUser(@WebParam(name="user") @XmlElement(required=true) User user) throws ServiceException;
	void deleteUser(@WebParam(name="id") long id) throws ServiceException;
}
