import java.lang.reflect.Modifier

fun main() {
    val clazz = Class.forName("com.google.ai.client.generativeai.type.Schema")
    println("Constructors:")
    clazz.constructors.forEach { println(it) }
    println("Methods:")
    clazz.declaredMethods.filter { Modifier.isPublic(it.modifiers) }.forEach { println(it) }
    
    val fdClazz = Class.forName("com.google.ai.client.generativeai.type.FunctionDeclaration")
    println("FunctionDeclaration Constructors:")
    fdClazz.constructors.forEach { println(it) }
}
