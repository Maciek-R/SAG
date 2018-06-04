package pl.sag.utils

import java.io.{File, PrintWriter}
import java.util.Base64

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
    var pageSource: Option[String] = None
    val fileName = FileManager.productsFolder + Base64.getEncoder.encodeToString(productUrl.getBytes("UTF-8"))
    try {
      val productFile = Source.fromFile(fileName)
      log(s"Loaded file from disc $productUrl", LogLevel.INFO)
      pageSource = Some(productFile.mkString)
    }
    catch {
      case e:Exception => {
        pageSource = XKomParser.cutPageSource(HttpClient.downloadPageSource(productUrl))
        pageSource = XKomParser.mapToPolishCharacters(pageSource)
        pageSource match {
          case None =>
          case Some(source) => {
            log(s"saving file $fileName")
            new File(fileName).createNewFile()
            log(s"Saved file $fileName")
            val writer = new PrintWriter(fileName)
            writer.write(source)
            writer.close()
          }
        }
      }
    }
    pageSource match {
      case None => (None, None, None)
      case Some(source) => {
        val description = XKomParser.getProductDescription(source)
        val title = XKomParser.getProductTitle(source)
        val imgUrl = XKomParser.getProductImgUrl(source)
        (description, title, imgUrl)
      }
    }
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
        downloadProduct(link)
      }
      ).filter(_.description.isDefined).toList
  }

  def downloadProduct(link: String) = {
    val (description, title, imgUrl) = getProductInfo(link)
    ProductInfo(
      link,
      description,
      title,
      imgUrl
    )
  }
}
