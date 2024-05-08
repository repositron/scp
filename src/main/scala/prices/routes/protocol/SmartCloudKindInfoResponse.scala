package prices.routes.protocol

import cats.effect.Concurrent
import io.circe.generic.auto.*
import io.circe.*
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.*

case class SmartCloudKindInfoResponse(kind: String, price: Double, timestamp: String)

object SmartCloudKindInfoResponse:

  given Encoder[SmartCloudKindInfoResponse] = (smcki: SmartCloudKindInfoResponse) => Json.obj(
    ("kind", Json.fromString(smcki.kind)),
    ("price", Encoder[Double].apply(smcki.price)),
    ("timestamp", Json.fromString(smcki.timestamp))
  )

  given [F[_]]: EntityEncoder[F, SmartCloudKindInfoResponse] = jsonEncoderOf

  given [F[_]: Concurrent]: EntityDecoder[F, SmartCloudKindInfoResponse] = jsonOf

