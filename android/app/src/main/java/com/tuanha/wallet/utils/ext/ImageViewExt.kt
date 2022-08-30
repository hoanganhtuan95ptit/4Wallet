package com.tuanha.wallet.utils.ext

import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import com.tuanha.coreapp.utils.extentions.ImageDrawable
import jdenticon.Jdenticon

fun String.toDrawable(sizePx: Int): ImageDrawable {

    var genAddress = this

    val svg = SVG.getFromString(Jdenticon.toSvg(genAddress.removePrefix("0x"), sizePx))

    return ImageDrawable(PictureDrawable(svg.renderToPicture()))
}
