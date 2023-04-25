package com.vxksoftware.model

import com.vxksoftware.util.Codec.{*, given}
import zio.json.JsonCodec

import java.net.URL

case class TitlebotRequest(
  url: URL
) derives JsonCodec
