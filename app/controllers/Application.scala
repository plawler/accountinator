package controllers

import models._
import play.api.Logger
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import services._

object Application extends Controller {

  val service: AccountService2 = MongoAccountService

  def index = Action {
    Ok(views.html.index("Welcome to the Chorely Accounts service!"))
  }

  def create = Action.async(parse.json) { implicit request =>
    request.body.validate[ChorelyAccount].map { account =>
      service.createAccount(account).map { ca =>
        Created(Json.toJson(ca))
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def getByUsername(username: String) = Action.async {
    service.findAccount(username).map {
      case Some(a) => Ok(Json.toJson(a))
      case _ =>
        Logger.info("No account found")
        BadRequest(Json.obj("message" -> s"No account found for username: $username"))
    }
  }

  def getByEmailAndProvider(email: String, provider: String) = Action.async {
    service.findAccount(email, provider).map {
      case Some(a) => Ok(Json.toJson(a))
      case _ =>
        Logger.info("No account found")
        BadRequest(Json.obj("message" -> s"No account found for email: $email and provider: $provider"))
    }
  }

  def update = Action.async(parse.json) { implicit request =>
    request.body.validate[ChorelyAccount].map { account =>
      service.updateAccount(account).map { result =>
        if (result) Ok("Account updated successfully") else Ok("No accounts were updated")
      } recover {
        case e: RuntimeException => BadRequest(e.getMessage)
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

}