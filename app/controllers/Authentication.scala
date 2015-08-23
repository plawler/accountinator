package controllers

import services.authentication.{Token, AuthenticationService}

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

import com.google.inject.Inject

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by paullawler on 8/22/15.
 */
class Authentication @Inject() (authenticationService: AuthenticationService) extends Controller {

  implicit val tokenFormat = Json.format[Token]

  def token = Action.async { implicit request =>
    authenticationService.retrieveToken(request) match {
      case Success(futureToken) => futureToken.map(token => Ok(Json.toJson(token)))
      case Failure(e) => Future.successful(BadRequest(Json.obj("message" -> s"${e.getMessage}")))
    }
  }

}
