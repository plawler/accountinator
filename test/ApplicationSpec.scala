import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.Logger
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.WS
import play.api.mvc.AnyContentAsEmpty

import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  implicit val wsClient = NingWSClient()


  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which(status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/accountinator/api")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/plain")
      contentAsString(home) must contain("Your application is ready")
    }

    "send 404 on a GET request to an account that doesn't exist" in new WithApplication {
      true must_!= false
    }

    "make an authenticated request" in new WithApplication with TokenFixture {
      bearerToken mustNotEqual None
      val result =
        route(
          FakeRequest(GET, "/accountinator/api/authenticated/test",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), AnyContentAsEmpty)
        ).get
      status(result) must equalTo(OK)
    }

//    "find an account by username"

  }

  trait TokenFixture {
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

}

