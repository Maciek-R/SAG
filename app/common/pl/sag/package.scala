package pl

import pl.sag.product.{ProductInfo, ProductsInfo}

package object sag {

  case object StartCollectingData
  case object StartCollectingDataBlock
  case object CollectData
  case object CollectDataBl
  case class GetBestMatches(productUrl: String)
  case class GetBestMatchesBl(productUrl: String)
  case class SendCollectedProductsInfoToMainActor(productsInfo: ProductsInfo)
  case class SendBestMatchesToMainActor(topMatches: Seq[(ProductInfo, Double)])
  case object ShowProductsInfo
  case object GetProductsInfo
  case object TerminateChildren
  case object CheckIfGotAllMessages
  case object ShowCurrentLinksAndImgsOfProducts
  case object UpdateLocalBaseCategoriesAndProductsLinks
}
