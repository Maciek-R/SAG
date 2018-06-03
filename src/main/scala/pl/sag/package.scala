package pl

import pl.sag.product.{ProductInfo, ProductsInfo}

package object sag {

  case object StartCollectingData
  case object CollectData
  case class GetBestMatches(productUrl: String)
  case class SendCollectedProductsInfoToMainActor(productsInfo: ProductsInfo)
  case class SendBestMatchesToMainActor(topMatches: Seq[(ProductInfo, Double)])
  case object ShowProductsInfo
  case object TerminateChildren
  case object CheckIfGotAllMessages
  case object ShowCurrentLinksAndImgsOfProducts
  case object UpdateLocalBaseCategoriesAndProductsLinks
}
