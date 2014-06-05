package com.shen.xi.android.tut

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager

import com.google.api.client.auth.oauth2.{BearerToken, ClientParametersAuthentication, Credential}
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer, HttpTransport}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.{JsonFactory, JsonObjectParser}
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.inject.{AbstractModule, Guice, Injector, Provides, Scopes, Singleton, TypeLiteral}
import com.google.inject.name.Named
import com.nostra13.universalimageloader.core.{DisplayImageOptions, ImageLoader, ImageLoaderConfiguration}
import com.squareup.otto.Bus
import com.shen.xi.android.tut.weibo.WeiboClient
import java.io.IOException
import java.util.{Arrays => JArrays, Collection => JCollection}


object TuTModule {

  val WEIBO_CLIENT_ID = "3016222086"
  val WEIBO_CLIENT_SECRET = "2f23dcc09bc9ebd1d2fa1d316d3cf87a"
  val WEIBO_ACCESS_TOKEN = "2.00uOPaHD1JlHSDcc83013405KD6O9D"
  val ClassName = classOf[TuTModule].getName

  var injectorInstance: Injector = null

  def initialize(application: Application) =
    injectorInstance = Guice.createInjector(new TuTModule(application))

  def getInjector = injectorInstance match {
    case null => throw new IllegalStateException(String.format("%s is not initialized.", ClassName))
    case i: Injector => injectorInstance
  }

  private class TypeLiteralCollectionString extends TypeLiteral[JCollection[String]] {}

}

class TuTModule(application: Application) extends AbstractModule {

  import TuTModule._
  import com.google.inject.name.Names.named

  override protected def configure() = {
    bind(classOf[String])
      .annotatedWith(named("weibo client id"))
      .toInstance(WEIBO_CLIENT_ID)
    bind(classOf[String])
      .annotatedWith(named("weibo client secret"))
      .toInstance(WEIBO_CLIENT_SECRET)
    bind(classOf[String])
      .annotatedWith(named("weibo access token"))
      .toInstance(WEIBO_ACCESS_TOKEN)
    bind(classOf[String])
      .annotatedWith(named("token_server_url"))
      .toInstance("https://api.weibo.com/oauth2/access_token")
    bind(new TuTModule.TypeLiteralCollectionString())
      .annotatedWith(named("scope"))
      .toInstance(JArrays.asList("all"))
    bind(classOf[String])
      .annotatedWith(named("authorization_server_encoded_url"))
      .toInstance("https://api.weibo.com/oauth2/authorize")

    bind(classOf[HttpTransport]).to(classOf[NetHttpTransport])
    bind(classOf[JsonFactory]).to(classOf[JacksonFactory]).in(Scopes.SINGLETON)
    bind(classOf[Credential.AccessMethod]).toInstance(BearerToken.queryParameterAccessMethod())
    bind(classOf[WeiboClient]).in(Scopes.SINGLETON)

    // normal image resolution
    bind(classOf[DisplayImageOptions]).toInstance(
      new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .build()
    )

    bind(classOf[Bus]).in(Scopes.SINGLETON)
  }

  @Provides
  @Singleton
  private def provideImageLoader(configuration: ImageLoaderConfiguration) = {
    val instance = ImageLoader.getInstance()
    instance.init(configuration)

    instance
  }

  @Provides
  private def provideImageLoaderConfiguration(options: DisplayImageOptions) = new ImageLoaderConfiguration.Builder(application).defaultDisplayImageOptions(options).build()

  @Provides
  private def provideBitmapOptions() = new BitmapFactory.Options() {
    inSampleSize = 2
  }

  @Provides
  @Named("low resolution")
  private def provideDisplayImageOptions(options: BitmapFactory.Options) =
    new DisplayImageOptions.Builder()
      .decodingOptions(options)
      .cacheOnDisk(true)
      .build()

  @Provides
  @Singleton
  private def provideJsonObjectParser(jsonFactory: JsonFactory) = new JsonObjectParser(jsonFactory)

  @Provides
  @Singleton
  private def provideHttpExecuteInterceptor(@Named("weibo client id") clientId: String, @Named("weibo client secret") clientSecret: String) = new ClientParametersAuthentication(clientId, clientSecret)

  @Provides
  @Singleton
  private def provideLayoutInflater = application.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  @Provides
  @Singleton
  private def provideInputMethodManager = application.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]

  @Provides
  @Singleton
  private def provideCredential(accessMethod: Credential.AccessMethod, @Named("weibo access token") accessToken: String) = new Credential(accessMethod).setAccessToken(accessToken)

  @Provides
  @Singleton
  @Named("Weibo")
  private def provideWeiboRequestFactory(httpTransport: HttpTransport, jsonObjectParser: JsonObjectParser, credential: Credential) =
    httpTransport.createRequestFactory(new HttpRequestInitializer() {
      @throws[IOException]
      override def initialize(request: HttpRequest) = {
        credential.initialize(request)
        request.setParser(jsonObjectParser)
      }
    })

  @Provides
  @Singleton
  @Named("qing request factory")
  private def provideRequestFactory(httpTransport: HttpTransport, jsonObjectParser: JsonObjectParser) =
    httpTransport.createRequestFactory(new HttpRequestInitializer() {

      @throws[IOException]
      override def initialize(request: HttpRequest) {
        request.setParser(jsonObjectParser)
      }
    })

  @Provides
  @Singleton
  @Named("weibo request factory")
  private def provideRequestFactoryForWeiboClient(httpTransport: HttpTransport, jsonObjectParser: JsonObjectParser, credential: Credential) =
    httpTransport.createRequestFactory(new HttpRequestInitializer() {

      @throws[IOException]
      override def initialize(request: HttpRequest) {
        credential.initialize(request)
        request.setParser(jsonObjectParser)
      }
    })

  @Provides
  @Singleton
  private def provideDiscCacheAware(imageLoader: ImageLoader) = imageLoader.getDiskCache

}
