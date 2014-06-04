package com.shen.xi.android.tut.test;

import android.app.Application;

import com.google.api.client.http.HttpRequestFactory;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.shen.xi.android.tut.TuTModule;
import com.shen.xi.android.tut.weibo.WeiboClient;

import junit.framework.TestCase;

import java.util.Collection;


public class GuiceInjectorConfigurationTest extends TestCase {
    private Injector injector;

    public void setUp() throws Exception {
        injector = Guice.createInjector(new TuTModule(new Application()));
    }

    public void testInjectWeiboClient() {
        WeiboClient client = injector.getInstance(WeiboClient.class);

        assertNotNull(client);
        assertTrue(WeiboClient.class.isInstance(client));
    }

    public void testInjectWeiboScope() {
        WeiboScope object = injector.getInstance(WeiboScope.class);

        assertNotNull(object);
        assertFalse(object.getScope().isEmpty());
    }

    public void testInjectQingRequestFactory() {
        HttpRequestFactory instance = injector.getInstance(Key.get(HttpRequestFactory.class, Names.named("qing request factory")));
        assertNotNull(instance);
    }

    private static class WeiboScope {
        private final Collection<String> mScope;

        @Inject
        public WeiboScope(@Named("scope") Collection<String> scope) {

            this.mScope = scope;
        }

        public Collection<String> getScope() {
            return mScope;
        }
    }
}
