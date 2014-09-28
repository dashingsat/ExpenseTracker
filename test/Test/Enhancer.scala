package Test

import java.awt.event.{ActionEvent, ActionListener}

/**
 * Created by kanhasatya on 9/3/14.
 */
 object Enhancer {

  implicit def actionPerformedWrapper(func: (ActionEvent) => Unit) =
    new ActionListener { def actionPerformed(e:ActionEvent) = func(e) }

   def showMe = {

     val z = (x:Int) => x+1

     print(z(2))
   }

}
