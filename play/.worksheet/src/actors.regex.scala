package actors

object regex {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(74); 
  println("Welcome to the Scala worksheet");$skip(49); 

  val dateP1 = """(\d\d\d\d)-(\d\d)-(\d\d)""".r;System.out.println("""dateP1  : scala.util.matching.Regex = """ + $show(dateP1 ));$skip(189); 

  val copyright: String = dateP1 findFirstIn "Date of this document: 2011-07-15" match {
    case Some(dateP1(year, month, day)) => "Copyright " + year
    case None => "No copyright"
  };System.out.println("""copyright  : String = """ + $show(copyright ));$skip(691); 

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
  };System.out.println("""getStartTimeDetails: (str: String)(Option[Int], Option[Int], Option[Int])""");$skip(50); val res$0 = 

  getStartTimeDetails("10:30 pm - 1:30 am (21)");System.out.println("""res0: (Option[Int], Option[Int], Option[Int]) = """ + $show(res$0));$skip(33); val res$1 = 
  getStartTimeDetails("1:00 am");System.out.println("""res1: (Option[Int], Option[Int], Option[Int]) = """ + $show(res$1));$skip(43); val res$2 = 
  getStartTimeDetails("2:00 pm - 3:00 pm");System.out.println("""res2: (Option[Int], Option[Int], Option[Int]) = """ + $show(res$2));$skip(42); val res$3 = 
  getStartTimeDetails("Anything goes...");System.out.println("""res3: (Option[Int], Option[Int], Option[Int]) = """ + $show(res$3));$skip(279); 

  def getEndTimeDetails(str: String): Option[(Int, Int, Int)] = {
    val sTime = """- +(\d+):(\d\d) +(am|pm)""".r
    sTime findFirstIn str match {
      case Some(sTime(hh, mm, ampm)) => (Some(hh toInt, mm toInt, if(ampm == "pm") 12 else 0))
      case None => None
    }
  };System.out.println("""getEndTimeDetails: (str: String)Option[(Int, Int, Int)]""");$skip(47); val res$4 = 
  getEndTimeDetails("10:30 pm - 1:30 am (21)");System.out.println("""res4: Option[(Int, Int, Int)] = """ + $show(res$4));$skip(31); val res$5 = 
  getEndTimeDetails("1:00 am");System.out.println("""res5: Option[(Int, Int, Int)] = """ + $show(res$5));$skip(41); val res$6 = 
  getEndTimeDetails("2:00 pm - 3:00 pm");System.out.println("""res6: Option[(Int, Int, Int)] = """ + $show(res$6));$skip(40); val res$7 = 
  getEndTimeDetails("Anything goes...");System.out.println("""res7: Option[(Int, Int, Int)] = """ + $show(res$7));$skip(42); 

  val endDatePattern = """\((\d+)\)""".r;System.out.println("""endDatePattern  : scala.util.matching.Regex = """ + $show(endDatePattern ));$skip(139); val res$8 = 
  endDatePattern findFirstIn "10:30 pm - 1:30 am (21)" match {
  	case Some(endDatePattern(dd)) => Some(dd toInt)
  	case None => None
  };System.out.println("""res8: Option[Int] = """ + $show(res$8));$skip(134); val res$9 = 

  endDatePattern findFirstIn "2:00 pm - 3:00 pm" match {
  	case Some(endDatePattern(dd)) => Some(dd toInt)
  	case None => None
  };System.out.println("""res9: Option[Int] = """ + $show(res$9))}


}
