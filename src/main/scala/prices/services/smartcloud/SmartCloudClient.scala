package prices.services.smartcloud

import cats.effect.kernel.Concurrent
import cats.effect.*
import cats.implicits.*
import io.circe.*
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.Status.Successful
import org.http4s.circe.*
import org.http4s.client.*
import org.typelevel.ci.CIString
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import prices.config.Config
import prices.config.Config.RootConfig
import prices.routes.protocol.SmartCloudKindInfoResponse

trait SmartCloudClient[F[_]]:
  def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]]
  def getAllKinds: F[Either[ServerError, List[String]]]

object SmartCloudClient:
  given [F[_] : Sync]: Logger[F] = Slf4jLogger.getLogger[F]
  given [F[_]: Concurrent]: EntityDecoder[F, List[String]] = jsonOf

  def make[F[_]: Sync: Async: MonadCancelThrow](
                         config: RootConfig,
                         client: Client[F]
   ): SmartCloudClient[F] = {

    def getRequest(uri: Uri, token: String): Request[F] = {
      val headers = Headers.apply(Header.Raw(
        CIString("Authorization"), s"Bearer $token")
      )
      Request[F](method = GET, uri = uri, headers = headers)
    }


    given [F[_] : Concurrent]: EntityDecoder[F, SmartCloudKindInfoResponse] = jsonOf
    
    
    new SmartCloudClient[F] {
      override def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]] = {
        for {
          uri <- Uri.fromString(config.smartcloud.baseUri)
            .map(_ / "instances" / kind)
            .liftTo[F]
          request = getRequest(uri, config.smartcloud.token)
          response <- client.run(request).use {
            case Successful(resp) =>
              resp.as[SmartCloudKindInfoResponse].map(Right(_))
            case resp =>
              Sync[F].pure(Left[ServerError, SmartCloudKindInfoResponse](ServerError(resp.status, resp.status.reason)))
          }
        } yield response
      }
      
      override def getAllKinds: F[Either[ServerError, List[String]]] = {
        for {
          uri <- Uri.fromString(config.smartcloud.baseUri)
            .map(_ / "instances")
            .liftTo[F]
          request = getRequest(uri, config.smartcloud.token)
          response <- client.run(request).use {
            case Successful(resp) =>
              resp.as[List[String]].map(Right(_))
            case resp =>
              Sync[F].pure(Left[ServerError, List[String]](ServerError(resp.status, resp.status.reason)))
          }
        } yield response
      }
    }
  }

