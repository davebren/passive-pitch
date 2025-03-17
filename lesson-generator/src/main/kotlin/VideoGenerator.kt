import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class VideoGenerator {

  /**
   * Creates an MP4 video file from an MP3 audio file and a PNG image file
   * This function uses FFmpeg which must be installed on the system and available in the PATH
   * Optimized for static images by using a more efficient encoding approach
   *
   * @param lesson The lesson to create a video for
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

      // Build the FFmpeg command with optimizations for static images
      // Key optimizations:
      // 1. Using -framerate 1 to generate fewer frames from the input
      // 2. Using -preset veryslow for better compression with static content
      // 3. Using appropriate -g (keyframe interval) for static content
      val command = listOf(
        "ffmpeg",
        "-y", // Overwrite output file if it exists
        "-loop", "1",
        "-framerate", "1", // Lower framerate for input since image is static
        "-i", imageFile.absolutePath,
        "-i", audioFile.absolutePath,
        "-c:v", "libx264",
        "-crf", videoQuality.toString(),
        "-preset", "veryslow", // Slower encoding but better compression for static content
        "-tune", "stillimage",
        "-g", "600", // Large GOP size since all frames are identical
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