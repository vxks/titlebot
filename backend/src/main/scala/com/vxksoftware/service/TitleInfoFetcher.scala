package com.vxksoftware.service

import com.vxksoftware.model.{InvalidUrlException, TitlebotResponse}
import com.vxksoftware.service.TitleInfoFetcher
import zio.*
import zio.http.{URL as ZURL, *}

import java.net.URL
import scala.util.Try

trait TitleInfoFetcher:
  def fetch(url: URL): Task[TitlebotResponse]

object TitleInfoFetcher:
  val live: ULayer[TitleInfoFetcher] = ZLayer.fromFunction(TitleInfoFetcherLive.apply _)

  def test(fetcher: URL => Task[TitlebotResponse]): ULayer[TitleInfoFetcher] =
    ZLayer.succeed(TitleInfoFetcherDummy(fetcher))

  case class TitleInfoFetcherLive() extends TitleInfoFetcher {
    // Task[Option[String]] - page could have no title?
    private def getTitle(html: String): Task[String] = {
      val pattern = ".*?<title>(.*?)</title>.*".r // doesn't account for commented lines etc.

      pattern.findFirstMatchIn(html) match
        case Some(titleMatch) => ZIO.succeed(titleMatch.group(1))
        case _                => ZIO.fail(new RuntimeException("Requested page has no title"))
    }

    def fetch(url: java.net.URL): Task[TitlebotResponse] = {
      // don't really need the whole text - could only read the needed bytes
      // https://github.com/com-lihaoyi/requests-scala#streaming-requests
      val response = Try(requests.get(url.toString).text()).fold(
        _ => ZIO.fail(InvalidUrlException(url)), // potentially missing error info
        text => ZIO.succeed(text)
      )

      for
        html   <- response
        title  <- getTitle(html)
        iconUrl = new URL(url.getProtocol, url.getHost, url.getPort, "/favicon.ico")
      yield TitlebotResponse(
        title = title,
        iconUrl = iconUrl
      )
    }
  }

  case class TitleInfoFetcherDummy(fetcher: URL => Task[TitlebotResponse]) extends TitleInfoFetcher {
    def fetch(url: URL): Task[TitlebotResponse] = fetcher(url)
  }
