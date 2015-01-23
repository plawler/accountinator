package controllers

import play.api.Logger
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import services.{StormpathAccountService, AccountService, ChorelyAccount}

object Application extends Controller {

  implicit val fmtAccount = Json.format[ChorelyAccount]

  val service: AccountService = StormpathAccountService

  def index = Action {
    Ok(views.html.index("Welcome to the Chorely Accounts service!"))
  }

  def create = Action(parse.json) { request =>
    request.body.validate[ChorelyAccount].map { account =>
      val newAccount = ChorelyAccount(None, account.firstName, account.lastName, account.email)
      service.createAccount(newAccount) match {
        case ca: ChorelyAccount => Ok(Json.toJson(ca))
      }
    }.recoverTotal(e => BadRequest("Error: " + JsError.toFlatJson(e)))
  }

  def get(email: String) = Action {
    service.findAccount(email) match {
      case Some(account) => Ok(Json.toJson(account))
      case None => BadRequest(s"No account found for email: $email")
    }
  }

  def delete(email: String) = Action {
    Logger.debug("Implement account removal")
    service.deleteAccount(email)
    Ok
  }

}