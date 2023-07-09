fun main(args: Array<String>) {

    val botToken = args[0]
    val service = TelegramBotService(botToken)
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = service.getUpdates(updateId)

        val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
        val updateIdString = updateIdRegex.find(updates)?.groups?.get(1)?.value

        if (updateIdString == null) continue
        else {
            updateId = updateIdString.toInt() + 1

            val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
            val text = messageTextRegex.find(updates)?.groups?.get(1)?.value

            val chatIdRegex: Regex = "\"id\":(.+?),\"is".toRegex()
            val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value

            val sendMessage: String = service.sendMessage(chatId!!, text!!)
            println(sendMessage)
        }
    }
}