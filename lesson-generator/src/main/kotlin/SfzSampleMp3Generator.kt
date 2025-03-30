import java.io.File
import javazoom.jl.converter.Converter
import javax.sound.sampled.*
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.pow



// Example usage
fun main(args: Array<String>) {
  val generator = SfzSampleMp3Generator()

  val projectDir = Paths.get("").toAbsolutePath().toString()
  val sfzPath = File("$projectDir/audio-files/sfz/piano-kawai/map.sfz").absolutePath

  val outputDir = File("$projectDir/audio-files/instruments/piano-kawai").absolutePath

  generator.generateMp3NotesFromSfz(sfzPath, outputDir, 21..108)
}

class SfzSampleMp3Generator {

  /**
   * Generates MP3 files for each semitone from an SFZ file and its WAV samples
   * @param sfzFilePath Path to the SFZ file
   * @param outputDir Directory to save generated MP3 files (defaults to same directory as SFZ)
   * @param noteRange Range of MIDI notes to generate (default: 21-108, piano range)
   */
  fun generateMp3NotesFromSfz(
    sfzFilePath: String,
    outputDir: String = File(sfzFilePath).parent,
    noteRange: IntRange = 21..108
  ) {
    val sfzFile = File(sfzFilePath)
    if (!sfzFile.exists()) {
      throw IllegalArgumentException("SFZ file not found: $sfzFilePath")
    }

    // Parse SFZ file to extract regions with sample information
    val sfzContent = sfzFile.readText()
    val sfzRegions = parseSfzRegions(sfzContent)

    // Create output directory if it doesn't exist
    val outputDirectory = File(outputDir)
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs()
    }

    // Generate MP3 for each MIDI note in the range
    for (midiNote in noteRange) {
      val region = findBestRegionForNote(sfzRegions, midiNote)
      if (region != null) {
        generateMp3ForNote(region, midiNote, sfzFile.parent, outputDirectory)
      }
    }

