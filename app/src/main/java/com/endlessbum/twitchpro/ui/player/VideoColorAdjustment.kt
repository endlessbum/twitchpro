package com.endlessbum.twitchpro.ui.player

import android.content.Context
import androidx.media3.effect.RgbMatrix
import com.endlessbum.twitchpro.util.C
import com.endlessbum.twitchpro.util.prefs

/**
 * GPU-side color adjustment of the video picture (player color rendition).
 * Brightness, contrast and saturation are linear per-channel ops, so they compose
 * into one RGB matrix — unlike gamma, which is non-linear and is not offered.
 * 100/100/100 produces the identity matrix, so the neutral state never alters the picture.
 */
class VideoColorAdjustmentEffect(
    private val brightness: Int,
    private val contrast: Int,
    private val saturation: Int,
) : RgbMatrix {

    override fun getMatrix(presentationTimeUs: Long, isInputTextureSrgb: Boolean): FloatArray {
        val m = identity4x5()
        if (saturation != 100) {
            m.compose(colorMatrix(saturationMatrix(saturation / 100f)))
        }
        if (contrast != 100) {
            val c = contrast / 100f
            m.compose(colorMatrix(affine(c, 255f * 0.5f * (1f - c))))
        }
        if (brightness != 100) {
            m.compose(colorMatrix(affine(1f, 255f * (brightness / 100f - 1f))))
        }
        return glMatrix(m)
    }

    private fun identity4x5() = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    /** scale + offset on every RGB channel, alpha untouched. */
    private fun affine(scale: Float, offset: Float) = floatArrayOf(
        scale, 0f, 0f, 0f, offset,
        0f, scale, 0f, 0f, offset,
        0f, 0f, scale, 0f, offset,
        0f, 0f, 0f, 1f, 0f,
    )

    /** luminance-preserving saturation matrix (Android ColorMatrix.setSaturation math). */
    private fun saturationMatrix(sat: Float): FloatArray {
        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f
        val sr = (1f - sat) * lumR
        val sg = (1f - sat) * lumG
        val sb = (1f - sat) * lumB
        return floatArrayOf(
            sr + sat, sg, sb, 0f, 0f,
            sr, sg + sat, sb, 0f, 0f,
            sr, sg, sb + sat, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** in-place this = this ∘ other (other is applied to the color first). */
    private fun FloatArray.compose(other: FloatArray) {
        val result = FloatArray(20)
        for (row in 0..3) {
            for (col in 0..4) {
                var sum = 0f
                for (k in 0..3) sum += this[row * 5 + k] * other[k * 5 + col]
                sum += this[row * 5 + 4] * other[4 * 5 + col]
                result[row * 5 + col] = sum
            }
        }
        System.arraycopy(result, 0, this, 0, 20)
    }

    private fun colorMatrix(raw: FloatArray) = raw

    /** 4x5 RGBA (offset in the 5th column) -> row-major 4x4 GL matrix with normalized offsets in the 4th column. */
    private fun glMatrix(m: FloatArray): FloatArray {
        val gl = FloatArray(16)
        for (row in 0..3) {
            gl[row * 4 + 0] = m[row * 5 + 0]
            gl[row * 4 + 1] = m[row * 5 + 1]
            gl[row * 4 + 2] = m[row * 5 + 2]
            gl[row * 4 + 3] = m[row * 5 + 4] / 255f
        }
        return gl
    }
}

/** Reads the persisted slider values and attaches the effect to an ExoPlayer instance. */
object VideoColorAdjustment {

    val BRIGHTNESS_RANGE = 50..200
    val CONTRAST_RANGE = 50..200
    val SATURATION_RANGE = 0..200

    fun brightness(context: Context) = context.prefs().getInt(C.TV_VIDEO_BRIGHTNESS, 100)
    fun contrast(context: Context) = context.prefs().getInt(C.TV_VIDEO_CONTRAST, 100)
    fun saturation(context: Context) = context.prefs().getInt(C.TV_VIDEO_SATURATION, 100)

    fun isNeutral(context: Context) =
        brightness(context) == 100 && contrast(context) == 100 && saturation(context) == 100

    /** Neutral values disable the effects pipeline entirely — the picture is untouched by default. */
    fun applyTo(player: androidx.media3.common.Player?, context: Context) {
        if (player !is androidx.media3.exoplayer.ExoPlayer) return
        if (isNeutral(context)) {
            player.setVideoEffects(emptyList())
        } else {
            player.setVideoEffects(
                listOf(
                    VideoColorAdjustmentEffect(
                        brightness(context),
                        contrast(context),
                        saturation(context),
                    )
                )
            )
        }
    }

    fun reset(context: Context) {
        context.prefs().edit()
            .putInt(C.TV_VIDEO_BRIGHTNESS, 100)
            .putInt(C.TV_VIDEO_CONTRAST, 100)
            .putInt(C.TV_VIDEO_SATURATION, 100)
            .apply()
    }
}
