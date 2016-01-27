package jp.sf.amateras.scala.nio.file

import java.io._

import jp.sf.amateras.scala.nio._

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
  def move(file: File, dest: File): Unit = if(!file.renameTo(dest)){
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
  def copy(file: File, dest: File): Unit = file match {
    case x if x.isDirectory => x.listFiles.foreach { child =>
      copy(child, new File(dest, child.getName))
      if(!dest.mkdir()){
        throw new IOException(s"Failed to create directory ${dest.getAbsolutePath}")
      }
    }
    case x => using(new FileInputStream(x), new FileOutputStream(dest)){ case (in, out) =>
      StreamUtils.transfer(in, out)
    }
  }

  /**
   * Read file content as byte array.
   *
   * @param file the file
   * @return the file content as byte array
   */
  def readAsBytes(file: File): Array[Byte] = using(new FileInputStream(file))(StreamUtils.readAsBytes)

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
   * Write stream contents to file.
   *
   * @param file the file
   * @param in the input stream
   */
  def write(file: File, in: InputStream): Unit = using(new FileOutputStream(file)){ out =>
    StreamUtils.transfer(in, out)
  }

  /**
   * Write file contents to stream.
   *
   * @param file the file
   * @param out the output stream
   */
  def writeTo(file: File, out: OutputStream): Unit = using(new FileInputStream(file)){ in =>
    StreamUtils.transfer(in, out)
  }

  /**
   * Process each lines of the file.
   *
   * @param file the file
   * @param charset the character encoding, default is UTF-8.
   * @param f the function to process lines
   */
  def foreachLines(file: File, charset: String = "UTF-8")(f: String => Unit): Unit =
    using(new FileInputStream(file)){ in =>
      StreamUtils.foreachLines(in, charset)(f)
    }

  /**
   * Process each chunked bytes of the file.
   *
   * @param file the file
   * @param chunkSize the chunk size (bytes), default is 1024 * 8.
   * @param f the function to process lines
   */
  def foreachBytes(file: File, chunkSize: Int = 1024 * 8)(f: Array[Byte] => Unit): Unit =
    using(new FileInputStream(file)){ in =>
      StreamUtils.foreachBytes(in, chunkSize)(f)
    }

//  /**
//   * Returns stream which returns lines (NOT including newline character(s)).
//   *
//   * @param file the file
//   * @param charset the character encoding, default is UTF-8.
//   * @return the stream which returns lines
//   */
//  def readLines(file: File, charset: String = "UTF-8"): Stream[String] = {
//    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))
//
//    def readLineStream(): Stream[String] = {
//      reader.readLine() match {
//        case null => {
//          closeQuietly(reader)
//          Stream.empty
//        }
//        case line => line #:: readLineStream()
//      }
//    }
//
//    readLineStream()
//  }
//
//  /**
//   * Returns stream which returns bytes with a specified chunk size.
//   *
//   * @param file the file
//   * @param chunkSize the chunk size as bytes, default is 1024 * 8.
//   * @return the stream which returns bytes
//   */
//  def readBytes(file: File, chunkSize: Int = 1024 * 8): Stream[Array[Byte]] = {
//    val in     = new BufferedInputStream(new FileInputStream(file), chunkSize)
//    val buffer = new Array[Byte](chunkSize)
//
//    def readBytesStream: Stream[Array[Byte]] = {
//      in.read(buffer) match {
//        case -1 => {
//          closeQuietly(in)
//          Stream.empty
//        }
//        case length => Arrays.copyOf(buffer, length) #:: readBytesStream
//      }
//    }
//
//    readBytesStream
//  }

  def getExtension(file: File): Option[String] = file.getName.lastIndexOf('.') match {
    case -1 => None
    case i  => Some(file.getName.substring(i + 1))
  }

  def findFile(dir: File)(condition: (File) => Boolean): Seq[File] = dir match {
    case x if x.isDirectory => x.listFiles.toSeq.flatMap(findFile(_)(condition))
    case x => if(condition(x)) Seq(x) else Nil
  }

}
