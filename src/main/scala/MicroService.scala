import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.{ActorMaterializer}
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
      complete { "ping" }
    } ~
    path("crash"){
      complete { "boom" }
    }
  }
}

object CustomeRejectionHandler extends PredefinedToResponseMarshallers{
  import StatusCodes._

  private def annonomous(route:Route): Route = complete (HttpResponse(401, entity = "BadBadBad"))

  implicit def jsonRejectionHandler: RejectionHandler = {
    rejctionSeq: scala.collection.immutable.Seq[Rejection] => RejectionHandler.default(rejctionSeq).map(annonomous)
  }
}

object MicroService extends App {

  import Api._
  import CustomeRejectionHandler._

  implicit val system = ActorSystem()
  implicit val exec = system.dispatcher
  implicit val mat = ActorMaterializer()

  Http().bindAndHandle(route ,"172.27.20.184", 8080)

}
