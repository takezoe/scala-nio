package jp.sf.amateras.scala.nio

/**
 * Trait for closeable resources in scala-nio.
 *
 * @tparam T type of the original resource
 */
trait CloseableResource[T] {

  /**
   * the original resource
   */
  val resource: T

  /**
   * Close this resource.
   */
  def close(): Unit

}
