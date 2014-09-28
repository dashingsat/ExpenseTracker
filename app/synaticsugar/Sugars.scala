package synaticsugar



/**
 * Created by kanhasatya on 9/9/14.
 */
object Sugars {

  implicit class Libsugars(obj:Any){

    def equalsTo(obj2:Any):Boolean = obj == obj2

    def notEqualsTo(obj2:Any):Boolean = obj != obj2

    def as[C](className:C):C  = obj.asInstanceOf[C]

    def is[C](className:C):Boolean = obj match {
      case obj:C => true
      case _ => false
    }




  }

  implicit class BooleanSugars(obj: => Boolean){

     def and(obj2 : => Boolean) = if(obj && obj2) true else false

     def or(obj2 : => Boolean) = if(obj || obj2) true else false
  }

  implicit  class  StringSugars(data:String) {

    def ifBlankOrNull: Boolean = if(data equalsTo  null or data.equalsIgnoreCase("")) true else false
  }






}
