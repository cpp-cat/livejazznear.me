/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testSuites;

import org.scalatest.Suites

class TestSuite extends Suites (
  new testModels.TestSuite,
  new testActors.TestSuite)
