package org.eski

import SilenceFile
import runCommand


fun main() {
  Lesson.all.forEach {
    generateLessonFile(it)
  }
}

fun generateLessonFile(lesson: Lesson) {
  println("generate lesson: $lesson")
  val commandList = mutableListOf("cd ../")
  commandList.add("source scripts/venv/bin/activate")

  val silencesBetweenPrompts = SilenceFile.fromSeconds(lesson.promptSpacingSeconds)
  val silencesBetweenAnswer = SilenceFile.fromSeconds(lesson.promptAnswerSpacingSeconds)

  val tempFiles = listOf(
    "lessons/temp-out-0.mp3",
    "lessons/temp-out-1.mp3"
  )
  var tempFileIndex: Int? = null

  for (promptIndex in 0 until lesson.totalPrompts) {
    val note = lesson.notes.random()
    println("note: $note")
    val instrument = lesson.instruments.random()

    val letterFileName = if (note.natural()) {
      "${note.nameo[0].lowercaseChar()}"
    } else "${note.nameFlat[0].lowercaseChar()}b"

    val noteFileName = "$letterFileName${note.octave}"

    val noteFile = "audio-files/instruments/$instrument/$noteFileName.mp3"
    val silenceFile = "audio-files/silence/10.mp3"
    val letterFile = "audio-files/note-letters/$letterFileName.mp3"

    val concatCommandBuilder = StringBuilder("python scripts/combine-audio.py -c ")
    tempFileIndex?.let {
      concatCommandBuilder.append(tempFiles[it]).append(" ")
    }
    tempFileIndex = adjustTempFileIndex(tempFileIndex)
    val tempFile = tempFiles[tempFileIndex]

    if (promptIndex > 0) {
      silencesBetweenPrompts.forEach {
        concatCommandBuilder.append(it.filepath).append(" ")
      }
    }

    concatCommandBuilder.append(noteFile).append(" ")

    silencesBetweenAnswer.forEach {
      concatCommandBuilder.append(it.filepath).append(" ")
    }

    concatCommandBuilder.append(letterFile).append(" ")

    concatCommandBuilder.append("-o $tempFile")

    commandList.add(concatCommandBuilder.toString())
  }

  tempFileIndex?.let { commandList.add("cp ${tempFiles[it]} lessons/${lesson.fileName}") }
  tempFiles.forEach { commandList.add("rm $it") }
  commandList.forEach { println(it) }
  commandList.joinToString("; ").runCommand()
}

private fun adjustTempFileIndex(startIndex: Int?): Int {
  return when(startIndex) {
    null -> 0
    0 -> 1
    1 -> 0
    else -> throw IllegalArgumentException()
  }
}