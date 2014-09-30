package datasource

import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientGraph, OrientGraphFactory}
import play.api.Logger


/**
 * Created by kanhasatya on 8/17/14.
 */


object ManageDataSource {

  var factory : Option[OrientGraphFactory] = None
  var factoryNonTransactional : Option[OrientGraphFactory] = None

  def tx (body : => Unit)(implicit graph:OrientGraph) : Boolean = {
    try{
      Logger.info("Graph transaction starts")
      body
      Logger.info("About to commit")
      graph.commit
      Logger.info("Commit Successful:")
      true
    } catch{

       case e: OConcurrentModificationException => Logger.info("Suppressing the error for time being. It will be fixed in the release version of Orient"); true
       case e:Exception =>{

        Logger.info("Failed to commit transaction due to error :"+ e.getClass+":" + e.getMessage)
        graph.rollback
        false
        }

    }
    finally {
      graph.shutdown()
    }

  }


  def instantiate(uri:String, uriType:String) : (Option[OrientGraphFactory], Option[OrientGraphFactory])  = {

    factory  = uriType match {

      case "local" =>  Some(new OrientGraphFactory("plocal:"+uri).setupPool(1, 50))
      case "remote" =>  Some(new OrientGraphFactory("remote:"+uri).setupPool(1,50))
      case _ => None

    }

    factoryNonTransactional = uriType match {

      case "local" =>  Some(new OrientGraphFactory("plocal:"+uri).setTransactional(false).setupPool(1,50))
      case "remote" =>  Some(new OrientGraphFactory("remote:"+uri).setTransactional(false).setupPool(1,50))
      case _ => None

    }

    (factory,factoryNonTransactional)
  }

  def getInstance : Option[OrientGraph] = factory match {
    case Some(factory) => Some(factory.getTx)
    case _ => None
  }

  def getNonTransactionalInstance : Option[OrientGraphNoTx] = factoryNonTransactional match {
    case Some(factoryNonTransactional) => Some(factoryNonTransactional.getNoTx)
    case _ => None
  }

  def shutDown = {

     factory match {
       case Some(factory) => factory.close()
       case None => Logger.info("There is no factory to begin with")
     }

    factoryNonTransactional match {
      case Some(factory) => factory.close()
      case None => Logger.info("There is no factory to begin with")
    }
  }

}