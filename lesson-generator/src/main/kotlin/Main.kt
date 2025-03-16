package org.eski

import Lesson
import runCommand
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import javax.imageio.ImageIO


fun main() {
  val lesson = Lesson.all[0]
  generateLessonFilesKotlin(lesson)
}

fun generateLessonFilesKotlin(lesson: Lesson) {
  println("generate lesson: $lesson")

  val projectDir = Paths.get("").toAbsolutePath().toString()
  File("$projectDir/lessons/images").mkdirs()
  val imageOutputPath = "$projectDir/lessons/images/${lesson.fileName.replace(".mp3", ".png")}"

  val subtext = "    This lesson will help you learn to identify notes by ear. It is designed to play a note every ${lesson.promptSpacingSeconds} " +
      "seconds in order to help you learn what each note sounds like. You can keep the audio playing in the background to rehearse throughout the day. " +
      "Try to identify each note before it is spoken. Each lesson will get a tiny bit more difficult." +
      "<br>    The passive pitch method involves consistent rehearsal and holding the memory of each note in your mind for as long as possible each day. " +
      "Try to identify the common feature between the same note played in different octaves and by different instruments. " +
      "It will be most effective when combined with active forms of perfect pitch training." +
      "<br>   You can find the playlist of all lessons in order on my channel page. " +
      "There will be links in the description for more music learning content and apps. Good luck on you perfect pitch journey!"

  generateLessonImage(lesson, imageOutputPath, subtext)

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

/**
 * Generates a PNG image file for a lesson with information about the lesson
 *
 * @param lesson The lesson to generate an image for
 * @param outputPath The path where the image file should be saved
 * @param subtext Additional text to display as a smaller paragraph, can include \t for tabs and \n for newlines
 * @param width Image width in pixels (default 1200)
 * @param height Image height in pixels (default 630)
 * @return true if the image was successfully generated, false otherwise
 */
fun generateLessonImage(
  lesson: Lesson,
  outputPath: String,
  subtext: String,
  width: Int = 1200,
  height: Int = 630
): Boolean {
  try {
    // Create a new image with black background
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g2d = image.createGraphics()

    // Enable anti-aliasing for smoother text
    g2d.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )

    // Set background to black
    g2d.color = Color.BLACK
    g2d.fillRect(0, 0, width, height)

    // Set text color to white
    g2d.color = Color.WHITE

    // Draw the lesson number
    g2d.font = Font("Arial", Font.BOLD, 60)
    val lessonTitle = "Passive Pitch - Lesson ${lesson.levelIndex}"
    val titleMetrics = g2d.fontMetrics
    val titleX = (width - titleMetrics.stringWidth(lessonTitle)) / 2
    g2d.drawString(lessonTitle, titleX, 80)

    // Draw the instruments list
    g2d.font = Font("Arial", Font.PLAIN, 40)
    val instrumentsText = "Instruments: ${lesson.instruments.joinToString(", ")}"
    val instrumentsMetrics = g2d.fontMetrics
    val instrumentsX = (width - instrumentsMetrics.stringWidth(instrumentsText)) / 2
    g2d.drawString(instrumentsText, instrumentsX, 160)

    // Draw the notes list
    val notesText = "Notes: ${lesson.notes.joinToString(", ") { it.toString() }}"
    val notesMetrics = g2d.fontMetrics
    val notesX = (width - notesMetrics.stringWidth(notesText)) / 2
    g2d.drawString(notesText, notesX, 220)

    // Draw the subtext paragraph with formatting support
    if (subtext.isNotEmpty()) {
      g2d.font = Font("Arial", Font.ITALIC, 24)

      // Constants for formatting
      val tabSize = 40 // Pixels for a tab indent
      val lineHeight = g2d.fontMetrics.height
      val paragraphSpacing = lineHeight / 2 // Extra space between paragraphs
      val marginX = 50 // Left/right margin
      val textWidth = width - (marginX * 2) // Maximum text width

      // Split text into paragraphs
      val paragraphs = subtext.split("<br>")
      var yPosition = 300

      for (paragraph in paragraphs) {
        // Check if paragraph starts with a tab
        val leftMargin = marginX
        val processedParagraph = paragraph

        // Process this paragraph with word wrapping
        val words = processedParagraph.split(" ")
        var currentLine = ""

        for (word in words) {
          val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
          val testWidth = g2d.fontMetrics.stringWidth(testLine)

          if (testWidth > textWidth - (leftMargin - marginX)) {
            // Line is full, draw it and start a new one
            g2d.drawString(currentLine, leftMargin, yPosition)
            currentLine = word
            yPosition += lineHeight
          } else {
            currentLine = testLine
          }
        }

        // Draw the last line of the paragraph
        if (currentLine.isNotEmpty()) {
          g2d.drawString(currentLine, leftMargin, yPosition)
          yPosition += lineHeight
        }

        // Add extra spacing between paragraphs
        yPosition += paragraphSpacing
      }
    }

    // Clean up resources
    g2d.dispose()

    // Save the image
    val outputFile = File(outputPath)
    ImageIO.write(image, "png", outputFile)

    println("Successfully generated lesson image: $outputPath")
    return true

  } catch (e: Exception) {
    println("Error generating lesson image: ${e.message}")
    e.printStackTrace()
    return false
  }
}