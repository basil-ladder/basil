package org.bytekeeper.ctr

import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.Race
import org.springframework.aop.framework.autoproxy.AutoProxyUtils
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.core.MethodIntrospector
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class GameEnded(val winner: Bot,
                val loser: Bot,
                val map: String,
                val timestamp: Instant = Instant.now(),
                val gameTime: Double,
                val gameHash: String,
                val frameCount: Int?)

class GameCrashed(val botA: Bot,
                  val botB: Bot,
                  val map: String,
                  val botACrashed: Boolean,
                  val botBCrashed: Boolean,
                  val timestamp: Instant = Instant.now(),
                  val gameTime: Double,
                  val gameHash: String,
                  val frameCount: Int?)

class GameTimedOut(val botA: Bot,
                   val botB: Bot,
                   val slowerBot: Bot?,
                   val scoreA: Int,
                   val scoreB: Int,
                   val map: String,
                   val timestamp: Instant = Instant.now(),
                   val realTimedOut: Boolean,
                   val gameTimedOut: Boolean,
                   val gameTime: Double,
                   val gameHash: String,
                   val frameCount: Int?)

class GameFailedToStart(val botA: Bot,
                        val botB: Bot,
                        val map: String,
                        val timestamp: Instant = Instant.now(),
                        val gameHash: String)

data class GameWon(val winner: Bot, val loser: Bot, val gameHash: String)
class EloUpdated(val bot: Bot, val newRating: Int, val timestamp: Instant = Instant.now(), val gameHash: String)
class BotCreated(val bot: Bot)
class BotDisabled(val bot: Bot)
class BotEnabled(val bot: Bot)
class BotUpdated(val bot: Bot, val timestamp: Instant)

@Service
class Events {
    private val log = LogManager.getLogger()
    private val listeners = ConcurrentHashMap<Class<*>, MutableList<(Any) -> Unit>>()
    private val executService = Executors.newSingleThreadExecutor()
    @Volatile
    final var postsRunning = 0
        private set

    fun post(event: Any) {
        postsRunning++
        val listeners = listeners[event.javaClass] ?: run { log.error("Event $event was not handled!"); return }
        executService.submit {
            try {
                listeners.forEach { it(event) }
            } finally {
                postsRunning--
            }
        }
    }

    inline final fun <reified T> register(noinline listener: (T) -> Unit) = register(T::class.java, listener)

    fun <T> register(type: Class<T>, listener: (T) -> Unit) {
        val listenerList = listeners.computeIfAbsent(type) { CopyOnWriteArrayList() }
        listenerList += { x -> listener(x as T) }
    }
}

class PreparePublish
class CreateBot(val name: String, val race: Race?, val botType: String?, val lastUpdated: Instant?)

@Service
class Commands {
    private val handlers = ConcurrentHashMap<Class<*>, MutableList<(Any) -> Unit>>()


    fun handle(command: Any) {
        val handlers = handlers[command.javaClass] ?: throw IllegalStateException("Command $command was not handled!")
        handlers.forEach { it(command) }
    }

    inline final fun <reified T> register(noinline listener: (T) -> Unit) = register(T::class.java, listener)

    fun <T> register(type: Class<T>, handler: (T) -> Unit) {
        val handlerList = handlers.computeIfAbsent(type) { CopyOnWriteArrayList() }
        handlerList += { x -> handler(x as T) }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class CommandHandler

@Target(AnnotationTarget.FUNCTION)
annotation class EventHandler

@Component
class EventsAndCommandsProcessor(private val events: Events,
                                 private val commands: Commands,
                                 private val beanFactory: ConfigurableListableBeanFactory) : BeanPostProcessor {
    private val log = LogManager.getLogger()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        try {
            val targetType = AutoProxyUtils.determineTargetClass(beanFactory, beanName)!!

            MethodIntrospector
                    .selectMethods(targetType) { it.getAnnotation(EventHandler::class.java) != null }
                    .forEach { method ->
                        events.register(method.parameterTypes[0]) { method(bean, it) }
                    }
            MethodIntrospector
                    .selectMethods(targetType) { it.getAnnotation(CommandHandler::class.java) != null }
                    .forEach { method ->
                        commands.register(method.parameterTypes[0]) { method(bean, it) }
                    }

        } catch (e: Throwable) {
            log.debug(e)
        }
        return bean
    }
}