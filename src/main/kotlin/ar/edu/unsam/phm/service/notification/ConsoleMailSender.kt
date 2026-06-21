package ar.edu.unsam.phm.service.notification

import org.springframework.stereotype.Component

@Component
class ConsoleMailSender : MailSender {
    override fun sendMail(mail: Mail) {
        println("Enviando mail: $mail")
    }
}
