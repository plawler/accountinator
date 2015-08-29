package services

import java.util
import java.util.concurrent.TimeUnit

import com.fasterxml.uuid.Generators
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.api.ApiKeys
import com.stormpath.sdk.application.Application
import com.stormpath.sdk.cache.Caches._
import com.stormpath.sdk.client.{Clients, Client}
import models.{Account => ChorelyAccount}
import play.Play
import play.api.Logger

import scala.collection.JavaConversions._
import scala.concurrent.Future


/**
 * Created by paullawler on 2/22/15.
 */
object StormpathAccountService extends AccountService {

  val stormpathApiKeyId = Play.application.configuration.getString("stormpath.apiKey.id")
  val stormpathApiKeySecret = Play.application.configuration.getString("stormpath.apiKey.secret")
  val stormpathContextPath = Play.application.configuration.getString("stormpath.context")

  lazy val client: Client = {
    val apiKey = ApiKeys.builder().setId(stormpathApiKeyId).setSecret(stormpathApiKeySecret).build()

    Clients.builder()
      .setApiKey(apiKey)
      .setCacheManager(
        newCacheManager()
          .withDefaultTimeToLive(1, TimeUnit.DAYS)
          .withDefaultTimeToIdle(2, TimeUnit.HOURS)
          .withCache(
            forResource(classOf[Account])
              .withTimeToLive(1, TimeUnit.HOURS)
              .withTimeToIdle(30, TimeUnit.MINUTES))
          .build()
      ).build()
  }

  lazy val application = client.getResource(stormpathContextPath, classOf[Application])

  override def createAccount(account: ChorelyAccount): Future[ChorelyAccount] = {
    Logger.debug("Creating the account")

    val spAccount = client.instantiate(classOf[Account])
    spAccount.setUsername(account.username)
    spAccount.setGivenName(account.firstName)
    spAccount.setSurname(account.lastName)
    spAccount.setEmail(account.email)
    spAccount.setPassword(account.passwordInfo.get.password)

    val customData = spAccount.getCustomData
    val accountId = Generators.timeBasedGenerator().generate()
    customData.put("authentication", account.authentication)
    customData.put("provider", account.provider)
    customData.put("password", account.passwordInfo.get.password) // ss requires the password and stormpath has no getter
    customData.put("id", accountId)

    application.createAccount(spAccount)
    Future.successful(account.copy(_id = Some(accountId)))
  }

  override def findAccount(email: String): Future[Option[ChorelyAccount]] = {
    Logger.debug("Finding the account")
    val queryParams = new util.HashMap[String, Object]() // http://alvinalexander.com/scala/how-to-convert-maps-scala-java
    queryParams.put("email", email)

    val account = application.getAccounts(queryParams).headOption.map { account =>
      ChorelyAccount(
        account.getUsername,
        account.getGivenName,
        account.getSurname,
        account.getEmail,
        account.getCustomData.get("authentication").toString,
        account.getCustomData.get("provider").toString,
        None, //account.getCustomData.get("password").toString,
        None //Some(UUID.fromString(account.getCustomData.get("id").toString))
      )
    }
    Future.successful(account)
  }

  override def findAccount(email: String, provider: String): Future[Option[ChorelyAccount]] = ???

  override def updateAccount(account: ChorelyAccount): Future[Boolean] = ???

  def deleteAccount(username: String): Unit = {
    val queryParams = new util.HashMap[String, Object]()
    queryParams.put("username", username)
    try {
      application.getAccounts(queryParams).head.delete()
    } catch {
      case e: NoSuchElementException => Logger.debug(s"No account found with username $username")
    }
  }

}
