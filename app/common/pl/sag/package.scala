package pl

import pl.sag.product.ProductInfo

package object sag {

  // From controller to MainActor
  case object CreateSubActor
  case class RemoveSubActor(index: Int)
  case object CountReadySubActors
  case object ListSubActors

  case object TerminateChildren
  case object UpdateLocalBaseCategoriesAndProductsLinks

  // From MainActor to SubActors
  case object BuildModel
  case object SubActorIsReady


  // From SubActors to MainActor
  case class CollectBestMatches(bestMatches: Seq[(ProductInfo, Double)])

  // Common
  case class SearchByStringQuery(text: String)
  case class SearchByProductInfo(product: ProductInfo)
}
