package pl.sag

object Logger {
  val loggerOn = true

  def log(name: String, message: => String) = {
    if(loggerOn)
      println(name + " " + message)
  }

  def log(name: String): Unit = log(name, "")
}
