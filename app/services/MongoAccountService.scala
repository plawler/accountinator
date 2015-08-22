package services

import models._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play
import play.api.libs.json.Json
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.ReactiveMongoApi

import com.fasterxml.uuid.Generators
import reactivemongo.api.{DB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

/**
  * Created by paullawler on 2/22/15.
 *
 */
//trait ReactiveMongoApi {
//  def driver: MongoDriver
//  def connection: MongoConnection
//  def db: DB
//}

class MongoAccountService extends AccountService {

  val reactiveMongoApi = Play.current.injector.instanceOf[ReactiveMongoApi]

  def collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("accounts")

  override def createAccount(account: ChorelyAccount): Future[ChorelyAccount] = {
    val accountWithId = account.copy(_id = Some(Generators.timeBasedGenerator().generate()))
    for (
      result <- collection.insert(accountWithId)
    ) yield result match {
      case ok if result.ok => accountWithId
      case error => throw new RuntimeException(error.message)
    }
  }

  override def findAccount(username: String): Future[Option[ChorelyAccount]] = {
    collection.find(Json.obj("username" -> username)).one[ChorelyAccount]
  }

  override def findAccount(email: String, provider: String): Future[Option[ChorelyAccount]] = {
    collection.find(Json.obj("email" -> email, "provider" -> provider)).one[ChorelyAccount]
  }

  // may want to just implement a saveAccount method for both POST and PUT
  override def updateAccount(account: ChorelyAccount): Future[Boolean] = {
    val selector = BSONDocument("_id" -> account._id.fold("")(_.toString))
    collection.update(selector, account) map (_.ok)
  }

}
