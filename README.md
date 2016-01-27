scala-nio
=========

Simple I/O library for Scala

## File I/O


## JDBC

```scala
// Only add this line to use the scala-jdbc
import jp.sf.amateras.scala.nio.jdbc._

// Get JDBC connection by any way
using(DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/test", "root", "root")){ conn =>

  // Update
  conn.update(
    sql"INSERT INTO ARTICLE (TITLE, AUTHOR CONTENT) VALUES ($title, $author, $content)")

  // Select the first column in the first row as Int
  val count = conn.selectInt("SELECT COUNT(*) FROM ARTICLE")

  // Select rows as the given retriever
  val articles: Seq[Map[String, Any]] =
    conn.selectInt(sql"SELECT * FROM ARTICLE WHERE AUTHOR = $author"){ rs =>
      Map(
        "id"      => rs.getInt("ID"),
        "title"   => rs.getString("TITLE"),
        "author"  => rs.getString("AUTHOR"),
        "content" => rs.getString("CONTENT")
      )
    }
}
```

## Network

