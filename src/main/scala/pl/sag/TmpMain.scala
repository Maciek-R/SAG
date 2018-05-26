package pl.sag

import pl.sag.utils._

object TmpMain extends App{

  val categoriesLinks = XKomClient.getAllCategoriesLinks(XKomMainPage.toString)
  println(s"Number of categories links: ${categoriesLinks.size}")

  val categoriesToProductsLinks = categoriesLinks.par
    .map(categoryLink => (categoryLink, XKomClient.getAllProductsLinks(categoryLink))).toMap

  categoriesToProductsLinks.foreach{case (categoryLink, productLinks) =>
    println(categoryLink + " --- Number of products: " + productLinks.size)
  }
  println(s"All found products: " + categoriesToProductsLinks.flatMap(_._2).size)

  val products = categoriesToProductsLinks.flatMap{case (_, productLinks) => productLinks}
      .map(link => XKomClient.getProductInfo(link))
  println(s"All products with description got: " + products.size)
  //println(productsLinks.size)
  //val productsInfo = productsLinks.take(5).map(url => XKomClient.getProductInfo(XKomMainPage.toString+url))
  //productsInfo.zipWithIndex.foreach{case(info, index) => println(s"$index $info")}

  //val categoriesLinks.map(XKomMain.toString+HttpClient.getAllProductsLinks(_))
}
