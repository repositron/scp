package prices.routes

import cats.effect.*
import cats.implicits.*
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.dsl.io.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import prices.routes.protocol.SmartCloudKindInfoResponse
import prices.services.SmartcloudApiService
import prices.services.smartcloud.ServerError

final case class InstanceKindRoutes[F[_]: Sync](instanceKindService: SmartcloudApiService[F]) extends Http4sDsl[F] {
  given routesLogger: Logger[F] = Slf4jLogger.getLogger[F]

  object KindQueryMatcher extends QueryParamDecoderMatcher[String]("kind")

  given EntityEncoder[F, List[Map[String, String]]] = jsonEncoderOf

  given [F[_]]: EntityEncoder[F, SmartCloudKindInfoResponse] = jsonEncoderOf
  val route: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "prices" :? KindQueryMatcher(kind) =>
      instanceKindService
        .getKindPrices(kind)
        .flatMap(_.fold(
          e => {
            if (e.statusCode == Status.NotFound)
              NotFound(e.copy(message = s"price for $kind not found"))
            else
              InternalServerError(e)
          },
          r => Ok(r)
        ))

    case GET -> Root / "prices" =>
      BadRequest("Missing required 'kind' query parameter")

    case GET -> Root / "instance-kinds" =>
      instanceKindService.getAllKinds
        .flatMap(_.fold(
          e => InternalServerError(e),
          r => Ok(r.map(kind => Map("kind" -> kind)))
      ))
  }
}
