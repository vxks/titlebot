package com.vxksoftware

import com.vxksoftware.model.{InvalidRequestException, NonexistentUrlException, TitlebotRequest, TitlebotResponse}
import com.vxksoftware.service.{Cache, TitleInfoFetcher}
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
    allowedMethods = AccessControlAllowMethods(Method.GET)
  )

  val routes: Http[TitleInfoFetcher, Throwable, Request, Response] = Http.collectZIO[Request] {
    case request @ Method.GET -> !! / "titlebot" / "titleInfo" =>
      val titleInfo = for
        url <- request.url.queryParams
                 .get("url") match {
                 case Some(Chunk(url)) => ZIO.attempt(new URL(url)).orElseFail(InvalidRequestException)
                 case _                => ZIO.fail(InvalidRequestException)
               }
        fetcher  <- ZIO.service[TitleInfoFetcher]
        response <- fetcher.fetch(url)
      yield response

      titleInfo.map(info => Response.json(info.toJson)).catchSome {
        case _: NonexistentUrlException => ZIO.succeed(Response.status(Status.NotFound))
        case InvalidRequestException    => ZIO.succeed(Response.status(Status.BadRequest))
      }
  } @@ RequestHandlerMiddlewares.debug @@ cors(corsConfig)

  val backendLive: ZIO[TitleInfoFetcher with Server, IOException, Unit] = for
    _ <- Console.printLine("Starting server on port 8080...")
    _ <- Server.serve(routes.withDefaultErrorResponse)
  yield ()

  val backendDev: ZIO[TitleInfoFetcher with Server, Throwable, Unit] = for
    _           <- Console.printLine("Starting development server on port 8080...")
    serverFiber <- Server.serve(routes.withDefaultErrorResponse).fork
    _           <- Console.printLine("Development server started")
    _           <- Console.readLine
    _           <- Console.printLine("Shutting down server...")
    _           <- serverFiber.interrupt
  yield ()

  val backend: ZIO[TitleInfoFetcher & Server, Throwable, ExitCode] =
    for
      environment <- System.envOrElse("ENV", "development")
      _ <- environment match {
             case "production" => backendLive
             case _            => backendDev
           }
    yield ExitCode.success

  val run =
    for
      cache <- Cache.make[URL, TitlebotResponse]
      _ <- backend.provide(
             Server.default,
             TitleInfoFetcher.live,
             ZLayer.succeed(cache)
           )
    yield ()
