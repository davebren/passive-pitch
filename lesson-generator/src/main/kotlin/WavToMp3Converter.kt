import java.io.File
import java.nio.file.Paths
import kotlin.time.Duration


fun main() {
//  val converter = WavToMp3Converter()
//  val projectDir = Paths.get("").toAbsolutePath().toString()
//  val sourcePath = File("$projectDir/audio-files/instruments/piano-kawai/mp3-128").absolutePath
//  val copyPath = File("$sourcePath/android").absolutePath
//
//  converter.copyToAndroidFormat(Instrument.pianoKawai, sourcePath, copyPath)

//  val projectDir = Paths.get("").toAbsolutePath().toString()
//  val sourcePath = File("$projectDir/audio-files/letters").absolutePath
//  (8 until 9).forEach { i ->
//    "cp $sourcePath/a.mp3 ${sourcePath}/a$i.mp3".runCommand()
//    "cp $sourcePath/b.mp3 ${sourcePath}/b$i.mp3".runCommand()
//    "cp $sourcePath/c.mp3 ${sourcePath}/c$i.mp3".runCommand()
//    "cp $sourcePath/d.mp3 ${sourcePath}/d$i.mp3".runCommand()
//    "cp $sourcePath/e.mp3 ${sourcePath}/e$i.mp3".runCommand()
//    "cp $sourcePath/f.mp3 ${sourcePath}/f$i.mp3".runCommand()
//    "cp $sourcePath/g.mp3 ${sourcePath}/g$i.mp3".runCommand()
//  }
}

class WavToMp3Converter {
  fun generateFiles(
    directoryPath: String,
    outputPath: String,
    duration: Duration?,
    bitRateK: Int = 128,
  ) {
    val inputPath = File(directoryPath)
    val outputPath = File(outputPath).apply { mkdirs() }

    val wavFiles = inputPath.listFiles()?.filter { it.name.endsWith(".wav") }

    wavFiles?.forEach { wavFile ->
      val outputFile = File("${outputPath.absolutePath}/${wavFile.nameWithoutExtension}.mp3")

      val command = "ffmpeg " +
          "-i ${wavFile.absolutePath} " +
          "-vn " +
          (duration?.let { "-t ${it.inWholeSeconds} "} ?: "") +
          "-ar 44100 " +
          "-ac 2 " +
          "-b:a ${bitRateK}k " +
          outputFile.absolutePath
      println(command)
      command.runCommand()
    }
  }

  fun copyToAndroidFormat(
    instrument: Instrument,
    sourceDirectory: String,
    outputDirectory: String
  ) {
    val sourceFiles = File(sourceDirectory).listFiles()?.filter { it.name.endsWith(".mp3") }
    File(outputDirectory).mkdirs()

    sourceFiles?.forEach { sourceFile ->
      val command = "cp ${sourceFile.absolutePath} " +
          "$outputDirectory/${instrument.name.lowercase()}_${sourceFile.name}"
      println(command)
      command.runCommand()
    }
  }
}