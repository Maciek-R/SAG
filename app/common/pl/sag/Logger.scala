package pl.sag


object Logger {

  case object LogLevel extends Enumeration {
    val NONE, INFO, ERROR, WARNING, DEBUG = Value
  }

  val level: LogLevel.Value = LogLevel.DEBUG

  def log(title: String, message: => String, logLevel: LogLevel.Value): Unit = {
    if (logLevel <= level)
      println(s"<$logLevel> $title: $message")
  }

  def log(title: String, message: => String): Unit = log(title, message, LogLevel.INFO)

  def log(message: => String, logLevel: LogLevel.Value): Unit = log("", message, logLevel)

  def log(message: => String): Unit = log("", message)
}


