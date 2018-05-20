package pl

package object sag {

  case object CreateSubActor
  case object StartCollectingData
  case object CollectData
  case class SendCollectedProductsInfo(productsInfo: Seq[Int])
  case object ShowProductsInfo
}
