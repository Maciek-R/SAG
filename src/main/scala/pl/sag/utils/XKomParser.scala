package pl.sag.utils

import pl.sag.Logger._

object XKomParser {

  val productDescriptionStartMark = "Opis produktu"
  val productDescriptionEndMark = "Specyfikacja"

  val dataCategory = "<li data-category-id"
  val dataCategoryMark = "li"

  val productItem = "<div class=\"product-item"
  val productItemMark = "div"

  val productDetails = "<div class=\"col-xs-12 product-detail-impression\""
  val productDetailsEndMark = "Zdjęcie 1"
  val productName = "data-product-name=\""
  val productImg = "<img itemprop=\"image\" src=\""

  val cssFilterMark = "main-wrapper"

  def getProductDescription(pageSource: String): Option[String] = {
    pageSource.indexOf(productDescriptionStartMark) match {
      case -1 => None
      case index => {
        val fullProductDescription = pageSource.substring(
          index + productDescriptionStartMark.length,
          pageSource.indexOf(productDescriptionEndMark)
        )
        val descriptionWithRemovedMarks = removedMarks(fullProductDescription)
        descriptionWithRemovedMarks.indexOf(cssFilterMark) match {
          case -1 => Some(descriptionWithRemovedMarks)
          case cssIndex => {
            log("wrapper section", LogLevel.DEBUG)
            Some(descriptionWithRemovedMarks.substring(0, cssIndex))
          }
        }
      }
    }
  }

  def getProductTitle(pageSource: String): Option[String] = {
    pageSource.indexOf(productDetails) match {
      case -1 => None
      case index => {
        val productDetails = pageSource.substring(
          index,
          pageSource.indexOf(productDetailsEndMark)
        )
        val productDetailIndex = productDetails.indexOf(productName)
        log("getting title", LogLevel.DEBUG)
        val productTitle = productDetails.substring(
          productDetailIndex,
          productDetails.substring(productDetailIndex + productName.length).indexOf("\"") + productDetails.indexOf(productName) + productName.length
        ).substring(productName.length)
        Some(productTitle)
      }
    }
  }

  def getProductImgUrl(pageSource: String): Option[String] = {
    pageSource.indexOf(productDetails) match {
      case -1 => None
      case index => {
        val productDetails = pageSource.substring(
          index,
          pageSource.indexOf(productDetailsEndMark)
        )
        val productImgIndex = productDetails.indexOf(productImg)
        log("getting url", LogLevel.DEBUG)
        val productImgUrl = productDetails.substring(
          productImgIndex,
          productDetails.substring(productImgIndex + productImg.length).indexOf("\"") + productDetails.indexOf(productImg) + productImg.length
        ).substring(productImg.length)
        Some(productImgUrl)
      }
    }
  }

  def getAllInfoInsideMarkWithText(pageSource: String, mark: String, text: String) = {

    def getAllIndexesWithMark(source: String, mark: String, startingIndex: Int): List[Int] = {
      source.indexOf(mark, startingIndex) match {
        case -1 => Nil
        case num => num :: getAllIndexesWithMark(source, mark, num + mark.length)
      }
    }

    def getEndIndexesWithMark(source: String, mark: String, startIndexes: List[Int], startingIndex: Int) = {
      startIndexes.map(startIndex => source.indexOf(mark, startIndex))
    }

    val startIndexes = getAllIndexesWithMark(pageSource, text, 0)
    val endIndexes = getEndIndexesWithMark(pageSource, "</" + mark + ">", startIndexes, startIndexes.head)

    startIndexes
      .zip(endIndexes)
      .map { case (startIndex, endIndex) => pageSource.substring(startIndex, endIndex) }
  }

  def getTextBetween(source: String, mark1: String, mark2: String) = {
    source.substring(
      source.indexOf(mark1),
      source.indexOf(mark2))
  }

  def getLinkInText(source: String) = {
    getTextBetween(source, "a href=\"", ".html").substring(8) + ".html"
  }

  private def removedMarks(fullProductDescription: String) = {
    val sB = StringBuilder.newBuilder
    var isAdding = false
    var isOpen = false
    for (i <- fullProductDescription) {
      if (!isOpen) {
        if (i == '>')
          isOpen = true
      }
      else {
        if (i.isLetterOrDigit || i == '.' || i == ',')
          isAdding = true
        else if (i == '<') {
          if (isAdding) {
            sB.append(". ")
          }
          isAdding = false
          isOpen = false
        }
      }
      if (isAdding)
        sB.append(i)
    }
    sB.toString()
  }
}
