package com.vxksoftware.model

import java.net.URL

case object InvalidRequestException extends RuntimeException("Invalid request")

case class NonexistentUrlException(url: URL) extends RuntimeException(s"URL does not exist: [${url.toString}]")
