package jp.sf.amateras.scala

package object nio {

  def using[A, Z](r1: Resource[A])(action: (A) => Z): Z = try {
    action(r1.resource)
  } finally {
    if(r1 != null){ r1.close() }
  }

  def using[A, B, Z](r1: Resource[A], r2: Resource[B])(action: (A, B) => Z): Z = try {
    action(r1.resource, r2.resource)
  } finally {
    if(r1 != null){ r1.close() }
    if(r2 != null){ r2.close() }
  }

  def using[A, B, C, Z](r1: Resource[A], r2: Resource[B], r3: Resource[C])(action: (A, B, C) => Z): Z = try {
    action(r1.resource, r2.resource, r3.resource)
  } finally {
    if(r1 != null){ r1.close() }
    if(r2 != null){ r2.close() }
    if(r3 != null){ r3.close() }
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


}
