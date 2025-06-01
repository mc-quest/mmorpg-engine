package com.shadowforgedmmo.engine.instance

import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.MinecraftServer

class InstanceManager(instances: Collection<Instance>) {
    private val instancesById = instances.associateBy(Instance::id)

    val instances
        get() = instancesById.values

    fun start() {
        instancesById.values.forEach {
            MinecraftServer.getInstanceManager().registerInstance(it.instanceContainer)
        }
    }

    fun tick(runtime: Runtime) {
        instancesById.values.forEach { it.tick(runtime) }
    }

    fun getInstance(id: String) = instancesById.getValue(id)
}
