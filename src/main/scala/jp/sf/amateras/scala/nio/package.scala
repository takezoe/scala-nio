package jp.sf.amateras.scala

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
  private def closeQuietly(closeable: AutoCloseable): Unit = {
    if(closeable != null){
      try {
        closeable.close()
      } catch {
        case ex: Exception =>
      }
    }
  }

}
