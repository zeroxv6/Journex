package space.zeroxv6.journex.shared.util
import space.zeroxv6.journex.shared.model.Prompt
object PromptGenerator {
    private val reflectionPrompts = listOf(
        "What made you smile today?",
        "What's something you're grateful for right now?",
        "Describe a moment today when you felt at peace.",
        "What's one thing you learned about yourself recently?",
        "If you could relive one moment from today, which would it be?",
        "What's weighing on your mind right now?",
        "Describe your current mood in three words.",
        "What would make tomorrow better than today?",
        "What's a small victory you had today?",
        "How did you take care of yourself today?"
    )
    private val creativityPrompts = listOf(
        "If you could have any superpower, what would it be and why?",
        "Describe your perfect day from start to finish.",
        "Write about a place you've never been but want to visit.",
        "If you could talk to your younger self, what would you say?",
        "Imagine your life 10 years from now. What do you see?",
        "Write a letter to someone who has influenced your life.",
        "Describe a dream you remember vividly.",
        "If you could master any skill instantly, what would it be?",
        "Create a bucket list of 5 things you want to do this year.",
        "Write about a book, movie, or song that changed your perspective."
    )
    private val growthPrompts = listOf(
        "What's a fear you'd like to overcome?",
        "Describe a challenge you're currently facing.",
        "What habit would you like to build or break?",
        "What does success mean to you?",
        "Write about a mistake that taught you something valuable.",
        "What are your top 3 priorities in life right now?",
        "How have you grown in the past year?",
        "What's holding you back from achieving your goals?",
        "Describe your ideal version of yourself.",
        "What would you do if you knew you couldn't fail?"
    )
    private val relationshipPrompts = listOf(
        "Who is someone you appreciate but haven't told lately?",
        "Describe a meaningful conversation you had recently.",
        "What qualities do you value most in friendships?",
        "Write about someone who inspires you.",
        "How do you show love to the people in your life?",
        "What's a relationship you'd like to improve?",
        "Describe a memory with a loved one that makes you happy.",
        "What have you learned from a difficult relationship?",
        "Who would you like to reconnect with?",
        "How do you handle conflict in relationships?"
    )
    private val mindfulnessPrompts = listOf(
        "What are 5 things you can see, hear, and feel right now?",
        "Describe your breathing and how your body feels.",
        "What sounds can you hear in this moment?",
        "Write about something beautiful you noticed today.",
        "How does your body feel right now? Scan from head to toe.",
        "What emotions are you experiencing right now?",
        "Describe the present moment without judgment.",
        "What are you holding onto that you need to let go?",
        "Write about a moment of stillness you experienced.",
        "What brings you into the present moment?"
    )
    val allPrompts: List<Prompt> by lazy {
        val prompts = mutableListOf<Prompt>()
        var id = 1
        reflectionPrompts.forEach { text ->
            prompts.add(Prompt(id = (id++).toString(), text = text, category = "Reflection"))
        }
        creativityPrompts.forEach { text ->
            prompts.add(Prompt(id = (id++).toString(), text = text, category = "Creativity"))
        }
        growthPrompts.forEach { text ->
            prompts.add(Prompt(id = (id++).toString(), text = text, category = "Growth"))
        }
        relationshipPrompts.forEach { text ->
            prompts.add(Prompt(id = (id++).toString(), text = text, category = "Relationships"))
        }
        mindfulnessPrompts.forEach { text ->
            prompts.add(Prompt(id = (id++).toString(), text = text, category = "Mindfulness"))
        }
        prompts
    }
    val categories = listOf("Reflection", "Creativity", "Growth", "Relationships", "Mindfulness")
    fun getRandomPrompt(): Prompt = allPrompts.random()
    fun getRandomPromptByCategory(category: String): Prompt? {
        return allPrompts.filter { it.category == category }.randomOrNull()
    }
    fun getPromptsByCategory(category: String): List<Prompt> {
        return allPrompts.filter { it.category == category }
    }
}
