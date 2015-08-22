package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created By: paullawler
 */
@ImplementedBy(classOf[MongoAccountService])
trait AccountService {
  def createAccount(account: ChorelyAccount): Future[ChorelyAccount]
  def findAccount(username: String): Future[Option[ChorelyAccount]]
  def findAccount(email: String, provider: String): Future[Option[ChorelyAccount]]
  def updateAccount(account: ChorelyAccount): Future[Boolean]
}

