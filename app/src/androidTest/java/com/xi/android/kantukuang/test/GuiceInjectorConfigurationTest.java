package com.xi.android.kantukuang.test;

import android.app.Application;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;

import junit.framework.TestCase;

import java.util.Collection;


public class GuiceInjectorConfigurationTest extends TestCase {
    private Injector injector;

    public void setUp() throws Exception {
        injector = Guice.createInjector(new KanTuKuangModule(new Application()));
    }

    public void testInjectWeiboClient() {
        WeiboClient client = injector.getInstance(WeiboClient.class);

        assertNotNull(client);
        assertTrue(WeiboClient.class.isInstance(client));
    }

    public void testInjectAuthorizationCodeFlow() {
        AuthorizationCodeFlow object = injector.getInstance(AuthorizationCodeFlow.class);
        assertNotNull(object);
        assertFalse(object.getScopes().isEmpty());
    }

    public void testInjectWeiboScope() {
        WeiboScope object = injector.getInstance(WeiboScope.class);

        assertNotNull(object);
        assertFalse(object.getScope().isEmpty());
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
