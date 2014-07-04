package com.shen.xi.android.tut.test

import java.io.IOException

import com.google.api.client.http.{HttpRequest, HttpRequestInitializer, HttpTransport}
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.json.{JsonFactory, JsonObjectParser}
import com.google.api.client.testing.http.{MockLowLevelHttpRequest, MockLowLevelHttpResponse}
import com.google.inject.{AbstractModule, Inject, Provides, Scopes, Singleton}


object TestModule {

  class MockHttpTransport extends com.google.api.client.testing.http.MockHttpTransport {
    private var mResponse: MockLowLevelHttpResponse = null
    @Inject
    private var mRequest: MockLowLevelHttpRequest = null

    @throws[IOException]
    override def buildRequest(method: String, url: String) = {
      mRequest.setResponse(mResponse)

      mRequest
    }

    def setResponse(mResponse: MockLowLevelHttpResponse) = this.mResponse = mResponse

  }

}

class TestModule extends AbstractModule {

  import com.shen.xi.android.tut.test.TestModule.MockHttpTransport


  override def configure() = {
    bind(classOf[JsonFactory]).to(classOf[JacksonFactory]).in(Scopes.SINGLETON)
    bind(classOf[HttpTransport]).to(classOf[MockHttpTransport]).in(Scopes.SINGLETON)
  }

  @Provides
  @Singleton
  private def provideJsonObjectParser(jsonFactory: JsonFactory) = new JsonObjectParser(jsonFactory)

  @Provides
  @Singleton
  private def provideHttpRequestInitializer(jsonObjectParser: JsonObjectParser) =
    new HttpRequestInitializer() {

      @throws[IOException]
      override def initialize(request: HttpRequest) =
        request.setParser(jsonObjectParser)

    }

  @Provides
  @Singleton
  private def provideHttpRequestFactory(transport: HttpTransport, initializer: HttpRequestInitializer) =
    transport.createRequestFactory(initializer)

}
