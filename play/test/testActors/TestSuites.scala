/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testActors;

import org.scalatest.Suites

class TestSuite extends Suites (
  new HtmlCleanerSpec,
  new HotHouseJazzSpec,
  new CrawlerHelperSpec)