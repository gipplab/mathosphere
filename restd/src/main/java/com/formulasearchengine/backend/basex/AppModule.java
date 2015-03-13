package com.formulasearchengine.backend.basex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.*;

import javax.inject.Named;
import java.nio.file.Paths;

@Module
public class AppModule {
	@Provides
	public SignatureKey signatureKey() {
		return new SignatureKey( "6527500837392160508 BaseXMathSearch 9ce54d33-6085-4281-ada9-26cd95a80a10 restd".getBytes( Charsets.UTF_8 ) );
	}

	@Provides
	@Named("restx.admin.password")
	public String restxAdminPassword() {
		return System.getProperty( "password" , " " );
	}

	@Provides
	public ConfigSupplier appConfigSupplier( ConfigLoader configLoader ) {
		// Load settings.properties in com.formulasearchengine.backend.basex package as a set of config entries
		return configLoader.fromResource( "com/formulasearchengine/backend/basex/settings" );
	}

	@Provides
	public CredentialsStrategy credentialsStrategy() {
		return new BCryptCredentialsStrategy();
	}

	@Provides
	public BasicPrincipalAuthenticator basicPrincipalAuthenticator(
		SecuritySettings securitySettings, CredentialsStrategy credentialsStrategy,
		@Named("restx.admin.passwordHash") String defaultAdminPasswordHash, ObjectMapper mapper ) {
		return new StdBasicPrincipalAuthenticator( new StdUserService<>(
			// use file based users repository.
			// Developer's note: prefer another storage mechanism for your users if you need real user management
			// and better perf
			new FileBasedUserRepository<>(
				StdUser.class, // this is the class for the User objects, that you can get in your app code
				// with RestxSession.current().getPrincipal().get()
				// it can be a custom user class, it just need to be json deserializable
				mapper,

				// this is the default restx admin, useful to access the restx admin console.
				// if one user with restx-admin role is defined in the repository, this default user won't be
				// available anymore
				new StdUser( "admin", ImmutableSet.<String>of( "*" ) ),

				// the path where users are stored
				Paths.get( "data/users.json" ),

				// the path where credentials are stored. isolating both is a good practice in terms of security
				// it is strongly recommended to follow this approach even if you use your own repository
				Paths.get( "data/credentials.json" ),

				// tells that we want to reload the files dynamically if they are touched.
				// this has a performance impact, if you know your users / credentials never change without a
				// restart you can disable this to get better perfs
				true ),
			credentialsStrategy, defaultAdminPasswordHash ),
			securitySettings );
	}
}
