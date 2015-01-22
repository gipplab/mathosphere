/*
    Copyright 2011, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.formulasearchengine.backend.basex.restd;

import com.formulasearchengine.backend.basex.restd.rest.BasexController;
import org.restexpress.RestExpress;
import org.restexpress.util.Environment;

import java.util.Properties;

public class Configuration
	extends Environment {
	private static final String PATH_PROPERTY = "path";
	private static final String PORT_PROPERTY = "port";
	private static final String PASSWORD_PROPERTY = "password";

	private int port;
	private String path;

	public String getPassword () {
		return password;
	}

	private String password;

	private BasexController basexController = new BasexController();

	@Override
	protected void fillValues (Properties p) {
		this.path = p.getProperty( PATH_PROPERTY, "/tmp" );
		this.password = p.getProperty( PASSWORD_PROPERTY, "2015" );
		this.port = Integer.parseInt( p.getProperty( PORT_PROPERTY, String.valueOf( RestExpress.DEFAULT_PORT ) ) );
	}


	public int getPort () {
		return port;
	}

	public String getPath () {
		return path;
	}

	public BasexController getBasexController () {
		return basexController;
	}

}
