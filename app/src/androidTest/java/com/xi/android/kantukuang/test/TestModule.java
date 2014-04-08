package com.xi.android.kantukuang.test;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import java.io.IOException;


public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JsonFactory.class).to(JacksonFactory.class).in(Scopes.SINGLETON);
        bind(HttpTransport.class).to(MockHttpTransport.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    private JsonObjectParser provideJsonObjectParser(JsonFactory jsonFactory) {
        return new JsonObjectParser(jsonFactory);
    }

    @Provides
    @Singleton
    private HttpRequestInitializer provideHttpRequestInitializer(
            final JsonObjectParser jsonObjectParser) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setParser(jsonObjectParser);
            }
        };
    }

    @Provides
    @Singleton
    private HttpRequestFactory provideHttpRequestFactory(HttpTransport transport,
                                                         HttpRequestInitializer initializer) {
        return transport.createRequestFactory(initializer);
    }

    public static class MockHttpTransport extends com.google.api.client.testing.http.MockHttpTransport {
        private MockLowLevelHttpResponse mResponse;
        @Inject
        private MockLowLevelHttpRequest mRequest;

        @Override
        public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            mRequest.setResponse(mResponse);

            return mRequest;
        }

        public void setResponse(MockLowLevelHttpResponse mResponse) {
            this.mResponse = mResponse;
        }
    }
}
