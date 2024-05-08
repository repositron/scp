package prices

import cats.data.OptionT
import cats.effect.*
import cats.effect.kernel.Async
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import org.http4s.HttpRoutes
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Server
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import prices.config.Config
import prices.config.Config.RootConfig
import prices.routes.InstanceKindRoutes
import prices.services.SmartcloudApiService
import prices.services.smartcloud.{Retry, SmartCloudClient}

object Main extends IOApp.Simple {
  given logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  private def httpServer[F[_] : Async](config: RootConfig, routes: HttpRoutes[F]): Resource[F, Server] =
    def errorHandler(t: Throwable, msg: => String) : OptionT[F, Unit] =
      OptionT.liftF(
        Logger[F].error(t)(msg)
      )

    val withErrorLogging = ErrorHandling.Recover.total(
      ErrorAction.log(
        routes,
        messageFailureLogAction = errorHandler,
        serviceErrorLogAction = errorHandler
      )
    )

    EmberServerBuilder
      .default[F]
      .withHost(Host.fromString(config.app.host).get)
      .withPort(Port.fromInt(config.app.port).get)
      .withHttpApp(withErrorLogging.orNotFound)
      .build

  def makeApp[F[_]: Async]: F[Nothing]  = {
    for {
      emberClient <- EmberClientBuilder.default[F].build
      config <- Config.load
      client = SmartCloudClient.make[F](config, Retry.make[F].apply(emberClient))
      kindService = SmartcloudApiService.make[F](client)
      routes = InstanceKindRoutes[F](kindService).route
      _ <- httpServer(config, routes)
    } yield ()
  }.useForever

  def run: IO[Unit] = {
    makeApp[IO]
  }
}
