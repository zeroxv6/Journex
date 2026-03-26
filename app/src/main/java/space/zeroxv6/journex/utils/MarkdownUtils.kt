package space.zeroxv6.journex.utils
object MarkdownUtils {
    fun applyBold(text: String, selection: Pair<Int, Int>): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val before = text.substring(0, start)
        val selected = text.substring(start, end)
        val after = text.substring(end)
        val newText = "$before**$selected**$after"
        val newSelection = Pair(start + 2, end + 2)
        return Pair(newText, newSelection)
    }
    fun applyItalic(text: String, selection: Pair<Int, Int>): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val before = text.substring(0, start)
        val selected = text.substring(start, end)
        val after = text.substring(end)
        val newText = "$before*$selected*$after"
        val newSelection = Pair(start + 1, end + 1)
        return Pair(newText, newSelection)
    }
    fun applyStrikethrough(text: String, selection: Pair<Int, Int>): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val before = text.substring(0, start)
        val selected = text.substring(start, end)
        val after = text.substring(end)
        val newText = "$before~~$selected~~$after"
        val newSelection = Pair(start + 2, end + 2)
        return Pair(newText, newSelection)
    }
    fun applyHeader(text: String, selection: Pair<Int, Int>, level: Int): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val lineStart = text.lastIndexOf('\n', start - 1) + 1
        val before = text.substring(0, lineStart)
        val line = text.substring(lineStart, end)
        val after = text.substring(end)
        val hashes = "#".repeat(level)
        val newText = "$before$hashes $line$after"
        val offset = level + 1
        val newSelection = Pair(start + offset, end + offset)
        return Pair(newText, newSelection)
    }
    fun applyBulletList(text: String, selection: Pair<Int, Int>): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val lineStart = text.lastIndexOf('\n', start - 1) + 1
        val before = text.substring(0, lineStart)
        val line = text.substring(lineStart, end)
        val after = text.substring(end)
        val newText = "$before- $line$after"
        val newSelection = Pair(start + 2, end + 2)
        return Pair(newText, newSelection)
    }
    fun applyNumberedList(text: String, selection: Pair<Int, Int>): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val lineStart = text.lastIndexOf('\n', start - 1) + 1
        val before = text.substring(0, lineStart)
        val line = text.substring(lineStart, end)
        val after = text.substring(end)
        val newText = "$before. $line$after"
        val newSelection = Pair(start + 3, end + 3)
        return Pair(newText, newSelection)
    }
    fun applyCheckbox(text: String, selection: Pair<Int, Int>): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val lineStart = text.lastIndexOf('\n', start - 1) + 1
        val before = text.substring(0, lineStart)
        val line = text.substring(lineStart, end)
        val after = text.substring(end)
        val newText = "$before- [ ] $line$after"
        val newSelection = Pair(start + 6, end + 6)
        return Pair(newText, newSelection)
    }
    fun applyCodeBlock(text: String, selection: Pair<Int, Int>, language: String = ""): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val before = text.substring(0, start)
        val selected = text.substring(start, end)
        val after = text.substring(end)
        val newText = "$before\n```$language\n$selected\n```\n$after"
        val offset = 4 + language.length + 1
        val newSelection = Pair(start + offset, end + offset)
        return Pair(newText, newSelection)
    }
    fun applyLink(text: String, selection: Pair<Int, Int>, url: String): Pair<String, Pair<Int, Int>> {
        val (start, end) = selection
        val before = text.substring(0, start)
        val selected = text.substring(start, end)
        val after = text.substring(end)
        val linkText = if (selected.isEmpty()) "link" else selected
        val newText = "$before[$linkText]($url)$after"
        val newSelection = Pair(start + 1, start + 1 + linkText.length)
        return Pair(newText, newSelection)
    }
    fun applyTable(rows: Int = 3, cols: Int = 3): String {
        val header = "| " + (1..cols).joinToString(" | ") { "Header $it" } + " |"
        val separator = "|" + (1..cols).joinToString("|") { " --- " } + "|"
        val dataRows = (1..rows).joinToString("\n") { row ->
            "| " + (1..cols).joinToString(" | ") { "Cell $row,$it" } + " |"
        }
        return "$header\n$separator\n$dataRows"
    }
    fun convertToPlainText(markdown: String): String {
        return markdown
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") 
            .replace(Regex("\\*(.+?)\\*"), "$1") 
            .replace(Regex("~~(.+?)~~"), "$1") 
            .replace(Regex("^#{1,6}\\s+"), "") 
            .replace(Regex("^[-*+]\\s+"), "") 
            .replace(Regex("^\\d+\\.\\s+"), "") 
            .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1") 
            .replace(Regex("```[\\s\\S]*?```"), "") 
            .trim()
    }
    fun extractLinks(markdown: String): List<Pair<String, String>> {
        val linkRegex = Regex("\\[(.+?)\\]\\((.+?)\\)")
        return linkRegex.findAll(markdown).map { 
            Pair(it.groupValues[1], it.groupValues[2])
        }.toList()
    }
    fun extractCodeBlocks(markdown: String): List<Pair<String, String>> {
        val codeRegex = Regex("```(\\w*)\\n([\\s\\S]*?)```")
        return codeRegex.findAll(markdown).map {
            Pair(it.groupValues[1], it.groupValues[2])
        }.toList()
    }
}
