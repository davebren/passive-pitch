package org.eski

import runCommand
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths


fun main() {
  generateLessonFileKotlin(Lesson.all[1])
}

fun generateLessonFileKotlin(lesson: Lesson) {
  println("generate lesson: $lesson")

  val projectDir = Paths.get("").toAbsolutePath().toString()

  val tempFiles = listOf(
    "$projectDir/lessons/temp-out-0.mp3",
    "$projectDir/lessons/temp-out-1.mp3"
  )
  var tempFileIndex: Int? = null

  for (promptIndex in 0 until lesson.totalPrompts) {
    val inputFiles = mutableListOf<Pair<String, Int>>()

    val note = lesson.notes.random()
    println("note: $note")
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

  tempFileIndex?.let { "cp ${tempFiles[it]} lessons/${lesson.fileName}".runCommand() }
  tempFiles.forEach { "rm $it".runCommand() }
}

private fun adjustTempFileIndex(startIndex: Int?): Int {
  return when(startIndex) {
    null -> 0
    0 -> 1
    1 -> 0
    else -> throw IllegalArgumentException()
  }
}

fun concatenateMp3Files(inputFilesWithSilence: List<Pair<String, Int>>, outputFilePath: String): Boolean {
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
        writeSilenceFrames(outputStream, silenceSeconds)
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
 * Writes silent MP3 frames directly to the output stream
 *
 * @param outputStream the output stream to write to
 * @param seconds the number of seconds of silence to write
 */
private fun writeSilenceFrames(outputStream: FileOutputStream, seconds: Int) {
  // MP3 parameters
  val sampleRate = 44100 // 44.1 kHz
  val bitRate = 128000 // 128 kbps
  val channels = 2 // stereo

  // Calculate frame parameters
  val frameSize = (144 * bitRate / sampleRate) // Frame size formula for MP3
  val framesPerSecond = sampleRate / 1152 // 1152 samples per frame is standard for MPEG1 Layer 3
  val totalFrames = framesPerSecond * seconds

  // Create a silent MP3 frame (this is a simplified approach)
  // A real silent frame would require proper MP3 frame headers and data
  // For a proper implementation, you would use a library like LAME
  val silentFrame = ByteArray(frameSize) { 0 }

  // Set basic MP3 frame header (simplified)
  // In a real implementation, this would be much more detailed
  silentFrame[0] = 0xFF.toByte() // Frame sync (11 bits)
  silentFrame[1] = 0xFB.toByte() // MPEG1, Layer 3, No CRC

  // Write the silent frames
  for (i in 0 until totalFrames) {
    outputStream.write(silentFrame)
  }
}