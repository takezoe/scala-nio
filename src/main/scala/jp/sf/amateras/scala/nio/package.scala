package jp.sf.amateras.scala

import java.io._
import scala.language.implicitConversions

package object nio {

  def using[A, Z](r1: CloseableResource[A])(action: (A) => Z): Z = try {
    action(r1.resource)
  } finally {
    r1.close()
  }

  def using[A, B, Z](r1: CloseableResource[A], r2: CloseableResource[B])(action: (A, B) => Z): Z = try {
    action(r1.resource, r2.resource)
  } finally {
    r1.close()
    r2.close()
  }

  def using[A, B, C, Z](r1: CloseableResource[A], r2: CloseableResource[B], r3: CloseableResource[C])(action: (A, B, C) => Z): Z = try {
    action(r1.resource, r2.resource, r3.resource)
  } finally {
    r1.close()
    r2.close()
    r3.close()
  }

  /**
   * Provides implicit conversion to [[jp.sf.amateras.scala.nio.CloseableResource]] for [[java.lang.AutoCloseable]].
   */
  implicit def AutoCloseableResource[T <: AutoCloseable](closeable: T) = new CloseableResource[T](){
    val resource = closeable
    def close() = closeQuietly(closeable)
  }

  /**
   * Close AutoCloseable with no exceptions. This is a utility method for internal use.
   */
  private[nio] def closeQuietly(closeable: AutoCloseable): Unit = {
    if(closeable != null){
      try {
        closeable.close()
      } catch {
        case ex: Exception =>
      }
    }
  }

  implicit class FileOps(file: File){
    lazy val extension = FileUtils.getExtension(file)

    def readAsBytes() = FileUtils.readAsBytes(file)
    def readAsString(charset: String = "UTF-8") = FileUtils.readAsString(file, charset)
    def write(content: Array[Byte]) = FileUtils.write(file, content)
    def write(content: String, charset: String = "UTF-8") = FileUtils.write(file, content, charset)
    def remove() = FileUtils.remove(file)
    def moveTo(dest: File) = FileUtils.move(file, dest)
    def copyTo(dest: File) = FileUtils.copy(file, dest)
    def lines(charset: String = "UTF-8") = FileUtils.readLines(file, charset)
    def bytes(chunkSize: Int = 1024 * 8) = FileUtils.readBytes(file, chunkSize)
  }

}
