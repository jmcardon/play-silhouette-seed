package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.auth.DefaultEnv
import utils.ratelimiter.UserLimiter

import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry,
  implicit val webJarAssets: WebJarAssets)(implicit ac: ActorSystem)
  extends Controller with I18nSupport {

  private val defaultUserLimiter = UserLimiter.defaultUserFilter

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index: Action[AnyContent] = (silhouette.SecuredAction andThen defaultUserLimiter).async { implicit request =>

    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
