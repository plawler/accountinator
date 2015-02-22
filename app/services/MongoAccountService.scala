package services

import com.fasterxml.uuid.Generators
import models._
import play.api.Play.current
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.core.commands.LastError

// fixes "no json serializer found for type reactivemongo.bson.bsondocument"
import reactivemongo.api._
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by paullawler on 2/22/15.
 */
object MongoAccountService extends AccountService2 {

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("accounts")

  override def createAccount(account: ChorelyAccount)(implicit ec: ExecutionContext): Future[ChorelyAccount] = {
    val accountWithId = account.copy(_id = Some(Generators.timeBasedGenerator().generate()))
    for (
      result <- collection.save(accountWithId)
    ) yield result match {
      case ok if result.ok => accountWithId
      case error => throw new RuntimeException(error.message)
    }
  }

  override def findAccount(username: String)(implicit ec: ExecutionContext): Future[Option[ChorelyAccount]] = {
    collection.find(Json.obj("username" -> username)).one[ChorelyAccount]
  }

  // may want to just implement a saveAccount method for both POST and PUT
  override def updateAccount(account: ChorelyAccount)(implicit ec: ExecutionContext): Future[Boolean] = {
    val selector = BSONDocument("_id" -> account._id.fold("")(_.toString))
    for (
      result <- collection.update(selector, Json.toJson(account))
    ) yield result match {
      case ok if result.ok => result.updatedExisting
      case error => throw new RuntimeException(error.message)
    }
  }
}
