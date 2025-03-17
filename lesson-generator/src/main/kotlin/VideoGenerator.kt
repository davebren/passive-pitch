import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class VideoGenerator {

  /**
   * Creates an MP4 video file from an MP3 audio file and a PNG image file
   * This function uses FFmpeg which must be installed on the system and available in the PATH
   *
   * @param audioFilePath Path to the MP3 audio file
   * @param imageFilePath Path to the PNG image file to use as a still frame
   * @param outputFilePath Path where the output MP4 file should be saved
   * @param videoQuality Quality of the video output (0-51, where 0 is best quality), default is 23
   * @return true if the video was successfully created, false otherwise
   */
  fun createVideoFromAudioAndImage(
    lesson: Lesson,
    videoQuality: Int = 23
  ): Boolean {
    try {
      val projectDir = Paths.get("").absolutePathString()
      val audioFile = File("$projectDir/lessons/${lesson.fileName}")
      val imageFile = File("$projectDir/lessons/${lesson.fileName.replace(".mp3", ".png")}")
      val outputFilePath = "$projectDir/lessons/${lesson.fileName.replace(".mp3", ".mp4")}"

      if (!audioFile.exists() || !audioFile.isFile) {
        println("Error: Audio file not found: ${audioFile.absolutePath}")
        return false
      }

      if (!imageFile.exists() || !imageFile.isFile) {
        println("Error: Image file not found: ${imageFile.absolutePath}")
        return false
      }

      // Build the FFmpeg command
      // -loop 1: Loop the image
      // -i [image]: Input image file
      // -i [audio]: Input audio file
      // -c:v libx264: Use H.264 codec for video
      // -crf [quality]: Set constant rate factor (quality)
      // -tune stillimage: Optimize for still image
      // -c:a aac: Use AAC codec for audio
      // -b:a 192k: Set audio bitrate
      // -shortest: Finish encoding when the shortest input stream ends
      // -pix_fmt yuv420p: Use YUV 4:2:0 pixel format (for compatibility)
      val command = listOf(
        "ffmpeg",
        "-y", // Overwrite output file if it exists
        "-loop", "1",
        "-i", imageFile.absolutePath,
        "-i", audioFile.absolutePath,
        "-c:v", "libx264",
        "-crf", videoQuality.toString(),
        "-tune", "stillimage",
        "-c:a", "aac",
        "-b:a", "192k",
        "-shortest",
        "-pix_fmt", "yuv420p",
        outputFilePath
      )

      // Execute the command
      val processBuilder = ProcessBuilder(command)
      processBuilder.redirectErrorStream(true)
      val process = processBuilder.start()

      // Read and print output
      val reader = BufferedReader(InputStreamReader(process.inputStream))
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        println(line)
      }

      // Wait for the process to complete
      val exitCode = process.waitFor()

      if (exitCode == 0) {
        println("Successfully created video: $outputFilePath")
        return true
      } else {
        println("Error creating video. FFmpeg exited with code: $exitCode")
        return false
      }

    } catch (e: Exception) {
      println("Error creating video: ${e.message}")
      e.printStackTrace()
      return false
    }
  }
}