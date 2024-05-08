package prices.services

import cats.effect.{Async, Sync}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import prices.routes.protocol.{InstanceKindResponse, SmartCloudKindInfoResponse}
import prices.services.smartcloud.*

trait SmartcloudApiService[F[_] : Sync : Async] {
  def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]]
  def getAllKinds: F[Either[ServerError, List[InstanceKindResponse]]]
}

object SmartcloudApiService:
  given [F[_] : Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def make[F[_]: Sync: Async](smartCloudClient: SmartCloudClient[F]): SmartcloudApiService[F] = {
    new SmartcloudApiService[F]:
      override def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]] = {
        smartCloudClient.getKindPrices(kind)
      }

      override def getAllKinds: F[Either[ServerError, List[InstanceKindResponse]]] = {
        smartCloudClient.getAllKinds
      }
  }
