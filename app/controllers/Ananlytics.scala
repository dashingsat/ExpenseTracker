package controllers

import controllers.Authentication._
import play.api.libs.json.Json
import play.api.mvc.{BodyParsers, Action, Controller}

/**
 * Created by kanhasatya on 9/21/14.
 */
object Ananlytics extends Controller{

  def ExpensesBeyondCertainLimit(boundryAmount : Double) = isUserConnected(
     Action(BodyParsers.parse.json){
        implicit  request => Ok

     }

  )

}
