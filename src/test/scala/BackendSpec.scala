import com.vxksoftware.model.{NonexistentUrlException, TitlebotRequest, TitlebotResponse}
import com.vxksoftware.Titlebot
import com.vxksoftware.service.TitleInfoFetcher
import io.netty.util.AsciiString
import zio.*
import zio.http.*
import zio.http.netty.*
import zio.json.*
import zio.test.Assertion.*
import zio.test.*

object BackendSpec extends ZIOSpecDefault:
  val url: URL = URL(!! / "titlebot" / "titleInfo").withQueryParams(QueryParams(Map("url" -> Chunk("https://cnn.com"))))
  val defaultRequest: Request = Request(
    body = Body.empty,
    headers = Headers.empty,
    method = Method.GET,
    url = url,
    version = Version.Http_1_1,
    remoteAddress = None
  )

  val fetcherDummyLayer: ULayer[TitleInfoFetcher] = TitleInfoFetcher.test { url =>
    url.toString match {
      case "https://cnn.com" =>
        ZIO.succeed {
          TitlebotResponse(
            title = "Breaking News, Latest News and Videos | CNN",
            iconUrl = new java.net.URL("https://cnn.com/favicon.ico")
          )
        }

      case "https://nonexistent.url" => ZIO.fail(NonexistentUrlException(new java.net.URL("https://nonexistent.url")))
      case _url                      => ZIO.fail(new Exception(s"Unhandled URL: ${_url}"))
    }
  }

  def responseBody(response: TitlebotResponse): Body = NettyBody.fromAsciiString(new AsciiString(response.toJson))

  def spec = suite("backend")(
    test("valid URL request should return title and icon URL") {
      for {
        response <- Titlebot.routes.runZIO(defaultRequest)
      } yield assertTrue(
        response.status == Status.Ok &&
          response.body == responseBody(
            TitlebotResponse(
              title = "Breaking News, Latest News and Videos | CNN",
              iconUrl = new java.net.URL("https://cnn.com/favicon.ico")
            )
          )
      )
    },
    test("invalid request should return 400") {
      for {
        response <- Titlebot.routes.runZIO(defaultRequest.copy(url = url.copy(queryParams = QueryParams.empty)))
      } yield assertTrue(
        response.status == Status.BadRequest
      )
    },
    test("non-existent URL request should fail with 404") {
      for {
        response <-
          Titlebot.routes.runZIO {
            defaultRequest.copy(
              url = url.copy(queryParams =
                QueryParams(Map("url" -> Chunk(new java.net.URL("https://nonexistent.url").toString)))
              )
            )
          }
      } yield assertTrue(
        response.status == Status.NotFound
      )
    }
  ).provide(fetcherDummyLayer)
