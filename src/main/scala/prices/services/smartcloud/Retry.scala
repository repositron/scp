package prices.services.smartcloud

import cats.effect.{Async, Sync}
import org.http4s.client.{Client, middleware}
import org.http4s.{Response, Status}

import scala.concurrent.duration.*

object Retry:
  def make[F[_]: Sync: Async]: Client[F] => Client[F] = {

    val retryStatusCodes = Set(
      Status.TooManyRequests,
      Status.BadGateway,
      Status.ServiceUnavailable,
      Status.GatewayTimeout
    )

    def shouldRetry(result: Either[Throwable, Response[F]]): Boolean =
      result.fold (
        _ => true,
        response => retryStatusCodes.contains(response.status)
      )

    val retryPolicy =  middleware.RetryPolicy[F](
      backoff = middleware.RetryPolicy.exponentialBackoff(maxWait = 5.seconds, maxRetry = 5),
      retriable = (_, result) => shouldRetry(result)
    )
    middleware.Retry[F](retryPolicy)
}
