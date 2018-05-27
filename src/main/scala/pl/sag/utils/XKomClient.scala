package pl.sag.utils

import pl.sag.product.ProductInfo
import pl.sag.utils.HttpClient._

import scala.util.Random

object XKomClient {

  val productDescriptionStartMark = "Opis produktu"
  val productDescriptionEndMark = "Specyfikacja"

  val dataCategory = "<li data-category-id"
  val dataCategoryMark = "li"

  val productItem = "<div class=\"product-item"
  val productItemMark = "div"

  def getAllCategoriesLinks(pageUrl: String) = {
    XKomParser.getAllInfoInsideMarkWithText(
      HttpClient.downloadPageSource(pageUrl),
      dataCategoryMark,
      dataCategory
    ).map(XKomMainPage.toString+XKomParser.getLinkInText(_))
  }

  def getAllProductsLinks(categoryUrl: String) = {
    def getAllPageSources(page: Int): List[String] = {
      val pageSource = HttpClient.downloadPageSource(categoryUrl+"?page="+page+"&per_page=90")
        pageSource.indexOf("Niestety nie znaleźliśmy tego czego szukasz") match {
          case -1 => pageSource :: getAllPageSources(page + 1)
          case _ => Nil
        }
    }

    getAllPageSources(1).flatMap{
      pageSource => XKomParser.getAllInfoInsideMarkWithText(
        pageSource,
        productItemMark,
        productItem
      )
    }.map(XKomMainPage.toString+XKomParser.getLinkInText(_))
  }

  def getProductInfo(productUrl: String) = {
    val pageSource = HttpClient.downloadPageSource(productUrl)
    XKomParser.getProductInfo(pageSource)
  }

  def downloadRandomProducts(numberOfProducts: Int): List[ProductInfo] = {
    val categoriesLinks = XKomClient.getAllCategoriesLinks(XKomMainPage.toString)

    var categoriesToProductsLinks = scala.collection.mutable.HashMap[String, List[String]]()

    val r = new Random

    def getProductLinks(category: String) = {
      categoriesToProductsLinks.get(category) match {
        case Some(productLinks) => productLinks
        case None => {
          categoriesToProductsLinks += ((category, XKomClient.getAllProductsLinks(category)))
          categoriesToProductsLinks(category)
        }
      }
    }

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
          Some(XKomClient.getProductInfo(link))
        )
      ).toList
  }
}
