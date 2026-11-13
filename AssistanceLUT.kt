package ch.unifr.fuzzy.travelease

/**
 * presentation layouts matching the specification boundaries.
 */
enum class AssistanceLayout {
    LOW,        // [0.00, 0.35): single hint
    MODERATE,   // [0.35, 0.65]: sequential checklist
    HIGH        // (0.65, 1.00]: interactive walkthrough
}

/**
 * bilinear interpolated look-up table (LUT) for Assistance inference
 * precomputes the non-linear fuzzy function f: U_D x U_T -> U_A.
 */
object  AssistanceLUT {

    const val GRID_SIZE = 11
    private const val STEP = 0.1f

    // 11x11 precomputed float values exported from scikit-fuzzy (Row-Major: D x T)
    private val LUT_DATA = floatArrayOf(
        0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, // D = 0.0
        0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, // D = 0.1
        0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, 0.1873f, // D = 0.2
        0.2464f, 0.2464f, 0.2464f, 0.2464f, 0.2464f, 0.2464f, 0.2443f, 0.2285f, 0.2038f, 0.1911f, 0.1873f, // D = 0.3
        0.4137f, 0.4137f, 0.4137f, 0.4137f, 0.4137f, 0.4137f, 0.3951f, 0.3340f, 0.2520f, 0.2057f, 0.1873f, // D = 0.4
        0.5000f, 0.5000f, 0.5000f, 0.5000f, 0.5000f, 0.5000f, 0.4789f, 0.3951f, 0.2831f, 0.2144f, 0.1873f, // D = 0.5
        0.5863f, 0.5863f, 0.5863f, 0.5863f, 0.5863f, 0.5863f, 0.5627f, 0.4789f, 0.3440f, 0.2407f, 0.1985f, // D = 0.6
        0.7536f, 0.7536f, 0.7536f, 0.7536f, 0.7536f, 0.7536f, 0.7301f, 0.6387f, 0.4905f, 0.3692f, 0.3083f, // D = 0.7
        0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.7952f, 0.7227f, 0.5878f, 0.4682f, 0.4137f, // D = 0.8
        0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.7952f, 0.7227f, 0.5878f, 0.4682f, 0.4137f, // D = 0.9
        0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.8127f, 0.7952f, 0.7227f, 0.5878f, 0.4682f, 0.4137f  // D = 1.0
    )

    /**
     * computes continuous inputs D and T in [0.0, 1.0] using 2D bilinear interpolation
     */
    fun evaluate(difficulty: Float, timePressure: Float): Float {
        val dClamped = difficulty.coerceIn(0.0f, 1.0f)
        val tClamped = timePressure.coerceIn(0.0f, 1.0f)

        // grid coordinates
        val gx = dClamped / STEP
        val gy = tClamped / STEP

        val i0 = gx.toInt().coerceAtMost(GRID_SIZE - 2)
        val j0 = gy.toInt().coerceAtMost(GRID_SIZE - 2)
        val i1 = i0 + 1
        val j1 = j0 + 1

        val dx = gx - i0
        val dy = gy - j0

        // fetch 4 bounding grid points
        val q00 = LUT_DATA[i0 * GRID_SIZE + j0]
        val q01 = LUT_DATA[i0 * GRID_SIZE + j1]
        val q10 = LUT_DATA[i1 * GRID_SIZE + j0]
        val q11 = LUT_DATA[i1 * GRID_SIZE + j1]

        // bilinear interpolation
        val top = q00 * (1.0f - dy) + q01 * dy
        val bottom = q10 * (1.0f - dy) + q11 * dy

        return top * (1.0f - dx) + bottom * dx
    }

    /**
     *  query method for sliders [0, 10]
     */
    fun queryFromIntegerScale(difficulty: Int, timePressure: Int): AssistanceLayout {
        val dNorm = difficulty.coerceIn(0, 10) / 10.0f
        val tNorm = timePressure.coerceIn(0, 10) / 10.0f
        val assistance = evaluate(dNorm, tNorm)
        return resolveLayout(assistance)
    }

    /**
     * mapping assistance to assistaneLayout
     */
    fun resolveLayout(assistance: Float): AssistanceLayout = when {
        assistance < 0.35f -> AssistanceLayout.LOW
        assistance <= 0.65f -> AssistanceLayout.MODERATE
        else -> AssistanceLayout.HIGH
    }
}
