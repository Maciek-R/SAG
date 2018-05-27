package pl.sag.utils

import pl.sag.product.ProductInfo
import pl.sag.utils.HttpClient._

import scala.util.Random

class XKomClient {
  val categoriesLinks = getAllCategoriesLinks(XKomMainPage.toString)
  var categoriesToProductsLinks = scala.collection.mutable.HashMap[String, List[String]]()
  val r = new Random

  private def getAllCategoriesLinks(pageUrl: String) = {
    XKomParser.getAllInfoInsideMarkWithText(
      HttpClient.downloadPageSource(pageUrl),
      XKomParser.dataCategoryMark,
      XKomParser.dataCategory
    ).map(XKomMainPage.toString+XKomParser.getLinkInText(_))
  }

  private def getAllProductsLinks(categoryUrl: String) = {
    def getAllPagesSources(page: Int): List[String] = {
      val pageSource = HttpClient.downloadPageSource(categoryUrl+"?page="+page+"&per_page=90")
        pageSource.indexOf("Niestety nie znaleźliśmy tego czego szukasz") match {
          case -1 => pageSource :: getAllPagesSources(page + 1)
          case _ => Nil
        }
    }

    categoriesToProductsLinks.get(categoryUrl) match {
      case Some(productLinks) => productLinks
      case None => {
        val allCategoryProducts = getAllPagesSources(1).flatMap{
          pageSource => XKomParser.getAllInfoInsideMarkWithText(
            pageSource,
            XKomParser.productItemMark,
            XKomParser.productItem
          )
        }.map(XKomMainPage.toString+XKomParser.getLinkInText(_))
        categoriesToProductsLinks += (categoryUrl -> allCategoryProducts)
      }
    }
    categoriesToProductsLinks(categoryUrl)
  }

  private def getProductInfo(productUrl: String) = {
    val pageSource = HttpClient.downloadPageSource(productUrl)
    XKomParser.getProductInfo(pageSource)
  }

  private def getProductLinks(category: String) = {
    categoriesToProductsLinks.get(category) match {
      case Some(productLinks) => productLinks
      case None => {
        categoriesToProductsLinks += (category -> getAllProductsLinks(category))
        categoriesToProductsLinks(category)
      }
    }
  }

  def downloadRandomProducts(numberOfProducts: Int): List[ProductInfo] = {
    val randomCategories = for {
      i <- 0 until numberOfProducts
      category = categoriesLinks(r.nextInt(categoriesLinks.length))
    } yield category

    val randomProductsLinksFromCategories = for {
      category <- randomCategories
      productLinks = getProductLinks(category)
      randomProductLink = productLinks(r.nextInt(productLinks.length))
    } yield randomProductLink

    randomProductsLinksFromCategories
      .map(link =>
        ProductInfo(
          link,
          Some(getProductInfo(link))
        )
      ).toList
  }
}
