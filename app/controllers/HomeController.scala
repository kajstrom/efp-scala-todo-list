package controllers

import javax.inject._

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.i18n._
import com.redis._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val todoForm = Form(
    mapping(
      "todo" -> text
    )(TodoData.apply)(TodoData.unapply)
  )

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    val r = new RedisClient("localhost", 6379)
    val todo = r.lrange("todo", 0, r.llen("todo").get.toInt)
    r.disconnect

    Ok(views.html.index(todoForm, todo.get))
  }

  def add = Action { implicit request =>
    val errorFunction = { formWithErrors: Form[TodoData] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      Redirect(routes.HomeController.index()).flashing("info" -> "Todo task was not added! Try again")
    }

    val successFunction = { data: TodoData =>
      val r = new RedisClient("localhost", 6379)
      r.lpush("todo", data.todo)
      r.disconnect

      Redirect(routes.HomeController.index()).flashing("info" -> "Todo task added!")
    }

    val formValidationResult = todoForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}

case class TodoData(todo: String)