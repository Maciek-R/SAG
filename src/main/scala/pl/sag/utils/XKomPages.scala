package pl.sag.utils

sealed trait XKomPage

case object XKomMainPage extends XKomPage {
  override def toString: String = "https://www.x-kom.pl/"
}
