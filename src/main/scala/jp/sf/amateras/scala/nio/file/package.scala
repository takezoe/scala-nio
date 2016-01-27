package jp.sf.amateras.scala.nio

import java.io._
import scala.language.implicitConversions

package object file {

  /**
   * Implicit class for [[java.io.File]] to add some usable fields and methods.
   * Implementation of added features are provided in [[FileUtils]].
   */
  implicit class FileOps(file: File){
    lazy val extension = FileUtils.getExtension(file)

    def readAsBytes(): Array[Byte] = FileUtils.readAsBytes(file)
    def readAsString(charset: String = "UTF-8"): String = FileUtils.readAsString(file, charset)
    def write(content: Array[Byte]): Unit = FileUtils.write(file, content)
    def write(content: String, charset: String = "UTF-8"): Unit = FileUtils.write(file, content, charset)
    def write(in: InputStream): Unit = FileUtils.write(file, in)
    def writeTo(out: OutputStream): Unit = FileUtils.writeTo(file, out)
    def remove(): Unit = FileUtils.remove(file)
    def moveTo(dest: File): Unit = FileUtils.move(file, dest)
    def copyTo(dest: File): Unit = FileUtils.copy(file, dest)
    def foreachLines(charset: String = "UTF-8")(f: String => Unit): Unit = FileUtils.foreachLines(file, charset)(f)
    def foreachBytes(chunkSize: Int = 1024 * 8)(f: Array[Byte] => Unit): Unit = FileUtils.foreachBytes(file, chunkSize)(f)
//    def lines(charset: String = "UTF-8") = FileUtils.readLines(file, charset)
//    def bytes(chunkSize: Int = 1024 * 8) = FileUtils.readBytes(file, chunkSize)
    def findFile(condition: (File) => Boolean): Seq[File] = FileUtils.findFile(file)(condition)
  }

}
