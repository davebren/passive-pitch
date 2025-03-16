enum class SilenceFile(val duration: Int, val filepath: String) {
  oneS(1, "audio-files/silence/1.mp3"),
  fiveS(5, "audio-files/silence/5.mp3"),
  tenS(10, "audio-files/silence/10.mp3"),
  sixtyS(60, "audio-files/silence/60.mp3");

  companion object {
    fun fromSeconds(seconds: Int): List<SilenceFile> {
      val silences = mutableListOf<SilenceFile>()
      var timeLeft = seconds

      while (timeLeft > 0) {
       SilenceFile.entries.reversed().forEach {
         if (timeLeft >= it.duration) {
           silences.add(it)
           timeLeft -= it.duration
           return@forEach
         }
       }
      }
      return silences
    }
  }
}