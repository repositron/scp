package prices.services.smartcloud

import io.circe.{Encoder, Json}
import org.http4s.{EntityEncoder, Status}
import org.http4s.circe.jsonEncoderOf


sealed trait SmartCloudError

case class NotFoundError(message: String) extends SmartCloudError

case class ServerError(statusCode: Status, message: String) extends SmartCloudError

object ServerError:
  given Encoder[ServerError] = (a: ServerError) => Json.obj(
    ("status", Json.fromString(a.statusCode.reason)),
    ("message", Json.fromString(a.message))
  )
  given [F[_]]: EntityEncoder[F, ServerError] = jsonEncoderOf
