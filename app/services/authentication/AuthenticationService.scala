package services.authentication

import java.util.concurrent.TimeUnit

import com.google.inject.ImplementedBy
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.api.ApiKeys
import com.stormpath.sdk.application.Application
import com.stormpath.sdk.cache.Caches._
import com.stormpath.sdk.client.{Clients, Client}
import com.stormpath.sdk.http.{HttpMethod, HttpRequests, HttpRequest}
import com.stormpath.sdk.oauth.OauthAuthenticationResult
import play.api.Play
import play.api.mvc.Request
import play.api.Play.current

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.Try

case class Token(accessToken: String, tokenType: String, expiresIn: Int)

@ImplementedBy(classOf[StormpathAuthenticationService])
trait AuthenticationService {
  def authenticate[A](request: Request[A]): Try[Future[Account]]
}

class StormpathAuthenticationService extends AuthenticationService {

  val apiKeyId = Play.application.configuration.getString("application.apikey.id").getOrElse("MISSING API ID")
  val apiKeySecret = Play.application.configuration.getString("application.apikey.secret").getOrElse("MISSING API SECRET")
  val applicationContextPath = Play.application.configuration.getString("stormpath.application.href").getOrElse("MISSING APPLICATION HREF")

  lazy val client: Client = {

    val apiKey = ApiKeys.builder().setId(apiKeyId).setSecret(apiKeySecret).build()

    Clients.builder()
      .setApiKey(apiKey)
      .setCacheManager(newCacheManager()
                        .withDefaultTimeToLive(1, TimeUnit.DAYS)
                        .withDefaultTimeToIdle(2, TimeUnit.HOURS)
                        .withCache(forResource(classOf[Account])
                                    .withTimeToLive(1, TimeUnit.HOURS)
                                    .withTimeToIdle(30, TimeUnit.MINUTES)
                                  ).build())
      .build()
  }

  lazy val application = client.getResource(applicationContextPath, classOf[Application])

  def authenticate[A](request: Request[A]): Try[Future[Account]] = {
    Try{
      val result: OauthAuthenticationResult = application.authenticateOauthRequest(toStormPathRequest(request)).execute()
      Future.successful(result.getAccount)
    }
  }

  private def toStormPathRequest[A](request: Request[A]): HttpRequest = {
    val headers = request.headers.toMap.mapValues(v => v.toArray)
    HttpRequests.method(HttpMethod.fromName(request.method))
      .headers(headersWithAuthorization(request))
      .queryParameters(request.rawQueryString)
      .build()
  }

  private def headersWithAuthorization[A](request: Request[A]): Map[String, Array[String]] = {
    request.getQueryString("api_key") match {
      case Some(key) => (request.headers.toMap ++ Map("Authorization" -> Seq("Bearer " + key))).mapValues(v => v.toArray)
      case None => request.headers.toMap.mapValues(v => v.toArray)
    }
  }

}