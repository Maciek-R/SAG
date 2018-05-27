package pl.sag.utils

import java.net.URL
import java.nio.charset.StandardCharsets

import scala.io.Source

object HttpClient {

  def downloadPageSource(pageUrl: String): String = {
    def retryDownloadPageSource(pageUrl: String, numTriesLeft: Int): String = {
      try {
        val html = Source.fromURL(pageUrl)
        html.mkString
      }
      catch {
        case e: Exception => {
          println(s"Retrying download: $pageUrl .Tries left: $numTriesLeft")
          if (numTriesLeft > 0)
            retryDownloadPageSource(pageUrl, numTriesLeft - 1)
          else
            sys.error("Error during downloading page source")
        }
      }
    }

    retryDownloadPageSource(pageUrl, 10)
  }
}
