package pl.sag

import pl.sag.utils._

object TmpMain extends App{

  val xKomClient = new XKomClient(true)
  val products = xKomClient.downloadRandomProducts(20)
  products.zipWithIndex.foreach{case (product, index) =>
    println(s"$index  $product")}

}
