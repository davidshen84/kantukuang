package com.xi.android.kantukuang;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.xi.android.kantukuang.weibo.WeiboClient;

import java.util.Arrays;
import java.util.Collection;

public class KanTuKuangModule extends AbstractModule {

    private static final String CLIENT_ID = "3016222086";
    private static final String CLIENT_SECRET = "2f23dcc09bc9ebd1d2fa1d316d3cf87a";
    private static Injector injectorInstance = Guice.createInjector(new KanTuKuangModule());

    public static Injector getInjector() {
        return injectorInstance;
    }

    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("client_id"))
                .toInstance(CLIENT_ID);
        bind(String.class)
                .annotatedWith(Names.named("client_secret"))
                .toInstance(CLIENT_SECRET);
        bind(String.class)
                .annotatedWith(Names.named("token_server_url"))
                .toInstance("https://api.weibo.com/oauth2/access_token");
        bind(new TypeLiteralCollectionString())
                .annotatedWith(Names.named("scope"))
                .toInstance(Arrays.asList("all"));
        bind(String.class).annotatedWith(Names.named("authorization_server_encoded_url"))
                .toInstance("https://api.weibo.com/oauth2/authorize");

        bind(String.class).annotatedWith(Names.named("access token"))
                .toInstance("2.00uOPaHD1JlHSDcc83013405KD6O9D");

        bind(String.class).annotatedWith(Names.named("redirect uri")).toInstance("kantukuang.com/");
        bind(HttpTransport.class).to(NetHttpTransport.class);
        bind(JsonFactory.class).to(JacksonFactory.class);

        bind(Credential.AccessMethod.class).toInstance(BearerToken.queryParameterAccessMethod());

        bind(WeiboClient.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    private AuthorizationCodeFlow provideAuthorizationCodeFlow(Credential.AccessMethod method,
                                                               HttpTransport transport,
                                                               JsonFactory jsonFactory,
                                                               @Named("token_server_url") String tokenServerUrl,
                                                               HttpExecuteInterceptor clientAuthentication,
                                                               @Named("client_id") String clientId,
                                                               @Named("authorization_server_encoded_url") String authorizationServerEncodedUrl,
                                                               @Named("scope") Collection<String> scope) {


        return new AuthorizationCodeFlow
                .Builder(method, transport, jsonFactory, new GenericUrl(tokenServerUrl),
                         clientAuthentication,
                         clientId, authorizationServerEncodedUrl)
                .setScopes(scope)
                .build();


    }

    @Provides
    @Singleton
    private JsonObjectParser provideJsonObjectParser(JsonFactory jsonFactory) {
        return new JsonObjectParser(jsonFactory);
    }

    @Provides
    private HttpExecuteInterceptor provideHttpExecuteInterceptor(
            @Named("client_id") String clientId, @Named("client_secret") String clientSecret) {
        return new ClientParametersAuthentication(clientId, clientSecret);
    }

    private static class TypeLiteralCollectionString extends TypeLiteral<Collection<String>> {
    }
}
