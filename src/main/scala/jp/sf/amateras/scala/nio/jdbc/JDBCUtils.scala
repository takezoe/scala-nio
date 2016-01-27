package jp.sf.amateras.scala.nio.jdbc

import java.sql._
import scala.reflect.ClassTag

import jp.sf.amateras.scala.nio._

object JDBCUtils {

  def update(conn: Connection, template: SqlTemplate): Int = {
    execute(conn, template){ stmt =>
      stmt.executeUpdate()
    }
  }

  def select[T](conn: Connection, template: SqlTemplate)(f: ResultSet => T): Seq[T] = {
    execute(conn, template){ stmt =>
      using(stmt.executeQuery()){ rs =>
        val list = new scala.collection.mutable.ListBuffer[T]
        while(rs.next){
          list += f(rs)
        }
        list.toSeq
      }
    }
  }

  def selectInt(conn: Connection, template: SqlTemplate): Int = selectFirst[Int](conn, template).getOrElse(0)

  def selectString(conn: Connection, template: SqlTemplate): String = selectFirst[String](conn, template).getOrElse("")

  def selectFirst[T](conn: Connection, template: SqlTemplate)(implicit m: ClassTag[T]): Option[T] = {
    execute(conn, template){ stmt =>
      using(stmt.executeQuery()){ rs =>
        if(rs.next){
          Some(TypeMapper.get[T](rs, 1))
        } else {
          None
        }
      }
    }
  }

  private def execute[T](conn: Connection, template: SqlTemplate)(f: (PreparedStatement) => T): T = {
    using(conn.prepareStatement(template.sql)){ stmt =>
      template.params.zipWithIndex.foreach { case (x, i) =>
        TypeMapper.set(stmt, i + 1, x)
      }
      f(stmt)
    }
  }

}

case class SqlTemplate(sql: String, params: Any*)