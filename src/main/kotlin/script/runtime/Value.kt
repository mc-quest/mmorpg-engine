package net.mcquest.engine.script.runtime

import net.mcquest.engine.script.parser.FunctionDeclaration

abstract class Value

class IntegerValue(val value: Int) : Value()

class BooleanValue(val value: Boolean) : Value()

class StringValue(val value: String) : Value()

class Null : Value()

class Function(val declaration: FunctionDeclaration) {
    fun call(vararg args: Value): Value = TODO()
}
