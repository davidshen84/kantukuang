package com.xi.android.kantukuang;

import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
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
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.otto.Bus;
import com.xi.android.kantukuang.weibo.WeiboClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class KanTuKuangModule extends AbstractModule {

    private static final String CLIENT_ID = "3016222086";
    private static final String CLIENT_SECRET = "2f23dcc09bc9ebd1d2fa1d316d3cf87a";
    private static final String ACCESS_TOKEN = "2.00uOPaHD1JlHSDcc83013405KD6O9D";
    private static final String ClassName = KanTuKuangModule.class.getName();
    private static Injector injectorInstance;
    private final Application mApplication;

    public KanTuKuangModule(Application application) {
        mApplication = application;
    }

    public static Injector getInjector() {
        if (injectorInstance == null) {
            throw new IllegalStateException(String.format("%s is not initialized.", ClassName));
        }

        return injectorInstance;
    }

    public static void initialize(Application application) {
        injectorInstance = Guice.createInjector(new KanTuKuangModule(application));
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
                .annotatedWith(Names.named("access token"))
                .toInstance(ACCESS_TOKEN);
        bind(String.class)
                .annotatedWith(Names.named("token_server_url"))
                .toInstance("https://api.weibo.com/oauth2/access_token");
        bind(new TypeLiteralCollectionString())
                .annotatedWith(Names.named("scope"))
                .toInstance(Arrays.asList("all"));
        bind(String.class).annotatedWith(Names.named("authorization_server_encoded_url"))
                .toInstance("https://api.weibo.com/oauth2/authorize");

        bind(String.class).annotatedWith(Names.named("redirect uri")).toInstance("kantukuang.com/");
        bind(HttpTransport.class).to(NetHttpTransport.class);
        bind(JsonFactory.class).to(JacksonFactory.class).in(Scopes.SINGLETON);

        bind(Credential.AccessMethod.class).toInstance(BearerToken.queryParameterAccessMethod());

        bind(WeiboClient.class).in(Scopes.SINGLETON);

        // normal image resolution
        bind(DisplayImageOptions.class).toInstance(
                new DisplayImageOptions.Builder()
                        .cacheOnDisc(true)
                        .build()
        );

        bind(Bus.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    private ImageLoader provideImageLoader(ImageLoaderConfiguration configuration) {
        ImageLoader instance = ImageLoader.getInstance();
        instance.init(configuration);

        return instance;
    }

    @Provides
    private ImageLoaderConfiguration provideImageLoaderConfiguration(DisplayImageOptions options) {
        return new ImageLoaderConfiguration.Builder(mApplication)
                .defaultDisplayImageOptions(options)
                .build();
    }

    @Provides
    private BitmapFactory.Options provideBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        return options;
    }

    @Provides
    @Named("low resolution")
    private DisplayImageOptions provideDisplayImageOptions(BitmapFactory.Options options) {
        return new DisplayImageOptions.Builder()
                .decodingOptions(options)
                .cacheOnDisc(true)
                .build();
    }

    @Provides
    @Singleton
    private JsonObjectParser provideJsonObjectParser(JsonFactory jsonFactory) {
        return new JsonObjectParser(jsonFactory);
    }

    @Provides
    @Singleton
    private HttpExecuteInterceptor provideHttpExecuteInterceptor(
            @Named("client_id") String clientId, @Named("client_secret") String clientSecret) {
        return new ClientParametersAuthentication(clientId, clientSecret);
    }

    @Provides
    @Singleton
    private LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) mApplication.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    @Singleton
    private InputMethodManager provideInputMethodManager() {
        return (InputMethodManager) mApplication.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides
    @Singleton
    private HttpRequestFactory provideRequestFactory(HttpTransport httpTransport,
                                                     final JsonObjectParser jsonObjectParser) {
        return httpTransport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setParser(jsonObjectParser);
            }
        });
    }

    @Provides
    @Singleton
    private Credential provideWeiboCredential(Credential.AccessMethod accessMethod, @Named("access token") String accessToken){
        return new Credential(accessMethod).setAccessToken(accessToken);
    }

    @Provides
    @Singleton
    @Named("weibo")
    private HttpRequestFactory provideRequestFactoryForWeiboClient(HttpTransport httpTransport,
                                                                   final JsonObjectParser jsonObjectParser,
                                                                   final Credential credential) {
        return httpTransport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                credential.initialize(request);
                request.setParser(jsonObjectParser);
            }
        });
    }

    @Provides
    @Singleton
    private DiscCacheAware provideDiscCacheAware(ImageLoader imageLoader) {
        return imageLoader.getDiscCache();
    }

    private static class TypeLiteralCollectionString extends TypeLiteral<Collection<String>> {
    }
}
