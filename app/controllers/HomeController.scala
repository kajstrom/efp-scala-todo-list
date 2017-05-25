package controllers

import javax.inject._

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.i18n._

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
    Ok(views.html.index(todoForm))
  }

  def add = Action { implicit request =>
    val errorFunction = { formWithErrors: Form[TodoData] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.index(formWithErrors))
    }

    val successFunction = { data: TodoData =>
      // This is the good case, where the form was successfully parsed as a Data.

      Redirect(routes.HomeController.index()).flashing("info" -> "Todo task added!")
    }

    val formValidationResult = todoForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}

case class TodoData(todo: String)