package jp.sf.amateras.scala.nio.file

import java.io._
import java.util.jar._
import java.util.zip._

import scala.collection.immutable.TreeSet
import scala.collection.mutable.HashSet

import jp.sf.amateras.scala.nio._

/**
 * Provides utility methods to archive and extract. These methods are copied from sbt-io.
 */
object ArchiveUtils {

  /**
   * Creates a jar file.
   *
   * @param sources The files to include in the jar file paired with the entry name in the jar.  Only the pairs explicitly listed are included.
   * @param outputJar The file to write the jar to.
   * @param manifest The manifest for the jar.
   */
  def jar(sources: Traversable[(File, String)], outputJar: File, manifest: Manifest): Unit =
    archive(sources.toSeq, outputJar, Some(manifest))

  /**
   * Creates a zip file.
   *
   * @param sources The files to include in the zip file paired with the entry name in the zip.  Only the pairs explicitly listed are included.
   * @param outputZip The file to write the zip to.
   */
  def zip(sources: Traversable[(File, String)], outputZip: File): Unit =
    archive(sources.toSeq, outputZip, None)

  private def archive(sources: Seq[(File, String)], outputFile: File, manifest: Option[Manifest]) {
    if (outputFile.isDirectory)
      sys.error("Specified output file " + outputFile + " is a directory.")
    else {
      val outputDir = outputFile.getParentFile
      outputDir.mkdirs()
      withZipOutput(outputFile, manifest) { output =>
        val createEntry: (String => ZipEntry) = if (manifest.isDefined) new JarEntry(_) else new ZipEntry(_)
        writeZip(sources, output)(createEntry)
      }
    }
  }

  private def writeZip(sources: Seq[(File, String)], output: ZipOutputStream)(createEntry: String => ZipEntry) {
    val files = sources.flatMap { case (file, name) => if (file.isFile) (file, normalizeName(name)) :: Nil else Nil }

    val now = System.currentTimeMillis
    // The CRC32 for an empty value, needed to store directories in zip files
    val emptyCRC = new CRC32().getValue()

    def addDirectoryEntry(name: String) {
      output putNextEntry makeDirectoryEntry(name)
      output.closeEntry()
    }

    def makeDirectoryEntry(name: String) =
    {
      //			log.debug("\tAdding directory " + relativePath + " ...")
      val e = createEntry(name)
      e setTime now
      e setSize 0
      e setMethod ZipEntry.STORED
      e setCrc emptyCRC
      e
    }

    def makeFileEntry(file: File, name: String) =
    {
      //			log.debug("\tAdding " + file + " as " + name + " ...")
      val e = createEntry(name)
      e setTime file.lastModified
      e
    }
    def addFileEntry(file: File, name: String) {
      output putNextEntry makeFileEntry(file, name)
      FileUtils.writeTo(file, output)
      output.closeEntry()
    }

    //Calculate directories and add them to the generated Zip
    allDirectoryPaths(files) foreach addDirectoryEntry

    //Add all files to the generated Zip
    files foreach { case (file, name) => addFileEntry(file, name) }
  }

  // map a path a/b/c to List("a", "b")
  private def relativeComponents(path: String): List[String] =
    path.split("/").toList.dropRight(1)

  // map components List("a", "b", "c") to List("a/b/c/", "a/b/", "a/", "")
  private def directories(path: List[String]): List[String] =
    path.foldLeft(List(""))((e, l) => (e.head + l + "/") :: e)

  // map a path a/b/c to List("a/b/", "a/")
  private def directoryPaths(path: String): List[String] =
    directories(relativeComponents(path)).filter(_.length > 1)


  // produce a sorted list of all the subdirectories of all provided files
  private def allDirectoryPaths(files: Iterable[(File, String)]) =
    TreeSet[String]() ++ (files flatMap { case (file, name) => directoryPaths(name) })

  private def normalizeDirName(name: String) = {
    val norm1 = normalizeName(name)
    if (norm1.endsWith("/")) norm1 else (norm1 + "/")
  }

  private def normalizeName(name: String) = {
    val sep = File.separatorChar
    if (sep == '/') name else name.replace(sep, '/')
  }

  private def withZipOutput(file: File, manifest: Option[Manifest])(f: ZipOutputStream => Unit) {
    using(new FileOutputStream(file)){ fileOut =>
      val (zipOut, ext) = manifest match {
        case Some(mf) => {
          import Attributes.Name.MANIFEST_VERSION
          val main = mf.getMainAttributes
          if (!main.containsKey(MANIFEST_VERSION)){
            main.put(MANIFEST_VERSION, "1.0")
          }
          (new JarOutputStream(fileOut, mf), "jar")
        }
        case None => (new ZipOutputStream(fileOut), "zip")
      }
      try {
        f(zipOut)
      } finally {
        zipOut.close
      }
    }
  }

  def unzip(from: File, toDirectory: File): Set[File] =
    using(new FileInputStream(from)){ in =>
      unzipStream(in, toDirectory)
    }

  def unzipStream(from: InputStream, toDirectory: File): Set[File] = {
    toDirectory.mkdirs()
    using(new ZipInputStream(from)){ zipIn =>
      extract(zipIn, toDirectory)
    }
  }

  private def extract(from: ZipInputStream, toDirectory: File) = {
    val set = new HashSet[File]
    def next() {
      val entry = from.getNextEntry
      if (entry == null)
        ()
      else {
        val name = entry.getName
        val target = new File(toDirectory, name)
        //log.debug("Extracting zip entry '" + name + "' to '" + target + "'")
        if (entry.isDirectory){
          target.mkdirs()
        } else {
          set += target
          FileUtils.write(target, from)
        }
//        if (preserveLastModified){
//          target.setLastModified(entry.getTime)
//        }
        from.closeEntry()
        next()
      }
    }
    next()
    Set() ++ set
  }

}
