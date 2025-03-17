import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO

class ImageGenerator {

  fun generateLessonImage(lesson: Lesson): String {
    val projectDir = Paths.get("").toAbsolutePath().toString()
    File("$projectDir/lessons").mkdirs()
    val imageOutputPath = "$projectDir/lessons/${lesson.fileName.replace(".mp3", ".png")}"

    val subtext = "    This lesson will help you learn to identify notes by ear. It is designed to play a note every ${lesson.promptSpacingSeconds} " +
        "seconds in order to help you learn what each note sounds like. You can keep the audio playing in the background to rehearse throughout the day. " +
        "Try to identify each note before it is spoken. Each lesson will get a tiny bit more difficult." +
        "<br>    The passive pitch method involves consistent rehearsal and holding the memory of each note in your mind for as long as possible each day. " +
        "Try to identify the common feature between the same note played in different octaves and by different instruments. " +
        "It will be most effective when combined with active forms of perfect pitch training." +
        "<br>   You can find the playlist of all lessons in order on my channel page. " +
        "There will be links in the description for more music learning content and apps. Good luck on you perfect pitch journey!"

    generateLessonImage(lesson, imageOutputPath, subtext)
    return imageOutputPath
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
  private fun generateLessonImage(
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
}