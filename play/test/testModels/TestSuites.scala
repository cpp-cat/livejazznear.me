/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testModels;

import org.scalatest.Suites

class TestSuite extends Suites (
  new PartySpec,
  new EventSpec,
  new SourceSpec,
  new WebsiteSpec)