package org.eski

import AudioGenerator
import ImageGenerator
import Lesson
import VideoGenerator
import runCommand


fun main() {
//  val lesson = Lesson.all[0]
//  generateLessonFiles(lesson, keepOnlyVideo = true)

  Lesson.all.forEach { generateLessonFiles(it, keepOnlyVideo = true) }
}

fun generateLessonFiles(
  lesson: Lesson,
  keepOnlyVideo: Boolean,
  audioGenerator: AudioGenerator = AudioGenerator(),
  imageGenerator: ImageGenerator = ImageGenerator(),
  videoGenerator: VideoGenerator = VideoGenerator()
) {
  val imagePath = imageGenerator.generateLessonImage(lesson)
  val audioPath = audioGenerator.generateAudioFile(lesson)
  videoGenerator.createVideoFromAudioAndImage(lesson)

  if (keepOnlyVideo) {
    "rm $imagePath".runCommand()
    "rm $audioPath".runCommand()
  }
}
