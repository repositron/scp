package prices.services

import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.*
import prices.data.*

object SmartcloudInstanceKindService {

  final case class Config(
      baseUri: String,
      token: String
  )

  def make[F[_]: Concurrent](config: Config): InstanceKindService[F] = new SmartcloudInstanceKindService(config)

  private final class SmartcloudInstanceKindService[F[_]: Concurrent](
      config: Config
  ) extends InstanceKindService[F] {

    given EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    val getAllUri = s"${config.baseUri}/instances"

    override def getAll(): F[List[InstanceKind]] = {
      List("sc2-micro", "sc2-small", "sc2-medium") // Dummy data. Your implementation should call the smartcloud API.
        .map(InstanceKind.apply)
        .pure[F]
    }

  }

}
