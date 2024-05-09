package prices.routes.protocol

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.Status
import org.http4s.ember.client.EmberClientBuilder
import prices.config.Config.{AppConfig, RootConfig, SmartcloudConfig}
import prices.services.smartcloud.{ServerError, SmartCloudClient}

class SmartCloudClientTest extends CatsEffectSuite:
  // these tests use the smart cloud docker endpoint, and because that can return 500 randomly, these test can fail.

  test("when request know value it should return value and amount (requires docker running)") {
    EmberClientBuilder.default[IO].build.use { ember =>
      val config = RootConfig(AppConfig("http://localhost/", 8082), SmartcloudConfig("http://localhost:9999/", "lxwmuKofnxMxz6O2QE1Ogh"))
      val sc = SmartCloudClient.make[IO](config, ember)

      sc.getKindPrices("sc2-std-2").map { response =>
        val smartCloudKindInfoResponse = response.getOrElse(fail("failed"))
        assertEquals(smartCloudKindInfoResponse.kind, "sc2-std-2")
      }
    }.unsafeRunSync()
  }

  test("when unknown kind is request respond with 404 (requires docker running)") {
    EmberClientBuilder.default[IO].build.use { ember =>
      val config = RootConfig(AppConfig("http://localhost/", 8082), SmartcloudConfig("http://localhost:9999/", "lxwmuKofnxMxz6O2QE1Ogh"))
      val sc = SmartCloudClient.make[IO](config, ember)

      sc.getKindPrices("unknown").map { response =>
        assertEquals(response, Left(ServerError(Status.fromInt(404).getOrElse(fail("test failed")), "Not Found")))
      }
    }.unsafeRunSync()
  }



