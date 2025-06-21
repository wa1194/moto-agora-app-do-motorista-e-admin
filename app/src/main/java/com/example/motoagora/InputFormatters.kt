package com.example.motoagora

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val trimmed = if (text.text.length >= 11) text.text.substring(0..10) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 5) out += "."
            if (i == 8) out += "-"
        }

        val cpfOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset + 1
                if (offset <= 8) return offset + 2
                if (offset <= 11) return offset + 3
                return 14
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset - 1
                if (offset <= 11) return offset - 2
                if (offset <= 14) return offset - 3
                return 11
            }
        }
        return TransformedText(AnnotatedString(out), cpfOffsetTranslator)
    }
}

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val mask = "(##) #####-####"
        val digits = text.text.filter { it.isDigit() }
        var out = ""
        var maskIndex = 0
        var digitIndex = 0

        while (maskIndex < mask.length && digitIndex < digits.length) {
            if (mask[maskIndex] == '#') {
                out += digits[digitIndex]
                digitIndex++
            } else {
                out += mask[maskIndex]
            }
            maskIndex++
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = offset
                var specialCharsBefore = 0
                val maskSub = mask.substring(0, (transformedOffset + specialCharsBefore).coerceAtMost(mask.length))

                maskSub.forEachIndexed { index, c ->
                    if (index < transformedOffset + specialCharsBefore) {
                        if (c != '#') {
                            specialCharsBefore++
                        }
                    }
                }
                return (offset + specialCharsBefore).coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return out.substring(0, offset).count { it.isDigit() }
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}