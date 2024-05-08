package prices.routes.protocol

import cats.data.EitherT
import cats.effect.IO
import io.circe.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.circe.*
import org.http4s.implicits.*
import prices.data.InstanceKind
import prices.routes.InstanceKindRoutes
import prices.services.InstanceKindService
import prices.services.smartcloud.ServerError

class InstantKindRoutesTest extends CatsEffectSuite:

  def decodeToIkResponse(r: Response[IO]): IO[List[InstanceKindResponse]] = {
    given  EntityDecoder[IO, List[String]] = jsonOf
    r.as[List[String]].map((kinds: List[String]) => kinds.map(k => InstanceKindResponse(InstanceKind(k))))
  }

  def priceLookup(uriStr: String, service: InstanceKindService[IO]): IO[Response[IO]] = {
    val uri = Uri.unsafeFromString(uriStr)
    val request = Request[IO](Method.GET, uri)
    val route = InstanceKindRoutes(service).route.orNotFound
    route(request)
  }
  def priceLookupByKind(kind: String, service: InstanceKindService[IO]): IO[Response[IO]] = {
    priceLookup(s"/prices?kind=$kind", service)
  }

   def make(kindInfoList: List[SmartCloudKindInfoResponse]): InstanceKindService[IO] = {
     val kindResponse = kindInfoList.map(_.kind).map(s => InstanceKindResponse(InstanceKind(s)))
     val kindInfo = kindInfoList.map(i => i.kind ->  i).toMap
     new InstanceKindService[IO]:
       def getKindPrices(kind: String): IO[Either[ServerError, SmartCloudKindInfoResponse]] =
         EitherT.fromOption[IO](kindInfo.get(kind), ServerError(Status.NotFound, "instance.kind.invalid")).value

       def getAllKinds: IO[Either[ServerError, List[InstanceKindResponse]]] =
         EitherT.rightT[IO, ServerError](kindResponse).value
   }
  def makeInstanceKindService(info: SmartCloudKindInfoResponse, kinds: List[String]): InstanceKindService[IO] = {
    val kindResponse = kinds.map(s => InstanceKindResponse(InstanceKind(s)))
    new InstanceKindService[IO]:
      def getKindPrices(kind: String): IO[Either[ServerError, SmartCloudKindInfoResponse]] =
        EitherT.rightT[IO, ServerError](info).value

      def getAllKinds: IO[Either[ServerError, List[InstanceKindResponse]]] =
        EitherT.rightT[IO, ServerError](kindResponse).value
  }

  test("instance kinds are return with Ok") {
    val scInfo = SmartCloudKindInfoResponse("kind1", 0.444, "2024-05-08T00:35:40.896Z")
    val kinds = List("kind1", "kind2", "kind3")
    val service = makeInstanceKindService(scInfo, kinds)

    val request = Request[IO](Method.GET, uri"/instance-kinds")
    val route = InstanceKindRoutes(service).route.orNotFound
    val response = route(request)
    assertIO(response.map(_.status), Status.Ok)
    //assertIO(response.flatMap(decodeToIkResponse), kinds)
  }

  test("when smart cloud price available for kind1 response is Ok") {
    val scInfo = SmartCloudKindInfoResponse("kind1", 0.444, "2024-05-08T00:35:40.896Z")
    val kinds = List("kind1", "kind2", "kind3")
    val service = makeInstanceKindService(scInfo, kinds)
    val response = priceLookupByKind("kind1", service)
    assertIO(response.flatMap(r => r.as[SmartCloudKindInfoResponse]), scInfo)
  }

  test("when kind parameter is not available ") {
    val service = make(List(
      SmartCloudKindInfoResponse("kind1", 0.444, "2024-05-08T00:35:40.896Z"),
      SmartCloudKindInfoResponse("kind2", 0.446, "2024-05-08T00:35:40.896Z")
    ))
    val response = priceLookupByKind("kindnotavailable", service)
    val status = response.map(_.status).unsafeRunSync()
    assertEquals(status, Status.NotFound)
  }

