package models

import java.util.UUID

import play.api.libs.json.Json

/**
 * Created by paullawler on 2/22/15.
 */
case class ChorelyAccount(username: String,
                          firstName: String,
                          lastName: String,
                          email: String,
                          authentication: String,
                          provider: String,
                          password: String,
                          _id: Option[UUID] = None)

object ChorelyAccount {

  implicit val accountFormat = Json.format[ChorelyAccount]

}

