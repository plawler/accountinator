import java.util.UUID

import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import de.flapdoodle.embed.mongo.distribution.Version
import models.Account

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}

import play.api.{Logger, Play}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeApplication, FakeRequest}
import play.modules.reactivemongo.json._

import scala.None

//, ImplicitBSONHandlers._
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by paullawler on 9/4/15.
 */
class ApplicationAccountsSpec extends PlaySpec with OneAppPerSuite with MongoEmbedDatabase with BeforeAndAfter {

  implicit override lazy val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map("mongodb.uri" -> "mongodb://localhost:12345/accountinator")
    )

  "Application" should {

    "insert an account" in {
      val reactiveMongoApi = Play.current.injector.instanceOf[ReactiveMongoApi]

      withEmbedMongoFixture(12345, Version.V3_0_5) { mongoProps =>
        Logger.debug("Started embedded mongo")
        val collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("accounts")
        val account = Account("tester", "Test", "Tester", "tester@mailinator.com", "test", "test", _id = Some(UUID.randomUUID()))

        val result = Await.result(collection.insert(account), 2 minutes)
        result.ok mustBe true

        val found = Await.result(collection.find(Json.obj("username" -> "tester")).one[Account], 5 seconds)
        found.get.email mustEqual "tester@mailinator.com"

        Logger.debug("end of test")
      }
    }

    "create a new account" in new TokenFixture {
      val accountJson = Json.toJson(Account("tester", "Test", "Tester", "tester@mailinator.com", "test", "test"))
      withEmbedMongoFixture(12345, Version.V3_0_5) { mongoProps =>
        val result = route(FakeRequest(POST, "/accountinator/api/v1/accounts",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), accountJson)).get
        status(result) mustEqual CREATED
      }
    }

    "find an account by username that does not exist" in new TokenFixture {
      withEmbedMongoFixture(12345) { mongodProps =>
        val result = route(FakeRequest(GET, "/accountinator/api/v1/accounts/tester",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), AnyContentAsEmpty)).get
        status(result) mustEqual BAD_REQUEST
      }
    }

    "find an account by username" in new TokenFixture with MongoSupport {
      withEmbedMongoFixture(12345) { mongodProps =>
        val collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("accounts")
        val account = Account("tester", "Test", "Tester", "tester@mailinator.com", "test", "test", _id = Some(UUID.randomUUID()))
        Await.result(collection.insert(account), 60 seconds)

        val result = route(FakeRequest(GET, "/accountinator/api/v1/accounts/tester",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), AnyContentAsEmpty)).get
        status(result) mustEqual OK
      }
    }

    "find an account by email and provider" in new TokenFixture with MongoSupport {
      withEmbedMongoFixture(12345) { mongodProps =>
        val collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("accounts")
        val account = Account("tester", "Test", "Tester", "tester@mailinator.com", "test", "test", _id = Some(UUID.randomUUID()))
        Await.result(collection.insert(account), 60 seconds)

        val result = route(FakeRequest(GET, "/accountinator/api/v1/accounts/tester@mailinator.com/test",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), AnyContentAsEmpty)).get
        status(result) mustEqual OK
      }
    }

    "update an account" in new TokenFixture with MongoSupport {
      withEmbedMongoFixture(12345) { mongodProps =>
        val collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("accounts")
        val account = Account("tester", "Test", "Tester", "tester@mailinator.com", "test", "test", _id = Some(UUID.randomUUID()))
        Await.result(collection.insert(account), 60 seconds)

        val updatedAccount = account.copy(username = "updatedtester")
        val accountJson = Json.toJson(updatedAccount)

        val result = route(FakeRequest(PUT, "/accountinator/api/v1/accounts",
          FakeHeaders(Seq(AUTHORIZATION -> s"Bearer $bearerToken")), accountJson)).get
        status(result) mustEqual OK

        val update = Await.result(collection.find(Json.obj("username" -> updatedAccount.username)).one[Account], 60 seconds)
        update mustNot be (None)
        update match {
          case Some(u) => u.username mustEqual "updatedtester"
          case None => false
        }
      }
    }

  }

  trait MongoSupport {
    val reactiveMongoApi = Play.current.injector.instanceOf[ReactiveMongoApi]
  }

}
