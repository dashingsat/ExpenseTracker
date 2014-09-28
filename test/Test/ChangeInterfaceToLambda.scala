package Test

/**
 * Created by kanhasatya on 9/3/14.
 */
import java.awt.event.ActionEvent
import javax.swing._
import Test.Enhancer._


object ChangeInterfaceToLambda  { /* extends JFrame("Hello Swing") {
  def showButtonMessage(msg: String)  =
    JOptionPane.showMessageDialog(null, String.format("""<html>Hello from <b>Scala</b>. Button %s pressed""", Array(msg)));*/

    def takeFunction(x:Int)(fun : (Int) => Int) :Boolean = {

      if (fun(x) > 10 ) true else false
    }

  def main(args: Array[String]) {
   /* setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    val button = new JButton("Click Me")

    def showPopup(e:ActionEvent) :Unit =  showButtonMessage(e.getActionCommand.toString)

    button.addActionListener((e:ActionEvent) => showButtonMessage(e.getActionCommand.toString))
    getContentPane add button
    pack
    setVisible(true)*/

     print (takeFunction(10)(x => x*2))

  }
}