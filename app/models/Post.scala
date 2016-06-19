package models

import events._

/**
  * A specific blog post with its current content.
  */
case class Post(id: PostId, content: PostContent)

/**
  * The current state of blog posts, derived from all committed PostEvents.
  */
case class Posts(postsRepo: Map[PostId, Post] = Map.empty, orderedByTimeAdded: Seq[PostId] = Vector.empty) {

  def get(postId: PostId): Option[Post] = postsRepo.get(postId) // retourne de la map postsRepo le post d'id postId

  def mostRecent(n: Int): Seq[Post] = orderedByTimeAdded.takeRight(n).reverse.map(postsRepo)


  def apply(event: PostEvent): Posts = event match {
    case PostAdded(id, content) =>
      this.copy(postsRepo = postsRepo.updated(id, Post(id, content)), orderedByTimeAdded = orderedByTimeAdded :+ id)
    case PostEdited(id, content) =>
      this.copy(postsRepo = postsRepo.updated(id, postsRepo(id).copy(content = content)))
    case PostDeleted(id) =>
      this.copy(postsRepo = postsRepo - id, orderedByTimeAdded = orderedByTimeAdded.filterNot(_ == id))
  }

}
object Posts {
  def fromHistory(events: PostEvent*): Posts = events.foldLeft(Posts())(_ apply _)
}