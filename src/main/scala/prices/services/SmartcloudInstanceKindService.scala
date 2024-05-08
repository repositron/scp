package prices.services

import cats.effect.{Async, Sync}
import cats.implicits.*
import prices.routes.protocol.{InstanceKindResponse, SmartCloudKindInfoResponse}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import prices.services.smartcloud.*

object SmartcloudInstanceKindService {
  given [F[_] : Sync]: Logger[F] = Slf4jLogger.getLogger[F]
  final case class Config(
      baseUri: String,
      token: String
  )

  def make[F[_]: Sync: Async](smartCloudClient: SmartCloudClient[F]): InstanceKindService[F]
    = new SmartcloudInstanceKindService(smartCloudClient)

  private final class SmartcloudInstanceKindService[F[_]: Sync: Async](
      smartCloudClient: SmartCloudClient[F]
  ) extends InstanceKindService[F] {

    
    override def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]] = {
      smartCloudClient.getKindPrices(kind)
    }

    override def getAllKinds: F[Either[ServerError, List[InstanceKindResponse]]]= {
      smartCloudClient.getAllKinds
    }

  }

}
