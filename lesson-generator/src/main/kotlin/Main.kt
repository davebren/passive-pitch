package org.eski

import AudioGenerator
import ImageGenerator
import Lesson
import VideoGenerator


fun main() {
  val lesson = Lesson.all[0]
  generateLessonFiles(lesson)
}

fun generateLessonFiles(
  lesson: Lesson,
  audioGenerator: AudioGenerator = AudioGenerator(),
  imageGenerator: ImageGenerator = ImageGenerator(),
  videoGenerator: VideoGenerator = VideoGenerator()
) {
  imageGenerator.generateLessonImage(lesson)
  audioGenerator.generateAudioFile(lesson)
  videoGenerator.createVideoFromAudioAndImage(lesson)
}
