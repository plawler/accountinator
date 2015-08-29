package models

import java.util.UUID

import play.api.libs.json.Json

/**
 * Created by paullawler on 2/22/15.
 */

case class PasswordInfo(hasher: String, password: String)

case class OAuth2Info(accessToken: String, tokenType: Option[String] = None,
                      expiresIn: Option[Int] = None, refreshToken: Option[String] = None)

case class Account( username: String,
                    firstName: String,
                    lastName: String,
                    email: String,
                    authentication: String,
                    provider: String,
                    passwordInfo: Option[PasswordInfo] = None,
                    oauth2Info: Option[OAuth2Info] = None,
                    _id: Option[UUID] = None )

object Account {

  implicit val oauthFormat = Json.format[OAuth2Info]
  implicit val passwordFormat = Json.format[PasswordInfo]
  implicit val accountFormat = Json.format[Account]

}

