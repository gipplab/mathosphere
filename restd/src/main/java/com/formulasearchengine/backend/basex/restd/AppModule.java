package com.formulasearchengine.backend.basex.restd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import org.mindrot.jbcrypt.BCrypt;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.*;

import javax.inject.Named;

@Module
public class AppModule {
    @Provides
    public SignatureKey signatureKey() {
         return new SignatureKey("3228323774839763938 fse-backend-restd 0390fce5-4811-4cd0-9aa7-ed843e0f6218 restd".getBytes(Charsets.UTF_8));
    }

    @Provides
    public ConfigSupplier appConfigSupplier(ConfigLoader configLoader) {
        // Load settings.properties in com.formulasearchengine.backend.basex package as a set of config entries
        return configLoader.fromResource("com/formulasearchengine/backend/settings");
    }

    @Provides
    public CredentialsStrategy credentialsStrategy() {
        return new BCryptCredentialsStrategy();
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(
            SecuritySettings securitySettings, CredentialsStrategy credentialsStrategy,
            @Named("restx.admin.passwordHash") String defaultAdminPasswordHash, ObjectMapper mapper) {

        String pwh = BCrypt.hashpw( Hashing.md5().hashString(AppServer.adminPassword,Charsets.UTF_8).toString() , BCrypt.gensalt() );
        return new StdBasicPrincipalAuthenticator(new StdUserService<>(
            new SimpleUserRepository<>(StdUser.class, new StdUser("admin", ImmutableSet.<String>of("*")), "admin", pwh    ),

                credentialsStrategy, defaultAdminPasswordHash),
                securitySettings);
    }
}
