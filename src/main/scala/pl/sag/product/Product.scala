package pl.sag.product

case class ProductsInfo (info: Seq[Int])

case class ProductInfo(
  linkPage: String,
  description: Option[String]
)