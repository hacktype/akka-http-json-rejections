import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import scala.concurrent.Future

object Api {
  val route:Route = get {
    pathSingleSlash {
      complete { HttpResponse(200, entity = "cool, you have two options /ping or /crash") }
    } ~
    path("ping") {
      complete { "pong" }
    } ~
    path("crash"){
      complete { "boom" }
    }
  }
}

object CustomeRejectionHandler {
  import scala.reflect.ClassTag
  import akka.http.scaladsl.model.MediaTypes.`application/json`
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit def jsonRejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
    .handleAll[Rejection] { rejections =>

      def prefixEntity(entity: ResponseEntity): ResponseEntity = entity match {
        case HttpEntity.Strict(contentType, data) => {
          import spray.json._
          val text =  data.utf8String
          HttpEntity(ContentTypes.`application/json`, s"""{ "message": "$text" }""")
        }
        case _ => throw new IllegalStateException("Unexpected entity type")
      }
      val route: Route = extractRequestContext { ctx =>
        mapResponseEntity(prefixEntity) {
          RejectionHandler.default.apply(rejections).getOrElse{
            complete(StatusCodes.InternalServerError)
          }
        }
      }
      route
      /*ctx: RequestContext => {

          val route: Route = RejectionHandler.default(rejections).getOrElse( complete { HttpResponse(404, entity = HttpEntity(`application/json`, """{ "message": "404 is BaaD M'kay" }""")) } )

          route.apply(ctx).flatMap { res : RouteResult => res match {
              case RouteResult.Complete(HttpResponse(status, header, entity, protocol)) => {

                implicit val system = ActorSystem("rejection")
                implicit val mat = ActorMaterializer()

                entity.toStrict(5.seconds)(mat) map ( entity => RouteResult.Complete(HttpResponse(status, header, HttpEntity(`application/json`, s"""{ "message": entity.data.utf8String }"""), protocol)))
              }

              case RouteResult.Rejected(seqOfRejections) => Future (RouteResult.Complete( HttpResponse(404, entity = HttpEntity(`application/json`, """{ "message": "404 is BaaD M'kay" }"""))))
            }
          }

        }*/
    }
    .handleNotFound { complete { HttpResponse(404, entity = HttpEntity(`application/json`, """{ "message": "404 is BaaD M'kay" }""")) }}
    .result()

}


import CustomeRejectionHandler.jsonRejectionHandler

object MicroService extends App {

  import Api.route

  implicit val system = ActorSystem()
  implicit val exec = system.dispatcher
  implicit val mat = ActorMaterializer()

  Http().bindAndHandle(route ,"localhost", 8080)

}
