package pl.sag

import pl.sag.product.ProductInfo
import pl.sag.utils._

object TmpMain extends App{

  val xKomClient = new XKomClient
  val products = xKomClient.downloadRandomProducts(3)
  products.zipWithIndex.foreach{case (product, index) => println(s"$index  $product")}

  /*val categoriesLinks = XKomClient.getAllCategoriesLinks(XKomMainPage.toString)
  println(s"Number of categories links: ${categoriesLinks.size}")

  val categoriesToProductsLinks = categoriesLinks.par
    .map(categoryLink => (categoryLink, XKomClient.getAllProductsLinks(categoryLink))).toMap

  categoriesToProductsLinks.foreach{case (categoryLink, productLinks) =>
    println(categoryLink + " --- Number of products: " + productLinks.size)
  }
  println(s"All found products: " + categoriesToProductsLinks.flatMap(_._2).size)

  val products = categoriesToProductsLinks.flatMap{case (_, productLinks) => productLinks}
      .map(link =>
        ProductInfo(
          link,
          Some(XKomClient.getProductInfo(link))
        )
      )
  println(s"All products with description got: " + products.size)*/
}
