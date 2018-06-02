package pl.sag.utils

import java.io.{File, PrintWriter}

import pl.sag.Logger.{LogLevel, log}
import pl.sag.product.ProductInfo

import scala.io.Source
import scala.util.Random

class XKomClient(var workLocally: Boolean) {

  val categoriesLinks = getAllCategoriesLinks(XKomMainPage.toString)
  var categoriesToProductsLinks = scala.collection.mutable.HashMap[String, List[String]]()
  val r = new Random

  private def getAllCategoriesLinks(pageUrl: String) = {
    if (workLocally)
      getAllCategoriesLinksLocally(pageUrl)
    else
      getAllCategoriesLinksRemotely(pageUrl)
  }

  private def getAllCategoriesLinksLocally(pageUrl: String) = {
    try {
      Source.fromFile(FileManager.linksFile)
        .getLines()
        .map(_.split(" "))
        .map(_.head)
        .toList
    }
    catch {
      case e: Exception => {
        log("Can't get categories locally. Switching to remote mode.", LogLevel.WARNING)
        workLocally = false
        getAllCategoriesLinksRemotely(pageUrl)
      }
    }
  }

  private def getAllCategoriesLinksRemotely(pageUrl: String) = {
    XKomParser.getAllInfoInsideMarkWithText(
      HttpClient.downloadPageSource(pageUrl),
      XKomParser.dataCategoryMark,
      XKomParser.dataCategory
    ).map(XKomMainPage.toString + XKomParser.getLinkInText(_))
  }

  private def getAllProductsLinks(categoryUrl: String) = {

    def getAllPagesSourcesRemotely(): List[String] = {
      def getAllPagesSourcesRemotelyIter(page: Int): List[String] = {
        val pageSource = HttpClient.downloadPageSource(categoryUrl + "?page=" + page + "&per_page=90")
        pageSource.indexOf("Niestety nie znaleźliśmy tego czego szukasz") match {
          case -1 => pageSource :: getAllPagesSourcesRemotelyIter(page + 1)
          case _ => Nil
        }
      }
      getAllPagesSourcesRemotelyIter(1)
    }

    def getAllPagesSourcesLocally(): List[String] = {
      val productLinks = Source.fromFile(FileManager.linksFile)
        .getLines()
        .map(_.split(" "))
        .find(_.head == categoryUrl)
        .map(_.drop(1))

      productLinks match {
        case Some(links) => links.toList
        case None => List()
      }
    }

    if(workLocally) {
      val allCategoryProducts = getAllPagesSourcesLocally()
      categoriesToProductsLinks += (categoryUrl -> allCategoryProducts)
    }
    else {
      categoriesToProductsLinks.get(categoryUrl) match {
        case Some(productLinks) => productLinks
        case None => {
          val allCategoryProducts = getAllPagesSourcesRemotely().flatMap {
            pageSource =>
              XKomParser.getAllInfoInsideMarkWithText(
                pageSource,
                XKomParser.productItemMark,
                XKomParser.productItem
              )
          }.map(XKomMainPage.toString + XKomParser.getLinkInText(_))
          categoriesToProductsLinks += (categoryUrl -> allCategoryProducts)
        }
      }
    }
    val x = categoriesToProductsLinks(categoryUrl)
    categoriesToProductsLinks(categoryUrl)
  }

  private def getProductInfo(productUrl: String) = {
    var pageSource = ""
    val fileName = FileManager.productsFolder + productUrl.replaceAll("/", "^*^") + ".txt"
    try {
      val productFile = Source.fromFile(fileName)
      pageSource = productFile.mkString
    }
    catch {
      case e:Exception => {
        pageSource = HttpClient.downloadPageSource(productUrl)
        new File(fileName).createNewFile()
        val writer = new PrintWriter(fileName)
        writer.write(pageSource)
        writer.close()
      }
    }
    val description = XKomParser.getProductDescription(pageSource)
    val title = XKomParser.getProductTitle(pageSource)
    val imgUrl = XKomParser.getProductImgUrl(pageSource)
    (description, title, imgUrl)
  }

  def getProductLinks(category: String) = {
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
      _ <- 0 until numberOfProducts
      category = categoriesLinks(r.nextInt(categoriesLinks.length))
    } yield category

    val randomProductsLinksFromCategories = for {
      category <- randomCategories
      productLinks = getProductLinks(category)
      randomProductLink = productLinks(r.nextInt(productLinks.length))
    } yield randomProductLink

    randomProductsLinksFromCategories
      .map(link => {
        val (description, title, imgUrl) = getProductInfo(link)
        ProductInfo(
          link,
          description,
          title,
          imgUrl
        )
      }
      ).filter(_.description.isDefined).toList
  }
}
