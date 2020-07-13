--
-- Copyright 2020 E.Luinstra
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE client
(
	id								INTEGER					AUTO_INCREMENT PRIMARY KEY,
	name							VARCHAR(256)		NOT NULL UNIQUE,
	certificate				BLOB						NOT NULL
);

CREATE TABLE file
(
	virtual_path			VARCHAR(256)		NOT NULL PRIMARY KEY,
	real_path					VARCHAR(256)		NOT NULL,
	filename					VARCHAR(256)		NULL,
	content_type			VARCHAR(256)		NOT NULL,
	md5_checksum			VARCHAR(32)			NULL,
	sha256_checksum		VARCHAR(64)			NULL,
	timestamp					TIMESTAMP				NOT NULL DEFAULT CURRENT_TIMESTAMP,
	start_date				TIMESTAMP				NULL,
	end_date					TIMESTAMP				NULL,
	client_id					INTEGER					NOT NULL,
	file_length				BIGINT					NULL,
	file_type					SMALLINT				NULL,
	FOREIGN KEY (client_id) REFERENCES client(id)
);
