package pl.sag

import pl.sag.utils.XKomParser.productName
import pl.sag.utils._

object TmpMain extends App{

  val xKomClient = new XKomClient
  val products = xKomClient.downloadRandomProducts(3)
  products.zipWithIndex.foreach{case (product, index) =>
    println(s"$index  ${product.title} ${product.imageUrl} ${product.linkPage} ${product.description}")}

}
