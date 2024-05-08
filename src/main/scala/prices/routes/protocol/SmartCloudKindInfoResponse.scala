package prices.routes.protocol

import cats.effect.Concurrent
import io.circe.generic.auto.*
import io.circe.*
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.*

case class SmartCloudKindInfoResponse(kind: String, amount: Double)

object SmartCloudKindInfoResponse:

  given Encoder[SmartCloudKindInfoResponse] = (smcki: SmartCloudKindInfoResponse) => Json.obj(
    ("kind", Json.fromString(smcki.kind)),
    ("amount", Encoder[Double].apply(smcki.amount))
  )

  given [F[_]]: EntityEncoder[F, SmartCloudKindInfoResponse] = jsonEncoderOf

  given [F[_]]: Decoder[SmartCloudKindInfoResponse] = (cursor: HCursor) =>
    for {
      kind <- cursor.downField("kind").as[String]
      amount <- cursor.downField("price").as[Double]
    } yield SmartCloudKindInfoResponse(kind, amount)

  given [F[_]: Concurrent]: EntityDecoder[F, SmartCloudKindInfoResponse] = jsonOf

