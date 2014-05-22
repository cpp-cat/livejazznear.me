/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package models

import org.joda.time._
import org.joda.time.format._
import anorm._

// -----------------------------------------------------------------------------------------------------------
// Helper object to integrate org.joda-time.DateTime with anorm
// -----------------------------------------------------------------------------------------------------------

object AnormExtension {


  //* This will not longer be needed in Play 2.3
  implicit def columnToFloat: Column[Float] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case f: Float => Right(f)
      case l: Long => Right(l.floatValue)
      case i: Int => Right(i.toFloat)
      case s: Short => Right(s.toFloat)
      case b: Byte => Right(b.toFloat)
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Float for column $qualified"))
    }
  }

  //* This will not longer be needed in Play 2.3
  implicit val floatToStatement = new ToStatement[Float] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Float): Unit = {
      s.setFloat(index, aValue)
    }
  }
  
  // Implicit converter to DateTime
  implicit def columnToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
      case d: java.sql.Date => Right(new DateTime(d.getTime))
      case time: Long => Right(new DateTime(time))												// time is the milliseconds from 1970-01-01T00:00:00Z using ISOChronology in the default time zone
      case str: java.lang.String => Right(ISODateTimeFormat.dateTime().parseDateTime(str))		// ISO standard format for datetime, which is yyyy-MM-dd'T'HH:mm:ss.SSSZZ
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Date for column $qualified"))
    }
  }

  // Implicit converter of DateTime to sql.Timestamp
  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.withMillisOfSecond(0).getMillis()))
    }
  }


  // Implicit converter to LocalDate
  implicit def columnToLocalDate: Column[LocalDate] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case d: java.sql.Date => Right(new LocalDate(d.getTime()))
      case time: Long => Right(new LocalDate(time))													// time is the milliseconds from 1970-01-01T00:00:00Z using ISOChronology in the default time zone
      case str: java.lang.String => Right(ISODateTimeFormat.dateParser().parseLocalDate(str))		// ISO standard format for datetime, which is yyyy-MM-dd'T'HH:mm:ss.SSSZZ
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Date for column $qualified"))
    }
  }

  // Implicit converter of LocalDate to sql.Date
  implicit val localDateToStatement = new ToStatement[LocalDate] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LocalDate): Unit = {
      s.setDate(index, new java.sql.Date(aValue.toDate().getTime()))
    }
  }
  
}