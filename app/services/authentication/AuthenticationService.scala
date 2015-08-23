package services.authentication

import java.util.concurrent.TimeUnit

import com.google.inject.ImplementedBy
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.api.ApiKeys
import com.stormpath.sdk.application.Application
import com.stormpath.sdk.cache.Caches._
import com.stormpath.sdk.client.{Clients, Client}
import com.stormpath.sdk.http.{HttpMethod, HttpRequests, HttpRequest}
import com.stormpath.sdk.oauth.AccessTokenResult
import play.api.{Logger, Play}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import play.api.Play.current

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.Try

/**
 * Created by paullawler on 8/22/15.
 */
case class Token(accessToken: String, tokenType: String, expiresIn: Int)

@ImplementedBy(classOf[StormpathAuthenticationService])
trait AuthenticationService {

  def retrieveToken[A](request: Request[A]): Try[Future[Token]]

}

class StormpathAuthenticationService extends AuthenticationService {

  val apiKeyId = Play.application.configuration.getString("application.apiKey.id").getOrElse("MISSING API ID")
  val apiKeySecret = Play.application.configuration.getString("application.apiKey.secret").getOrElse("MISSING API SECRET")
  val applicationContextPath = Play.application.configuration.getString("stormpath.context").getOrElse("MISSING APPLICATION CONTEXT PATH")

  lazy val client: Client = {

    val apiKey = ApiKeys.builder().setId(apiKeyId).setSecret(apiKeySecret).build()

    Clients.builder()
      .setApiKey(apiKey)
      .setCacheManager(newCacheManager()
      .withDefaultTimeToLive(1, TimeUnit.DAYS)
      .withDefaultTimeToIdle(2, TimeUnit.HOURS)
      .withCache(forResource(classOf[Account])
      .withTimeToLive(1, TimeUnit.HOURS)
      .withTimeToIdle(30, TimeUnit.MINUTES))
      .build()
      ).build()
  }

  lazy val application = client.getResource(applicationContextPath, classOf[Application])

  /**
   * In Java, when asking the Application to authenticate an API authentication request, the return type of a successful authentication request will vary based on the request headers. This includes:
   *
   * 1. ApiAuthenticationResult – Authorization header is present, with the Basic method and the base64 encoded API_KEY_ID:API_KEY_SECRET.
   * 2. AccessTokenResult – HTTP Method is POST. Authorization header is present, with the Basic method and the base64 encoded API_KEY_ID:API_KEY_SECRET. As part of the query or body of the request, the ‘grant_type’ is specified as ‘client_credentials’. Content-type is set to x-www-form-urlencoded.
   * 3. OauthAuthenticationResult – Authorization header is present, with the Bearer method and the OAuth 2.0 Access Token retrieved from the Stormpath SDK in a previous request.
   */

  def retrieveToken[A](request: Request[A]): Try[Future[Token]] = {
    Try {
      val result: AccessTokenResult = application.authenticateApiRequest(toStormPathRequest(request)).asInstanceOf[AccessTokenResult]
      val token = result.getTokenResponse
      Future.successful(Token(token.getAccessToken, token.getTokenType, token.getExpiresIn.toInt))
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