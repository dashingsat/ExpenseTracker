package controllers

import model.{UserSession, User, AuthenticationModule}
import play.api.libs.json._
import play.api.mvc._
import play.api.Logger

import synaticsugar.Sugars._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by kanhasatya on 9/9/14.
 */
object Authentication extends Controller{

  def authenticate = Action(BodyParsers.parse.json){
    implicit  request =>

    val userName:String = (request.body\"userName").as[String]
    val password = (request.body\"password").as[String]

     val authModule = new AuthenticationModule(userName,password)

     authModule.authenticate match {

       case Some(sessionId) => Ok(Json.obj("status" -> "true", "userSessionId" -> sessionId))
       case None => Ok(Json.obj("status" -> "false"))
     }


  }

  def register = Action(BodyParsers.parse.json){
     implicit request =>

       val userName:String = (request.body\"userName").as[String]
       val password = (request.body\"password").as[String]

       val user = User(userName,password)


       Ok(Json.obj("status" -> user.save))
  }

  def logOut =  Action(BodyParsers.parse.json){

    implicit request =>

      val userSessionId:String = (request.body\"userSessionId").as[String]

      val session = UserSession("",userSessionId)

      Ok(Json.obj("status" -> session.destroy))

  }

  def isUserConnected[A](action:Action[A]) = Action.async(action.parser){
    request => Logger.info("Trying to authenticate")

      request.headers.get("userSessionId") match{
        case Some(userSessionid) => {
           UserSession("",userSessionid).getUser(userSessionid) match {

             case Some(user) => action(request)

             case None =>  Future{Ok(Json.obj("status" -> "false","reason" -> "No valid user found for this session"))}
           }

        }

        case None =>  Future{Ok(Json.obj("status" -> "false","reason" -> "Please login to use the application"))}

      }

  }

}
