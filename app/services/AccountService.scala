package services

import java.util.UUID

import play.api.Logger

/**
 * Created By: paullawler
 */

case class ChorelyAccount(id: Option[UUID], firstName: String, lastName: String, username: String, email: String)

trait AccountService {
  def createAccount(account: ChorelyAccount)
  def findAccount(id: UUID): Option[ChorelyAccount]
}

object StormpathAccountService extends AccountService {

  override def createAccount(account: ChorelyAccount): Unit = Logger.debug("Creating the account")

  override def findAccount(id: UUID): Option[ChorelyAccount] = {
    Logger.debug("Finding the account")
    Some(ChorelyAccount(Some(id), "Paul", "Lawler", "pelawler", "paul.lawler@gmail.com"))
  }

}
