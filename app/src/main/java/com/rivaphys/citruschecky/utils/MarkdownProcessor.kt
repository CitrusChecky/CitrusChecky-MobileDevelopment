package com.rivaphys.citruschecky.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import java.util.regex.Pattern

object MarkdownProcessor {

    /**
     * Converts markdown formatting to Android SpannableString
     * Supports: **bold**, *italic*, # headers, ## subheaders
     * Also removes unwanted markdown symbols
     */
    fun processMarkdown(input: String): SpannableString {
        // First clean up the input text
        var cleanedInput = cleanMarkdownSymbols(input)

        val spannableBuilder = SpannableStringBuilder()

        // Process the text line by line to maintain structure
        val lines = cleanedInput.split("\n")

        for (i in lines.indices) {
            val processedLine = processLineMarkdown(lines[i])
            spannableBuilder.append(processedLine)

            // Add newline except for the last line
            if (i < lines.size - 1) {
                spannableBuilder.append("\n")
            }
        }

        return SpannableString(spannableBuilder)
    }

    private fun cleanMarkdownSymbols(input: String): String {
        var cleaned = input

        // Remove markdown horizontal rules (--- or ***)
        cleaned = cleaned.replace(Regex("^[-*]{3,}$", RegexOption.MULTILINE), "")

        // Remove backticks for code (` or ```)
        cleaned = cleaned.replace(Regex("```[\\s\\S]*?```"), "")
        cleaned = cleaned.replace(Regex("`([^`]+?)`"), "$1")

        // Remove > blockquotes at start of lines
        cleaned = cleaned.replace(Regex("^>\\s*", RegexOption.MULTILINE), "")

        // Clean up multiple consecutive newlines
        cleaned = cleaned.replace(Regex("\\n{3,}"), "\n\n")

        return cleaned.trim()
    }

    private fun processLineMarkdown(line: String): SpannableString {
        var processedLine = line
        val spannableBuilder = SpannableStringBuilder()

        // First, process headers (# and ##)
        when {
            processedLine.startsWith("### ") -> {
                processedLine = processedLine.substring(4)
                spannableBuilder.append(processedLine)
                spannableBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, processedLine.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableBuilder.setSpan(
                    RelativeSizeSpan(1.1f),
                    0, processedLine.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            processedLine.startsWith("## ") -> {
                processedLine = processedLine.substring(3)
                spannableBuilder.append(processedLine)
                spannableBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, processedLine.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableBuilder.setSpan(
                    RelativeSizeSpan(1.2f),
                    0, processedLine.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            processedLine.startsWith("# ") -> {
                processedLine = processedLine.substring(2)
                spannableBuilder.append(processedLine)
                spannableBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, processedLine.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableBuilder.setSpan(
                    RelativeSizeSpan(1.3f),
                    0, processedLine.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            else -> {
                spannableBuilder.append(processedLine)
            }
        }

        // Then process inline formatting
        processBoldText(spannableBuilder)
        processItalicText(spannableBuilder)

        return SpannableString(spannableBuilder)
    }

    private fun processBoldText(spannable: SpannableStringBuilder) {
        val boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*")
        val matcher = boldPattern.matcher(spannable)

        // Process from end to start to avoid index shifting
        val matches = mutableListOf<MatchResult>()
        while (matcher.find()) {
            matches.add(MatchResult(matcher.start(), matcher.end(), matcher.group(1) ?: ""))
        }

        for (match in matches.reversed()) {
            // Replace **text** with text and apply bold span
            spannable.replace(match.start, match.end, match.text)
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                match.start,
                match.start + match.text.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun processItalicText(spannable: SpannableStringBuilder) {
        // Process single asterisks that are not part of double asterisks and not at start of line for lists
        val italicPattern = Pattern.compile("(?<!\\*)(?<!^\\s)\\*([^*\\n]+?)\\*(?!\\*)")
        val matcher = italicPattern.matcher(spannable)

        val matches = mutableListOf<MatchResult>()
        while (matcher.find()) {
            matches.add(MatchResult(matcher.start(), matcher.end(), matcher.group(1) ?: ""))
        }

        for (match in matches.reversed()) {
            // Replace *text* with text and apply italic span
            spannable.replace(match.start, match.end, match.text)
            spannable.setSpan(
                StyleSpan(Typeface.ITALIC),
                match.start,
                match.start + match.text.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Remove remaining standalone asterisks that are used as bullet points
        removeBulletAsterisks(spannable)
    }

    private fun removeBulletAsterisks(spannable: SpannableStringBuilder) {
        // Remove asterisks used as bullet points at the start of lines
        val bulletPattern = Pattern.compile("^\\s*\\*\\s+", Pattern.MULTILINE)
        val matcher = bulletPattern.matcher(spannable)

        val matches = mutableListOf<MatchResult>()
        while (matcher.find()) {
            val replacement = matcher.group().replace("*", "•") // Replace with bullet point
            matches.add(MatchResult(matcher.start(), matcher.end(), replacement))
        }

        for (match in matches.reversed()) {
            spannable.replace(match.start, match.end, match.text)
        }

        // Remove any remaining standalone asterisks
        val standalonePattern = Pattern.compile("\\s\\*\\s")
        val standaloneMatcher = standalonePattern.matcher(spannable)

        val standaloneMatches = mutableListOf<MatchResult>()
        while (standaloneMatcher.find()) {
            standaloneMatches.add(MatchResult(standaloneMatcher.start(), standaloneMatcher.end(), " • "))
        }

        for (match in standaloneMatches.reversed()) {
            spannable.replace(match.start, match.end, match.text)
        }
    }

    data class MatchResult(
        val start: Int,
        val end: Int,
        val text: String
    )
}