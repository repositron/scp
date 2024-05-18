package prices.services.smartcloud

import cats.data.EitherT
import cats.effect.*
import cats.effect.kernel.Concurrent
import cats.implicits.*
import io.circe.*
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.circe.*
import org.http4s.client.*
import org.typelevel.ci.CIString
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import prices.config.Config
import prices.config.Config.RootConfig
import prices.data.InstanceKind
import prices.routes.protocol.{InstanceKindResponse, SmartCloudKindInfoResponse}

trait SmartCloudClient[F[_]]:
  def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]]
  def getAllKinds: F[Either[ServerError, List[InstanceKindResponse]]]

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

    new SmartCloudClient[F] {
      override def getKindPrices(kind: String): F[Either[ServerError, SmartCloudKindInfoResponse]] = {
        for {
          uri <- Uri.fromString(config.smartcloud.baseUri)
            .map(_ / "instances" / kind)
            .liftTo[F]
          request = getRequest(uri, config.smartcloud.token)
          response <- client.run(request).use { resp =>
            if (resp.status.isSuccess)
              resp.as[SmartCloudKindInfoResponse].map(Right(_))
            else
              Sync[F].pure(Left[ServerError, SmartCloudKindInfoResponse](ServerError(resp.status, resp.status.reason)))
          }
        } yield response
      }

      override def getAllKinds: F[Either[ServerError, List[InstanceKindResponse]]] = {
        for {
          uri <- Uri.fromString(config.smartcloud.baseUri)
            .map(_ / "instances")
            .liftTo[F]
          request = getRequest(uri, config.smartcloud.token)
          response <- client.run(request).use { resp =>
            if resp.status.isSuccess then
              resp.as[List[String]].map((kinds: List[String]) => Right(kinds.map(k => InstanceKindResponse(InstanceKind(k)))))
            else
              Sync[F].pure(Left[ServerError, List[InstanceKindResponse]](ServerError(resp.status, resp.status.reason)))
          }
        } yield response
      }
    }
  }

