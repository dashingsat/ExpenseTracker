package datasource

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.orientechnologies.orient.core.db.record.ODatabaseRecord
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientGraph}
import scala.collection.JavaConversions._

/**
 * Created by kanhasatya on 8/24/14.
 */
 object GraphSyntaticSugar {

   implicit class GraphEnhancer (val graph : OrientGraph){

     def  addCleanVertex : Vertex = graph.addVertex(null,Nil: _*)

     def addCleanVertexWithProperties(properties:Map[String,Any]) :Vertex =  {



        val vertex = graph.addCleanVertex

       for((name,data) <- properties){
         vertex.setProperty(name.toString,data)
       }
       vertex

     }

     def getCleanVertices(searchParameters:Map[String,AnyRef]) : List[Vertex] = {

       val keySearchKey:String = searchParameters.head._1
       val keySearchValue = searchParameters.head._2

       val mainVertices = graph.getVertices(keySearchKey,keySearchValue)

       val furtherSearchMap = searchParameters - keySearchKey

       implicit val currentDatabase:ODatabaseRecord = ODatabaseRecordThreadLocal.INSTANCE.get

       val vertices = mainVertices.iterator.toList.par.filter(_ matches furtherSearchMap).toList

       vertices

     }

   }

  implicit class VertexEnhancer(val vertex:Vertex){

    def matches(property:Map[String,AnyRef])(implicit currentDatabase:ODatabaseRecord):Boolean = {
      ODatabaseRecordThreadLocal.INSTANCE.set(currentDatabase.asInstanceOf[ODatabaseRecord])

      for((key,value) <- property){
        if(vertex.getProperty(key) != value)

          return false
      }
      true
    }
  }

  implicit class NoTxGraphEnhancer(val graph : OrientGraphNoTx){

    def getCleanVertices(searchParameters:Map[String,AnyRef]) : List[Vertex] = {

      val keySearchKey:String = searchParameters.head._1
      val keySearchValue = searchParameters.head._2

      val furtherSearchMap:Map[String,AnyRef] =  searchParameters.size match {
        case 1 => searchParameters
        case _ => searchParameters - keySearchKey

      }
      val mainVertices = graph.getVertices(keySearchKey,keySearchValue)
      implicit val currentDatabase:ODatabaseRecord = ODatabaseRecordThreadLocal.INSTANCE.get

      mainVertices.size match {
        case 0 => Nil
        case x if x>0 => mainVertices.iterator.toList.par.filter(_ matches furtherSearchMap).toList
        case _ => Nil
      }


    }
  }

}

