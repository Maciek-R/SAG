package pl.sag.product

case class ProductsInfo (productsInfo: List[ProductInfo])

case class ProductInfo(
  linkPage: String,
  description: Option[String]
)