package org.bytekeeper.ctr

class GameRunner(val botNameProvider: () -> String, val mapNameProvider: () -> String) : Runnable {
    override fun run() {
        while (true) {
            val scbwRunner = ScbwRunner(bots = listOf(botNameProvider(), botNameProvider()))
            scbwRunner.run()
        }
    }
}