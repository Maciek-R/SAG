package pl.sag.utils

import pl.sag.utils.XKomClient.{productDescriptionEndMark, productDescriptionStartMark}

object XKomParser {

  def getProductInfo(pageSource: String) = {
    val fullProductDescription = pageSource.substring(
      pageSource.indexOf(productDescriptionStartMark)+productDescriptionStartMark.length,
      pageSource.indexOf(productDescriptionEndMark)
    )
    removeMarks(fullProductDescription)
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
    val endIndexes = getEndIndexesWithMark(pageSource, "</"+mark+">", startIndexes, startIndexes.head)

    startIndexes
      .zip(endIndexes)
      .map{case (startIndex, endIndex) => pageSource.substring(startIndex, endIndex)}
  }

  def getTextBetween(source: String, mark1: String, mark2: String) = {
    source.substring(
      source.indexOf(mark1),
      source.indexOf(mark2))
  }

  def getLinkInText(source: String) = {
    getTextBetween(source, "a href=\"", ".html").substring(8)+".html"
  }

  private def removeMarks(fullProductDescription: String) = {
    val sB = StringBuilder.newBuilder
    var isAdding = false
    var isOpen = false
    for (i <- fullProductDescription) {
      if(!isOpen){
        if(i=='>')
          isOpen=true
      }
      else{
        if(i.isLetterOrDigit || i=='.' || i==',')
          isAdding = true
        else if(i=='<') {
          if(isAdding) {sB.append(". ")}
          isAdding = false
          isOpen = false
        }
      }
      if(isAdding)
        sB.append(i)
    }
    sB.toString()
  }
}
