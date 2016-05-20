scala-nio
=========

Simple I/O library for Scala

```scala
resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

libraryDependencies += "jp.sf.amateras" %% "scala-nio" % "0.0.1"
```

## Loan pattern

Loan pattern is available for `AutoCloseable` resources. Also some utility methods are added to `java.io.InputStream` and `java.io.OutputStream`.

```scala
import jp.sf.amateras.scala.nio._

// Loan pattern
using(new java.io.FileInputStream("test.txt")){ in =>
  val bytes = in.readAsBytes()
  ...
}

// Loan pattern for 2 resources
using(new java.io.FileInputStream("test.txt"), 
      new java.io.FileOutputStream("test.bak")){ case (in, out) =>
  in.writeTo(out)
}
```

## File

Extends `java.io.File` to add methods to enforce file I/O and operation easily.

```scala
import jp.sf.amateras.scala.nio.file._

val file = new java.io.File("test.txt")

// Read from file
val bytes = file.readAsBytes()
val string = file.readAsString("UTF-8")

// Write to file
file.write(bytes)
file.write(string, "UTF-8")

// Process lines
file.foreachLines { line =>
  ...
}

// File operations
file.moveTo(new java.io.File("test.bak"))
file.copyTo(new java.io.File("test.bak"))
file.remove() // If file is directory, removed recursively
```

## JDBC

Extends `java.sql.Connection` to add methods to select and update database easily.

```scala
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

TODO