    println("MP3 generation complete. Files saved to: ${outputDirectory.absolutePath}")
  }

  /**
   * Represents a region in the SFZ file
   */
  data class SfzRegion(
    val samplePath: String,
    val rootKey: Int,
    val loKey: Int,
    val hiKey: Int,
    val loVel: Int = 0,
    val hiVel: Int = 127,
    val volume: Double = 0.0
  )

  /**
   * Parses an SFZ file content and extracts regions
   */
  fun parseSfzRegions(sfzContent: String): List<SfzRegion> {
    val regions = mutableListOf<SfzRegion>()
    val regionBlocks = sfzContent.split("<region>").drop(1)

    for (block in regionBlocks) {
      var samplePath = ""
      var rootKey = 60 // Default to middle C
      var loKey = 0
      var hiKey = 127
      var loVel = 0
      var hiVel = 127
      var volume = 0.0

      // Extract parameters from region
      val lines = block.split("\n")
      for (line in lines) {
        val trimmed = line.trim()
        when {
          trimmed.startsWith("sample=") ->
            samplePath = trimmed.substring(7).trim().removeSurrounding("\"")
          trimmed.startsWith("key=") -> {
            rootKey = trimmed.substring(4).trim().toInt()
            loKey = rootKey
            hiKey = rootKey
          }
          trimmed.startsWith("pitch_keycenter=") ->
            rootKey = trimmed.substring(16).trim().toInt()
          trimmed.startsWith("lokey=") ->
            loKey = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("hikey=") ->
            hiKey = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("lovel=") ->
            loVel = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("hivel=") ->
            hiVel = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("volume=") ->
            volume = trimmed.substring(7).trim().toDouble()
        }
      }

      if (samplePath.isNotEmpty()) {
        regions.add(SfzRegion(samplePath, rootKey, loKey, hiKey, loVel, hiVel, volume))
      }
    }

    return regions
  }

  /**
   * Finds the best region to use for a given MIDI note
   */
  fun findBestRegionForNote(regions: List<SfzRegion>, midiNote: Int): SfzRegion? {
    // First, find regions where the note is in range
    val validRegions = regions.filter { midiNote in it.loKey..it.hiKey }

    if (validRegions.isEmpty()) return null

    // Prefer exact key match, then closest root key
    val exactMatch = validRegions.find { it.rootKey == midiNote }
    if (exactMatch != null) return exactMatch

    // Otherwise, return region with closest root key
    return validRegions.minByOrNull { Math.abs(it.rootKey - midiNote) }
  }

  /**
   * Generates MP3 file for a specific MIDI note using the given region
   */
  fun generateMp3ForNote(
    region: SfzRegion,
    midiNote: Int,
    sfzDirectory: String,
    outputDirectory: File
  ) {
    val sampleFile = File(sfzDirectory, region.samplePath)
    if (!sampleFile.exists()) {
      println("Warning: Sample file not found: ${sampleFile.absolutePath}")
      return
    }

    // Calculate pitch shift ratio
    val semitonesDifference = midiNote - region.rootKey
    val pitchShiftRatio = 2.0.pow(semitonesDifference / 12.0)

    // Create temporary WAV file for the pitch-shifted sample
    val tempWavFile = File.createTempFile("pitch_shifted_", ".wav")

    try {
      // Pitch shift the sample
      pitchShiftSample(sampleFile.absolutePath, tempWavFile.absolutePath, pitchShiftRatio)

      // Convert to MP3
      val noteName = midiNoteToName(midiNote)
      val mp3File = File(outputDirectory, "${noteName}_${midiNote}.mp3")
      convertWavToMp3(tempWavFile.absolutePath, mp3File.absolutePath)

      println("Generated MP3 for note $noteName (MIDI: $midiNote)")
    } finally {
      // Clean up temp file
      tempWavFile.delete()
    }
  }

  /**
   * Pitch shifts a WAV sample by the given ratio
   */
  fun pitchShiftSample(inputWavPath: String, outputWavPath: String, pitchRatio: Double) {
    // Open input audio file
    val inputFile = File(inputWavPath)
    val audioInputStream = AudioSystem.getAudioInputStream(inputFile)
    val format = audioInputStream.format

    // Read audio data
    val frameSize = format.frameSize
    val inputBytes = audioInputStream.readAllBytes()
    val inputFrames = inputBytes.size / frameSize

    // Calculate output size
    val outputFrames = (inputFrames / pitchRatio).toInt()
    val outputBytes = ByteArray(outputFrames * frameSize)

    // Simple resampling (for more complex instruments, a proper time-stretching
    // algorithm would be better, but this works for basic pitch shifting)
    for (outFrame in 0 until outputFrames) {
      val inFrame = (outFrame * pitchRatio).toInt()
      if (inFrame < inputFrames) {
        System.arraycopy(
          inputBytes, inFrame * frameSize,
          outputBytes, outFrame * frameSize,
          frameSize
        )
      }
    }

    // Write output file
    val outputAis = AudioInputStream(
      outputBytes.inputStream(),
      format,
      outputFrames.toLong()
    )

    AudioSystem.write(outputAis, AudioFileFormat.Type.WAVE, File(outputWavPath))

    // Close streams
    audioInputStream.close()
    outputAis.close()
  }

  /**
   * Converts a WAV file to MP3 using JavaZoom JLayer
   */
  fun convertWavToMp3(wavFilePath: String, mp3FilePath: String) {
    val converter = Converter()
    converter.convert(wavFilePath, mp3FilePath)
  }

  /**
   * Converts a MIDI note number to a note name (e.g., 60 -> "C4")
   */
  fun midiNoteToName(midiNote: Int): String {
    val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val noteName = noteNames[midiNote % 12]
    val octave = (midiNote / 12) - 1
    return "$noteName$octave"
  }
}