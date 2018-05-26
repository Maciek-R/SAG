package pl.sag.utils

import java.net.URL
import java.nio.charset.StandardCharsets

import scala.io.Source

object HttpClient {

  def downloadPageSource(pageUrl: String) = {
    val html = Source.fromURL(pageUrl)
    html.mkString
  }
}
