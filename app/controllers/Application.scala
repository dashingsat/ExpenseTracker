package controllers

import com.tinkerpop.gremlin.java.GremlinPipeline
import datasource.ManageDataSource
import model.Expense
import play.api.Logger
import play.api.libs.json.{JsError, JsPath, Reads}
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.collection.JavaConversions._
import controllers.Authentication._
import synaticsugar.GremlinPipelineSugar._


object Application extends Controller {

  case class T1(amount:Double ,dateTime:String,name:String){
    def time = 3
  }

  implicit val expenseReads : Reads[Expense] = (

    (JsPath \ "amount").read[Double] and
    (JsPath \ "dateTime").read[String] and
      (JsPath \ "category").read[String] and
      (JsPath \ "subCategory").read[String] and
       (JsPath \ "need").read[String] and
        (JsPath \ "shortTerm").read[Boolean] and
          (JsPath \ "at").read[String] and
           (JsPath \ "shortDescription").read[String]

    )(Expense.apply _)



  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def lodgeExpense =  isUserConnected(Action(BodyParsers.parse.json)  {  implicit request =>

    val  expenseData = request.body.validate[Expense]
    expenseData.fold(
      errors => {
        BadRequest(Json.obj("status" ->"RequestParsingError", "message" -> JsError.toFlatJson(errors)))
      },

      expense => {

        implicit val headers = request.headers
        Ok("Expense lodge status:"+expense.save)
      }
    )

  })


  def getExpense = Action {

    Ok("ok,carry on")
  }

  def testGremlin = Action{

    ManageDataSource.getNonTransactionalInstance match{
      case Some(graph) => {

           Ok("see logs:"+flow(graph.getVertices("type" , "Place")).toList.toString)
      }

      case None => Ok("Graph instance not found")
    }

  }



}