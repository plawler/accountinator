package services

import models._
import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created By: paullawler
 */
trait AccountService2 {
  def createAccount(account: ChorelyAccount)(implicit ec: ExecutionContext): Future[ChorelyAccount]
  def findAccount(email: String)(implicit ec: ExecutionContext): Future[Option[ChorelyAccount]]
  def updateAccount(account: ChorelyAccount)(implicit ec: ExecutionContext): Future[Boolean]
}

trait AccountService {
  def createAccount(account: ChorelyAccount): ChorelyAccount
  def findAccount(email: String): Option[ChorelyAccount]
  def deleteAccount(email: String)
}

