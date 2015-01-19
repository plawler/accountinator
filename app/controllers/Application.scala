package controllers

import java.util.UUID

import com.fasterxml.uuid.Generators
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
      val accountId = Generators.timeBasedGenerator().generate()
      val newAccount = ChorelyAccount(Some(accountId), account.firstName, account.lastName, account.username, account.email)
      Ok(Json.toJson(newAccount))
    }.recoverTotal(e => BadRequest("Error: " + JsError.toFlatJson(e)))
  }

  def get(id: String) = Action {
    Ok(Json.toJson(service.findAccount(UUID.fromString(id))))
  }

}