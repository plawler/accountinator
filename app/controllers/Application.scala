package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import services.authentication.AuthenticationService
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import services._

class Application @Inject() (auth: AuthenticationService, accounts: AccountService)
  extends Controller with ActionBuilders {

  def index = Action {
    Ok("Your application is ready")
  }

  def create = TrustedAction(auth).async(parse.json) { implicit request =>
    request.body.validate[ChorelyAccount].map { account =>
      accounts.createAccount(account).map { ca =>
        Created(Json.toJson(ca))
      }
    }.getOrElse(Future.successful(BadRequest(Json.obj("message" -> "invalid json"))))
  }

  def getByUsername(username: String) = Action.async {
    accounts.findAccount(username).map {
      case Some(a) => Ok(Json.toJson(a))
      case _ =>
        Logger.info("No account found")
        BadRequest(Json.obj("message" -> s"No account found for username: $username"))
    }
  }

  def getByEmailAndProvider(email: String, provider: String) = Action.async {
    accounts.findAccount(email, provider).map {
      case Some(a) => Ok(Json.toJson(a))
      case _ =>
        Logger.info("No account found")
        BadRequest(Json.obj("message" -> s"No account found for email: $email and provider: $provider"))
    }
  }

  def update = Action.async(parse.json) { implicit request =>
    request.body.validate[ChorelyAccount].map { account =>
      accounts.updateAccount(account).map { result =>
        if (result) Ok("Account updated successfully") else Ok("No accounts were updated")
      } recover {
        case e: RuntimeException => BadRequest(e.getMessage)
      }
    }.getOrElse(Future.successful(BadRequest(Json.obj("message" -> "invalid json"))))
  }

}