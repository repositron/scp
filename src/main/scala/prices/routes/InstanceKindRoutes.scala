package prices.routes

import cats.effect.*
import cats.implicits.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityEncoder, HttpRoutes}
import prices.services.InstanceKindService

final case class InstanceKindRoutes[F[_]: Sync](instanceKindService: InstanceKindService[F]) extends Http4sDsl[F] {

  val prefix = "/instance-kinds"

  import protocol.*
  given EntityEncoder[F, List[InstanceKindResponse]] = jsonEncoderOf

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root =>
      instanceKindService.getAll().flatMap(kinds => Ok(kinds.map(k => InstanceKindResponse(k))))
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}
