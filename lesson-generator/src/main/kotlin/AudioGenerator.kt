import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths

class AudioGenerator {
  fun generateAudioFile(lesson: Lesson): String {
    println("generate lesson: $lesson")

    val projectDir = Paths.get("").toAbsolutePath().toString()
    File("$projectDir/lessons").mkdirs()
    val outputFilePath = "$projectDir/lessons/${lesson.fileName}"

    val tempFiles = listOf(
      "$projectDir/lessons/temp-out-0.mp3",
      "$projectDir/lessons/temp-out-1.mp3"
    )
    var tempFileIndex: Int? = null

    for (promptIndex in 0 until lesson.totalPrompts) {
      val inputFiles = mutableListOf<Pair<String, Int>>()

      val note = lesson.notes.random()
      val instrument = lesson.instruments.random()

      val letterFileName = if (note.natural()) {
        "${note.nameo[0].lowercaseChar()}"
      } else "${note.nameFlat[0].lowercaseChar()}b"

      val noteFileName = "$letterFileName${note.octave}"

      val noteFile = "$projectDir/audio-files/instruments/$instrument/$noteFileName.mp3"
      val letterFile = "$projectDir/audio-files/note-letters/$letterFileName.mp3"

      tempFileIndex?.let {
        inputFiles.add(Pair(tempFiles[it], 0))
      }

      tempFileIndex = adjustTempFileIndex(tempFileIndex)
      val outputFile = tempFiles[tempFileIndex]

      inputFiles.add(Pair(noteFile, lesson.promptAnswerSpacingSeconds))
      inputFiles.add(Pair(letterFile, lesson.promptSpacingSeconds))

      concatenateMp3Files(inputFiles, outputFile)
    }

    tempFileIndex?.let { "cp ${tempFiles[it]} $outputFilePath".runCommand() }
    tempFiles.forEach { "rm $it".runCommand() }
    return outputFilePath
  }

  private fun adjustTempFileIndex(startIndex: Int?): Int {
    return when(startIndex) {
      null -> 0
      0 -> 1
      1 -> 0
      else -> throw IllegalArgumentException()
    }
  }

  private fun concatenateMp3Files(inputFilesWithSilence: List<Pair<String, Int>>, outputFilePath: String): Boolean {
    if (inputFilesWithSilence.isEmpty()) {
      println("Error: No input files provided")
      return false
    }

    try {
      val outputFile = File(outputFilePath)
      val outputStream = FileOutputStream(outputFile)

      for ((filePath, silenceSeconds) in inputFilesWithSilence) {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
          println("Error: Input file not found: $filePath")
          outputStream.close()
          return false
        }

        // Copy the audio file to the output
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
        }

        inputStream.close()

        // Add silence directly after the file
        if (silenceSeconds > 0) {
          addSilence(outputStream, silenceSeconds)
        }
      }

      outputStream.close()
      println("Successfully concatenated audio files with silence to: $outputFilePath")
      return true

    } catch (e: Exception) {
      println("Error during MP3 concatenation: ${e.message}")
      e.printStackTrace()
      return false
    }
  }

  /**
   * Adds silence to the MP3 stream by creating a silent MP3 file and appending it
   *
   * @param outputStream the output stream to write to
   * @param seconds the number of seconds of silence to write
   */
  private fun addSilence(outputStream: FileOutputStream, seconds: Int) {
    try {
      // Create a temporary silence MP3 file
      val projectDir = Paths.get("").toAbsolutePath().toString()
      val silenceFile = "$projectDir/silence_temp.mp3"

      // Use ffmpeg to generate a silent MP3 file with the correct format
      val ffmpegCommand = "ffmpeg -y -f lavfi -i anullsrc=r=44100:cl=stereo -t $seconds " +
          "-ab 128k -acodec libmp3lame -f mp3 $silenceFile"

      val process = Runtime.getRuntime().exec(ffmpegCommand)
      process.waitFor()

      // Read the silent MP3 file and write it to the output stream
      val silenceFileObj = File(silenceFile)
      if (silenceFileObj.exists()) {
        val silenceInputStream = FileInputStream(silenceFileObj)
        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (silenceInputStream.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
        }

        silenceInputStream.close()
        silenceFileObj.delete() // Clean up temporary file
      } else {
        println("Failed to create silence file")
      }
    } catch (e: Exception) {
      println("Error generating silence: ${e.message}")
      e.printStackTrace()
    }
  }

  /**
   * Simplification of the previous writeSilenceFrames method
   * This is a basic implementation that writes null bytes as silence
   * For more accurate silence, use the addSilence method above
   *
   * @param outputStream the output stream to write to
   * @param seconds the number of seconds of silence to write
   */
  private fun writeSilenceFrames(outputStream: FileOutputStream, seconds: Int) {
    // MP3 parameters
    val sampleRate = 44100 // 44.1 kHz
    val bitRate = 128000 // 128 kbps

    // Calculate byte size for the duration
    val bytesPerSecond = bitRate / 8
    val totalBytes = bytesPerSecond * seconds

    // Create a buffer for writing silence in chunks
    val bufferSize = 8192
    val buffer = ByteArray(bufferSize)

    // Write silence in chunks to avoid memory issues with large durations
    var remainingBytes = totalBytes
    while (remainingBytes > 0) {
      val bytesToWrite = minOf(bufferSize, remainingBytes)
      outputStream.write(buffer, 0, bytesToWrite)
      remainingBytes -= bytesToWrite
    }
  }
}