import com.typesafe.scalalogging.Logger

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Date
import scala.io.Source
import scala.math.BigDecimal.RoundingMode

object project_scala extends App {
  val lines = Source.fromFile("src/TRX1000.csv").getLines().drop(1).toList
  val format1 = new SimpleDateFormat("yyyy-MM-dd")
  val format2 = new SimpleDateFormat("MM/dd/yyyy")
  val logger = Logger("name")
  val trans = lines.map {
    element => val pair = element.split(",")
      (format1.parse(pair(0).substring(0,10)),pair(1),format2.parse(pair(2)), pair(3).toInt, pair(4).toDouble, pair(5), pair(6))
  }
  val url = "jdbc:postgresql://localhost:5432/SuperMarket"
  val username = "postgres"
  val password = "123"

  // Load the PostgreSQL JDBC driver
  Class.forName("org.postgresql.Driver")

  // Establish a connection
  val connection: Connection = DriverManager.getConnection(url, username, password)
  val preparedStatement: PreparedStatement = connection.prepareStatement("Delete from Processed_TRX")
  def aCheckExpiry(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if ( ChronoUnit.DAYS.between(x._1.toInstant, x._3.toInstant)  < 30) {
      A(x)
    }
    else 0
  }
  def A(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    x._5* (30-ChronoUnit.DAYS.between(x._1.toInstant, x._3.toInstant))/100
  }
  def bCheckType(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if ( x._2.startsWith("Cheese") || x._2.startsWith("Wine")) {
      B(x)
    }
    else 0
  }
  def B(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if (x._2.startsWith("Cheese")) x._5 * 0.1
    else x._5 * 0.05
  }
  def cCheckDate(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if ( x._1.getMonth == "March" && x._1.getDate == 23) {
      C(x)
    }

    else 0
  }
  def C(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    x._5 * 0.50
  }
  def dCheckQuantity(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if ( x._4 > 5) D(x)
    else 0
  }
  def D(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if (x._4 <= 9) x._5 * 0.05
    else if (x._4 <= 14) x._5 * 0.07
    else x._5 * 0.10
  }
  def eCheckChannel(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if ( x._6.contains("App")) E(x)
    else 1.0
  }
  def E(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if (x._4 % 5 == 0)  x._5 *x._4/100
    else  x._5 * (x._4 + (5 - (x._4 % 5))).toDouble / 100
  }
  def fCheckVisa(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    if ( x._7 == "Visa") F(x)
    else 0.0
  }
  def F(x: Tuple7[Date,String, Date, Int, Double, String, String]):Double ={
    x._5 * 0.05
  }
  def checkDiscount(x: List[Double]): List[Double] = {
    if (x.max > 0)
      x.sorted.reverse.take(2)
    else List(0.0 , 0.0)
  }
  def processTRX(d: Date, prod: String,txD: Date,qty: Int,price: Double,ch: String,payment: String, discount: Double): Int = {
    val sql = "INSERT INTO Processed_TRX VALUES (?, ?,?,?,?,?,?,?,?)"
    val preparedStatement: PreparedStatement = connection.prepareStatement(sql)
    val timestamp = new java.sql.Date(d.getTime)
    val expiryDate = new java.sql.Date(txD.getTime)
    // Set values for the parameters
    preparedStatement.setDate(1, timestamp)
    preparedStatement.setString(2, prod)
    preparedStatement.setDate(3, expiryDate)
    preparedStatement.setInt(4, qty)
    preparedStatement.setDouble(5, price)
    preparedStatement.setString(6, ch)
    preparedStatement.setString(7, payment)
    preparedStatement.setDouble(8, discount)
    preparedStatement.setDouble(9, price-discount)

    if (preparedStatement.executeUpdate() > 0 && discount > 0)
      logger.info(s"Transaction qualifies for a: ${BigDecimal(discount).setScale(2, RoundingMode.HALF_UP).toDouble} Discount")
    else if (preparedStatement.executeUpdate() > 0 && discount == 0)
      logger.info(s"Transaction doesn't qualify for any Discount")
    else
      logger.warn(s"Transaction failed!")

    // Execute the INSERT statement
    preparedStatement.executeUpdate()
  }
  trans.foreach(x => {
    val res = checkDiscount(List(aCheckExpiry(x), bCheckType(x), cCheckDate(x),dCheckQuantity(x),eCheckChannel(x),fCheckVisa(x)))
    if (res.head == 0)
      processTRX(x._1,x._2,x._3,x._4,x._5,x._6,x._7,0.00)
    else {
      if (res(1) == 0)
        processTRX(x._1,x._2,x._3,x._4,x._5,x._6,x._7,res.head)
      else
      processTRX(x._1,x._2,x._3,x._4,x._5,x._6,x._7,(res.head+res(1))/2.00)
    }
  })

}