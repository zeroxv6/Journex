package space.zeroxv6.journex.utils
import android.content.Context
import space.zeroxv6.journex.model.Note
import java.io.File
import java.time.format.DateTimeFormatter
object ExportUtils {
    fun exportToMarkdown(note: Note): String {
        return buildString {
            appendLine("# ${note.title}")
            appendLine()
            appendLine("**Created:** ${note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}")
            appendLine("**Updated:** ${note.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}")
            appendLine("**Category:** ${note.category.label}")
            if (note.tags.isNotEmpty()) {
                appendLine("**Tags:** ${note.tags.joinToString(", ") { "#$it" }}")
            }
            appendLine()
            appendLine("---")
            appendLine()
            appendLine(note.content)
            if (note.checklistItems.isNotEmpty()) {
                appendLine()
                appendLine("## Checklist")
                note.checklistItems.forEach { item ->
                    val checkbox = if (item.isChecked) "[x]" else "[ ]"
                    appendLine("- $checkbox ${item.text}")
                }
            }
            if (note.links.isNotEmpty()) {
                appendLine()
                appendLine("## Links")
                note.links.forEach { link ->
                    appendLine("- [${link.title ?: link.url}](${link.url})")
                }
            }
        }
    }
    fun exportToHtml(note: Note): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <title>${note.title}</title>")
            appendLine("    <style>")
            appendLine("        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }")
            appendLine("        h1 { color: #333; }")
            appendLine("        .metadata { color: #666; font-size: 0.9em; margin-bottom: 20px; }")
            appendLine("        .content { line-height: 1.6; }")
            appendLine("        .checklist { list-style: none; padding-left: 0; }")
            appendLine("        .checklist li { margin: 5px 0; }")
            appendLine("        .checked { text-decoration: line-through; color: #999; }")
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <h1>${note.title}</h1>")
            appendLine("    <div class=\"metadata\">")
            appendLine("        <p><strong>Created:</strong> ${note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}</p>")
            appendLine("        <p><strong>Category:</strong> ${note.category.label}</p>")
            if (note.tags.isNotEmpty()) {
                appendLine("        <p><strong>Tags:</strong> ${note.tags.joinToString(", ") { "#$it" }}</p>")
            }
            appendLine("    </div>")
            appendLine("    <hr>")
            appendLine("    <div class=\"content\">")
            appendLine("        ${convertMarkdownToHtml(note.content)}")
            appendLine("    </div>")
            if (note.checklistItems.isNotEmpty()) {
                appendLine("    <h2>Checklist</h2>")
                appendLine("    <ul class=\"checklist\">")
                note.checklistItems.forEach { item ->
                    val className = if (item.isChecked) "checked" else ""
                    val checkbox = if (item.isChecked) "☑" else "☐"
                    appendLine("        <li class=\"$className\">$checkbox ${item.text}</li>")
                }
                appendLine("    </ul>")
            }
            appendLine("</body>")
            appendLine("</html>")
        }
    }
    fun exportToText(note: Note): String {
        return buildString {
            appendLine("=" .repeat(60))
            appendLine(note.title)
            appendLine("=" .repeat(60))
            appendLine()
            appendLine("Created: ${note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}")
            appendLine("Category: ${note.category.label}")
            if (note.tags.isNotEmpty()) {
                appendLine("Tags: ${note.tags.joinToString(", ") { "#$it" }}")
            }
            appendLine()
            appendLine("-" .repeat(60))
            appendLine()
            appendLine(MarkdownUtils.convertToPlainText(note.content))
            if (note.checklistItems.isNotEmpty()) {
                appendLine()
                appendLine("CHECKLIST:")
                note.checklistItems.forEach { item ->
                    val checkbox = if (item.isChecked) "[✓]" else "[ ]"
                    appendLine("  $checkbox ${item.text}")
                }
            }
            appendLine()
            appendLine("-" .repeat(60))
            appendLine("Words: ${note.wordCount} | Characters: ${note.characterCount} | Reading Time: ${note.readingTime} min")
        }
    }
    fun exportToJson(note: Note): String {
        val gson = com.google.gson.GsonBuilder()
            .setPrettyPrinting()
            .create()
        return gson.toJson(note)
    }
    private fun convertMarkdownToHtml(markdown: String): String {
        var html = markdown
        html = html.replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
        html = html.replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
        html = html.replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")
        html = html.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        html = html.replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
        html = html.replace(Regex("~~(.+?)~~"), "<del>$1</del>")
        html = html.replace(Regex("\\[(.+?)\\]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")
        html = html.replace("\n", "<br>")
        return html
    }
    fun saveToFile(context: Context, content: String, fileName: String): File {
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(content)
        return file
    }
}
