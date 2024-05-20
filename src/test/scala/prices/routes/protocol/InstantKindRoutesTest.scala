package prices.routes.protocol

import cats.data.EitherT
import cats.effect.IO
import io.circe.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.circe.*
import org.http4s.implicits.*
import prices.routes.InstanceKindRoutes
import prices.services.SmartcloudApiService
import prices.services.smartcloud.ServerError

class InstantKindRoutesTest extends CatsEffectSuite:
  object Util:
    def decodeToIkResponse(r: Response[IO]): IO[List[InstanceKindResponse]] = {
      given EntityDecoder[IO, List[String]] = jsonOf
      r.as[List[String]].map((kinds: List[String]) => kinds.map(k => InstanceKindResponse(k)))
    }

    def priceLookup(uriStr: String, service: SmartcloudApiService[IO]): IO[Response[IO]] = {
      val uri = Uri.unsafeFromString(uriStr)
      val request = Request[IO](Method.GET, uri)
      val route = InstanceKindRoutes(service).route.orNotFound
      route(request)
    }
    def priceLookupByKind(kind: String, service: SmartcloudApiService[IO]): IO[Response[IO]] = {
      priceLookup(s"/prices?kind=$kind", service)
    }

     def makeMockInstanceKindService(kindInfoList: List[SmartCloudKindInfoResponse]): SmartcloudApiService[IO] = {
       val kindResponse = kindInfoList.map(_.kind)
       val kindInfo = kindInfoList.map(i => i.kind ->  i).toMap
       new SmartcloudApiService[IO]:
         def getKindPrices(kind: String): IO[Either[ServerError, SmartCloudKindInfoResponse]] =
           EitherT.fromOption[IO](kindInfo.get(kind), ServerError(Status.NotFound, "instance.kind.invalid")).value

         def getAllKinds: IO[Either[ServerError, List[String]]] =
           EitherT.rightT[IO, ServerError](kindResponse).value
     }
  import Util.*

  val instantKindMockService = makeMockInstanceKindService(List(
    SmartCloudKindInfoResponse("kind1", 0.444),
    SmartCloudKindInfoResponse("kind2", 0.446),
    SmartCloudKindInfoResponse("kind3", 0.546),
  ))

  test("instance kinds are return with Ok") {
    val request = Request[IO](Method.GET, uri"/instance-kinds")
    val route = InstanceKindRoutes(instantKindMockService).route.orNotFound
    val response = route(request)
    assertIO(response.map(_.status), Status.Ok)
    given EntityDecoder[IO, List[Map[String, String]]] = jsonOf
    assertIO(response.flatMap(_.as[List[Map[String, String]]]),
      List(Map("kind" -> "kind1") ,Map("kind" -> "kind2"), Map("kind" -> "kind3")))
  }

  test("when smart cloud price available for kind1 response is Ok") {
    val response = priceLookupByKind("kind1", instantKindMockService)
    assertIO(response.map(_.status), Status.Ok)
    assertIO(response.flatMap(_.as[String]), """{"kind":"kind1","amount":0.444}""")
  }

  test("when kind parameter is not available the response with NotFound") {
    val response = priceLookupByKind("kindnotavailable", instantKindMockService)
    val status = response.map(_.status).unsafeRunSync()
    assertEquals(status, Status.NotFound)
  }

  test("when kind query parameter then respond with BadRequest") {
    val response = priceLookup(s"/prices", instantKindMockService)
    val status = response.map(_.status).unsafeRunSync()
    assertEquals(status, Status.BadRequest)
  }
