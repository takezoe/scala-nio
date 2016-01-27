package jp.sf.amateras.scala.nio.file

import java.io._

import org.scalatest.FunSuite

class FileUtilsTest extends FunSuite {

  val file = new File("README.md")

//  test("FileUtils#readLines returns Stream[String]"){
//    val sb = new StringBuilder
//    FileUtils.readLines(file).foreach { line =>
//      sb.append(line).append("\n")
//    }
//    assert(sb.toString() == FileUtils.readAsString(file))
//  }
//
//  test("FileUtils#readBytes returns Stream[Array[Byte]]"){
//    val content = using(new ByteArrayOutputStream()){ out =>
//      FileUtils.readBytes(file, 1).foreach { bytes =>
//        out.write(bytes)
//      }
//      new String(out.toByteArray, "UTF-8")
//    }
//    assert(content == FileUtils.readAsString(file))
//  }

  test("FileUtils#getExtension returns Option[String]"){
    assert(None == FileUtils.getExtension(new File("test")))
    assert(Some("md") == FileUtils.getExtension(file))
  }

  test("FileUtils#write creates a new file if it does not exist"){
    val file = new File("test.txt")
    try {
      file.write("abc")
      assert(file.exists)
      assert(file.isFile)
      assert(file.readAsString() == "abc")
    } finally {
      file.delete()
    }
  }

}
