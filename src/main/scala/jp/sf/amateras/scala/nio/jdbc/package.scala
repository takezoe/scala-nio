package jp.sf.amateras.scala.nio

import java.sql._

import scala.reflect.ClassTag

package object jdbc {

  implicit class ConnectionOps(conn: Connection){
    def update(template: SqlTemplate): Int = JDBCUtils.update(conn, template)
    def select[T](template: SqlTemplate)(f: ResultSet => T): Seq[T] = JDBCUtils.select(conn, template)(f)
    def selectInt(template: SqlTemplate): Int = JDBCUtils.selectInt(conn, template)
    def selectString(template: SqlTemplate): String = JDBCUtils.selectString(conn, template)
    def selectFirst[T](template: SqlTemplate)(implicit m: ClassTag[T]): Option[T] = JDBCUtils.selectFirst(conn, template)(m)
  }

  /**
   * Implicit conversion to convert a raw string with no parameter to SqlTemplate
   */
  implicit def String2SqlTemplate(sql: String) = SqlTemplate(sql)

  /**
   * String interpolation to write variable embeddable SQL
   */
  implicit class SqlStringInterpolation(val sc: StringContext) extends AnyVal {
    def sql(args: Any*): (String, Seq[Any]) = (sc.parts.mkString("?"), args.toSeq)
  }

}
