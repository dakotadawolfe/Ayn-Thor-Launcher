package com.launcher.aynthords

import java.io.BufferedReader
import java.io.InputStreamReader

object RootShell {

    /**
     * Runs commands through `su`.
     * Returns (exitCode, combinedOutput).
     */
    fun run(vararg commands: String): Pair<Int, String> {
        val process = Runtime.getRuntime().exec("su")
        val output = StringBuilder()

        // Write commands
        process.outputStream.bufferedWriter().use { w ->
            commands.forEach {
                w.write(it)
                w.newLine()
            }
            w.write("exit")
            w.newLine()
            w.flush()
        }

        // Read stdout/stderr
        val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()

        val code = process.waitFor()

        if (stdout.isNotBlank()) output.append(stdout)
        if (stderr.isNotBlank()) {
            if (output.isNotEmpty()) output.append("\n")
            output.append(stderr)
        }

        return code to output.toString()
    }
}
