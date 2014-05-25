package actors

object regex {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  val dateP1 = """(\d\d\d\d)-(\d\d)-(\d\d)""".r   //> dateP1  : scala.util.matching.Regex = (\d\d\d\d)-(\d\d)-(\d\d)

  val copyright: String = dateP1 findFirstIn "Date of this document: 2011-07-15" match {
    case Some(dateP1(year, month, day)) => "Copyright " + year
    case None => "No copyright"
  }                                               //> copyright  : String = Copyright 2011

  /*
   *  possible format for input string:
   *		"10:30 pm - 1:30 am (21)" returns (Some(10*60+30+12*60), Some(60+30), Some(21))
   *		"1:00 pm"				  return (Some(60+12*60), None, None)
   *		"2:00 pm - 3:00 pm"		  returns (Some(2*60+12*60), Some(3*60), None)
   *		"Anything goes..."		  returns (None, None, None)
   *
   * @returns (startTime, endTime, endDay)
   */
  def getStartTimeDetails(str: String): (Option[Int], Option[Int], Option[Int]) = {
    val sTime = """(\d+):(\d\d) +(am|pm)""".r
    sTime findFirstIn str match {
      case Some(sTime(hh, mm, ampm)) => (Some(hh toInt), Some(mm toInt), Some(if(ampm == "pm") 12 else 0))
      case None => (None, None, None)
    }
  }                                               //> getStartTimeDetails: (str: String)(Option[Int], Option[Int], Option[Int])

  getStartTimeDetails("10:30 pm - 1:30 am (21)")  //> res0: (Option[Int], Option[Int], Option[Int]) = (Some(10),Some(30),Some(12)
                                                  //| )
  getStartTimeDetails("1:00 am")                  //> res1: (Option[Int], Option[Int], Option[Int]) = (Some(1),Some(0),Some(0))
  getStartTimeDetails("2:00 pm - 3:00 pm")        //> res2: (Option[Int], Option[Int], Option[Int]) = (Some(2),Some(0),Some(12))
  getStartTimeDetails("Anything goes...")         //> res3: (Option[Int], Option[Int], Option[Int]) = (None,None,None)

  def getEndTimeDetails(str: String): Option[(Int, Int, Int)] = {
    val sTime = """- +(\d+):(\d\d) +(am|pm)""".r
    sTime findFirstIn str match {
      case Some(sTime(hh, mm, ampm)) => (Some(hh toInt, mm toInt, if(ampm == "pm") 12 else 0))
      case None => None
    }
  }                                               //> getEndTimeDetails: (str: String)Option[(Int, Int, Int)]
  getEndTimeDetails("10:30 pm - 1:30 am (21)")    //> res4: Option[(Int, Int, Int)] = Some((1,30,0))
  getEndTimeDetails("1:00 am")                    //> res5: Option[(Int, Int, Int)] = None
  getEndTimeDetails("2:00 pm - 3:00 pm")          //> res6: Option[(Int, Int, Int)] = Some((3,0,12))
  getEndTimeDetails("Anything goes...")           //> res7: Option[(Int, Int, Int)] = None

  val endDatePattern = """\((\d+)\)""".r          //> endDatePattern  : scala.util.matching.Regex = \((\d+)\)
  endDatePattern findFirstIn "10:30 pm - 1:30 am (21)" match {
  	case Some(endDatePattern(dd)) => Some(dd toInt)
  	case None => None
  }                                               //> res8: Option[Int] = Some(21)

  endDatePattern findFirstIn "2:00 pm - 3:00 pm" match {
  	case Some(endDatePattern(dd)) => Some(dd toInt)
  	case None => None
  }                                               //> res9: Option[Int] = None


}