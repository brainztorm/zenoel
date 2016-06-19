package controllers

import scala.concurrent.stm._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import events._
import models._
import support.Mappings._
import javax.inject._

import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._


class PostsController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  /**
    * A Scala STM reference holding the current state of the application, derived from all committed events.
    */

    val initialPosts =   // Some handy test data.
    Posts.fromHistory(
      PostAdded(PostId.fromString("PostId(4e885ffe-870e-45b4-b5dd-f16d381d6f6a)").get, PostContent("Mark", "The power of feedback in Scrum", "Searching the web for ...")),
      PostAdded(PostId.fromString("PostId(4e885ffe-870e-45b4-b5dd-f16d381d6f6f)").get, PostContent("Erik", "Picking the right abstraction", "Recently I had to ...")),
      PostAdded(PostId.fromString("PostId(4e885ffe-870e-45b4-b5dd-f16d381d6f6c)").get, PostContent("Michael", "Architect in Scrum", "Last friday I gave ...")))

  val posts = Ref(initialPosts).single

  /**
    * Commits an event and applies it to the current state.
    */
  def commit(event: PostEvent): Unit = {
    posts.transform(_.apply(event))
    Logger.debug("Committed event: " + event)
  }

  /**
    * Show an overview of the most recent blog posts.
    */
  def index = Action { implicit request =>
    Ok(views.html.posts.index(posts().mostRecent(20)))
  }

  /**
    * Show a specific blog post.
    */
  def show(id: PostId) = Action { implicit request =>
    posts().get(id) match {
      case Some(post) => Ok(views.html.posts.show(post))
      case None       => notFound
    }
  }

  /**
    * Show and submit actions for adding a new blog post.
    */
  def showNewPostForm = Action { implicit request =>
    Ok(views.html.posts.add(PostId.generate(), postContentForm))
  }

  def submitNewPost(id: PostId) = Action { implicit request =>
    postContentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.posts.add(id, formWithErrors)),
      postContent => {
        commit(PostAdded(id, postContent))
        Redirect(controllers.routes.PostsController.show(id))
        //Redirect(controllers.PostsController.show(id))
      })
  }

  /**
    * Show and submit actions for editing an existing blog post.
    */
  def showEditForm(id: PostId) = Action { implicit request =>
    posts().get(id) match {
      case Some(post) => Ok(views.html.posts.edit(id, postContentForm.fill(post.content)))
      case None       => notFound
    }
  }

  def submitEditForm(id: PostId) = Action { implicit request =>
    postContentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.posts.edit(id, formWithErrors)),
      postContent => {
        commit(PostEdited(id, postContent))
        Redirect(routes.PostsController.show(id)).flashing("info" -> "Post saved.")
      })
  }

  /**
    * Delete a blog post.
    */
  def delete(id: PostId) = Action { implicit request =>
    commit(PostDeleted(id))
    Redirect(routes.PostsController.index).flashing("info" -> "Post deleted.")
  }

  /*
   * Blog content form definition.
   */
  private val postContentForm = Form(mapping(
    "author" -> trimmedText.verifying(minLength(3)),
    "title" -> trimmedText.verifying(minLength(3)),
    "body" -> trimmedText.verifying(minLength(3)))(PostContent.apply)(PostContent.unapply))

  /**
    * 404 Not Found response.
    */
  private[this] def notFound(implicit request: Request[_]): Result = NotFound(<h1>Page not found</h1>)


}