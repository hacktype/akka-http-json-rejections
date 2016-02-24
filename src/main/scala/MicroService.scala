import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers
import akka.http.scaladsl.model._



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

  private def annonomous(route:Route): Route = complete (HttpResponse(401, entity = "401 is BaaD M'kay"))

  implicit def jsonRejectionHandler[T <: Rejection: ClassTag]: RejectionHandler = RejectionHandler.newBuilder().handleAll[T] { rejections =>
    RejectionHandler.default(rejections).map(annonomous).getOrElse(complete { HttpResponse(401, entity = "401 is BaaD M'kay") } )
  }.result()

}


import CustomeRejectionHandler.jsonRejectionHandler

object MicroService extends App {

  import Api.route

  implicit val system = ActorSystem()
  implicit val exec = system.dispatcher
  implicit val mat = ActorMaterializer()

  Http().bindAndHandle(route ,"localhost", 8080)

}
