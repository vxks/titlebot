package com.vxksoftware

import com.vxksoftware.model.{InvalidRequestException, InvalidUrlException, TitlebotRequest, TitlebotResponse}
import com.vxksoftware.service.TitleInfoFetcher
import zio.*
import zio.http.*
import zio.http.Header.{AccessControlAllowMethods, AccessControlAllowOrigin, Origin}
import zio.http.HttpAppMiddleware.cors
import zio.http.internal.middlewares.Cors.CorsConfig
import zio.json.*

import java.io.IOException
import java.net.URL
import scala.util.{Failure, Success, Try}

object Titlebot extends ZIOAppDefault:

  val corsConfig: CorsConfig = CorsConfig(
    allowedOrigin = {
      case origin @ Origin.Value(_, host, Some(port)) if port == 3000 && Set("localhost", "127.0.0.1").contains(host) =>
        Some(AccessControlAllowOrigin.Specific(origin))
      case _ => None
    },
    allowedMethods = AccessControlAllowMethods(Method.POST)
  )

  val routes: Http[TitleInfoFetcher, Throwable, Request, Response] = Http.collectZIO[Request] {
    case request @ Method.POST -> !! / "titlebot" / "url" =>
      val titleInfo = for
        string <- request.body.asString
        json <- string
                  .fromJson[TitlebotRequest]
                  .fold(
                    err =>
                      Console.printLineError(s"Could not parse TitlebotRequest from request body: $err") *> ZIO
                        .fail(InvalidRequestException),
                    titlebotRequest => ZIO.succeed(titlebotRequest)
                  )
        fetcher  <- ZIO.service[TitleInfoFetcher]
        url       = json.url
        response <- fetcher.fetch(url)
      yield response

      titleInfo.map(info => Response.json(info.toJson)).catchSome {
        case _: InvalidUrlException  => ZIO.succeed(Response.status(Status.NotFound))
        case InvalidRequestException => ZIO.succeed(Response.status(Status.BadRequest))
      }
  } @@ RequestHandlerMiddlewares.debug @@ cors(corsConfig)

  val backend: ZIO[TitleInfoFetcher & Server, IOException, Unit] =
    for
      _           <- Console.printLine("Starting server...")
      serverFiber <- Server.serve(routes.withDefaultErrorResponse).fork
      _           <- Console.printLine("Server started on port 8080")
      _           <- Console.readLine
      _           <- Console.printLine("Shutting down server...")
      _           <- serverFiber.interrupt
    yield ()

  val run =
    backend.provide(
      Server.default,
      TitleInfoFetcher.live
    )
