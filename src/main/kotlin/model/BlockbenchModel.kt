package com.shadowforgedmmo.engine.model

import com.shadowforgedmmo.engine.resource.parseId
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.reader.ModelReader
import java.io.File

class BlockbenchModel(val id: String, val model: Model)

fun deserializeBlockbenchModel(id: String, file: File, modelReader: ModelReader) =
    BlockbenchModel(id, modelReader.read(file))

fun parseBlockbenchModelId(id: String) = parseId(id, "models")
