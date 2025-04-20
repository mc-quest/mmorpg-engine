package net.mcquest.engine.script

import com.google.common.base.CaseFormat
import net.mcquest.engine.resource.parseId
import org.python.core.PyObject
import org.python.util.PythonInterpreter

fun parseScriptId(id: String) = parseId(id, "scripts")

fun idToPythonClassName(id: String): String = CaseFormat.LOWER_UNDERSCORE.to(
    CaseFormat.UPPER_CAMEL,
    id.split('.').last()
)

fun getScriptClass(scriptId: String, interpreter: PythonInterpreter): PyObject {
    interpreter.exec("import $scriptId")
    val className = idToPythonClassName(scriptId)
    return interpreter.eval("${scriptId}.${className}")
}
