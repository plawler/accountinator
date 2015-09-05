import play.api.libs.ws.WS
import play.api.libs.ws.ning.NingWSClient
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

trait TokenFixture {

  implicit val wsClient = NingWSClient()

  val encodedCreds = sys.env("STORMPATH_CREDS_ENCODED")

  val requestHeaders = // just a heads up. play 2.4 uses Seq((String, String))
    Seq(AUTHORIZATION -> s"Basic $encodedCreds",
      CONTENT_TYPE -> "application/x-www-form-urlencoded",
      ACCEPT -> "application/json")

  val tokenatorUri = "https://young-sea-8252.herokuapp.com/tokenator/api/v1/oauth/token/chorely-accounts-api?grant_type=client_credentials"

  val futureResponse = WS.clientUrl(tokenatorUri).withHeaders(requestHeaders: _*).post(AnyContentAsEmpty)
  val response = Await.result(futureResponse, 30 seconds)

  val bearerToken = (response.json \ "accessToken").as[String]
}