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

import org.bitbucket.eluinstra.fs.core.dao.ClientDAO;
import org.bitbucket.eluinstra.fs.core.service.model.Client;
import org.springframework.transaction.annotation.Transactional;

import io.vavr.collection.Seq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@AllArgsConstructor
@Transactional(transactionManager = "dataSourceTransactionManager")
public class FSAdminServiceImpl implements FSAdminService
{
	@NonNull
	ClientDAO clientDAO;

	@Override
	public Client getClient(final long id) throws FSServiceException
	{
		return clientDAO.findClient(id).getOrNull();
	}

	@Override
	public Seq<Client> getClients() throws FSServiceException
	{
		return clientDAO.selectClients();
	}

	@Override
	public long createClient(@NonNull final Client client) throws FSServiceException
	{
		return clientDAO.insertClient(client);
	}

	@Override
	public void updateClient(@NonNull final Client client) throws FSServiceException
	{
		clientDAO.updateClient(client);
	}

	@Override
	public void deleteClient(final long id) throws FSServiceException
	{
		clientDAO.deleteClient(id);
	}
}
