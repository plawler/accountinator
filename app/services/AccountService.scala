package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * Created By: paullawler
 */
@ImplementedBy(classOf[MongoAccountService])
trait AccountService {
  def createAccount(account: Account): Future[Account]
  def findAccount(username: String): Future[Option[Account]]
  def findAccount(email: String, provider: String): Future[Option[Account]]
  def updateAccount(account: Account): Future[Boolean]
}

