package jp.sf.amateras.scala.nio

import java.io._

object StreamUtils {

  /**
   * Copy contents of the input stream to the output stream.
   * Both of streams is not closed in this method.
   *
   * @param in the input stream
   * @param out the output stream
   */
  def transfer(in: InputStream, out: OutputStream): Unit = {
    val buf = new Array[Byte](1024 * 8)
    var length = 0
    while(length != -1){
      length = in.read(buf)
      if(length > 0){
        out.write(buf, 0, length)
      }
    }
  }

  /**
   * Read contents of the input stream as byte array.
   * The input stream is not closed in this method.
   *
   * @param in the input stream
   * @return the input stream contents as byte array
   */
  def readAsBytes(in: InputStream): Array[Byte] = {
    using(new ByteArrayOutputStream()){ out =>
      transfer(in, out)
      out.toByteArray
    }
  }

  /**
   * Read contents of the input stream as string.
   * The input stream is not closed in this method.
   *
   * @param in the input stream
   * @param charset the character encoding, default is UTF-8.
   * @return the input stream contents as string
   */
  def readAsString(in: InputStream, charset: String = "UTF-8"): String =
    new String(readAsBytes(in), charset)

  /**
   * Process each lines of the file.
   * The input stream is not closed in this method.
   *
   * @param in the input stream
   * @param charset the character encoding, default is UTF-8.
   * @param f the function to process lines
   */
  def foreachLines(in: InputStream, charset: String = "UTF-8")(f: String => Unit): Unit = {
    val reader = new BufferedReader(new InputStreamReader(in, charset))
    var line: String = reader.readLine()
    while(line != null){
      f(line)
      line = reader.readLine()
    }
  }

  /**
   * Process each chunked bytes of the file.
   * The input stream is not closed in this method.
   *
   * @param in the input stream
   * @param chunkSize the chunk size (bytes), default is 1024 * 8.
   * @param f the function to process lines
   */
  def foreachBytes(in: InputStream, chunkSize: Int = 1024 * 8)(f: Array[Byte] => Unit): Unit = {
    val bytes = new Array[Byte](chunkSize)
    var length = in.read(bytes)
    while(length != -1){
      f(bytes)
      length = in.read(bytes)
    }
  }

}
