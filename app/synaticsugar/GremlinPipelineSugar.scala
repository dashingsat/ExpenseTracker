package synaticsugar

import com.tinkerpop.blueprints.{Vertex, Element}

import com.tinkerpop.gremlin.java.GremlinPipeline

import  scala.collection.JavaConversions._


/**
 * Created by kanhasatya on 9/21/14.
 */
object GremlinPipelineSugar {

    object flow{
      def apply(start:AnyRef)= new GremlinPipeline(start)
    }


   implicit class pipeSugar( pipeline:GremlinPipeline[_ <: Any, _ <: Element]){

       def getFirst[M]:Option[M] =
       {
          val list  =  pipeline.toList.asInstanceOf[java.util.ArrayList[M]]

          if(list.size >= 1) Some(list.get(0)) else None

       }
   }



}
