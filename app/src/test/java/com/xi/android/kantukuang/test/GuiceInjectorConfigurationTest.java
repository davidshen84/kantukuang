package com.xi.android.kantukuang.test;

import android.app.Application;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricGradleTestRunner.class)
public class GuiceInjectorConfigurationTest {
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new KanTuKuangModule(new Application()));
    }

    @Test
    public void testInjectWeiboClient() {
        WeiboClient client = injector.getInstance(WeiboClient.class);

        assertNotNull(client);
        assertTrue(WeiboClient.class.isInstance(client));
    }

    @Test
    public void testInjectAuthorizationCodeFlow() {
        AuthorizationCodeFlow object = injector.getInstance(AuthorizationCodeFlow.class);
        assertNotNull(object);
        assertFalse(object.getScopes().isEmpty());
    }

    @Test
    public void testInjectWeiboScope() {
        WeiboScope object = injector.getInstance(WeiboScope.class);

        assertNotNull(object);
        assertFalse(object.getScope().isEmpty());
    }

    private static class WeiboScope {
        private Collection<String> mScope;

        @Inject
        public WeiboScope(@Named("scope") Collection<String> scope) {

            this.mScope = scope;
        }

        public Collection<String> getScope() {
            return mScope;
        }
    }
}
