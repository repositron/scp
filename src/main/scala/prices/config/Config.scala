package prices.config

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
  
  case class Config(
    app: AppConfig, 
    smartcloud: SmartcloudConfig
 )

  given ConfigReader[Config] = ConfigReader.derived[Config]

  def load[F[_]: Sync]: F[Config] = {
    Sync[F].delay(ConfigSource.default.loadOrThrow[Config])
    
  }
}
