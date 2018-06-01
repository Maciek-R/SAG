package pl

import pl.sag.product.ProductsInfo

package object sag {

  case object StartCollectingData
  case object CollectData
  case class SendCollectedProductsInfoToMainActor(productsInfo: ProductsInfo)
  case object ShowProductsInfo
  case object TerminateChildren
  case object GotAllMessages
  case object ShowCurrentLinksAndImgsOfProducts
  case object UpdateLocalBaseCategoriesAndProductsLinks
}
