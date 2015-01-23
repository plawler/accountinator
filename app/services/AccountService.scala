package services

import java.util
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.fasterxml.uuid.Generators
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.api.ApiKeys
import com.stormpath.sdk.application.Application
import com.stormpath.sdk.client.{Client, Clients}
import com.stormpath.sdk.cache.Caches._

import play.Play
import play.api.Logger

import scala.collection.JavaConversions._

import play.api.Play.current

/**
 * Created By: paullawler
 */

case class ChorelyAccount(id: Option[UUID], firstName: String, lastName: String, email: String)

trait AccountService {
  def createAccount(account: ChorelyAccount): ChorelyAccount
  def findAccount(email: String): Option[ChorelyAccount]
  def deleteAccount(email: String)
}

object StormpathAccountService extends AccountService {

  val stormpathApiKeyId = Play.application.configuration.getString("stormpath.apiKey.id")
  val stormpathApiKeySecret = Play.application.configuration.getString("stormpath.apiKey.secret")
  val stormpathContextPath = Play.application.configuration.getString("stormpath.context")

  lazy val client: Client = {
    val apiKey = ApiKeys.builder().setId(stormpathApiKeyId).setSecret(stormpathApiKeySecret).build()

    Clients.builder()
      .setApiKey(apiKey)
      .setCacheManager(newCacheManager()
      .withDefaultTimeToLive(1, TimeUnit.DAYS)
      .withDefaultTimeToIdle(2, TimeUnit.HOURS)
      .withCache(forResource(classOf[Account])
      .withTimeToLive(1, TimeUnit.HOURS)
      .withTimeToIdle(30, TimeUnit.MINUTES))
      .build()
      ).build()
  }

  lazy val application = client.getResource(stormpathContextPath, classOf[Application])

  override def createAccount(account: ChorelyAccount): ChorelyAccount = {
    Logger.debug("Creating the account")

    val spAccount = client.instantiate(classOf[Account])
    spAccount.setGivenName(account.firstName)
    spAccount.setSurname(account.lastName)
    spAccount.setEmail(account.email)
    spAccount.setPassword("Ch@ngeM3")

    val customData = spAccount.getCustomData
    val accountId = Generators.timeBasedGenerator().generate()
    customData.put("id", accountId)

    application.createAccount(spAccount)
    account.copy(id = Some(accountId))
  }

  override def findAccount(email: String): Option[ChorelyAccount] = {
    Logger.debug("Finding the account")
    val queryParams = new util.HashMap[String, Object]()
    queryParams.put("email", email)
    try {
      val account = application.getAccounts(queryParams).head // http://alvinalexander.com/scala/how-to-convert-maps-scala-java
      Some(
        ChorelyAccount(
          Some(UUID.fromString(account.getCustomData.get("id").toString)),
                account.getGivenName,
                account.getSurname,
                account.getEmail)
      )
    } catch {
      case e: NoSuchElementException => None
    }
  }

  override def deleteAccount(email: String): Unit = {
    val queryParams = new util.HashMap[String, Object]()
    queryParams.put("email", email)
    try {
      application.getAccounts(queryParams).head.delete()
    } catch {
      case e: NoSuchElementException => Logger.debug(s"No account found with email $email")
    }
  }
}
