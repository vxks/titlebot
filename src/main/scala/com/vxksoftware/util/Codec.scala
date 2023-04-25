package com.vxksoftware.util

import zio.json.{JsonDecoder, JsonEncoder}

import java.net.URL
import scala.util.{Failure, Success, Try}

object Codec {
  given javaUrlEncoder: JsonEncoder[URL] = JsonEncoder.string.contramap(_.toString)

  given javaUrlDecoder: JsonDecoder[URL] = JsonDecoder.string.mapOrFail { string =>
    val trailingSlashRemoved = string match {
      case s"${url}/" => url
      case url        => url
    }
    Try(new URL(trailingSlashRemoved)) match
      case Failure(exception) => Left(exception.getMessage)
      case Success(value)     => Right(value)
  }
}
