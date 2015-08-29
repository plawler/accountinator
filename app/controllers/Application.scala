package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.authentication.AuthenticationService
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import services._

import scala.util.{Success, Failure}

class Application @Inject() (auth: AuthenticationService, accounts: AccountService)
  extends Controller with ActionBuilders {

  def index = Action {
    Ok("Your application is ready")
  }

  def create = TrustedAction(auth).async(parse.json) { implicit request =>
    request.body.validate[Account].map { account =>
      accounts.createAccount(account) map { createdAccount =>
        Created(Json.toJson(createdAccount))
      } recover {
        case e: RuntimeException => Conflict("Unable to create the account. Username already taken perhaps?")
      }
    }.getOrElse(Future.successful(BadRequest(Json.obj("message" -> "invalid json"))))
  }

  def getByUsername(username: String) = TrustedAction(auth).async { implicit request =>
    accounts.findAccount(username).map {
      case Some(a) => Ok(Json.toJson(a))
      case _ =>
        Logger.info("No account found")
        BadRequest(Json.obj("message" -> s"No account found for username: $username"))
    }
  }

  def getByEmailAndProvider(email: String, provider: String) = TrustedAction(auth).async {
    accounts.findAccount(email, provider).map {
      case Some(a) => Ok(Json.toJson(a))
      case _ =>
        Logger.info("No account found")
        BadRequest(Json.obj("message" -> s"No account found for email: $email and provider: $provider"))
    }
  }

  def update = TrustedAction(auth).async(parse.json) { implicit request =>
    request.body.validate[Account].map { account =>
      accounts.updateAccount(account).map { result =>
        if (result) Ok("Account updated successfully") else Ok("No accounts were updated")
      } recover {
        case e: RuntimeException => Conflict(e.getMessage)
      }
    }.getOrElse(Future.successful(BadRequest(Json.obj("message" -> "invalid json"))))
  }

}