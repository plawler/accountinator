package controllers

import services.authentication.AuthenticationService

import play.api.mvc.Controller
import com.google.inject.Inject

import scala.concurrent.Future

class Authentication @Inject()(authenticationService: AuthenticationService) extends Controller with ActionBuilders {

  def testAuth = TrustedAction(authenticationService).async { implicit request =>
    Future.successful(Ok("Authenticated!"))
  }

}
