package jp.sf.amateras.scala.nio

import java.io._

/**
 * Provides some utility methods to operate files and directories.
 */
object FileUtils {

  /**
   * Delete file or directory. If the given file is a directory, this method delete it recursively.
   */
  def delete(file: File): Unit = {
    file match {
      case dir if dir.isDirectory() => dir.listFiles().foreach(delete _)
      case _ =>
    }
    if(!file.delete()){
      throw new IOException(s"Failed to delete ${file.getAbsolutePath()}")
    }
  }

  /**
   * Move file or directory. This method tries to move using [[java.io.File#renameTo(java.io.File)]] at first.
   * If failed to rename, move by combination of copy and delete.
   */
  def move(file: File, dest: File): Unit = {
    if(!file.renameTo(dest)){
      copy(file, dest)
      delete(file)
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
      case x if x.isDirectory() => x.listFiles().foreach { child =>
        copy(child, new File(dest, child.getName()))
        if(!dest.mkdir()){
          throw new IOException(s"Failed to create directory ${dest.getAbsolutePath()}")
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

}
