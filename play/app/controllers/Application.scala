/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package controllers

import play.api.mvc._

object Application extends Controller {

  // Need to redo the view and put a login page
  def index = Action {
    
    Ok(views.html.main())
  }

}
