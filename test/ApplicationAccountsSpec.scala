import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import de.flapdoodle.embed.mongo.distribution.Version
import models.Account
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeApplication, FakeRequest}

/**
 * Created by paullawler on 9/4/15.
 */
class ApplicationAccountsSpec extends PlaySpec with OneAppPerSuite with MongoEmbedDatabase with BeforeAndAfter {

//  var mongoProps: MongodProps = null

  //  before {
  //    mongoProps = mongoStart(port = 27017, version = Version.V2_7_1)
  //  }
  //
  //  after {
  //    mongoStop(mongoProps)
  //  }

  implicit override lazy val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map("mongodb.uri" -> "mongodb://localhost:12345/accountinator")
    )

  "Application" should {

    "send 404 on a bad request" in {
      val result = route(FakeRequest(GET, "/boum")).get
      status(result) mustBe NOT_FOUND
    }

    "create a new account" in new TokenFixture {
      val accountJson = Json.toJson(Account("tester", "Test", "Tester", "tester@mailinator.com", "test", "test"))
      withEmbedMongoFixture(12345, Version.V3_0_5) { mongoProps =>
        val result = route(FakeRequest(POST, "/accountinator/api/v1/accounts",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), accountJson)).get
        status(result) mustEqual CREATED
      }
    }

    "find an account by username" in new TokenFixture {
      withEmbedMongoFixture(12345) { mongodProps =>
        val result = route(FakeRequest(GET, "/accountinator/api/v1/accounts/tester",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), AnyContentAsEmpty)).get
        status(result) mustEqual BAD_REQUEST
      }
    }

  }

}
