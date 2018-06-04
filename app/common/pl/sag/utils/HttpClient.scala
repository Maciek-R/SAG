package pl.sag.utils

import pl.sag.Logger._

import scala.io.Source

object HttpClient {

  def downloadPageSource(pageUrl: String): String = {
    def retryDownloadPageSource(pageUrl: String, numTriesLeft: Int): String = {
      try {
        val html = Source.fromURL(pageUrl)
        log(s"Successfully downloaded page $pageUrl", LogLevel.INFO)

        html.mkString
      }
      catch {
        case _: Exception => {
          log(s"Retrying download: $pageUrl .Tries left: $numTriesLeft", LogLevel.WARNING)
          if (numTriesLeft > 0)
            retryDownloadPageSource(pageUrl, numTriesLeft - 1)
          else
            log("Error during downloading page source", LogLevel.ERROR)
            ""
        }
      }
    }

    retryDownloadPageSource(pageUrl, 10)
  }
}
