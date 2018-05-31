package pl.sag.product

case class ProductsInfo (productsInfo: List[ProductInfo])

case class ProductInfo(
  linkPage: String,
  description: Option[String],
  title: Option[String],
  imageUrl: Option[String]
) {
  override def toString: String = {
    val shortDesc: String = description.get.substring(0, Math.min(description.get.length(), 100))
    s"# ${title.get} # ($linkPage) - $shortDesc..."
  }
}