package services

import models._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api.Play
import play.api.libs.json.Json
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.ReactiveMongoApi

import com.fasterxml.uuid.Generators
import reactivemongo.bson.BSONDocument

class MongoAccountService extends AccountService {

  val reactiveMongoApi = Play.current.injector.instanceOf[ReactiveMongoApi]

  def collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("accounts")

  override def createAccount(account: Account): Future[Account] = {
    val accountWithId = account.copy(_id = Some(Generators.timeBasedGenerator().generate()))
    for {
      isNoAccount <- findAccount(account.username).map(_.isEmpty)
      result <- collection.insert(accountWithId) if isNoAccount
    } yield result match {
      case ok if result.ok => accountWithId
      case error => throw new RuntimeException(error.message)
    }
  }

  override def findAccount(username: String): Future[Option[Account]] = {
    collection.find(Json.obj("username" -> username)).one[Account]
  }

  override def findAccount(email: String, provider: String): Future[Option[Account]] = {
    collection.find(Json.obj("email" -> email, "provider" -> provider)).one[Account]
  }

  // may want to just implement a saveAccount method for both POST and PUT
  override def updateAccount(account: Account): Future[Boolean] = {
    val selector = BSONDocument("_id" -> account._id.fold("")(_.toString))
    collection.update(selector, account) map (_.ok)
  }

}
