package com.vxksoftware.model

import com.vxksoftware.util.Codec.{*, given}
import zio.json.JsonEncoder

import java.net.URL

case class TitlebotResponse(
  title: String,
  iconUrl: URL
) derives JsonEncoder
