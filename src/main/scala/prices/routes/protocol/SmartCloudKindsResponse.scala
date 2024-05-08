package prices.routes.protocol

import cats.effect.kernel.Concurrent
import io.circe.*
import io.circe.Decoder.importedDecoder
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

case class SmartCloudKindsResponse(kind: String)


object SmartCloudKindsResponse:

  given encoder: Encoder[SmartCloudKindsResponse] = (a: SmartCloudKindsResponse) => Json.obj(
    ("kind", a.asJson)
  )

  given [F[_]: Concurrent]: EntityDecoder[F, SmartCloudKindsResponse] = jsonOf
  given [F[_]: Concurrent]: EntityDecoder[F, List[SmartCloudKindsResponse]] = jsonOf

  




