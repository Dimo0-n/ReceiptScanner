package com.example.myapplicationtmppp.ui.notifications

import java.util.*
import javax.mail.Session
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Message
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.MessagingException

class GmailSender(private val username: String, private val password: String) {

    fun sendEmail(to: String, subject: String, body: String): Boolean {

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        // Crearea sesiunii cu autentificare
        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        return try {
            // Crearea mesajului de email
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                setText(body)
            }

            // Trimiterea emailului
            Transport.send(message)
            println("✅ Email trimis cu succes!")
            true
        } catch (e: MessagingException) {
            e.printStackTrace()
            println("❌ Eroare la trimiterea emailului: ${e.message}")
            false
        }
    }
}