package jp.sf.amateras.scala.nio

import java.io._
import java.util.Arrays

/**
 * Provides some utility methods to operate files and directories.
 */
object FileUtils {

  /**
   * Delete file or directory. If the given file is a directory, this method delete it recursively.
   */
  def remove(file: File): Unit = {
    file match {
      case dir if dir.isDirectory => dir.listFiles.foreach(remove _)
      case _ =>
    }
    if(!file.delete()){
      throw new IOException(s"Failed to delete ${file.getAbsolutePath}")
    }
  }

  /**
   * Move file or directory. This method tries to move using [[java.io.File#renameTo(java.io.File)]] at first.
   * If failed to rename, move by combination of copy and delete.
   */
  def move(file: File, dest: File): Unit = {
    if(!file.renameTo(dest)){
      copy(file, dest)
      remove(file)
    }
  }

  /**
   * Move file or directory.
   *
   * @param file the source file or directory
   * @param dest the destination file or directory
   */
  def copy(file: File, dest: File): Unit = {
    file match {
      case x if x.isDirectory => x.listFiles.foreach { child =>
        copy(child, new File(dest, child.getName))
        if(!dest.mkdir()){
          throw new IOException(s"Failed to create directory ${dest.getAbsolutePath}")
        }
      }
      case x => using(new FileInputStream(x), new FileOutputStream(dest)){ case (in, out) =>
        copyStream(in, out)
      }
    }
  }

  private def copyStream(in: InputStream, out: OutputStream): Unit = {
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
   * Read file content as byte array.
   *
   * @param file the file
   * @return the file content as byte array
   */
  def readAsBytes(file: File): Array[Byte] = {
    using(new FileInputStream(file), new ByteArrayOutputStream()){ (in, out) =>
      copyStream(in, out)
      out.toByteArray
    }
  }

  /**
   * Read file content as string.
   *
   * @param file the file
   * @param charset the character encoding, default is UTF-8.
   * @return the file content as string
   */
  def readAsString(file: File, charset: String = "UTF-8"): String = new String(readAsBytes(file), charset)

  /**
   * Write byte array to file.
   *
   * @param file the file
   * @param content the file content as byte array
   */
  def write(file: File, content: Array[Byte]): Unit = using(new FileOutputStream(file))(_.write(content))

  /**
   * Write string to file.
   *
   * @param file the file
   * @param content the file content as string
   * @param charset the character encoding, default is UTF-8.
   */
  def write(file: File, content: String, charset: String = "UTF-8"): Unit = write(file, content.getBytes(charset))

  /**
   * Returns iterator which returns lines (NOT including newline character(s)).
   *
   * @param file the file
   * @param charset the character encoding, default is UTF-8.
   * @return the stream which returns lines
   */
  def readLines(file: File, charset: String = "UTF-8"): Iterator[String] = {
    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))

    def readLineStream(): Stream[String] = {
      reader.readLine() match {
        case null => {
          closeQuietly(reader)
          Stream.empty
        }
        case line => line #:: readLineStream()
      }
    }

    readLineStream()
  }

  /**
   * Returns iterator which returns bytes with a specified chunk size.
   *
   * @param file the file
   * @param chunkSize the chunk size as bytes, default is 1024 * 8.
   * @return the stream which returns bytes
   */
  def readBytes(file: File, chunkSize: Int = 1024 * 8): Iterator[Array[Byte]] = {
    val in     = new BufferedInputStream(new FileInputStream(file), chunkSize)
    val buffer = new Array[Byte](chunkSize)

    def readBytesStream: Stream[Array[Byte]] = {
      in.read(buffer) match {
        case -1 => {
          closeQuietly(in)
          Stream.empty
        }
        case length => Arrays.copyOf(buffer, length) #:: readBytesStream
      }
    }

    readBytesStream
  }

  def getExtension(file: File): Option[String] = {
    file.getName.lastIndexOf('.') match {
      case -1 => None
      case i  => Some(file.getName.substring(i + 1))
    }
  }

}
