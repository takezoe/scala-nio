package jp.sf.amateras.scala

import java.io._
import scala.language.implicitConversions

package object nio {

  def using[A, Z](r1: Resource[A])(action: (A) => Z): Z = try {
    action(r1.resource)
  } finally {
    r1.close()
  }

  def using[A, B, Z](r1: Resource[A], r2: Resource[B])(action: (A, B) => Z): Z = try {
    action(r1.resource, r2.resource)
  } finally {
    r1.close()
    r2.close()
  }

  def using[A, B, C, Z](r1: Resource[A], r2: Resource[B], r3: Resource[C])(action: (A, B, C) => Z): Z = try {
    action(r1.resource, r2.resource, r3.resource)
  } finally {
    r1.close()
    r2.close()
    r3.close()
  }

  /**
   * Trait for closeable resources in scala-nio.
   *
   * @tparam T type of the original resource
   */
  trait Resource[T] {

    /**
     * the original resource
     */
    val resource: T

    /**
     * Close this resource.
     */
    def close(): Unit

  }

  /**
   * Provides implicit conversion to [[jp.sf.amateras.scala.nio.Resource]] for [[java.lang.AutoCloseable]].
   */
  implicit def AutoCloseableResource[T <: AutoCloseable](closeable: T) = new Resource[T](){
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

  /**
   * Implicit class for [[java.io.File]] to add some usable fields and methods.
   * Implementation of added features are provided in [[jp.sf.amateras.scala.nio.FileUtils]].
   */
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
    def findFile(condition: (File) => Boolean) = FileUtils.findFile(file)(condition)
  }

}
