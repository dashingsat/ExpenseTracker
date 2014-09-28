package model

import com.tinkerpop.blueprints.{Element, Vertex}
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import datasource.GraphSyntaticSugar._
import datasource.ManageDataSource
import datasource.ManageDataSource._
import play.api.Logger
import play.api.mvc.Headers
import synaticsugar.GremlinPipelineSugar._

//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 * Created by kanhasatya on 8/24/14.
 */


case class Expense(amount: Double, dateTime: String, category: String, subCategory: String, need: String, shortTerm: Boolean,at:String,shortDescription:String) {


  def save(implicit headers:Headers): Boolean = {

    val saveStatus = ManageDataSource.getInstance match {

      case Some(graphObject) => {

        Logger.info("Got the graph object")

        implicit val graph: OrientGraph = graphObject

        def saveExpenseNode: Option[Vertex] = Some(graph.addCleanVertexWithProperties(getPropertiesMap))

        def addExpenseNodeToACategory(expenseNode: Vertex, categoryName: String, subCategory: String): Option[Boolean] = {

          def getCategoryVertex(categoryName: String, subCategoryName: String): Option[Vertex] = {
            val category: Category = Category(categoryName, subCategoryName)
            category.getNode
          }

          getCategoryVertex(categoryName, subCategory) match {
            case Some(categoryNode) => {
              Logger.info("Got category")
              expenseNode.addEdge("for", categoryNode)
              Some(true)
            }
            case None => Logger.info("No category found or added"); None

          }

        }

        def addPlaceToAnExpense(expenseNode:Vertex,at:String) : Option[Boolean] = {

            def getPlaceNode(at:String) : Option[Vertex] = new Place(at).getNode

            getPlaceNode(at) match {
              case Some(node) =>  {
                expenseNode.addEdge("at",node)
                Some(true)
              }
              case None => None

            }

        }

        def addExpenseToTheUser(expenseNode:Vertex) : Option[Boolean] = {


          val userNode =   User.getUserNode

            userNode match {

              case Some(node) => node.addEdge("spent",expenseNode) ; Some(true)
              case None => Logger.info("Didn't get the user node") ; None
            }

        }

        val saveNode: Option[Boolean] = Some(
          tx {
            val softSave = for {
              expenseNode <- saveExpenseNode
              status <- addExpenseNodeToACategory(expenseNode, category, subCategory)
              placeSaveStatus <- addPlaceToAnExpense(expenseNode,at)
              expenseUserAdd <- addExpenseToTheUser(expenseNode)
            } yield expenseUserAdd

            softSave match {
              case Some(status) => if (status == false) throw new Exception("Hard luck..There was an issue saving the expense node") else Logger.info("Soft save successful")
              case None => throw new Exception("Hard luck..There was an issue saving the expense node")
            }

          }
        )
        Logger.info("Real save status:"+saveNode.get)
        saveNode getOrElse false

      }

      case None => false
    }

    saveStatus

  }

  def getPropertiesMap: Map[String, Any] = Map("amount" -> amount, "dateTime" -> dateTime, "type" -> "Expense", "need" -> need, "shortTerm" -> shortTerm ,"shortDescription" -> shortDescription)
}

case class Category(name: String, subCategory: String, var limit: Double) {

  def getNode(implicit headers:Headers): Option[Vertex] = {
    val graph = ManageDataSource.getNonTransactionalInstance

    graph match {
      case Some(graph) => {
        Logger.info("Category searches begins with name :"+ name + "and type:"+"Category" )
        val vertexList = graph.getCleanVertices(Map("name" -> name, "type" -> "Category"))
        if (vertexList == Nil) {
          Logger.info("Could not get category node hence the node will be saved first and then retrieved" )
          save
          getNode
        }
        else {Logger.info("Category object found ..About to link the expense with the category") ; Some(vertexList head)}
      }
      case None => None
    }
  }

  def save(implicit headers:Headers): Option[Boolean] = ManageDataSource.getInstance match {
    case Some(graphObject) => {

      implicit val graph = graphObject

      val softSave = tx {
        try{
          val userNode = User.getUserNode
          userNode match{
            case Some(node) => node.addEdge("spendsFor",graph.addCleanVertexWithProperties(getPropertiesMap))
            case None => Logger.info("No user found for category to be added") ; throw new Exception("No user found for category to be added")
          }

        }
       catch{
         case e:Exception =>{
           Logger.info("Category Ops failed due to error :"+ e.getMessage)
           throw new Exception(e.getMessage)
         }
       }

      }
      Some(softSave)
    }
    case None => None

  }


  def setLimit(limit: Double): Boolean = {

    this.limit = limit
    true
  }

  def getName = name

  def getPropertiesMap: Map[String, Any] = if (limit > 0) Map("name" -> name, "type" -> "Category", "subCategory" ->subCategory, "limit" -> limit) else Map("name" -> name, "type" -> "Category", "subCategory" ->subCategory,"limit" -> -1)


}

object Category {
  def apply(categoryName: String, subCategory: String): Category = new Category(categoryName, subCategory, -1)

  def apply(name: String) = new Category(name, "", -1)


}

case class Place(name : String) {

  def save(implicit headers:Headers) : Boolean = {
     ManageDataSource.getInstance match{
       case Some(graphNode) => {

         implicit val graph = graphNode
         tx{
           val userNode = User.getUserNode
           userNode match{
             case Some(node) => node.addEdge("spendsAt",graph.addCleanVertexWithProperties(getProperties))
             case None => Logger.info("Didn't find the user to save the place for"); throw new Exception("Didn't find the user to save the place for")

           }

         }

       }

       case None => false

     }

  }


  def getProperties :Map[String,Any]  = Map("name" -> name ,"type" -> "Place")

  def getNode(implicit headers:Headers) : Option[Vertex] = {

    ManageDataSource.getNonTransactionalInstance match {
      case Some(graph) => {


       // val nodeList = new GremlinPipeline(graph.getVertices("type" , "Place")).has("name" ,name).toList.asInstanceOf[java.util.ArrayList[Vertex]]
         val categoryNode = flow(graph.getVertices("type" , "Place")).has("name" ,name).getFirst[Vertex]

        categoryNode match{

         // case nodeList:java.util.ArrayList[Vertex] if nodeList.size() >= 1 => Logger.info("Got the place node") ; Some(nodeList.get(0))
          case Some(node) => Logger.info("Got the place node") ; Some(node)

          case _ => {

            Logger.info(" About to save place node")
            save
            Logger.info("Place node now saved")
            getNode
          }
        }

      }

      case None => None
    }
  }
}

