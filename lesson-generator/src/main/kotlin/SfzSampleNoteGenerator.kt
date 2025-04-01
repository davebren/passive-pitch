import java.io.File
import java.nio.file.Paths
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.pow
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) {
  val generator = SfzSampleWavGenerator()
  val mp3Converter = WavToMp3Converter()

  val projectDir = Paths.get("").toAbsolutePath().toString()
  val sfzPath = File("$projectDir/audio-files/sfz/piano-yamaha/map.sfz").absolutePath
  val wavOutputDir = File("$projectDir/audio-files/instruments/piano-yamaha/wav").absolutePath
  val mp3OutputDir = File("$projectDir/audio-files/instruments/piano-yamaha/mp3-128").absolutePath

  generator.generateWavNotesFromSfz(sfzPath, wavOutputDir, Note.a0, Note.c8)
  mp3Converter.generateFiles(wavOutputDir, mp3OutputDir, 5.toDuration(DurationUnit.SECONDS), 128)

}

class SfzSampleWavGenerator {

  data class Region(
    val sample: String,
    val lokey: Int,
    val hikey: Int,
    val lovel: Int,
    val hivel: Int,
    val pitchKeycenter: Int,
    val tune: Int,       // Added for pitch tuning
    val volume: Float,   // Added for volume adjustment
    val offset: Int      // Added for sample offset
  ) {
    companion object {
      fun fromOpcodes(opcodes: Map<String, String>, defaultPath: String): Region? {
        val sample = opcodes["sample"] ?: return null // Require sample opcode
        val key = opcodes["key"]?.toIntOrNull()
        val lokey = key ?: opcodes["lokey"]?.toIntOrNull() ?: 0
        val hikey = key ?: opcodes["hikey"]?.toIntOrNull() ?: 127
        val pitchKeycenter = key ?: opcodes["pitch_keycenter"]?.toIntOrNull() ?: 60
        val lovel = opcodes["lovel"]?.toIntOrNull() ?: 0
        val hivel = opcodes["hivel"]?.toIntOrNull() ?: 127
        val tune = opcodes["tune"]?.toIntOrNull() ?: 0
        val volume = opcodes["volume"]?.toFloatOrNull() ?: 0f
        val offset = opcodes["offset"]?.toIntOrNull() ?: 0
        val fullSamplePath = if (defaultPath.isNotEmpty()) "$defaultPath$sample" else sample
        return Region(fullSamplePath, lokey, hikey, lovel, hivel, pitchKeycenter, tune, volume, offset)
      }
    }
  }

  /**
   * Generates WAV files for each semitone from an SFZ file and its WAV samples
   */
  fun generateWavNotesFromSfz(
    sfzFilePath: String,
    outputDir: String = File(sfzFilePath).parent,
    lowestNote: Note,
    highestNote: Note,
  ) {
    val noteRange = (Note.entries.indexOf(lowestNote) + 12)..(Note.entries.indexOf(highestNote) + 12)

    val sfzFile = File(sfzFilePath)
    val outputDirectory = File(outputDir)
    outputDirectory.mkdirs()

    // Parse SFZ file
    val lines = sfzFile.readLines()
    var defaultPath = ""
    for (line in lines) {
      if (line.startsWith("default_path=")) {
        defaultPath = line.split('=', limit = 2)[1].trim()
        if (defaultPath.isNotEmpty() && !defaultPath.endsWith('/')) defaultPath += '/'
        break
      }
    }

    val regions = mutableListOf<Region>()
    var currentGroupOpcodes = mutableMapOf<String, String>()
    var currentRegionOpcodes = mutableMapOf<String, String>()

    for (line in lines) {
      val trimmed = line.trim()
      when {
        trimmed.startsWith("<group>") -> {
          if (currentRegionOpcodes.isNotEmpty()) {
            // Process any pending region before switching to new group
            val combinedOpcodes = currentGroupOpcodes + currentRegionOpcodes
            Region.fromOpcodes(combinedOpcodes, defaultPath)?.let { regions.add(it) }
              ?: println("Skipping region: no sample defined")
            currentRegionOpcodes.clear()
          }
          currentGroupOpcodes.clear() // Start new group
        }
        trimmed.startsWith("<region>") -> {
          if (currentRegionOpcodes.isNotEmpty()) {
            val combinedOpcodes = currentGroupOpcodes + currentRegionOpcodes
            Region.fromOpcodes(combinedOpcodes, defaultPath)?.let { regions.add(it) }
              ?: println("Skipping region: no sample defined")
          }
          currentRegionOpcodes = mutableMapOf()
        }
        trimmed.isNotEmpty() && !trimmed.startsWith("//") -> {
          val parts = trimmed.split('=', limit = 2)
          if (parts.size == 2) {
            if (currentRegionOpcodes.isEmpty() && !currentGroupOpcodes.containsKey("sample")) {
              currentGroupOpcodes[parts[0].trim()] = parts[1].trim()
            } else {
              currentRegionOpcodes[parts[0].trim()] = parts[1].trim()
            }
          }
        }
      }
    }

    // Process the last region if present
    if (currentRegionOpcodes.isNotEmpty()) {
      val combinedOpcodes = currentGroupOpcodes + currentRegionOpcodes
      Region.fromOpcodes(combinedOpcodes, defaultPath)?.let { regions.add(it) }
        ?: println("Skipping region: no sample defined")
    }

    if (regions.isEmpty()) {
      println("No valid regions found in SFZ file")
      return
    }

    // Generate WAV files for each note
    for (note in noteRange) {
      val applicableRegions = regions.filter { region ->
        note in region.lokey..region.hikey && 127 in region.lovel..region.hivel
      }
      if (applicableRegions.isEmpty()) {
        println("No region found for note $note")
        continue
      }
      val region = applicableRegions.first() // Use highest velocity region for 127
      val sampleFile = File("${sfzFile.parent}/${region.sample}")
      if (!sampleFile.exists()) {
        println("Sample file not found: ${region.sample}")
        continue
      }

      // Calculate pitch shift including tune
      val shift = note - region.pitchKeycenter + (region.tune / 100.0) // Tune in cents
      val factor = 2.0.pow(shift / 12.0)

      // Load WAV file
      val audioInputStream = AudioSystem.getAudioInputStream(sampleFile)
      val format = audioInputStream.format
      val targetFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        format.sampleRate,
        16,
        format.channels,
        format.channels * 2,
        format.sampleRate,
        false
      )
      val convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream)
      val frameSize = convertedStream.format.frameSize
      val sampleRate = convertedStream.format.sampleRate
      val channels = convertedStream.format.channels
      val isBigEndian = convertedStream.format.isBigEndian
      val byteArray = convertedStream.readAllBytes()
      audioInputStream.close()
      convertedStream.close()

