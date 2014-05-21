/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testActors;

import org.scalatest._
import org.scalatestplus.play._
import org.htmlcleaner.HtmlCleaner
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.TagNode

class HtmlCleanerSpec extends PlaySpec with OneAppPerSuite {

  def fixture =
    new {
      val cleaner = new HtmlCleaner
      val props = cleaner.getProperties
      val rootNode = cleaner.clean("""
		<!DOCTYPE html>
		<html>
		    <head>
			    <meta charset="utf-8" />
			    <meta name="format-detection" content="telephone=no" />
		    </head>
		    <body>
		        <div data-role="page" id="home" data-theme="a">
		            <div data-role="header">
		                <h1>GoingPlaces.me</h1>
		                <a href="#left-menu" data-icon="bars" data-iconpos="notext">Menu</a>
		                <a href="#right-search" data-icon="search" data-iconpos="notext">Search</a>
		            </div><!-- /header -->
		            
					<div data-role="main" id="home-main" class="ui-content">	
		                <div id="home-default">
		                    <p class="blink">Home page content goes here.</p>
		                </div>
		                <div id="home-content"/>
			        </div>
		        </div>
		    </body>
		</html> 
        """)
    }

  "The HtmlCleaner class" must {

    "extract cleanly elements by name" in {

      val f = fixture
      f.rootNode.findElementByName("h1", true).getText.toString mustBe "GoingPlaces.me"
    }

    "provide a list of matching elements using XPath expression" in {

      val f = fixture
      val elements = f.rootNode.evaluateXPath("//div/div/a") map {
        case a: TagNode => a.getText().toString()
        case _ => fail()
      }
      elements mustBe Array[Object]("Menu", "Search")
    }

  }
}