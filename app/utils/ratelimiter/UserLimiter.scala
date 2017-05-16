package utils.ratelimiter

import java.util.UUID

import akka.actor.ActorSystem
import com.digitaltangible.playguard.{ RateLimitActionFilter, RateLimiter }
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.language.higherKinds

/**
 * A Limiter for user logic
 *
 */
object UserLimiter {

  type Secured[B] = SecuredRequest[DefaultEnv, B]

  /**
   * A Rate limiter Function for
   *
   * @param rateLimiter The rate limiter implementation
   * @param reject The function to apply on reject
   * @param requestKeyExtractor The Request Parameter we want to filter from
   * @param actorSystem The implicit Akka Actor system
   * @tparam K
   * @return
   */
  def apply[T <: Env, R[_] <: SecuredRequest[T, _], K](rateLimiter: RateLimiter)(reject: R[_] => Result, requestKeyExtractor: R[_] => K)(implicit actorSystem: ActorSystem): RateLimitActionFilter[R] with ActionFunction[R, R] = {
    new RateLimitActionFilter[R](rateLimiter)(reject, requestKeyExtractor) with ActionFunction[R, R]
  }

  /**
   * A default user filter implementation
   *
   * @param ac
   * @return
   */
  def defaultUserFilter(implicit ac: ActorSystem): RateLimitActionFilter[Secured] with ActionFunction[Secured, Secured] = {
    (apply[DefaultEnv, Secured, UUID](new RateLimiter(3, 1f / 10, "Default User Limiter"))(_ => Results.TooManyRequests("You've been refreshing too much. Please try again in 10 seconds"), r => r.identity.userID))
  }

}