      // Apply offset (in samples)
      val offsetBytes = region.offset * frameSize
      val effectiveByteArray = if (offsetBytes >= byteArray.size) {
        ByteArray(0)
      } else {
        byteArray.drop(offsetBytes).toByteArray()
      }

      // Convert bytes to float arrays per channel (16-bit PCM assumed)
      val samplesPerChannel = effectiveByteArray.size / frameSize
      val floatSamples = Array(channels) { FloatArray(samplesPerChannel) }
      for (i in 0 until samplesPerChannel) {
        for (ch in 0 until channels) {
          val offset = i * frameSize + ch * 2
          val sample = if (isBigEndian) {
            (effectiveByteArray[offset].toInt() shl 8) or (effectiveByteArray[offset + 1].toInt() and 0xFF)
          } else {
            (effectiveByteArray[offset + 1].toInt() shl 8) or (effectiveByteArray[offset].toInt() and 0xFF)
          }
          floatSamples[ch][i] = sample / 32768.0f * 10.0.pow(region.volume.toDouble() / 20.0).toFloat() // Apply volume in dB
        }
      }

      // Resample audio
      val newLength = (samplesPerChannel / factor).toInt()
      val resampled = Array(channels) { FloatArray(newLength) }
      for (ch in 0 until channels) {
        for (i in 0 until newLength) {
          val pos = i * factor
          val floorPos = floor(pos).toInt()
          val frac = pos - floorPos
          resampled[ch][i] = if (floorPos < samplesPerChannel - 1) {
            floatSamples[ch][floorPos] * (1 - frac).toFloat() + (floatSamples[ch][floorPos + 1] * frac).toFloat()
          } else {
            floatSamples[ch][samplesPerChannel - 1]
          }
        }
      }

      // Convert back to bytes
      val newByteArray = ByteArray(newLength * frameSize)
      for (i in 0 until newLength) {
        for (ch in 0 until channels) {
          val sample = (resampled[ch][i] * 32767).toInt().coerceIn(-32768, 32767)
          val offset = i * frameSize + ch * 2
          if (isBigEndian) {
            newByteArray[offset] = (sample shr 8).toByte()
            newByteArray[offset + 1] = sample.toByte()
          } else {
            newByteArray[offset] = sample.toByte()
            newByteArray[offset + 1] = (sample shr 8).toByte()
          }
        }
      }

      // Create new WAV file
      val newFormat = AudioFormat(sampleRate, 16, channels, true, isBigEndian)
      val newAudioInputStream = AudioInputStream(
        newByteArray.inputStream(),
        newFormat,
        newLength.toLong()
      )
      val filename = "${Note.entries[note - 12].filename()}.wav"
      val outputFile = File(outputDirectory, filename)
      AudioSystem.write(newAudioInputStream, AudioSystem.getAudioFileFormat(sampleFile).type, outputFile)
      newAudioInputStream.close()
      println("Generated WAV for note $note at ${outputFile.absolutePath}")
    }
  }
}