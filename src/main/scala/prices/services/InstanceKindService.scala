package prices.services

import cats.effect.{Async, Sync}

import scala.util.control.NoStackTrace
import prices.routes.protocol.{InstanceKindResponse, SmartCloudKindInfoResponse}
import prices.services.smartcloud.ServerError

trait InstanceKindService[F[_]: Sync: Async] {
  def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]]
  def getAllKinds: F[Either[ServerError, List[InstanceKindResponse]]]
}

object InstanceKindService {

  sealed trait Exception extends NoStackTrace
  object Exception {
    case class APICallFailure(message: String) extends Exception
  }

}
