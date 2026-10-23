package de.mermack.alwaysinfect

import org.bukkit.entity.Projectile
import org.bukkit.entity.Villager
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTransformEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import kotlin.random.Random

class AlwaysInfectPlugin : JavaPlugin(), Listener {

    private var infectionChance = 1.0

    /** Villagers whose roll failed this tick; vanilla must not convert them either. */
    private val denied = mutableSetOf<UUID>()

    override fun onEnable() {
        saveDefaultConfig()
        val overrideDefault = config.getBoolean("override-default", true)
        infectionChance = config.getDouble("override-percentage", 1.0).coerceIn(0.0, 1.0)
        if (!overrideDefault) {
            logger.info("override-default is false, using vanilla infection chance")
            return
        }
        logger.info("Infection chance: ${infectionChance * 100}%")
        server.pluginManager.registerEvents(this, this)
    }

    /**
     * Vanilla only converts on Hard (100%) or Normal (50%). We intercept the fatal hit
     * and roll our own chance: on success we cancel the hit and convert the villager
     * ourselves, on failure we let it die and block any vanilla conversion.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onVillagerDamage(event: EntityDamageByEntityEvent) {
        val villager = event.entity as? Villager ?: return
        if (resolveZombie(event) == null) return
        if (event.finalDamage < villager.health + villager.absorptionAmount) return

        if (Random.nextDouble() < infectionChance) {
            event.isCancelled = true
            villager.zombify()
        } else {
            val id = villager.uniqueId
            denied += id
            server.scheduler.runTask(this, Runnable { denied -= id })
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInfection(event: EntityTransformEvent) {
        if (event.transformReason != EntityTransformEvent.TransformReason.INFECTION) return
        if (event.entity.uniqueId in denied) event.isCancelled = true
    }

    /** The zombie responsible for the hit, either directly or as shooter (e.g. a Drowned's trident). */
    private fun resolveZombie(event: EntityDamageByEntityEvent): Zombie? =
        when (val damager = event.damager) {
            is Zombie -> damager
            is Projectile -> damager.shooter as? Zombie
            else -> null
        }
}
