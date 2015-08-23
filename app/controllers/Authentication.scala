package controllers

import services.authentication.{Token, AuthenticationService}

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

import com.google.inject.Inject

/**
 * Created by paullawler on 8/22/15.
 */
class Authentication @Inject() (authenticationService: AuthenticationService) extends Controller {

  implicit val tokenFormat = Json.format[Token]

  def token = Action.async { request =>
    val futureToken = authenticationService.retrieveToken(request)
    futureToken.map { token =>
      Ok(Json.toJson(token))
    }
  }

}
