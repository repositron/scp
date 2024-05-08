package prices.config

import cats.effect.Resource
import cats.effect.kernel.Sync
import pureconfig.*
import pureconfig.generic.derivation.default.*

object Config {

  case class AppConfig(
      host: String,
      port: Int
  )

  case class SmartcloudConfig(
      baseUri: String,
      token: String
  )
  
  case class RootConfig(
    app: AppConfig,
    smartcloud: SmartcloudConfig
 )

  given ConfigReader[RootConfig] = ConfigReader.derived[RootConfig]

  def load[F[_]: Sync]: Resource[F, RootConfig] = {
    Resource.eval(Sync[F].delay(ConfigSource.default.loadOrThrow[RootConfig]))
  }
}
